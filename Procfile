release: java -cp target/elide-heroku-example.jar liquibase.integration.commandline.Main --changeLogFile=src/main/resources/db/changelog/changelog.xml --url=$JDBC_DATABASE_URL update
web: java -Xmx224m -jar target/elide-heroku-example.jar
