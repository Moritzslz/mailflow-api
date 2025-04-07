package de.flowsuite.mailflowapi.messagecategory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import de.flowsuite.mailflowapi.common.entity.MessageCategory;
import de.flowsuite.mailflowapi.common.exception.EntityNotFoundException;
import de.flowsuite.mailflowapi.common.exception.IdConflictException;
import de.flowsuite.mailflowapi.common.exception.IdorException;
import de.flowsuite.mailflowapi.common.exception.UpdateConflictException;
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
class MessageCategoryServiceTest {

    @Mock private MessageCategoryRepository messageCategoryRepository;

    @InjectMocks private MessageCategoryService messageCategoryService;

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
    void testCreateMessageCategory_Success() {
        long customerId = 1L;
        long userId = 100L;
        MessageCategory category =
                MessageCategory.builder()
                        .userId(userId)
                        .category("Category A")
                        .isReply(true)
                        .isFunctionCall(false)
                        .description("Description A")
                        .build();

        setupDefaultAuthUtil();

        MessageCategory savedCategory =
                MessageCategory.builder()
                        .id(10L)
                        .userId(userId)
                        .category("Category A")
                        .isReply(true)
                        .isFunctionCall(false)
                        .description("Description A")
                        .build();
        when(messageCategoryRepository.save(any(MessageCategory.class))).thenReturn(savedCategory);

        MessageCategory result =
                messageCategoryService.createMessageCategory(customerId, userId, category, jwt);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Category A", result.getCategory());
    }

    @Test
    void testCreateMessageCategory_IdConflict() {
        long customerId = 1L;
        long userId = 100L;
        // Mismatched userId.
        MessageCategory category =
                MessageCategory.builder()
                        .userId(200L)
                        .category("Category A")
                        .isReply(true)
                        .isFunctionCall(false)
                        .description("Description A")
                        .build();

        setupDefaultAuthUtil();

        assertThrows(
                IdConflictException.class,
                () ->
                        messageCategoryService.createMessageCategory(
                                customerId, userId, category, jwt));
    }

    @Test
    void testListMessageCategories() {
        long customerId = 1L;
        long userId = 100L;
        MessageCategory category =
                MessageCategory.builder()
                        .id(1L)
                        .userId(userId)
                        .category("Category A")
                        .isReply(true)
                        .isFunctionCall(false)
                        .description("Description A")
                        .build();

        when(messageCategoryRepository.findByUserId(userId)).thenReturn(List.of(category));

        setupDefaultAuthUtil();

        List<MessageCategory> result =
                messageCategoryService.listMessageCategories(customerId, userId, jwt);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Category A", result.get(0).getCategory());
    }

    @Test
    void testUpdateMessageCategory_Success() {
        long customerId = 1L;
        long userId = 100L;
        long categoryId = 10L;
        MessageCategory categoryToUpdate =
                MessageCategory.builder()
                        .userId(userId)
                        .category("Updated Category")
                        .isReply(false)
                        .isFunctionCall(true)
                        .description("Updated description")
                        .build();

        MessageCategory existingCategory =
                MessageCategory.builder()
                        .id(categoryId)
                        .userId(userId)
                        .category("Category A")
                        .isReply(true)
                        .isFunctionCall(false)
                        .description("Description A")
                        .build();

        setupDefaultAuthUtil();

        when(messageCategoryRepository.findById(eq(categoryId)))
                .thenReturn(Optional.of(existingCategory));
        MessageCategory updatedCategory =
                MessageCategory.builder()
                        .id(categoryId)
                        .userId(userId)
                        .category("Updated Category")
                        .isReply(false)
                        .isFunctionCall(true)
                        .description("Updated description")
                        .build();
        when(messageCategoryRepository.save(any(MessageCategory.class)))
                .thenReturn(updatedCategory);

        MessageCategory result =
                messageCategoryService.updateMessageCategory(
                        customerId, userId, categoryId, categoryToUpdate, jwt);
        assertNotNull(result);
        assertEquals("Updated Category", result.getCategory());
    }

    @Test
    void testUpdateMessageCategory_EntityNotFound() {
        long customerId = 1L;
        long userId = 100L;
        long categoryId = 10L;
        MessageCategory categoryToUpdate =
                MessageCategory.builder()
                        .userId(userId)
                        .category("Updated Category")
                        .isReply(false)
                        .isFunctionCall(true)
                        .description("Updated description")
                        .build();

        setupDefaultAuthUtil();

        when(messageCategoryRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(
                EntityNotFoundException.class,
                () ->
                        messageCategoryService.updateMessageCategory(
                                customerId, userId, categoryId, categoryToUpdate, jwt));
    }

    @Test
    void testUpdateMessageCategory_UpdateConflict() {
        long customerId = 1L;
        long userId = 100L;
        long categoryId = 10L;
        MessageCategory categoryToUpdate =
                MessageCategory.builder()
                        .userId(userId)
                        .category("Updated Category")
                        .isReply(false)
                        .isFunctionCall(true)
                        .description("Updated description")
                        .build();

        // Existing category belongs to a different user.
        MessageCategory existingCategory =
                MessageCategory.builder()
                        .id(categoryId)
                        .userId(200L)
                        .category("Category A")
                        .isReply(true)
                        .isFunctionCall(false)
                        .description("Description A")
                        .build();

        setupDefaultAuthUtil();

        when(messageCategoryRepository.findById(eq(categoryId)))
                .thenReturn(Optional.of(existingCategory));
        assertThrows(
                UpdateConflictException.class,
                () ->
                        messageCategoryService.updateMessageCategory(
                                customerId, userId, categoryId, categoryToUpdate, jwt));
    }

    @Test
    void testDeleteMessageCategory_Success() {
        long customerId = 1L;
        long userId = 100L;
        long categoryId = 10L;
        MessageCategory existingCategory =
                MessageCategory.builder()
                        .id(categoryId)
                        .userId(userId)
                        .category("Category A")
                        .isReply(true)
                        .isFunctionCall(false)
                        .description("Description A")
                        .build();

        when(messageCategoryRepository.findById(eq(categoryId)))
                .thenReturn(Optional.of(existingCategory));

        setupDefaultAuthUtil();

        assertDoesNotThrow(
                () ->
                        messageCategoryService.deleteMessageCategory(
                                customerId, userId, categoryId, jwt));
        verify(messageCategoryRepository).delete(existingCategory);
    }

    @Test
    void testDeleteMessageCategory_NotFound() {
        long customerId = 1L;
        long userId = 100L;
        long categoryId = 10L;
        when(messageCategoryRepository.findById(eq(categoryId))).thenReturn(Optional.empty());

        setupDefaultAuthUtil();

        assertThrows(
                EntityNotFoundException.class,
                () ->
                        messageCategoryService.deleteMessageCategory(
                                customerId, userId, categoryId, jwt));
    }

    @Test
    void testDeleteMessageCategory_Idor() {
        long customerId = 1L;
        long userId = 100L;
        long categoryId = 10L;
        // Existing category belongs to a different user.
        MessageCategory existingCategory =
                MessageCategory.builder()
                        .id(categoryId)
                        .userId(200L)
                        .category("Category A")
                        .isReply(true)
                        .isFunctionCall(false)
                        .description("Description A")
                        .build();
        when(messageCategoryRepository.findById(eq(categoryId)))
                .thenReturn(Optional.of(existingCategory));

        setupDefaultAuthUtil();

        assertThrows(
                IdorException.class,
                () ->
                        messageCategoryService.deleteMessageCategory(
                                customerId, userId, categoryId, jwt));
    }
}
