web: java -jar target/elide-heroku-example.jar

release: java -jar target/dependency/liquibase.jar --changeLogFile=src/main/resources/db/changelog/changelog.xml --url=$JDBC_DATABASE_URL --classpath=target/dependency/postgres.jar update
