package de.flowsuite.mailflowapi.settings;

import static de.flowsuite.mailflowapi.common.util.Util.BERLIN_ZONE;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.flowsuite.mailflowapi.common.entity.Settings;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;

@WebMvcTest(SettingsResource.class)
@Import(SettingsServiceTestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class SettingsResourceTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private SettingsService settingsService;

    @Test
    void testCreateSettings() throws Exception {
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
                        .lastCrawlAt(ZonedDateTime.now(BERLIN_ZONE))
                        .nextCrawlAt(ZonedDateTime.now(BERLIN_ZONE).plusHours(200))
                        .build();

        when(settingsService.createSettings(
                        eq(customerId), eq(userId), any(Settings.class), nullable(Jwt.class)))
                .thenReturn(savedSettings);

        mockMvc.perform(
                        post("/customers/{customerId}/users/{userId}/settings", customerId, userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(settings)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imapHost").value("imap.example.com"))
                .andExpect(jsonPath("$.smtpHost").value("smtp.example.com"))
                .andExpect(jsonPath("$.crawlFrequencyInHours").value(200));
    }

    @Test
    void testGetSettings() throws Exception {
        long customerId = 1L;
        long userId = 100L;
        Settings savedSettings =
                Settings.builder()
                        .userId(userId)
                        .customerId(customerId)
                        .mailboxPassword("encrypted")
                        .mailboxPasswordHash("hashed")
                        .build();

        when(settingsService.getSettings(eq(customerId), eq(userId), nullable(Jwt.class)))
                .thenReturn(savedSettings);

        mockMvc.perform(get("/customers/{customerId}/users/{userId}/settings", customerId, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mailboxPassword").value("encrypted"));
    }

    @Test
    void testUpdateSettings() throws Exception {
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
                        null,
                        null,
                        "imap.updated.com",
                        "smtp.updated.com",
                        995,
                        465);

        Settings updatedSettings =
                Settings.builder()
                        .userId(userId)
                        .customerId(customerId)
                        .mailboxPassword("encrypted")
                        .mailboxPasswordHash("hashed")
                        .imapHost("imap.updated.com")
                        .smtpHost("smtp.updated.com")
                        .imapPort(995)
                        .smtpPort(465)
                        .crawlFrequencyInHours(300)
                        .build();

        when(settingsService.updateSettings(
                        eq(customerId),
                        eq(userId),
                        any(SettingsResource.UpdateSettingsRequest.class),
                        nullable(Jwt.class)))
                .thenReturn(updatedSettings);

        mockMvc.perform(
                        put("/customers/{customerId}/users/{userId}/settings", customerId, userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imapHost").value("imap.updated.com"))
                .andExpect(jsonPath("$.crawlFrequencyInHours").value(300));
    }

    @Test
    void testUpdateMailboxPassword() throws Exception {
        long customerId = 1L;
        long userId = 100L;
        SettingsResource.UpdateMailboxPasswordRequest request =
                new SettingsResource.UpdateMailboxPasswordRequest(
                        userId, customerId, "currentPass", "updatedPass");

        Settings updatedSettings =
                Settings.builder()
                        .userId(userId)
                        .customerId(customerId)
                        .mailboxPassword("new-encrypted")
                        .build();

        when(settingsService.updateMailboxPassword(
                        eq(customerId),
                        eq(userId),
                        any(SettingsResource.UpdateMailboxPasswordRequest.class),
                        nullable(Jwt.class)))
                .thenReturn(updatedSettings);

        mockMvc.perform(
                        put(
                                        "/customers/{customerId}/users/{userId}/settings/mailbox-password",
                                        customerId,
                                        userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mailboxPassword").value("new-encrypted"));
    }
}
