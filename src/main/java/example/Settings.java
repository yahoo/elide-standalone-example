/*
 * Copyright 2019, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */

package example;

import com.yahoo.elide.ElideSettings;
import com.yahoo.elide.ElideSettingsBuilder;
import com.yahoo.elide.contrib.swagger.SwaggerBuilder;
import com.yahoo.elide.core.DataStore;
import com.yahoo.elide.core.EntityDictionary;
import com.yahoo.elide.core.filter.dialect.RSQLFilterDialect;
import com.yahoo.elide.datastores.jpa.JpaDataStore;
import com.yahoo.elide.datastores.jpa.transaction.NonJtaTransaction;
import com.yahoo.elide.standalone.Util;
import com.yahoo.elide.standalone.config.ElideStandaloneSettings;
import example.models.ArtifactGroup;
import example.models.ArtifactProduct;
import example.models.ArtifactVersion;
import io.swagger.models.Info;
import io.swagger.models.Swagger;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.glassfish.hk2.api.ServiceLocator;

import javax.persistence.EntityManagerFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.TimeZone;

/**
 * This class contains common settings for both test and production.
 */
public abstract class Settings implements ElideStandaloneSettings {

    @Override
    public int getPort() {
        //Heroku exports port to come from $PORT
        return Optional.ofNullable(System.getenv("PORT"))
                .map(Integer::valueOf)
                .orElse(4080);
    }

    @Override
    public Map<String, Swagger> enableSwagger() {
        EntityDictionary dictionary = new EntityDictionary(new HashMap());

        dictionary.bindEntity(ArtifactGroup.class);
        dictionary.bindEntity(ArtifactProduct.class);
        dictionary.bindEntity(ArtifactVersion.class);
        Info info = new Info().title("Test Service").version("1.0");

        SwaggerBuilder builder = new SwaggerBuilder(dictionary, info);

        Swagger swagger = builder.build().basePath("/api/v1");

        Map<String, Swagger> docs = new HashMap<>();
        docs.put("test", swagger);
        return docs;
    }

    @Override
    public String getModelPackageName() {

        //This needs to be changed to the package where your models live.
        return "example.models";
    }

    @Override
    public ElideSettings getElideSettings(ServiceLocator injector) {
        EntityManagerFactory entityManagerFactory = Util.getEntityManagerFactory(this.getModelPackageName(),
                getDatabaseProperties());
        DataStore dataStore = new JpaDataStore(() -> {
            return entityManagerFactory.createEntityManager();
        }, (em) -> {
            return new NonJtaTransaction(em);
        });
        Map var10002 = this.getCheckMappings();
        injector.getClass();
        EntityDictionary dictionary = new EntityDictionary(var10002, injector::inject);
        ElideSettingsBuilder builder = (new ElideSettingsBuilder(dataStore))
                .withUseFilterExpressions(true)
                .withEntityDictionary(dictionary)
                .withJoinFilterDialect(new RSQLFilterDialect(dictionary))
                .withSubqueryFilterDialect(new RSQLFilterDialect(dictionary));
        if (this.enableIS06081Dates()) {
            builder = builder.withISO8601Dates("yyyy-MM-dd'T'HH:mm'Z'", TimeZone.getTimeZone("UTC"));
        }

        return builder.build();
    }

    public Properties getDatabaseProperties() {

        Properties dbProps;
        try {
            dbProps = new Properties();
            dbProps.load(
                    Main.class.getClassLoader().getResourceAsStream("dbconfig.properties")
            );

            dbProps.setProperty("javax.persistence.jdbc.url", System.getenv("JDBC_DATABASE_URL"));
            dbProps.setProperty("javax.persistence.jdbc.user", System.getenv("JDBC_DATABASE_USERNAME"));
            dbProps.setProperty("javax.persistence.jdbc.password", System.getenv("JDBC_DATABASE_PASSWORD"));
            return dbProps;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void updateServletContextHandler(ServletContextHandler servletContextHandler) {
       ResourceHandler resource_handler = new ResourceHandler();

       try {
           resource_handler.setDirectoriesListed(false);
           resource_handler.setResourceBase(Settings.class.getClassLoader()
                   .getResource("META-INF/resources/webjars/swagger-ui/3.23.8").toURI().toString());
           servletContextHandler.insertHandler(resource_handler);
       } catch (Exception e) {
           throw new IllegalStateException(e);
       }
    }
}
