package example.filters;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

import example.security.ElideStandaloneExampleSecurityContext;
import example.security.ElideStandaloneExampleUser;

/**
 * HTTP Auth filter Implementation for Elide Standalone Example to use async query.
 */
public class AuthFilter implements ContainerRequestFilter {

    private static final String TEST_USER = "test";

    @Override
    public void filter(ContainerRequestContext request) {

        ElideStandaloneExampleUser user = new ElideStandaloneExampleUser();
        user.setName(TEST_USER);

        request.setSecurityContext(new ElideStandaloneExampleSecurityContext(user));

    }
}
