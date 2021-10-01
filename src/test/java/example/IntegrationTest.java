/*
 * Copyright 2019, Verizon Media.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */

package example;

import com.yahoo.elide.standalone.ElideStandalone;
import com.yahoo.elide.standalone.config.ElideStandaloneAsyncSettings;
import com.yahoo.elide.standalone.config.ElideStandaloneSubscriptionSettings;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.server.JournalType;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import javax.jms.ConnectionFactory;

/**
 * Base class for running a set of functional Elide tests.  This class
 * sets up an Elide instance with an in-memory H2 database.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IntegrationTest {
    protected static final String EMBEDDED_JMS_URL = "vm://0";
    private ElideStandalone elide;
    protected int port = 8080;

    @BeforeAll
    public void init() throws Exception {
        Settings settings = new Settings(true) {
            @Override
            public int getPort() {
                return port;
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
                    public boolean shouldSendPingOnSubscribe() {
                        return true;
                    }

                    @Override
                    public ConnectionFactory getConnectionFactory() {
                        return new ActiveMQConnectionFactory(EMBEDDED_JMS_URL);
                    }
                };
            }

            @Override
            public ElideStandaloneAsyncSettings getAsyncProperties() {
                return new ElideStandaloneAsyncSettings() {

                    @Override
                    public boolean enabled() {
                        return true;
                    }

                    @Override
                    public boolean enableCleanup() {
                        return true;
                    }

                    @Override
                    public boolean enableExport() {
                        return true;
                    }
                };
            }
        };

        elide = new ElideStandalone(settings);

        settings.runLiquibaseMigrations();

        //Startup up an embedded active MQ.
        EmbeddedActiveMQ embedded = new EmbeddedActiveMQ();
        Configuration configuration = new ConfigurationImpl();
        configuration.addAcceptorConfiguration("default", EMBEDDED_JMS_URL);
        configuration.setPersistenceEnabled(false);
        configuration.setSecurityEnabled(false);
        configuration.setJournalType(JournalType.NIO);

        embedded.setConfiguration(configuration);
        embedded.start();


        elide.start(false);
    }

    @AfterAll
    public void shutdown() throws Exception {
        elide.stop();
    }
}
