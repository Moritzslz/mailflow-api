package de.flowsuite.mailflowapi.settings;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.flowsuite.mailflowapi.common.entity.Settings;
import de.flowsuite.mailflowapi.common.exception.EntityNotFoundException;
import de.flowsuite.mailflowapi.common.exception.IdConflictException;
import de.flowsuite.mailflowapi.common.exception.UpdateConflictException;
import de.flowsuite.mailflowapi.common.util.AesUtil;
import de.flowsuite.mailflowapi.common.util.AuthorisationUtil;
import de.flowsuite.mailflowapi.common.util.HmacUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class SettingsServiceTest {

    @Mock private SettingsRepository settingsRepository;

    @InjectMocks private SettingsService settingsService;

    private Jwt jwt;

    private MockedStatic<AuthorisationUtil> authUtilMock;
    private MockedStatic<HmacUtil> hmacUtilMock;
    private MockedStatic<AesUtil> aesUtilMock;

    @BeforeEach
    void setup() {
        jwt = mock(Jwt.class);
        authUtilMock = mockStatic(AuthorisationUtil.class);
        hmacUtilMock = mockStatic(HmacUtil.class);
        aesUtilMock = mockStatic(AesUtil.class);
    }

    @AfterEach
    void tearDown() {
        authUtilMock.close();
        hmacUtilMock.close();
        aesUtilMock.close();
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
    void testCreateSettings_Success() {
        long customerId = 1L;
        long userId = 100L;
        Settings settings =
                Settings.builder()
                        .userId(userId)
                        .customerId(customerId)
                        .mailboxPassword("plainpassword")
                        .imapHost("imap.example.com")
                        .smtpHost("smtp.example.com")
                        .imapPort(993)
                        .smtpPort(587)
                        .crawlFrequencyInHours(200)
                        .build();

        setupDefaultAuthUtil();

        hmacUtilMock.when(() -> HmacUtil.hash("plainpassword")).thenReturn("hashed");
        aesUtilMock.when(() -> AesUtil.encrypt("plainpassword")).thenReturn("encrypted");

        Settings savedSettings =
                Settings.builder()
                        .userId(userId)
                        .customerId(customerId)
                        .mailboxPassword("encrypted")
                        .mailboxPasswordHash("hashed")
                        .imapHost("imap.example.com")
                        .smtpHost("smtp.example.com")
                        .imapPort(993)
                        .smtpPort(587)
                        .crawlFrequencyInHours(200)
                        .lastCrawlAt(ZonedDateTime.now(ZoneId.of("Europe/Berlin")))
                        .nextCrawlAt(ZonedDateTime.now(ZoneId.of("Europe/Berlin")).plusHours(200))
                        .build();

        when(settingsRepository.save(any(Settings.class))).thenReturn(savedSettings);

        Settings result = settingsService.createSettings(customerId, userId, settings, jwt);
        assertNotNull(result);
        assertEquals("encrypted", result.getMailboxPassword());
        assertEquals("hashed", result.getMailboxPasswordHash());
    }

    @Test
    void testCreateSettings_IdConflict() {
        long customerId = 1L;
        long userId = 100L;
        // Mismatch: settings.userId is different.
        Settings settings =
                Settings.builder()
                        .userId(200L)
                        .customerId(customerId)
                        .mailboxPassword("plainpassword")
                        .build();

        setupDefaultAuthUtil();

        assertThrows(
                IdConflictException.class,
                () -> settingsService.createSettings(customerId, userId, settings, jwt));
    }

    @Test
    void testGetSettings_Success() {
        long customerId = 1L;
        long userId = 100L;
        Settings savedSettings =
                Settings.builder()
                        .userId(userId)
                        .customerId(customerId)
                        .mailboxPassword("encrypted")
                        .mailboxPasswordHash("hashed")
                        .build();
        when(settingsRepository.findById(userId)).thenReturn(Optional.of(savedSettings));

        setupDefaultAuthUtil();

        Settings result = settingsService.getSettings(customerId, userId, jwt);
        assertNotNull(result);
        assertEquals("encrypted", result.getMailboxPassword());
    }

    @Test
    void testGetSettings_NotFound() {
        long customerId = 1L;
        long userId = 100L;
        when(settingsRepository.findById(userId)).thenReturn(Optional.empty());

        setupDefaultAuthUtil();

        assertThrows(
                EntityNotFoundException.class,
                () -> settingsService.getSettings(customerId, userId, jwt));
    }

    @Test
    void testUpdateSettings_Success() {
        long customerId = 1L;
        long userId = 100L;
        SettingsResource.UpdateSettingsRequest request =
                new SettingsResource.UpdateSettingsRequest(
                        userId,
                        customerId,
                        true,
                        false,
                        true,
                        300,
                        "imap.updated.com",
                        "smtp.updated.com",
                        995,
                        465);

        Settings existing =
                Settings.builder()
                        .userId(userId)
                        .customerId(customerId)
                        .mailboxPassword("encrypted")
                        .mailboxPasswordHash("hashed")
                        .build();

        setupDefaultAuthUtil();

        when(settingsRepository.findById(userId)).thenReturn(Optional.of(existing));
        Settings updated =
                Settings.builder()
                        .userId(userId)
                        .customerId(customerId)
                        .mailboxPassword("encrypted")
                        .mailboxPasswordHash("hashed")
                        .isExecutionEnabled(true)
                        .isAutoReplyEnabled(false)
                        .isResponseRatingEnabled(true)
                        .crawlFrequencyInHours(300)
                        .imapHost("imap.updated.com")
                        .smtpHost("smtp.updated.com")
                        .imapPort(995)
                        .smtpPort(465)
                        .build();
        when(settingsRepository.save(any(Settings.class))).thenReturn(updated);

        Settings result = settingsService.updateSettings(customerId, userId, request, jwt);
        assertNotNull(result);
        assertTrue(result.isExecutionEnabled());
        assertEquals(300, result.getCrawlFrequencyInHours());
        assertEquals("imap.updated.com", result.getImapHost());
    }

    @Test
    void testUpdateSettings_IdConflict() {
        long customerId = 1L;
        long userId = 100L;
        // Mismatch in request userId.
        SettingsResource.UpdateSettingsRequest request =
                new SettingsResource.UpdateSettingsRequest(
                        200L,
                        customerId,
                        true,
                        false,
                        true,
                        300,
                        "imap.updated.com",
                        "smtp.updated.com",
                        995,
                        465);

        setupDefaultAuthUtil();

        assertThrows(
                IdConflictException.class,
                () -> settingsService.updateSettings(customerId, userId, request, jwt));
    }

    @Test
    void testUpdateSettings_NotFound() {
        long customerId = 1L;
        long userId = 100L;
        SettingsResource.UpdateSettingsRequest request =
                new SettingsResource.UpdateSettingsRequest(
                        userId,
                        customerId,
                        true,
                        false,
                        true,
                        300,
                        "imap.updated.com",
                        "smtp.updated.com",
                        995,
                        465);

        setupDefaultAuthUtil();

        when(settingsRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(
                EntityNotFoundException.class,
                () -> settingsService.updateSettings(customerId, userId, request, jwt));
    }

    @Test
    void testUpdateMailboxPassword_Success() {
        long customerId = 1L;
        long userId = 100L;
        Settings settings =
                Settings.builder()
                        .userId(userId)
                        .customerId(customerId)
                        .mailboxPassword("old-encrypted")
                        .mailboxPasswordHash("old-hashed")
                        .build();
        SettingsResource.UpdateMailboxPasswordRequest request =
                new SettingsResource.UpdateMailboxPasswordRequest(
                        userId, customerId, "currentPass", "updatedPass");

        setupDefaultAuthUtil();

        when(settingsRepository.findById(userId)).thenReturn(Optional.of(settings));
        hmacUtilMock.when(() -> HmacUtil.hash("currentPass")).thenReturn("old-hashed");
        hmacUtilMock.when(() -> HmacUtil.hash("updatedPass")).thenReturn("new-hashed");
        aesUtilMock.when(() -> AesUtil.encrypt("updatedPass")).thenReturn("new-encrypted");

        Settings updatedSettings =
                Settings.builder()
                        .userId(userId)
                        .customerId(customerId)
                        .mailboxPassword("new-encrypted")
                        .mailboxPasswordHash("new-hashed")
                        .build();
        when(settingsRepository.save(any(Settings.class))).thenReturn(updatedSettings);

        Settings result = settingsService.updateMailboxPassword(customerId, userId, request, jwt);
        assertNotNull(result);
        assertEquals("new-hashed", result.getMailboxPasswordHash());
        assertEquals("new-encrypted", result.getMailboxPassword());
    }

    @Test
    void testUpdateMailboxPassword_UpdateConflict() {
        long customerId = 1L;
        long userId = 100L;
        Settings settings =
                Settings.builder()
                        .userId(userId)
                        .customerId(customerId)
                        .mailboxPassword("old-encrypted")
                        .mailboxPasswordHash("old-hashed")
                        .build();
        SettingsResource.UpdateMailboxPasswordRequest request =
                new SettingsResource.UpdateMailboxPasswordRequest(
                        userId, customerId, "wrongPass", "updatedPass");

        setupDefaultAuthUtil();

        when(settingsRepository.findById(userId)).thenReturn(Optional.of(settings));
        hmacUtilMock.when(() -> HmacUtil.hash("wrongPass")).thenReturn("not-old-hashed");

        assertThrows(
                UpdateConflictException.class,
                () -> settingsService.updateMailboxPassword(customerId, userId, request, jwt));
    }
}
