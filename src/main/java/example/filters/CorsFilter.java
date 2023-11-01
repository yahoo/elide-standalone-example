/*
 * Copyright 2019, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */

package example.filters;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MultivaluedMap;

import java.io.IOException;

public class CorsFilter implements ContainerResponseFilter {
    @Override
    public void filter(ContainerRequestContext request,
                       ContainerResponseContext response) throws IOException {
        String accessControlHeaders = request.getHeaderString("Access-Control-Request-Headers");
        String origin = request.getHeaderString("Origin");

        MultivaluedMap<String, Object> requestHeaders = response.getHeaders();
        requestHeaders.putSingle("Vary", "Origin");
        requestHeaders.putSingle("Access-Control-Allow-Methods", "OPTIONS,GET,POST,PATCH,DELETE");
        requestHeaders.putSingle("Access-Control-Allow-Headers",
                accessControlHeaders != null ? accessControlHeaders : "");
        requestHeaders.putSingle("Access-Control-Allow-Origin", origin != null ? origin : "*");
        requestHeaders.putSingle("Access-Control-Allow-Credentials", "true");
    }
}
