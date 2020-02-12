package example.security;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

/**
 * Security Context Implementation for Elide Standalone Example to use async query.
 */
public class ElideStandaloneExampleSecurityContext implements SecurityContext {
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
