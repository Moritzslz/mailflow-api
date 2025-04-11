package de.flowsuite.mailflowapi.ragurl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import de.flowsuite.mailflowapi.common.entity.RagUrl;
import de.flowsuite.mailflowapi.common.exception.EntityNotFoundException;
import de.flowsuite.mailflowapi.common.exception.IdConflictException;
import de.flowsuite.mailflowapi.common.exception.IdorException;
import de.flowsuite.mailflowapi.common.util.AuthorisationUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class RagUrlServiceTest {

    @Mock private RagUrlRepository ragUrlRepository;

    @InjectMocks private RagUrlService ragUrlService;

    private Jwt jwt;

    private MockedStatic<AuthorisationUtil> authUtilMock;

    @BeforeEach
    void setup() {
        jwt = mock(Jwt.class);
        authUtilMock = mockStatic(AuthorisationUtil.class);
    }

    @AfterEach
    void tearDown() {
        authUtilMock.close();
    }

    void setupDefaultAuthUtil() {
        authUtilMock
                .when(() -> AuthorisationUtil.validateAccessToCustomer(anyLong(), any(Jwt.class)))
                .thenAnswer(invocation -> null);
        authUtilMock
                .when(() -> AuthorisationUtil.validateAccessToUser(anyLong(), any(Jwt.class)))
                .thenAnswer(invocation -> null);
    }

    @Test
    void testCreateRagUrl_Success() {
        long customerId = 1L;
        RagUrl ragUrl =
                RagUrl.builder()
                        .customerId(customerId)
                        .url("http://example.com")
                        .isLastCrawlSuccessful(true)
                        .build();

        setupDefaultAuthUtil();

        RagUrl savedRagUrl =
                RagUrl.builder()
                        .id(10L)
                        .customerId(customerId)
                        .url("http://example.com")
                        .isLastCrawlSuccessful(true)
                        .build();
        when(ragUrlRepository.save(any(RagUrl.class))).thenReturn(savedRagUrl);

        RagUrl result = ragUrlService.createRagUrl(customerId, ragUrl, jwt);
        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("http://example.com", result.getUrl());
    }

    @Test
    void testCreateRagUrl_IdConflict() {
        long customerId = 1L;
        // The ragUrl's customerId is different from the provided customerId.
        RagUrl ragUrl =
                RagUrl.builder()
                        .customerId(2L)
                        .url("http://example.com")
                        .isLastCrawlSuccessful(true)
                        .build();

        setupDefaultAuthUtil();

        assertThrows(
                IdConflictException.class,
                () -> ragUrlService.createRagUrl(customerId, ragUrl, jwt));
    }

    @Test
    void testListRagUrls() {
        long customerId = 1L;
        RagUrl ragUrl =
                RagUrl.builder()
                        .id(1L)
                        .customerId(customerId)
                        .url("http://example.com")
                        .isLastCrawlSuccessful(false)
                        .build();

        when(ragUrlRepository.findByCustomerId(customerId)).thenReturn(List.of(ragUrl));

        authUtilMock
                .when(() -> AuthorisationUtil.validateAccessToCustomer(anyLong(), any(Jwt.class)))
                .thenAnswer(invocation -> null);

        List<RagUrl> result = ragUrlService.listRagUrls(customerId, jwt);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("http://example.com", result.get(0).getUrl());
    }

    @Test
    void testDeleteRagUrl_Success() {
        long customerId = 1L;
        long ragUrlId = 10L;
        RagUrl ragUrl =
                RagUrl.builder()
                        .id(ragUrlId)
                        .customerId(customerId)
                        .url("http://example.com")
                        .isLastCrawlSuccessful(true)
                        .build();

        when(ragUrlRepository.findById(ragUrlId)).thenReturn(Optional.of(ragUrl));

        authUtilMock
                .when(() -> AuthorisationUtil.validateAccessToCustomer(anyLong(), any(Jwt.class)))
                .thenAnswer(invocation -> null);

        assertDoesNotThrow(() -> ragUrlService.deleteRagUrl(customerId, ragUrlId, jwt));
        verify(ragUrlRepository).delete(ragUrl);
    }

    @Test
    void testDeleteRagUrl_NotFound() {
        long customerId = 1L;
        long ragUrlId = 10L;

        when(ragUrlRepository.findById(ragUrlId)).thenReturn(Optional.empty());

        authUtilMock
                .when(() -> AuthorisationUtil.validateAccessToCustomer(anyLong(), any(Jwt.class)))
                .thenAnswer(invocation -> null);

        assertThrows(
                EntityNotFoundException.class,
                () -> ragUrlService.deleteRagUrl(customerId, ragUrlId, jwt));
    }

    @Test
    void testDeleteRagUrl_Idor() {
        long customerId = 1L;
        long ragUrlId = 10L;
        // RagUrl with a different customerId.
        RagUrl ragUrl =
                RagUrl.builder()
                        .id(ragUrlId)
                        .customerId(2L)
                        .url("http://example.com")
                        .isLastCrawlSuccessful(true)
                        .build();

        when(ragUrlRepository.findById(ragUrlId)).thenReturn(Optional.of(ragUrl));

        authUtilMock
                .when(() -> AuthorisationUtil.validateAccessToCustomer(anyLong(), any(Jwt.class)))
                .thenAnswer(invocation -> null);

        assertThrows(
                IdorException.class, () -> ragUrlService.deleteRagUrl(customerId, ragUrlId, jwt));
    }
}
