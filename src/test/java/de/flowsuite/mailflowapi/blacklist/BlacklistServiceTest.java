package de.flowsuite.mailflowapi.blacklist;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.flowsuite.mailflowapi.BaseServiceTest;
import de.flowsuite.mailflowapi.common.entity.BlacklistEntry;
import de.flowsuite.mailflowapi.common.entity.User;
import de.flowsuite.mailflowapi.common.exception.EntityAlreadyExistsException;
import de.flowsuite.mailflowapi.common.exception.EntityNotFoundException;
import de.flowsuite.mailflowapi.common.exception.IdConflictException;
import de.flowsuite.mailflowapi.common.exception.IdorException;

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
class BlacklistServiceTest extends BaseServiceTest {

    @Mock private BlacklistRepository blacklistRepository;

    @InjectMocks private BlacklistService blacklistService;

    private final User testUser = buildTestUser();
    private BlacklistEntry testBlacklistEntry;

    private BlacklistEntry buildTestBlacklistEntry() {
        return BlacklistEntry.builder()
                .id(10L)
                .userId(testUser.getId())
                .blacklistedEmailAddress("test@example.com")
                .build();
    }

    @BeforeEach
    void setup() {
        mockJwtForUser(testUser);
        testBlacklistEntry = buildTestBlacklistEntry();
    }

    @Test
    void testCreateBlacklistEntry_success() {
        when(blacklistRepository.existsByUserIdAndBlacklistedEmailAddressHash(
                        testUser.getId(), HASHED_VALUE))
                .thenReturn(false);

        testBlacklistEntry.setId(null);
        assertNull(testBlacklistEntry.getId());

        blacklistService.createBlacklistEntry(
                testUser.getCustomerId(), testUser.getId(), testBlacklistEntry, jwtMock);

        ArgumentCaptor<BlacklistEntry> blacklistEntryCaptor =
                ArgumentCaptor.forClass(BlacklistEntry.class);
        verify(blacklistRepository).save(blacklistEntryCaptor.capture());
        BlacklistEntry savedBlacklistEntry = blacklistEntryCaptor.getValue();

        assertEquals(ENCRYPTED_VALUE, savedBlacklistEntry.getBlacklistedEmailAddress());
        assertEquals(HASHED_VALUE, savedBlacklistEntry.getBlacklistedEmailAddressHash());
        assertEquals(testUser.getId(), savedBlacklistEntry.getUserId());
    }

    @Test
    void testCreateBlacklistEntry_alreadyExists() {
        when(blacklistRepository.existsByUserIdAndBlacklistedEmailAddressHash(
                        testUser.getId(), HASHED_VALUE))
                .thenReturn(true);

        testBlacklistEntry.setId(null);
        assertNull(testBlacklistEntry.getId());

        assertThrows(
                EntityAlreadyExistsException.class,
                () ->
                        blacklistService.createBlacklistEntry(
                                testUser.getCustomerId(),
                                testUser.getId(),
                                testBlacklistEntry,
                                jwtMock));

        verify(blacklistRepository, never()).save(any(BlacklistEntry.class));
    }

    @Test
    void testCreateBlacklistEntry_idConflict() {
        assertThrows(
                IdConflictException.class,
                () ->
                        blacklistService.createBlacklistEntry(
                                testUser.getCustomerId(),
                                testUser.getId(),
                                testBlacklistEntry,
                                jwtMock));

        testBlacklistEntry.setId(null);
        testBlacklistEntry.setUserId(testUser.getId() + 1);

        assertThrows(
                IdConflictException.class,
                () ->
                        blacklistService.createBlacklistEntry(
                                testUser.getCustomerId(),
                                testUser.getId(),
                                testBlacklistEntry,
                                jwtMock));

        verify(blacklistRepository, never()).save(any(BlacklistEntry.class));
    }

    @Test
    void testCreateBlacklistEntry_idor() {
        assertThrows(
                IdorException.class,
                () ->
                        blacklistService.createBlacklistEntry(
                                testUser.getCustomerId() + 1,
                                testUser.getId(),
                                testBlacklistEntry,
                                jwtMock));
        assertThrows(
                IdorException.class,
                () ->
                        blacklistService.createBlacklistEntry(
                                testUser.getCustomerId(),
                                testUser.getId() + 1,
                                testBlacklistEntry,
                                jwtMock));

        verify(blacklistRepository, never()).save(any(BlacklistEntry.class));
    }

