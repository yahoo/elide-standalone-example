/*
 * Copyright 2019, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */

package example;

import com.google.common.collect.Lists;
import com.yahoo.elide.datastores.aggregation.queryengines.sql.dialects.SQLDialectFactory;
import com.yahoo.elide.standalone.config.ElideStandaloneAnalyticSettings;
import com.yahoo.elide.standalone.config.ElideStandaloneAsyncSettings;
import com.yahoo.elide.standalone.config.ElideStandaloneSettings;
import com.yahoo.elide.standalone.config.ElideStandaloneSubscriptionSettings;
import example.filters.CorsFilter;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;

import java.io.IOException;
import java.sql.DriverManager;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import jakarta.jms.ConnectionFactory;

/**
 * This class contains common settings for both test and production.
 */
public abstract class Settings implements ElideStandaloneSettings {

    protected String jdbcUrl;
    protected String jdbcUser;
    protected String jdbcPassword;

    protected boolean inMemory;

    public Settings(boolean inMemory) {
        jdbcUrl = Optional.ofNullable(System.getenv("JDBC_DATABASE_URL"))
                .orElse("jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1");

        jdbcUser = Optional.ofNullable(System.getenv("JDBC_DATABASE_USERNAME"))
                .orElse("sa");

        jdbcPassword = Optional.ofNullable(System.getenv("JDBC_DATABASE_PASSWORD"))
                .orElse("");

        this.inMemory = inMemory;
    }

    @Override
    public int getPort() {
        //Heroku exports port to come from $PORT
        return Optional.ofNullable(System.getenv("PORT"))
                .map(Integer::valueOf)
                .orElse(8080);
    }

    @Override
    public boolean enableSwagger() {
        return true;
    }

    @Override
    public boolean enableGraphQL() {
        return true;
    }

    @Override
    public String getModelPackageName() {
        //This needs to be changed to the package where your models live.
        return "example.models";
    }

    @Override
    public ElideStandaloneAnalyticSettings getAnalyticProperties() {
        ElideStandaloneAnalyticSettings analyticPropeties = new ElideStandaloneAnalyticSettings() {
            @Override
            public boolean enableDynamicModelConfig() {
                return true;
            }

            @Override
            public boolean enableAggregationDataStore() {
                return true;
            }

            @Override
            public boolean enableMetaDataStore() {
                return false;
            }

            @Override
            public String getDefaultDialect() {
                if (inMemory) {
                    return SQLDialectFactory.getDefaultDialect().getDialectType();
                } else {
                    return SQLDialectFactory.getPostgresDialect().getDialectType();
                }
            }

            @Override
            public String getDynamicConfigPath() {
                return "src/main/resources/analytics";
            }
        };
        return analyticPropeties;
    }

    @Override
    public ElideStandaloneSubscriptionSettings getSubscriptionProperties() {
        return new ElideStandaloneSubscriptionSettings() {
            @Override
            public boolean enabled() {
                return true;
            }

            @Override
            public String getPath() {
                return "/subscription";
            }

            @Override
            public ConnectionFactory getConnectionFactory() {
                return new ActiveMQConnectionFactory("vm://0");
            }
        };
    }

    @Override
    public ElideStandaloneAsyncSettings getAsyncProperties() {
        return new ElideStandaloneAsyncSettings() {

            @Override
            public boolean enabled() {
                return false;
            }

            @Override
            public boolean enableCleanup() {
                return true;
            }

            @Override
            public boolean enableExport() {
                return false;
            }
        };
    }

    @Override
    public Properties getDatabaseProperties() {
        Properties dbProps;

        if (inMemory) {
            return getInMemoryProps();
        }

        try {
            dbProps = new Properties();
            dbProps.load(
                    Main.class.getClassLoader().getResourceAsStream("dbconfig.properties")
            );

            dbProps.setProperty("jakarta.persistence.jdbc.url", jdbcUrl);
            dbProps.setProperty("jakarta.persistence.jdbc.user", jdbcUser);
            dbProps.setProperty("jakarta.persistence.jdbc.password", jdbcPassword);
            return dbProps;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public List<Class<?>> getFilters() {
        return Lists.newArrayList(CorsFilter.class);
    }

    @Override
    public void updateServletContextHandler(ServletContextHandler servletContextHandler) {
       ResourceHandler resource_handler = new ResourceHandler();

       try {
           resource_handler.setDirectoriesListed(false);
           resource_handler.setResourceBase(Settings.class.getClassLoader()
                   .getResource("META-INF/resources/").toURI().toString());
           servletContextHandler.insertHandler(resource_handler);
       } catch (Exception e) {
           throw new IllegalStateException(e);
       }
    }

    protected Properties getInMemoryProps() {
        Properties options = new Properties();

        options.put("hibernate.show_sql", "true");
        options.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        options.put("hibernate.current_session_context_class", "thread");
        options.put("hibernate.jdbc.use_scrollable_resultset", "true");
        options.put("hibernate.default_batch_fetch_size", 100);

        options.put("jakarta.persistence.jdbc.driver", "org.h2.Driver");
        options.put("jakarta.persistence.jdbc.url", jdbcUrl);
        options.put("jakarta.persistence.jdbc.user", jdbcUser);
        options.put("jakarta.persistence.jdbc.password", jdbcPassword);

        return options;
    }

    public void runLiquibaseMigrations() throws Exception {
        //Run Liquibase Initialization Script
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(
                new JdbcConnection(DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword)));

        Liquibase liquibase = new liquibase.Liquibase(
                "db/changelog/changelog.xml",
                new ClassLoaderResourceAccessor(),
                database);

        liquibase.update("db1");
    }
}
