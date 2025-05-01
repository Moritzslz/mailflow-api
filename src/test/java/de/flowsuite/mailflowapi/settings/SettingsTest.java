package de.flowsuite.mailflowapi.settings;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.flowsuite.mailflow.common.entity.Settings;
import de.flowsuite.mailflow.common.entity.User;
import de.flowsuite.mailflow.common.exception.*;
import de.flowsuite.mailflow.common.util.HmacUtil;
import de.flowsuite.mailflowapi.BaseServiceTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class SettingsTest extends BaseServiceTest {

    protected static final int DEFAULT_CRAWL_FREQ = 168;
    protected static final int UPDATED_CRAWL_FREQ = 200;

    protected static final String DEFAULT_IMAP_HOST = "imapHost";
    protected static final String DEFAULT_SMTP_HOST = "smtpHost";
    protected static final String UPDATED_IMAP_HOST = "updated imapHost";
    protected static final String UPDATED_SMTP_HOST = "update smtpHost";

    protected static final int DEFAULT_PORT = 1;
    protected static final int UPDATED_PORT = 2;

    @Mock SettingsRepository settingsRepository;

    @InjectMocks SettingsService settingsService;

    private final User testUser = buildTestUser();
    private Settings testSettings;

    private Settings bulildTestSettings() {

        ZonedDateTime now = ZonedDateTime.now();

        return Settings.builder()
                .userId(testUser.getId())
                .customerId(testUser.getCustomerId())
                .isExecutionEnabled(true)
                .isAutoReplyEnabled(false)
                .isResponseRatingEnabled(true)
                .crawlFrequencyInHours(DEFAULT_CRAWL_FREQ)
                .lastCrawlAt(now)
                .nextCrawlAt(now.plusHours(DEFAULT_CRAWL_FREQ))
                .mailboxPasswordHash(HASHED_VALUE)
                .mailboxPassword(ENCRYPTED_VALUE)
                .imapHost(DEFAULT_IMAP_HOST)
                .smtpHost(DEFAULT_SMTP_HOST)
                .imapPort(DEFAULT_PORT)
                .smtpPort(DEFAULT_PORT)
                .build();
    }

    @BeforeEach
    void setup() {
        mockJwtWithUserAndCustomerClaims(testUser);
        testSettings = bulildTestSettings();
    }

    @Test
    void testCreateSettings_success() {
        when(settingsRepository.existsByUserId(testUser.getId())).thenReturn(false);

        settingsService.createSettings(
                testSettings.getCustomerId(), testSettings.getUserId(), testSettings, jwtMock);

        ArgumentCaptor<Settings> settingsCaptor = ArgumentCaptor.forClass(Settings.class);
        verify(settingsRepository).save(settingsCaptor.capture());
        Settings savedSettings = settingsCaptor.getValue();

        assertNotNull(savedSettings);
        assertEquals(testSettings, savedSettings);
    }

    @Test
    void testCreateSettings_alreadyExists() {
        when(settingsRepository.existsByUserId(testUser.getId())).thenReturn(true);

        assertThrows(
                EntityAlreadyExistsException.class,
                () ->
                        settingsService.createSettings(
                                testSettings.getCustomerId(),
                                testSettings.getUserId(),
                                testSettings,
                                jwtMock));

        verify(settingsRepository, never()).save(any(Settings.class));
    }

    @Test
    void testCreateSettings_idConflict() {
        Settings testSettingsIdConflict1 = bulildTestSettings();
        testSettingsIdConflict1.setCustomerId(testSettings.getCustomerId() + 1);

        Settings testSettingsIdConflict2 = bulildTestSettings();
        testSettingsIdConflict2.setUserId(testSettings.getUserId() + 1);

        assertThrows(
                IdConflictException.class,
                () ->
                        settingsService.createSettings(
                                testSettings.getCustomerId(),
                                testSettings.getUserId(),
                                testSettingsIdConflict1,
                                jwtMock));
        assertThrows(
                IdConflictException.class,
                () ->
                        settingsService.createSettings(
                                testSettings.getCustomerId(),
                                testSettings.getUserId(),
                                testSettingsIdConflict2,
                                jwtMock));

        verify(settingsRepository, never()).save(any(Settings.class));
    }

    @Test
    void testCreateSettings_idor() {
        assertThrows(
                IdorException.class,
                () ->
                        settingsService.createSettings(
                                testSettings.getCustomerId() + 1,
                                testSettings.getUserId(),
                                testSettings,
                                jwtMock));
        assertThrows(
                IdorException.class,
                () ->
                        settingsService.createSettings(
                                testSettings.getCustomerId(),
                                testSettings.getUserId() + 1,
                                testSettings,
                                jwtMock));

        verify(settingsRepository, never()).save(any(Settings.class));
    }

    @Test
    void getResponseRating_success() {
        when(settingsRepository.findById(testSettings.getUserId()))
                .thenReturn(Optional.of(testSettings));

        Settings settings =
                settingsService.getSettings(testUser.getCustomerId(), testUser.getId(), jwtMock);

        verify(settingsRepository).findById(testSettings.getUserId());

        assertEquals(testSettings, settings);
    }

    @Test
    void getResponseRating_notFound() {
        when(settingsRepository.findById(testSettings.getUserId())).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () ->
                        settingsService.getSettings(
                                testUser.getCustomerId(), testUser.getId(), jwtMock));
    }

    @Test
    void getResponseRating_idor() {
        assertThrows(
                IdorException.class,
                () ->
                        settingsService.getSettings(
                                testSettings.getCustomerId() + 1,
                                testSettings.getUserId(),
                                jwtMock));
        assertThrows(
                IdorException.class,
                () ->
                        settingsService.getSettings(
                                testSettings.getCustomerId(),
                                testSettings.getUserId() + 1,
                                jwtMock));

        verify(settingsRepository, never()).findById(anyLong());
    }

    @Test
    void testUpdateMessageCategory_success() {
        when(settingsRepository.findById(testSettings.getUserId()))
                .thenReturn(Optional.of(testSettings));

        ZonedDateTime lastCrawlAt = ZonedDateTime.now();
        ZonedDateTime nextCrawlAt = lastCrawlAt.plusHours(UPDATED_CRAWL_FREQ);

        SettingsResource.UpdateSettingsRequest updateSettingsRequest =
                new SettingsResource.UpdateSettingsRequest(
                        testUser.getId(),
                        testUser.getCustomerId(),
                        true,
                        true,
                        true,
                        UPDATED_CRAWL_FREQ,
                        lastCrawlAt,
                        nextCrawlAt,
                        UPDATED_IMAP_HOST,
                        UPDATED_SMTP_HOST,
                        UPDATED_PORT,
                        UPDATED_PORT);

        settingsService.updateSettings(
                testUser.getCustomerId(), testUser.getId(), updateSettingsRequest, jwtMock);

        ArgumentCaptor<Settings> settingsCaptor = ArgumentCaptor.forClass(Settings.class);
        verify(settingsRepository).save(settingsCaptor.capture());
        Settings savedSettings = settingsCaptor.getValue();

        assertNotNull(savedSettings);
        assertEquals(updateSettingsRequest.userId(), savedSettings.getCustomerId());
        assertEquals(updateSettingsRequest.customerId(), savedSettings.getUserId());
        assertEquals(
                updateSettingsRequest.isExecutionEnabled(), savedSettings.isExecutionEnabled());
        assertEquals(
                updateSettingsRequest.isAutoReplyEnabled(), savedSettings.isAutoReplyEnabled());
        assertEquals(
                updateSettingsRequest.isResponseRatingEnabled(),
                savedSettings.isResponseRatingEnabled());
        assertEquals(
                updateSettingsRequest.crawlFrequencyInHours(),
                savedSettings.getCrawlFrequencyInHours());
        assertEquals(updateSettingsRequest.lastCrawlAt(), savedSettings.getLastCrawlAt());
        assertEquals(updateSettingsRequest.nextCrawlAt(), savedSettings.getNextCrawlAt());
        assertEquals(updateSettingsRequest.imapHost(), savedSettings.getImapHost());
        assertEquals(updateSettingsRequest.smtpHost(), savedSettings.getSmtpHost());
        assertEquals(updateSettingsRequest.imapPort(), savedSettings.getImapPort());
        assertEquals(updateSettingsRequest.smtpPort(), savedSettings.getSmtpPort());
    }

    @Test
    void testUpdateMessageCategory_idConflict() {
        ZonedDateTime lastCrawlAt = ZonedDateTime.now();
        ZonedDateTime nextCrawlAt = lastCrawlAt.plusHours(UPDATED_CRAWL_FREQ);

        SettingsResource.UpdateSettingsRequest updateSettingsRequestIdConflict1 =
                new SettingsResource.UpdateSettingsRequest(
                        testUser.getId() + 1,
                        testUser.getCustomerId(),
                        true,
                        true,
                        true,
                        UPDATED_CRAWL_FREQ,
                        lastCrawlAt,
                        nextCrawlAt,
                        UPDATED_IMAP_HOST,
                        UPDATED_SMTP_HOST,
                        UPDATED_PORT,
                        UPDATED_PORT);

        SettingsResource.UpdateSettingsRequest updateSettingsRequestIdConflict2 =
                new SettingsResource.UpdateSettingsRequest(
                        testUser.getId(),
                        testUser.getCustomerId() + 2,
                        true,
                        true,
                        true,
                        UPDATED_CRAWL_FREQ,
                        lastCrawlAt,
                        nextCrawlAt,
                        UPDATED_IMAP_HOST,
                        UPDATED_SMTP_HOST,
                        UPDATED_PORT,
                        UPDATED_PORT);

        assertThrows(
                IdConflictException.class,
                () ->
                        settingsService.updateSettings(
                                testUser.getCustomerId(),
                                testUser.getId(),
                                updateSettingsRequestIdConflict1,
                                jwtMock));

        assertThrows(
                IdConflictException.class,
                () ->
                        settingsService.updateSettings(
                                testUser.getCustomerId(),
                                testUser.getId(),
                                updateSettingsRequestIdConflict2,
                                jwtMock));

        verify(settingsRepository, never()).save(any(Settings.class));
    }

    @Test
    void testUpdateMessageCategory_notFound() {
        when(settingsRepository.findById(testSettings.getUserId())).thenReturn(Optional.empty());

        ZonedDateTime lastCrawlAt = ZonedDateTime.now();
        ZonedDateTime nextCrawlAt = lastCrawlAt.plusHours(UPDATED_CRAWL_FREQ);

        SettingsResource.UpdateSettingsRequest updateSettingsRequest =
                new SettingsResource.UpdateSettingsRequest(
                        testUser.getId(),
                        testUser.getCustomerId(),
                        true,
                        true,
                        true,
                        UPDATED_CRAWL_FREQ,
                        lastCrawlAt,
                        nextCrawlAt,
                        UPDATED_IMAP_HOST,
                        UPDATED_SMTP_HOST,
                        UPDATED_PORT,
                        UPDATED_PORT);

        assertThrows(
                EntityNotFoundException.class,
                () ->
                        settingsService.updateSettings(
                                testUser.getCustomerId(),
                                testUser.getId(),
                                updateSettingsRequest,
                                jwtMock));

        verify(settingsRepository, never()).save(any(Settings.class));
    }

    @Test
    void testUpdateMessageCategory_idor() {
        ZonedDateTime lastCrawlAt = ZonedDateTime.now();
        ZonedDateTime nextCrawlAt = lastCrawlAt.plusHours(UPDATED_CRAWL_FREQ);

        SettingsResource.UpdateSettingsRequest updateSettingsRequest =
                new SettingsResource.UpdateSettingsRequest(
                        testUser.getId(),
                        testUser.getCustomerId(),
                        true,
                        true,
                        true,
                        UPDATED_CRAWL_FREQ,
                        lastCrawlAt,
                        nextCrawlAt,
                        UPDATED_IMAP_HOST,
                        UPDATED_SMTP_HOST,
                        UPDATED_PORT,
                        UPDATED_PORT);

        assertThrows(
                IdorException.class,
                () ->
                        settingsService.updateSettings(
                                testUser.getCustomerId() + 1,
                                testUser.getId(),
                                updateSettingsRequest,
                                jwtMock));

        assertThrows(
                IdorException.class,
                () ->
                        settingsService.updateSettings(
                                testUser.getCustomerId(),
                                testUser.getId() + 1,
                                updateSettingsRequest,
                                jwtMock));

        verify(settingsRepository, never()).save(any(Settings.class));
    }

    @Test
    void testUpdateMailboxPassword_success() {
        when(settingsRepository.findById(testUser.getId())).thenReturn(Optional.of(testSettings));

        String newPassword = "newPass";
        SettingsResource.UpdateMailboxPasswordRequest request =
                new SettingsResource.UpdateMailboxPasswordRequest(
                        testUser.getCustomerId(),
                        testUser.getId(),
                        testSettings.getMailboxPassword(),
                        newPassword);

        settingsService.updateMailboxPassword(
                testUser.getCustomerId(), testUser.getId(), request, jwtMock);

        ArgumentCaptor<Settings> settingsCaptor = ArgumentCaptor.forClass(Settings.class);
        verify(settingsRepository).save(settingsCaptor.capture());
        Settings savedSettings = settingsCaptor.getValue();
        assertEquals(savedSettings, testSettings);
    }

    @Test
    void testUpdateMailboxPassword_idConflict() {
        SettingsResource.UpdateMailboxPasswordRequest wrongUserIdRequest =
                new SettingsResource.UpdateMailboxPasswordRequest(
                        testUser.getCustomerId(),
                        testUser.getId() + 1,
                        testSettings.getMailboxPassword(),
                        "newPass");

        SettingsResource.UpdateMailboxPasswordRequest wrongCustomerIdRequest =
                new SettingsResource.UpdateMailboxPasswordRequest(
                        testUser.getCustomerId() + 1,
                        testUser.getId(),
                        testSettings.getMailboxPassword(),
                        "newPass");

        assertThrows(
                IdConflictException.class,
                () ->
                        settingsService.updateMailboxPassword(
                                testUser.getCustomerId(),
                                testUser.getId(),
                                wrongUserIdRequest,
                                jwtMock));

        assertThrows(
                IdConflictException.class,
                () ->
                        settingsService.updateMailboxPassword(
                                testUser.getCustomerId(),
                                testUser.getId(),
                                wrongCustomerIdRequest,
                                jwtMock));

        verify(settingsRepository, never()).save(any());
    }

    @Test
    void testUpdateMailboxPassword_notFound() {
        when(settingsRepository.findById(testUser.getId())).thenReturn(Optional.empty());

        SettingsResource.UpdateMailboxPasswordRequest request =
                new SettingsResource.UpdateMailboxPasswordRequest(
                        testUser.getCustomerId(),
                        testUser.getId(),
                        testSettings.getMailboxPassword(),
                        "newPass");

        assertThrows(
                EntityNotFoundException.class,
                () ->
                        settingsService.updateMailboxPassword(
                                testUser.getCustomerId(), testUser.getId(), request, jwtMock));

        verify(settingsRepository, never()).save(any());
    }

    @Test
    void testUpdateMailboxPassword_wrongCurrentPassword() {
        when(settingsRepository.findById(testUser.getId())).thenReturn(Optional.of(testSettings));

        SettingsResource.UpdateMailboxPasswordRequest request =
                new SettingsResource.UpdateMailboxPasswordRequest(
                        testUser.getCustomerId(), testUser.getId(), "wrongPassword", "newPass");

        when(HmacUtil.hash(anyString())).thenReturn("differentHash");

        assertThrows(
                UpdateConflictException.class,
                () ->
                        settingsService.updateMailboxPassword(
                                testUser.getCustomerId(), testUser.getId(), request, jwtMock));

        verify(settingsRepository, never()).save(any());
    }
}
