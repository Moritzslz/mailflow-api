package de.flowsuite.mailflowapi.blacklist;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.flowsuite.mailflowapi.common.entity.BlacklistEntry;
import de.flowsuite.mailflowapi.common.exception.EntityNotFoundException;
import de.flowsuite.mailflowapi.common.exception.IdConflictException;
import de.flowsuite.mailflowapi.common.exception.IdorException;
import de.flowsuite.mailflowapi.common.util.AesUtil;
import de.flowsuite.mailflowapi.common.util.AuthorisationUtil;
import de.flowsuite.mailflowapi.common.util.HmacUtil;
import de.flowsuite.mailflowapi.common.util.Util;

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
class BlacklistServiceTest {

    @Mock private BlacklistRepository blacklistRepository;

    @InjectMocks private BlacklistService blacklistService;

    private Jwt jwt;

    @BeforeEach
    void setup() {
        jwt = mock(Jwt.class);
    }

    @Test
    void testCreateBlacklistEntry_Success() {
        long customerId = 1L;
        long userId = 100L;
        BlacklistEntry entry =
                BlacklistEntry.builder()
                        .userId(userId)
                        .blacklistedEmailAddress("test@example.com")
                        .build();

        try (MockedStatic<AuthorisationUtil> authUtilMock = mockStatic(AuthorisationUtil.class);
                MockedStatic<Util> utilMock = mockStatic(Util.class);
                MockedStatic<HmacUtil> hmacUtilMock = mockStatic(HmacUtil.class);
                MockedStatic<AesUtil> aesUtilMock = mockStatic(AesUtil.class)) {

            authUtilMock
                    .when(() -> AuthorisationUtil.validateAccessToCustomer(customerId, jwt))
                    .thenAnswer(invocation -> null);
            authUtilMock
                    .when(() -> AuthorisationUtil.validateAccessToUser(userId, jwt))
                    .thenAnswer(invocation -> null);
            utilMock.when(() -> Util.validateEmailAddress("test@example.com"))
                    .thenAnswer(invocation -> null);

            hmacUtilMock.when(() -> HmacUtil.hash("test@example.com")).thenReturn("hashed-email");
            aesUtilMock
                    .when(() -> AesUtil.encrypt("test@example.com"))
                    .thenReturn("encrypted-email");

            BlacklistEntry savedEntry =
                    BlacklistEntry.builder()
                            .id(10L)
                            .userId(userId)
                            .blacklistedEmailAddress("encrypted-email")
                            .build();
            when(blacklistRepository.save(any(BlacklistEntry.class))).thenReturn(savedEntry);

            BlacklistEntry result =
                    blacklistService.createBlacklistEntry(customerId, userId, entry, jwt);

            assertNotNull(result);
            assertEquals(10L, result.getId());
            assertEquals("encrypted-email", result.getBlacklistedEmailAddress());
            verify(blacklistRepository).save(any(BlacklistEntry.class));
        }
    }

    @Test
    void testCreateBlacklistEntry_IdConflict() {
        long customerId = 1L;
        long userId = 100L;
        // Mismatched userId between method parameter and entity
        BlacklistEntry entry =
                BlacklistEntry.builder()
                        .userId(200L)
                        .blacklistedEmailAddress("test@example.com")
                        .build();

        try (MockedStatic<AuthorisationUtil> authUtilMock = mockStatic(AuthorisationUtil.class);
                MockedStatic<Util> utilMock = mockStatic(Util.class)) {
            authUtilMock
                    .when(() -> AuthorisationUtil.validateAccessToCustomer(customerId, jwt))
                    .thenAnswer(invocation -> null);
            authUtilMock
                    .when(() -> AuthorisationUtil.validateAccessToUser(userId, jwt))
                    .thenAnswer(invocation -> null);
            utilMock.when(() -> Util.validateEmailAddress("test@example.com"))
                    .thenAnswer(invocation -> null);

            assertThrows(
                    IdConflictException.class,
                    () -> blacklistService.createBlacklistEntry(customerId, userId, entry, jwt));
        }
    }

