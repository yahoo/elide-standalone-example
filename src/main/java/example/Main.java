/*
 * Copyright 2019, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package example;

import com.yahoo.elide.ElideSettings;
import com.yahoo.elide.ElideSettingsBuilder;
import com.yahoo.elide.core.DataStore;
import com.yahoo.elide.core.EntityDictionary;
import com.yahoo.elide.core.filter.dialect.RSQLFilterDialect;
import com.yahoo.elide.datastores.jpa.JpaDataStore;
import com.yahoo.elide.datastores.jpa.transaction.NonJtaTransaction;
import com.yahoo.elide.standalone.ElideStandalone;

import com.yahoo.elide.standalone.Util;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.hk2.api.ServiceLocator;

import javax.persistence.EntityManagerFactory;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

/**
 * Example app using Elide library.
 */
@Slf4j
public class Main {
    public static void main(String[] args) throws Exception {
        ElideStandalone elide = new ElideStandalone(new CommonElideSettings() {
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
        });

        elide.start();
    }
}
