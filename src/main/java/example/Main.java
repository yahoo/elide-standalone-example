/*
 * Copyright 2019, Verizon Media.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package example;

import com.yahoo.elide.standalone.ElideStandalone;

import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.server.JournalType;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import lombok.extern.slf4j.Slf4j;

/**
 * Example app using Elide library.
 */
@Slf4j
public class Main {
    public static void main(String[] args) throws Exception {

        //If JDBC_DATABASE_URL is not set, we'll run with H2 in memory.
        boolean inMemory = (System.getenv("JDBC_DATABASE_URL") == null);

        Settings settings = new Settings(inMemory) {};

        ElideStandalone elide = new ElideStandalone(settings);

        if (inMemory) {
            settings.runLiquibaseMigrations();
        }

        //Startup up an embedded active MQ.
        EmbeddedActiveMQ embedded = new EmbeddedActiveMQ();
        Configuration configuration = new ConfigurationImpl();
        configuration.addAcceptorConfiguration("default", "vm://0");
        configuration.setPersistenceEnabled(false);
        configuration.setSecurityEnabled(false);
        configuration.setJournalType(JournalType.NIO);

        embedded.setConfiguration(configuration);
        embedded.start();

        elide.start();
    }
}
