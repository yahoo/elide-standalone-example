/*
 * Copyright 2019, Verizon Media.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package example;

import com.yahoo.elide.standalone.ElideStandalone;

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

        settings.runLiquibaseMigrations();

        elide.start();
    }
}