    @Test
    void testListBlacklistEntries() {
        long customerId = 1L;
        long userId = 100L;
        BlacklistEntry entry =
                BlacklistEntry.builder()
                        .id(1L)
                        .userId(userId)
                        .blacklistedEmailAddress("encrypted-email")
                        .build();

        when(blacklistRepository.findByUserId(userId)).thenReturn(List.of(entry));

        try (MockedStatic<AuthorisationUtil> authUtilMock = mockStatic(AuthorisationUtil.class);
                MockedStatic<AesUtil> aesUtilMock = mockStatic(AesUtil.class)) {

            authUtilMock
                    .when(() -> AuthorisationUtil.validateAccessToCustomer(customerId, jwt))
                    .thenAnswer(invocation -> null);
            authUtilMock
                    .when(() -> AuthorisationUtil.validateAccessToUser(userId, jwt))
                    .thenAnswer(invocation -> null);
            aesUtilMock
                    .when(() -> AesUtil.decrypt("encrypted-email"))
                    .thenReturn("decrypted-email");

            List<BlacklistEntry> result =
                    blacklistService.listBlacklistEntries(customerId, userId, jwt);
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("decrypted-email", result.get(0).getBlacklistedEmailAddress());
        }
    }

    @Test
    void testDeleteBlacklistEntry_Success() {
        long customerId = 1L;
        long userId = 100L;
        long blacklistEntryId = 5L;
        BlacklistEntry entry =
                BlacklistEntry.builder()
                        .id(blacklistEntryId)
                        .userId(userId)
                        .blacklistedEmailAddress("encrypted-email")
                        .build();

        when(blacklistRepository.findById(blacklistEntryId)).thenReturn(Optional.of(entry));

        try (MockedStatic<AuthorisationUtil> authUtilMock = mockStatic(AuthorisationUtil.class)) {
            authUtilMock
                    .when(() -> AuthorisationUtil.validateAccessToCustomer(customerId, jwt))
                    .thenAnswer(invocation -> null);
            authUtilMock
                    .when(() -> AuthorisationUtil.validateAccessToUser(userId, jwt))
                    .thenAnswer(invocation -> null);

            assertDoesNotThrow(
                    () ->
                            blacklistService.deleteBlacklistEntry(
                                    customerId, userId, blacklistEntryId, jwt));
            verify(blacklistRepository).delete(entry);
        }
    }

    @Test
    void testDeleteBlacklistEntry_NotFound() {
        long customerId = 1L;
        long userId = 100L;
        long blacklistEntryId = 5L;

        when(blacklistRepository.findById(blacklistEntryId)).thenReturn(Optional.empty());

        try (MockedStatic<AuthorisationUtil> authUtilMock = mockStatic(AuthorisationUtil.class)) {
            authUtilMock
                    .when(() -> AuthorisationUtil.validateAccessToCustomer(customerId, jwt))
                    .thenAnswer(invocation -> null);
            authUtilMock
                    .when(() -> AuthorisationUtil.validateAccessToUser(userId, jwt))
                    .thenAnswer(invocation -> null);

            assertThrows(
                    EntityNotFoundException.class,
                    () ->
                            blacklistService.deleteBlacklistEntry(
                                    customerId, userId, blacklistEntryId, jwt));
        }
    }

    @Test
    void testDeleteBlacklistEntry_Idor() {
        long customerId = 1L;
        long userId = 100L;
        long blacklistEntryId = 5L;
        // Create an entry with a different userId to trigger IdorException.
        BlacklistEntry entry =
                BlacklistEntry.builder()
                        .id(blacklistEntryId)
                        .userId(200L)
                        .blacklistedEmailAddress("encrypted-email")
                        .build();

        when(blacklistRepository.findById(blacklistEntryId)).thenReturn(Optional.of(entry));

        try (MockedStatic<AuthorisationUtil> authUtilMock = mockStatic(AuthorisationUtil.class)) {
            authUtilMock
                    .when(() -> AuthorisationUtil.validateAccessToCustomer(customerId, jwt))
                    .thenAnswer(invocation -> null);
            authUtilMock
                    .when(() -> AuthorisationUtil.validateAccessToUser(userId, jwt))
                    .thenAnswer(invocation -> null);

            assertThrows(
                    IdorException.class,
                    () ->
                            blacklistService.deleteBlacklistEntry(
                                    customerId, userId, blacklistEntryId, jwt));
        }
    }
}
