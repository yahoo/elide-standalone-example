/*
 * Copyright 2019, Verizon Media.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package example;

import com.yahoo.elide.core.exceptions.HttpStatus;
import com.yahoo.elide.datastores.jms.websocket.SubscriptionWebSocketTestClient;
import com.yahoo.elide.test.graphql.GraphQLDSL;
import org.junit.jupiter.api.Test;

import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import javax.ws.rs.core.MediaType;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static com.yahoo.elide.Elide.JSONAPI_CONTENT_TYPE;
import static com.yahoo.elide.test.graphql.GraphQLDSL.field;
import static com.yahoo.elide.test.graphql.GraphQLDSL.query;
import static com.yahoo.elide.test.graphql.GraphQLDSL.selection;
import static com.yahoo.elide.test.graphql.GraphQLDSL.selections;
import static com.yahoo.elide.test.jsonapi.JsonApiDSL.attr;
import static com.yahoo.elide.test.jsonapi.JsonApiDSL.attributes;
import static com.yahoo.elide.test.jsonapi.JsonApiDSL.data;
import static com.yahoo.elide.test.jsonapi.JsonApiDSL.id;
import static com.yahoo.elide.test.jsonapi.JsonApiDSL.linkage;
import static com.yahoo.elide.test.jsonapi.JsonApiDSL.relation;
import static com.yahoo.elide.test.jsonapi.JsonApiDSL.relationships;
import static com.yahoo.elide.test.jsonapi.JsonApiDSL.resource;
import static com.yahoo.elide.test.jsonapi.JsonApiDSL.type;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import graphql.ExecutionResult;

import java.net.URI;
import java.util.List;

/**
 * Example functional test.
 */
public class ExampleTest extends IntegrationTest {

    /**
     * This test demonstrates an example test using the JSON-API DSL.
     */
    @Test
    void jsonApiTest() {
        when()
                .get("/api/v1/group")
                .then()
                .body(equalTo(
                        data(
                                resource(
                                        type( "group"),
                                        id("com.example.repository"),
                                        attributes(
                                                attr("commonName", "Example Repository"),
                                                attr("description", "The code for this project")
                                        ),
                                        relationships(
                                                relation("products")
                                        )
                                ),
                                resource(
                                        type( "group"),
                                        id("com.yahoo.elide"),
                                        attributes(
                                                attr("commonName", "Elide"),
                                                attr("description", "The magical library powering this project")
                                        ),
                                        relationships(
                                                relation("products",
                                                        linkage(type("product"), id("elide-core")),
                                                        linkage(type("product"), id("elide-standalone")),
                                                        linkage(type("product"), id("elide-datastore-hibernate5"))
                                                )
                                        )
                                )
                        ).toJSON())
                )
                .log().all()
                .statusCode(HttpStatus.SC_OK);
    }

    /**
     * This test demonstrates an example test using the GraphQL DSL.
     */
    @Test
    void graphqlTest() {
        given()
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body("{ \"query\" : \"" + GraphQLDSL.document(
                query(
                    selection(
                        field("group",
                            selections(
                                field("name"),
                                field("commonName"),
                                field("description")
                            )
                        )
                    )
                )
            ).toQuery() + "\" }"
        )
        .when()
            .post("/graphql/api/v1")
            .then()
            .body(equalTo(GraphQLDSL.document(
                selection(
                    field(
                        "group",
                        selections(
                            field("name", "com.example.repository"),
                            field( "commonName", "Example Repository"),
                            field("description", "The code for this project")
                        ),
                        selections(
                            field("name", "com.yahoo.elide"),
                            field( "commonName", "Elide"),
                            field("description", "The magical library powering this project")
                        )
                    )
                )
            ).toResponse()))
            .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void testLandingPage() {
        when()
                .get("index.html")
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK);

    }

    @Test
    public void testAsyncApiEndPoint() throws Exception {
        given()
                .when()
                .get("/api/v1/asyncQuery")
                .then()
                .statusCode(200);
    }

    @Test
    public void testDownloadAPI() throws Exception {
        given()
                .when()
                .get("/api/v1/downloads?fields[downloads]=downloads,group,product")
                .then()
                .statusCode(200);
    }

    @Test
    public void testSubscription() throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();

        SubscriptionWebSocketTestClient client = new SubscriptionWebSocketTestClient(1,
                List.of("subscription {group(topic: ADDED) {name}}"));

        try (Session session = container.connectToServer(client, new URI("ws://localhost:" + port + "/subscription"))) {

            //Wait for the socket to be full established.
            client.waitOnSubscribe(10);

            given()
                    .contentType(JSONAPI_CONTENT_TYPE)
                    .accept(JSONAPI_CONTENT_TYPE)
                    .body(
                            data(
                                    resource(
                                            type("group"),
                                            id("foo"),
                                            attributes(attr("description", "bar"))
                                    )
                            )
                    )
                    .post("/api/v1/group")
                    .then().statusCode(org.apache.http.HttpStatus.SC_CREATED).body("data.id", equalTo("foo"));


            List<ExecutionResult> results = client.waitOnClose(10);
            assertEquals(1, results.size());
        }
    }
}
