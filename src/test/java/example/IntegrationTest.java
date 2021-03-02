/*
 * Copyright 2019, Verizon Media.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */

package example;

import com.yahoo.elide.standalone.ElideStandalone;
import com.yahoo.elide.standalone.config.ElideStandaloneAsyncSettings;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

/**
 * Base class for running a set of functional Elide tests.  This class
 * sets up an Elide instance with an in-memory H2 database.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IntegrationTest {
    private ElideStandalone elide;

    @BeforeAll
    public void init() throws Exception {
        Settings settings = new Settings(true) {
            @Override
            public int getPort() {
                return 8080;
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

        elide.start(false);
    }

    @AfterAll
    public void shutdown() throws Exception {
        elide.stop();
    }
}
