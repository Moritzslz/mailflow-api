package de.flowsuite.mailflowapi.blacklist;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.flowsuite.mailflowapi.common.entity.BlacklistEntry;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

@WebMvcTest(BlacklistResource.class)
@Import(BlacklistServiceTestConfig.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for testing
class BlacklistResourceTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private BlacklistService blacklistService;

    @Test
    void testCreateBlacklistEntry() throws Exception {
        long customerId = 1L;
        long userId = 100L;
        BlacklistEntry entry =
                BlacklistEntry.builder()
                        .userId(userId)
                        .blacklistedEmailAddress("test@example.com")
                        .build();

        BlacklistEntry savedEntry =
                BlacklistEntry.builder()
                        .id(10L)
                        .userId(userId)
                        .blacklistedEmailAddress("encrypted-email")
                        .build();

        when(blacklistService.createBlacklistEntry(
                        eq(customerId), eq(userId), any(BlacklistEntry.class), nullable(Jwt.class)))
                .thenReturn(savedEntry);

        mockMvc.perform(
                        post("/customers/{customerId}/users/{userId}/blacklist", customerId, userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(entry)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.blacklistedEmailAddress").value("encrypted-email"));
    }

    @Test
    void testListBlacklistEntries() throws Exception {
        long customerId = 1L;
        long userId = 100L;
        BlacklistEntry entry =
                BlacklistEntry.builder()
                        .id(1L)
                        .userId(userId)
                        .blacklistedEmailAddress("encrypted-email")
                        .build();

        when(blacklistService.listBlacklistEntries(eq(customerId), eq(userId), nullable(Jwt.class)))
                .thenReturn(List.of(entry));

        mockMvc.perform(get("/customers/{customerId}/users/{userId}/blacklist", customerId, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void testDeleteBlacklistEntry() throws Exception {
        long customerId = 1L;
        long userId = 100L;
        long blacklistEntryId = 5L;

        mockMvc.perform(
                        delete(
                                "/customers/{customerId}/users/{userId}/blacklist/{blacklistEntryId}",
                                customerId,
                                userId,
                                blacklistEntryId))
                .andExpect(status().isNoContent());
    }
}
