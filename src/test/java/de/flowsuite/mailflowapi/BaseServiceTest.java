package de.flowsuite.mailflowapi;

import static org.mockito.Mockito.*;

import de.flowsuite.mailflowcommon.constant.Authorities;
import de.flowsuite.mailflowcommon.entity.User;
import de.flowsuite.mailflowcommon.util.AesUtil;
import de.flowsuite.mailflowcommon.util.AuthorisationUtil;
import de.flowsuite.mailflowcommon.util.HmacUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.ZonedDateTime;

public abstract class BaseServiceTest {

    protected static final String ENCRYPTED_VALUE = "encrypted-value";
    protected static final String DECRYPTED_VALUE = "decrypted-value";
    protected static final String HASHED_VALUE = "hashed-value";
    protected static final String VERIFICATION_TOKEN = "verification-token";

    protected MockedStatic<AesUtil> aesUtilMock;
    protected MockedStatic<HmacUtil> hmacUtilMock;
    protected Jwt jwtMock;

    protected User buildTestUser() {
        return User.builder()
                .id(100L)
                .customerId(100L)
                .firstName(ENCRYPTED_VALUE)
                .lastName(ENCRYPTED_VALUE)
                .emailAddressHash(HASHED_VALUE)
                .emailAddress(ENCRYPTED_VALUE)
                .password(HASHED_VALUE)
                .phoneNumber(ENCRYPTED_VALUE)
                .position(null)
                .role(Authorities.USER.getAuthority())
                .isAccountLocked(false)
                .isAccountEnabled(false)
                .isSubscribedToNewsletter(true)
                .verificationToken(VERIFICATION_TOKEN)
                .tokenExpiresAt(ZonedDateTime.now().plusMinutes(30))
                .build();
    }

    @BeforeEach
    void baseSetup() {
        aesUtilMock = mockStatic(AesUtil.class);
        hmacUtilMock = mockStatic(HmacUtil.class);
        jwtMock = mock(Jwt.class);
        when(AesUtil.encrypt(anyString())).thenReturn(ENCRYPTED_VALUE);
        when(AesUtil.decrypt(anyString())).thenReturn(DECRYPTED_VALUE);
        when(HmacUtil.hash(anyString())).thenReturn(HASHED_VALUE);
    }

    @AfterEach
    void baseTearDown() {
        aesUtilMock.close();
        hmacUtilMock.close();
    }

    protected void mockJwtWithUserAndCustomerClaims(User user) {
        when(jwtMock.getClaim(AuthorisationUtil.CLAIM_SCOPE))
                .thenReturn(Authorities.USER.getAuthority());
        when(jwtMock.getSubject()).thenReturn(String.valueOf(user.getId()));
        when(jwtMock.getClaim(AuthorisationUtil.CLAIM_CUSTOMER_ID))
                .thenReturn(user.getCustomerId());
    }

    protected void mockJwtWithCustomerClaimsOnly(User user) {
        when(jwtMock.getClaim(AuthorisationUtil.CLAIM_SCOPE))
                .thenReturn(Authorities.USER.getAuthority());
        when(jwtMock.getClaim(AuthorisationUtil.CLAIM_CUSTOMER_ID))
                .thenReturn(user.getCustomerId());
    }
}