    @Test
    void testGetBlacklistEntry_success() {
        when(blacklistRepository.findById(testBlacklistEntry.getId()))
                .thenReturn(Optional.of(testBlacklistEntry));

        BlacklistEntry blacklistEntry =
                blacklistService.getBlacklistEntry(
                        testUser.getCustomerId(),
                        testUser.getId(),
                        testBlacklistEntry.getId(),
                        jwtMock);

        assertEquals(DECRYPTED_VALUE, blacklistEntry.getBlacklistedEmailAddress());
    }

    @Test
    void testGetBlacklistEntry_notFound() {
        when(blacklistRepository.findById(testBlacklistEntry.getId())).thenReturn(Optional.empty());
        assertThrows(
                EntityNotFoundException.class,
                () ->
                        blacklistService.getBlacklistEntry(
                                testUser.getCustomerId(),
                                testUser.getId(),
                                testBlacklistEntry.getId(),
                                jwtMock));
    }

    @Test
    void testGetBlacklistEntry_idor() {
        BlacklistEntry testBlacklistEntryIdor = buildTestBlacklistEntry();
        testBlacklistEntryIdor.setId(testBlacklistEntryIdor.getId() + 1);
        testBlacklistEntryIdor.setUserId(testUser.getId() + 1);

        long testBlackListEntryId = testBlacklistEntry.getId();
        long testBlackListEntryIdIdor = testBlacklistEntryIdor.getId();

        when(blacklistRepository.findById(testBlackListEntryIdIdor))
                .thenReturn(Optional.of(testBlacklistEntryIdor));

        assertThrows(
                IdorException.class,
                () ->
                        blacklistService.getBlacklistEntry(
                                testUser.getCustomerId() + 1,
                                testUser.getId(),
                                testBlackListEntryId,
                                jwtMock));
        assertThrows(
                IdorException.class,
                () ->
                        blacklistService.getBlacklistEntry(
                                testUser.getCustomerId(),
                                testUser.getId() + 1,
                                testBlackListEntryId,
                                jwtMock));
        assertThrows(
                IdorException.class,
                () ->
                        blacklistService.getBlacklistEntry(
                                testUser.getCustomerId(),
                                testUser.getId(),
                                testBlackListEntryIdIdor,
                                jwtMock));
    }

    @Test
    void testListBlacklistEntries_success() {
        when(blacklistRepository.findByUserId(testUser.getId()))
                .thenReturn(List.of(testBlacklistEntry));

        List<BlacklistEntry> blacklistEntries =
                blacklistService.listBlacklistEntries(
                        testUser.getCustomerId(), testUser.getId(), jwtMock);

        assertEquals(1, blacklistEntries.size());
        assertEquals(testBlacklistEntry, blacklistEntries.get(0));
        assertEquals(DECRYPTED_VALUE, blacklistEntries.get(0).getBlacklistedEmailAddress());
    }

    @Test
    void testListBlacklistEntries_idor() {
        assertThrows(
                IdorException.class,
                () ->
                        blacklistService.listBlacklistEntries(
                                testUser.getCustomerId() + 1, testUser.getId(), jwtMock));
        assertThrows(
                IdorException.class,
                () ->
                        blacklistService.listBlacklistEntries(
                                testUser.getCustomerId(), testUser.getId() + 1, jwtMock));

        verify(blacklistRepository, never()).findByUserId(anyLong());
    }

    @Test
    void testDeleteBlacklistEntry_success() {
        when(blacklistRepository.findById(testBlacklistEntry.getId()))
                .thenReturn(Optional.of(testBlacklistEntry));

        blacklistService.deleteBlacklistEntry(
                testUser.getCustomerId(), testUser.getId(), testBlacklistEntry.getId(), jwtMock);

        ArgumentCaptor<BlacklistEntry> blacklistEntryCaptor =
                ArgumentCaptor.forClass(BlacklistEntry.class);
        verify(blacklistRepository).delete(blacklistEntryCaptor.capture());
        BlacklistEntry deletedBlacklistEntry = blacklistEntryCaptor.getValue();

        assertEquals(testBlacklistEntry, deletedBlacklistEntry);
    }

    @Test
    void testDeleteBlacklistEntry_notFound() {
        testGetBlacklistEntry_notFound();
        verify(blacklistRepository, never()).delete(any(BlacklistEntry.class));
    }

    @Test
    void testDeleteBlacklistEntry_idor() {
        testGetBlacklistEntry_idor();
        verify(blacklistRepository, never()).delete(any(BlacklistEntry.class));
    }
}
