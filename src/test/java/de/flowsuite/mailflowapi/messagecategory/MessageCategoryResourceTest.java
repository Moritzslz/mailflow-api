package de.flowsuite.mailflowapi.messagecategory;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.flowsuite.mailflowapi.common.entity.MessageCategory;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

@WebMvcTest(MessageCategoryResource.class)
@Import(MessageCategoryServiceTestConfig.class)
@AutoConfigureMockMvc(addFilters = false) // disable security filters for testing
class MessageCategoryResourceTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private MessageCategoryService messageCategoryService;

    @Test
    void testCreateMessageCategory() throws Exception {
        long customerId = 1L;
        MessageCategory category =
                MessageCategory.builder()
                        .customerId(customerId)
                        .category("Category A")
                        .isReply(true)
                        .isFunctionCall(false)
                        .description("Description A")
                        .build();

        MessageCategory savedCategory =
                MessageCategory.builder()
                        .id(10L)
                        .customerId(customerId)
                        .category("Category A")
                        .isReply(true)
                        .isFunctionCall(false)
                        .description("Description A")
                        .build();

        when(messageCategoryService.createMessageCategory(
                        eq(customerId), any(MessageCategory.class), nullable(Jwt.class)))
                .thenReturn(savedCategory);

        mockMvc.perform(
                        post("/customers/{customerId}/message-categories", customerId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(category)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.category").value("Category A"))
                .andExpect(jsonPath("$.isReply").value(true))
                .andExpect(jsonPath("$.isFunctionCall").value(false))
                .andExpect(jsonPath("$.description").value("Description A"));
    }

    @Test
    void testListMessageCategories() throws Exception {
        long customerId = 1L;
        MessageCategory category =
                MessageCategory.builder()
                        .id(1L)
                        .customerId(customerId)
                        .category("Category A")
                        .isReply(true)
                        .isFunctionCall(false)
                        .description("Description A")
                        .build();

        when(messageCategoryService.listMessageCategories(eq(customerId), nullable(Jwt.class)))
                .thenReturn(List.of(category));

        mockMvc.perform(get("/customers/{customerId}/message-categories", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].category").value("Category A"))
                .andExpect(jsonPath("$[0].isReply").value(true))
                .andExpect(jsonPath("$[0].isFunctionCall").value(false))
                .andExpect(jsonPath("$[0].description").value("Description A"));
    }

    @Test
    void testUpdateMessageCategory() throws Exception {
        long customerId = 1L;
        long categoryId = 10L;
        MessageCategory category =
                MessageCategory.builder()
                        .customerId(customerId)
                        .category("Updated Category")
                        .isReply(false)
                        .isFunctionCall(true)
                        .description("Updated description")
                        .build();

        MessageCategory updatedCategory =
                MessageCategory.builder()
                        .id(categoryId)
                        .customerId(customerId)
                        .category("Updated Category")
                        .isReply(false)
                        .isFunctionCall(true)
                        .description("Updated description")
                        .build();

        when(messageCategoryService.updateMessageCategory(
                        eq(customerId),
                        eq(categoryId),
                        any(MessageCategory.class),
                        nullable(Jwt.class)))
                .thenReturn(updatedCategory);

        mockMvc.perform(
                        put(
                                        "/customers/{customerId}/message-categories/{categoryId}",
                                        customerId,
                                        categoryId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(category)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId))
                .andExpect(jsonPath("$.category").value("Updated Category"))
                .andExpect(jsonPath("$.isReply").value(false))
                .andExpect(jsonPath("$.isFunctionCall").value(true))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    void testDeleteMessageCategory() throws Exception {
        long customerId = 1L;
        long categoryId = 10L;

        mockMvc.perform(
                        delete(
                                "/customers/{customerId}/message-categories/{categoryId}",
                                customerId,
                                categoryId))
                .andExpect(status().isNoContent());
    }
}
