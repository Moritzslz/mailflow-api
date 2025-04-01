package de.flowsuite.mailflowapi.common.util.security;

import de.flowsuite.mailflowapi.common.entity.Authorities;
import de.flowsuite.mailflowapi.common.exception.AuthenticationException;
import de.flowsuite.mailflowapi.common.exception.IdorException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class AuthorisationUtil {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorisationUtil.class);

    public static void checkUserAllowed(long userId) {
        Jwt jwt = getJWT();

        String scope = jwt.getClaim("scope");
        long jwtUserId = jwt.getClaim("userId");

        LOG.debug("Scope, {}", scope);
        LOG.debug("UserId, {}", userId);

        if (scope.contains(Authorities.ADMIN.getAuthority())
                || scope.contains(Authorities.CLIENT.getAuthority())) {
            return;
        }

        if (jwtUserId != userId) {
            throw new IdorException();
        }
    }

    public static void checkCustomerAllowed(long customerId) {
        Jwt jwt = getJWT();

        String scope = jwt.getClaim("scope");
        long jwtCustomerId = jwt.getClaim("customerId");

        LOG.debug("Scope, {}", scope);
        LOG.debug("CustomerId, {}", customerId);

        if (scope.contains(Authorities.ADMIN.getAuthority())
                || scope.contains(Authorities.CLIENT.getAuthority())) {
            return;
        }

        if (jwtCustomerId != customerId) {
            throw new IdorException();
        }
    }

    private static Jwt getJWT() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken)) {
            throw new AuthenticationException();
        }

        return (Jwt) authentication.getPrincipal();
    }
}
