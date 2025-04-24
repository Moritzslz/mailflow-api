package de.flowsuite.mailflowapi;

import static org.mockito.Mockito.*;

import de.flowsuite.mailflowapi.common.auth.Authorities;
import de.flowsuite.mailflowapi.common.entity.User;
import de.flowsuite.mailflowapi.common.util.AesUtil;
import de.flowsuite.mailflowapi.common.util.AuthorisationUtil;
import de.flowsuite.mailflowapi.common.util.HmacUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;
import org.springframework.security.oauth2.jwt.Jwt;

public abstract class BaseServiceTest {

    protected static final String ENCRYPTED_VALUE = "encrypted-value";
    protected static final String DECRYPTED_VALUE = "decrypted-value";
    protected static final String HASHED_VALUE = "hashed-value";
    protected static final String VERIFICATION_TOKEN = "verification-token";

    protected MockedStatic<AesUtil> aesUtilMock;
    protected MockedStatic<HmacUtil> hmacUtilMock;
    protected Jwt jwtMock;

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

    protected void mockJwtForUser(User user) {
        when(jwtMock.getClaim(AuthorisationUtil.CLAIM_SCOPE))
                .thenReturn(Authorities.USER.getAuthority());
        when(jwtMock.getSubject()).thenReturn(String.valueOf(user.getId()));
        when(jwtMock.getClaim(AuthorisationUtil.CLAIM_CUSTOMER_ID))
                .thenReturn(user.getCustomerId());
    }
}
