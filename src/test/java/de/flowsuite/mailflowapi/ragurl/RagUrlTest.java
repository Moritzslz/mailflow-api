package de.flowsuite.mailflowapi.ragurl;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import de.flowsuite.mailflowapi.BaseServiceTest;
import de.flowsuite.mailflowcommon.entity.MessageCategory;
import de.flowsuite.mailflowcommon.entity.RagUrl;
import de.flowsuite.mailflowcommon.entity.User;
import de.flowsuite.mailflowcommon.exception.EntityAlreadyExistsException;
import de.flowsuite.mailflowcommon.exception.EntityNotFoundException;
import de.flowsuite.mailflowcommon.exception.IdConflictException;
import de.flowsuite.mailflowcommon.exception.IdorException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class RagUrlTest extends BaseServiceTest {

    @Mock private RagUrlRepository ragUrlRepository;

    @InjectMocks private RagUrlService ragUrlService;

    private final User testUser = buildTestUser();
    private RagUrl testRagUrl;

    private RagUrl bulildTestRagUrl() {
        return new RagUrl(1L, testUser.getCustomerId(), "https://www.example.com", true);
    }

    @BeforeEach
    void setup() {
        mockJwtWithCustomerClaimsOnly(testUser);
        testRagUrl = bulildTestRagUrl();
    }

    @Test
    void testCreateRagUrl_success() {
        when(ragUrlRepository.existsByCustomerIdAndUrl(
                        testUser.getCustomerId(), testRagUrl.getUrl()))
                .thenReturn(false);

        testRagUrl.setId(null);
        assertNull(testRagUrl.getId());

        ragUrlService.createRagUrl(testUser.getCustomerId(), testRagUrl, jwtMock);

        ArgumentCaptor<RagUrl> ragUrlCaptor = ArgumentCaptor.forClass(RagUrl.class);
        verify(ragUrlRepository).save(ragUrlCaptor.capture());
        RagUrl savedRagUrl = ragUrlCaptor.getValue();

        assertNotNull(savedRagUrl);
        assertEquals(testRagUrl, savedRagUrl);
    }

    @Test
    void testCreateRagUrl_alreadyExists() {
        when(ragUrlRepository.existsByCustomerIdAndUrl(
                        testUser.getCustomerId(), testRagUrl.getUrl()))
                .thenReturn(true);

        testRagUrl.setId(null);
        assertNull(testRagUrl.getId());

        assertThrows(
                EntityAlreadyExistsException.class,
                () -> ragUrlService.createRagUrl(testUser.getCustomerId(), testRagUrl, jwtMock));

        verify(ragUrlRepository, never()).save(any(RagUrl.class));
    }

    @Test
    void testCreateRagUrl_idConflict() {
        assertThrows(
                IdConflictException.class,
                () -> ragUrlService.createRagUrl(testUser.getCustomerId(), testRagUrl, jwtMock));

        testRagUrl.setId(null);
        testRagUrl.setCustomerId(testUser.getCustomerId() + 1);

        assertThrows(
                IdConflictException.class,
                () -> ragUrlService.createRagUrl(testUser.getCustomerId(), testRagUrl, jwtMock));

        verify(ragUrlRepository, never()).save(any(RagUrl.class));
    }

    @Test
    void testCreateRagUrl_idor() {
        assertThrows(
                IdorException.class,
                () ->
                        ragUrlService.createRagUrl(
                                testUser.getCustomerId() + 1, testRagUrl, jwtMock));

        verify(ragUrlRepository, never()).save(any(RagUrl.class));
    }

    @Test
    void testGetRagUrl_success() {
        when(ragUrlRepository.findById(testRagUrl.getId())).thenReturn(Optional.of(testRagUrl));

        RagUrl ragUrl =
                ragUrlService.getRagUrl(testUser.getCustomerId(), testRagUrl.getId(), jwtMock);

        verify(ragUrlRepository).findById(testRagUrl.getId());

        assertEquals(testRagUrl, ragUrl);
    }

    @Test
    void testGetRagUrl_notFound() {
        when(ragUrlRepository.findById(testRagUrl.getId())).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () ->
                        ragUrlService.getRagUrl(
                                testUser.getCustomerId(), testRagUrl.getId(), jwtMock));
    }

    @Test
    void testGetRagUrl_idor() {
        when(ragUrlRepository.findById(testRagUrl.getId())).thenReturn(Optional.of(testRagUrl));

        assertThrows(
                IdorException.class,
                () ->
                        ragUrlService.getRagUrl(
                                testUser.getCustomerId() + 1, testRagUrl.getId(), jwtMock));

        testRagUrl.setCustomerId(testUser.getCustomerId() + 1);

        assertThrows(
                IdorException.class,
                () ->
                        ragUrlService.getRagUrl(
                                testUser.getCustomerId(), testRagUrl.getId(), jwtMock));
    }

    @Test
    void testListRagUrls_success() {
        when(ragUrlRepository.findByCustomerId(testUser.getCustomerId()))
                .thenReturn(List.of(testRagUrl));

        List<RagUrl> ragUrls = ragUrlService.listRagUrls(testUser.getCustomerId(), jwtMock);

        assertEquals(1, ragUrls.size());
        assertEquals(testRagUrl, ragUrls.get(0));
    }

    @Test
    void testListRagUrls_idor() {
        assertThrows(
                IdorException.class,
                () -> ragUrlService.listRagUrls(testUser.getCustomerId() + 1, jwtMock));

        verify(ragUrlRepository, never()).findByCustomerId(anyLong());
    }

    @Test
    void testDeleteRagUrl_success() {
        when(ragUrlRepository.findById(testRagUrl.getId())).thenReturn(Optional.of(testRagUrl));

        ragUrlService.deleteRagUrl(testUser.getCustomerId(), testRagUrl.getId(), jwtMock);

        ArgumentCaptor<RagUrl> ragUrlCaptor = ArgumentCaptor.forClass(RagUrl.class);
        verify(ragUrlRepository).delete(ragUrlCaptor.capture());
        RagUrl deletedRagUrl = ragUrlCaptor.getValue();

        assertEquals(testRagUrl, deletedRagUrl);
    }

    @Test
    void testDeleteBlacklistEntry_notFound() {
        testGetRagUrl_notFound();
        verify(ragUrlRepository, never()).delete(any(RagUrl.class));
    }

    @Test
    void testDeleteBlacklistEntry_idor() {
        testGetRagUrl_idor();
        verify(ragUrlRepository, never()).delete(any(RagUrl.class));
    }
}
