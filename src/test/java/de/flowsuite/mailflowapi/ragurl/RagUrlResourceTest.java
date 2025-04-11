package de.flowsuite.mailflowapi.ragurl;

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

import de.flowsuite.mailflowapi.common.entity.RagUrl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

@WebMvcTest(RagUrlResource.class)
@Import(RagUrlServiceTestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class RagUrlResourceTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private RagUrlService ragUrlService;

    @Test
    void testCreateRagUrl() throws Exception {
        long customerId = 1L;
        RagUrl ragUrl =
                RagUrl.builder()
                        .customerId(customerId)
                        .url("http://example.com")
                        .isLastCrawlSuccessful(true)
                        .build();

        RagUrl savedRagUrl =
                RagUrl.builder()
                        .id(10L)
                        .customerId(customerId)
                        .url("http://example.com")
                        .isLastCrawlSuccessful(true)
                        .build();

        when(ragUrlService.createRagUrl(eq(customerId), any(RagUrl.class), nullable(Jwt.class)))
                .thenReturn(savedRagUrl);

        mockMvc.perform(
                        post("/customers/{customerId}/rag-urls", customerId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(ragUrl)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.url").value("http://example.com"));
    }

    @Test
    void testListRagUrls() throws Exception {
        long customerId = 1L;
        RagUrl ragUrl =
                RagUrl.builder()
                        .id(1L)
                        .customerId(customerId)
                        .url("http://example.com")
                        .isLastCrawlSuccessful(false)
                        .build();

        when(ragUrlService.listRagUrls(eq(customerId), nullable(Jwt.class)))
                .thenReturn(List.of(ragUrl));

        mockMvc.perform(get("/customers/{customerId}/rag-urls", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].url").value("http://example.com"));
    }

    @Test
    void testDeleteRagUrl() throws Exception {
        long customerId = 1L;
        long ragUrlId = 10L;
        mockMvc.perform(delete("/customers/{customerId}/rag-urls/{ragUrlId}", customerId, ragUrlId))
                .andExpect(status().isNoContent());
    }
}
