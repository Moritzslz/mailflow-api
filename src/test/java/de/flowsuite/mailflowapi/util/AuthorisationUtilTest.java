package de.flowsuite.mailflowapi.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.flowsuite.mailflowapi.common.constant.Authorities;
import de.flowsuite.mailflowapi.common.exception.IdorException;
import de.flowsuite.mailflowapi.common.util.AuthorisationUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class AuthorisationUtilTest {

    private static final long USER_ID = 1L;
    private static final long DIFFERENT_USER_ID = 2L;
    private static final long CUSTOMER_ID = 1L;
    private static final long DIFFERENT_CUSTOMER_ID = 2L;

    private Jwt jwtMock;

    @BeforeEach
    void setup() {
        jwtMock = mock(Jwt.class);
    }

    @Test
    void validateAccessToUser_adminAccess_shouldPass() {
        when(jwtMock.getClaim(AuthorisationUtil.CLAIM_SCOPE))
                .thenReturn(Authorities.ADMIN.getAuthority());

        assertDoesNotThrow(() -> AuthorisationUtil.validateAccessToUser(USER_ID, jwtMock));
    }

    @Test
    void validateAccessToUser_clientAccess_shouldPass() {
        when(jwtMock.getClaim(AuthorisationUtil.CLAIM_SCOPE))
                .thenReturn(Authorities.CLIENT.getAuthority());

        assertDoesNotThrow(() -> AuthorisationUtil.validateAccessToUser(USER_ID, jwtMock));
    }

    @Test
    void validateAccessToUser_sameUser_shouldPass() {
        when(jwtMock.getClaim(AuthorisationUtil.CLAIM_SCOPE)).thenReturn("");
        when(jwtMock.getSubject()).thenReturn(String.valueOf(USER_ID));

        assertDoesNotThrow(() -> AuthorisationUtil.validateAccessToUser(USER_ID, jwtMock));
    }

    @Test
    void validateAccessToUser_differentUser_shouldThrowIdorException() {
        when(jwtMock.getClaim(AuthorisationUtil.CLAIM_SCOPE)).thenReturn("");
        when(jwtMock.getSubject()).thenReturn(String.valueOf(DIFFERENT_USER_ID));

        assertThrows(
                IdorException.class,
                () -> AuthorisationUtil.validateAccessToUser(USER_ID, jwtMock));
    }

    @Test
    void validateAccessToCustomer_adminAccess_shouldPass() {
        when(jwtMock.getClaim(AuthorisationUtil.CLAIM_SCOPE))
                .thenReturn(Authorities.ADMIN.getAuthority());

        assertDoesNotThrow(() -> AuthorisationUtil.validateAccessToCustomer(CUSTOMER_ID, jwtMock));
    }

    @Test
    void validateAccessToCustomer_clientAccess_shouldPass() {
        when(jwtMock.getClaim(AuthorisationUtil.CLAIM_SCOPE))
                .thenReturn(Authorities.CLIENT.getAuthority());

        assertDoesNotThrow(() -> AuthorisationUtil.validateAccessToCustomer(CUSTOMER_ID, jwtMock));
    }

    @Test
    void validateAccessToCustomer_sameCustomer_shouldPass() {
        when(jwtMock.getClaim(AuthorisationUtil.CLAIM_SCOPE)).thenReturn("");
        when(jwtMock.getClaim(AuthorisationUtil.CLAIM_CUSTOMER_ID)).thenReturn(CUSTOMER_ID);

        assertDoesNotThrow(() -> AuthorisationUtil.validateAccessToCustomer(CUSTOMER_ID, jwtMock));
    }

    @Test
    void validateAccessToCustomer_differentCustomer_shouldThrowIdorException() {
        when(jwtMock.getClaim(AuthorisationUtil.CLAIM_SCOPE)).thenReturn("");
        when(jwtMock.getClaim(AuthorisationUtil.CLAIM_CUSTOMER_ID))
                .thenReturn(DIFFERENT_CUSTOMER_ID);

        assertThrows(
                IdorException.class,
                () -> AuthorisationUtil.validateAccessToCustomer(CUSTOMER_ID, jwtMock));
    }
}
