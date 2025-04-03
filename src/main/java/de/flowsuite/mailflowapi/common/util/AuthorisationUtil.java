package de.flowsuite.mailflowapi.common.util;

import de.flowsuite.mailflowapi.common.entity.Authorities;
import de.flowsuite.mailflowapi.common.exception.IdorException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;

public class AuthorisationUtil {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorisationUtil.class);
    public static final String CLAIM_SCOPE = "scope";
    public static final String CLAIM_SUB = "sub";
    public static final String CLAIM_CUSTOMER_ID = "customerId";

    public static void validateAccess(long entityId, Jwt jwt, String claimType) {
        String scope = jwt.getClaim(CLAIM_SCOPE);

        LOG.debug("Scope: {}", scope);

        if (scope.contains(Authorities.ADMIN.getAuthority())
                || scope.contains(Authorities.CLIENT.getAuthority())) {
            return;
        }

        long jwtEntityId;
        if (claimType.equals(CLAIM_SUB)) {
            jwtEntityId = Long.parseLong(jwt.getClaim(claimType));
        } else {
            jwtEntityId = jwt.getClaim(claimType);
        }

        LOG.debug("jwtEntityId: {}, entityId: {}", jwtEntityId, entityId);

        if (jwtEntityId != entityId) {
            throw new IdorException();
        }
    }

    public static void validateAccessToUser(long userId, Jwt jwt) {
        validateAccess(userId, jwt, CLAIM_SUB);
    }

    public static void validateAccessToCustomer(long customerId, Jwt jwt) {
        validateAccess(customerId, jwt, CLAIM_CUSTOMER_ID);
    }
}
