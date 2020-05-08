/*
 * Copyright 2020, Verizon Media.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */

package example.filters;

import java.security.Principal;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.SecurityContext;

/**
 * Example HTTP Auth filter implementation for Elide Standalone Example to use async query.
 * This implementation fakes an anonymous Test user.
 * Please replace with your implementation when using this class.
 */
public class ExampleAuthFilter implements ContainerRequestFilter {

    private static final String TEST_USER = "test";

    @Override
    public void filter(ContainerRequestContext request) {

        ElideStandaloneExampleUser user = new ElideStandaloneExampleUser();
        
        user.setName(TEST_USER);

        request.setSecurityContext(new ElideStandaloneExampleSecurityContext(user));

    }
    
    /**
     * Principal Implementation for Elide Standalone Example to use async query.
     */
    class ElideStandaloneExampleUser implements Principal {
        private String name;

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }
    };
    
    /**
     * Security Context Implementation for Elide Standalone Example to use async query.
     */
    class ElideStandaloneExampleSecurityContext implements SecurityContext {
        private ElideStandaloneExampleUser user;

        public ElideStandaloneExampleSecurityContext(ElideStandaloneExampleUser user) {
            this.user = user;
        }

        @Override
        public String getAuthenticationScheme() {
            return SecurityContext.BASIC_AUTH;
        }

        @Override
        public Principal getUserPrincipal() {
            return this.user;
        }

        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public boolean isUserInRole(String arg0) {
            return false;
        }

    }
    
}

