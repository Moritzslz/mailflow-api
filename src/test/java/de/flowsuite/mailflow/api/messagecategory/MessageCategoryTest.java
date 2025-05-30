package de.flowsuite.mailflow.api.messagecategory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

import de.flowsuite.mailflow.api.BaseServiceTest;
import de.flowsuite.mailflow.common.entity.MessageCategory;
import de.flowsuite.mailflow.common.entity.User;
import de.flowsuite.mailflow.common.exception.*;

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
class MessageCategoryTest extends BaseServiceTest {

    @Mock private MessageCategoryRepository messageCategoryRepository;

    @InjectMocks private MessageCategoryService messageCategoryService;

    private final User testUser = buildTestUser();
    private MessageCategory testMessageCategory;

    private MessageCategory buildTestMessageCategory() {
        return MessageCategory.builder()
                .id(1L)
                .customerId(testUser.getCustomerId())
                .category("Category")
                .reply(true)
                .functionCall(true)
                .description(
                        "This is a detailed mocked description used solely for testing purposes"
                                + " that contains over one hundred characters.")
                .build();
    }

    @BeforeEach
    void setup() {
        testMessageCategory = buildTestMessageCategory();
        mockJwtWithCustomerClaimsOnly(testUser);
    }

    @Test
    void testCreateMessageCategory_success() {
        when(messageCategoryRepository.existsByCustomerIdAndCategory(
                        testUser.getCustomerId(), testMessageCategory.getCategory()))
                .thenReturn(false);

        testMessageCategory.setId(null);
        assertNull(testMessageCategory.getId());

        messageCategoryService.createMessageCategory(
                testUser.getCustomerId(), testMessageCategory, jwtMock);

        ArgumentCaptor<MessageCategory> messageCategoryCaptor =
                ArgumentCaptor.forClass(MessageCategory.class);
        verify(messageCategoryRepository).save(messageCategoryCaptor.capture());
        MessageCategory savedMessageCategory = messageCategoryCaptor.getValue();

        assertNotNull(savedMessageCategory);
        assertEquals(testMessageCategory.getCustomerId(), savedMessageCategory.getCustomerId());
        assertEquals(testMessageCategory.getCategory(), savedMessageCategory.getCategory());
        assertEquals(testMessageCategory.getReply(), savedMessageCategory.getReply());
        assertEquals(testMessageCategory.getFunctionCall(), savedMessageCategory.getFunctionCall());
        assertEquals(testMessageCategory.getDescription(), savedMessageCategory.getDescription());
    }

    @Test
    void testCreateBlacklistEntry_descriptionTooShort() {
        when(messageCategoryRepository.existsByCustomerIdAndCategory(
                        testUser.getCustomerId(), testMessageCategory.getCategory()))
                .thenReturn(false);

        testMessageCategory.setId(null);
        testMessageCategory.setDescription("Too short description");
        assertNull(testMessageCategory.getId());

        assertThrows(
                MessageCategoryDescriptionException.class,
                () ->
                        messageCategoryService.createMessageCategory(
                                testUser.getCustomerId(), testMessageCategory, jwtMock));

        verify(messageCategoryRepository, never()).save(any(MessageCategory.class));
    }

    @Test
    void testCreateBlacklistEntry_alreadyExists() {
        when(messageCategoryRepository.existsByCustomerIdAndCategory(
                        testUser.getCustomerId(), testMessageCategory.getCategory()))
                .thenReturn(true);

        testMessageCategory.setId(null);
        assertNull(testMessageCategory.getId());

        assertThrows(
                EntityAlreadyExistsException.class,
                () ->
                        messageCategoryService.createMessageCategory(
                                testUser.getCustomerId(), testMessageCategory, jwtMock));

        verify(messageCategoryRepository, never()).save(any(MessageCategory.class));
    }

    @Test
    void testCreateBlacklistEntry_limitReached() {
        when(messageCategoryRepository.existsByCustomerIdAndCategory(
                        testUser.getCustomerId(), testMessageCategory.getCategory()))
                .thenReturn(false);
        when(messageCategoryRepository.countByCustomerId(testUser.getCustomerId())).thenReturn(10);

        testMessageCategory.setId(null);
        assertNull(testMessageCategory.getId());

        assertThrows(
                MessageCategoryLimitException.class,
                () ->
                        messageCategoryService.createMessageCategory(
                                testUser.getCustomerId(), testMessageCategory, jwtMock));

        verify(messageCategoryRepository, never()).save(any(MessageCategory.class));
    }

    @Test
    void testCreateBlacklistEntry_idConflict() {
        assertThrows(
                IdConflictException.class,
                () ->
                        messageCategoryService.createMessageCategory(
                                testUser.getCustomerId(), testMessageCategory, jwtMock));

        testMessageCategory.setId(null);
        testMessageCategory.setCustomerId(testUser.getCustomerId() + 1);

        assertThrows(
                IdConflictException.class,
                () ->
                        messageCategoryService.createMessageCategory(
                                testUser.getCustomerId(), testMessageCategory, jwtMock));

        verify(messageCategoryRepository, never()).save(any(MessageCategory.class));
    }

    @Test
    void testCreateBlacklistEntry_idor() {
        assertThrows(
                IdorException.class,
                () ->
                        messageCategoryService.createMessageCategory(
                                testUser.getCustomerId() + 1, testMessageCategory, jwtMock));

        verify(messageCategoryRepository, never()).save(any(MessageCategory.class));
    }

    @Test
    void testGetMessageCategory_success() {
        when(messageCategoryRepository.findById(testMessageCategory.getId()))
                .thenReturn(Optional.of(testMessageCategory));

        MessageCategory messageCategory =
                messageCategoryService.getMessageCategory(
                        testUser.getCustomerId(), testMessageCategory.getId(), jwtMock);

        verify(messageCategoryRepository).findById(testMessageCategory.getId());

        assertEquals(testMessageCategory, messageCategory);
    }

    @Test
    void testGetMessageCategory_notFound() {
        when(messageCategoryRepository.findById(testMessageCategory.getId()))
                .thenReturn(Optional.empty());
        assertThrows(
                EntityNotFoundException.class,
                () ->
                        messageCategoryService.getMessageCategory(
                                testUser.getCustomerId(), testMessageCategory.getId(), jwtMock));
    }

    @Test
    void testGetMessageCategory_idor() {
        when(messageCategoryRepository.findById(testMessageCategory.getId()))
                .thenReturn(Optional.of(testMessageCategory));

        assertThrows(
                IdorException.class,
                () ->
                        messageCategoryService.getMessageCategory(
                                testUser.getCustomerId() + 1,
                                testMessageCategory.getId(),
                                jwtMock));

        testMessageCategory.setCustomerId(testUser.getCustomerId() + 1);

        assertThrows(
                IdorException.class,
                () ->
                        messageCategoryService.getMessageCategory(
                                testUser.getCustomerId(), testMessageCategory.getId(), jwtMock));
    }

    @Test
    void testListMessageCategories_success() {
        when(messageCategoryRepository.findByCustomerId(testUser.getCustomerId()))
                .thenReturn(List.of(testMessageCategory));

        List<MessageCategory> messageCategories =
                messageCategoryService.listMessageCategories(testUser.getCustomerId(), jwtMock);

        assertEquals(1, messageCategories.size());
        assertEquals(testMessageCategory, messageCategories.get(0));
    }

    @Test
    void testListMessageCategories_idor() {
        assertThrows(
                IdorException.class,
                () ->
                        messageCategoryService.listMessageCategories(
                                testUser.getCustomerId() + 1, jwtMock));

        verify(messageCategoryRepository, never()).findByCustomerId(anyLong());
    }

    @Test
    void testUpdateMessageCategory_success() {
        when(messageCategoryRepository.findById(testMessageCategory.getId()))
                .thenReturn(Optional.of(testMessageCategory));

        MessageCategory updatedMessageCategory = buildTestMessageCategory();
        updatedMessageCategory.setCategory("Updated Category");

        messageCategoryService.updateMessageCategory(
                testUser.getCustomerId(),
                testMessageCategory.getId(),
                updatedMessageCategory,
                jwtMock);

        ArgumentCaptor<MessageCategory> messageCategoryCaptor =
                ArgumentCaptor.forClass(MessageCategory.class);
        verify(messageCategoryRepository).save(messageCategoryCaptor.capture());
        MessageCategory savedMessageCategory = messageCategoryCaptor.getValue();

        assertEquals(updatedMessageCategory, savedMessageCategory);
    }

    @Test
    void testUpdateMessageCategory_idConflict() {
        MessageCategory updatedMessageCategory = buildTestMessageCategory();
        updatedMessageCategory.setCategory("Updated Category");
        updatedMessageCategory.setCustomerId(testUser.getCustomerId() + 1);

        assertThrows(
                IdConflictException.class,
                () ->
                        messageCategoryService.updateMessageCategory(
                                testUser.getCustomerId(),
                                testMessageCategory.getId(),
                                updatedMessageCategory,
                                jwtMock));

        verify(messageCategoryRepository, never()).save(any(MessageCategory.class));
    }

    @Test
    void testUpdateMessageCategory_notFound() {
        when(messageCategoryRepository.findById(testMessageCategory.getId()))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () ->
                        messageCategoryService.updateMessageCategory(
                                testUser.getCustomerId(),
                                testMessageCategory.getId(),
                                testMessageCategory,
                                jwtMock));

        verify(messageCategoryRepository, never()).save(any(MessageCategory.class));
    }

    @Test
    void testUpdateMessageCategory_idor() {
        MessageCategory updatedMessageCategory = buildTestMessageCategory();
        updatedMessageCategory.setCategory("Updated Category");
        updatedMessageCategory.setCustomerId(testUser.getCustomerId() + 1);

        when(messageCategoryRepository.findById(testMessageCategory.getId()))
                .thenReturn(Optional.of(updatedMessageCategory));

        assertThrows(
                IdorException.class,
                () ->
                        messageCategoryService.updateMessageCategory(
                                testUser.getCustomerId(),
                                testMessageCategory.getId(),
                                testMessageCategory,
                                jwtMock));

        verify(messageCategoryRepository, never()).save(any(MessageCategory.class));
    }

    @Test
    void testDeleteMessageCategory_success() {
        when(messageCategoryRepository.findById(testMessageCategory.getId()))
                .thenReturn(Optional.of(testMessageCategory));

        messageCategoryService.deleteMessageCategory(
                testUser.getCustomerId(), testMessageCategory.getId(), jwtMock);

        ArgumentCaptor<MessageCategory> messageCategoryCaptor =
                ArgumentCaptor.forClass(MessageCategory.class);
        verify(messageCategoryRepository).delete(messageCategoryCaptor.capture());
        MessageCategory deletedMessageCategory = messageCategoryCaptor.getValue();

        assertEquals(testMessageCategory, deletedMessageCategory);
    }

    @Test
    void testDeleteBlacklistEntry_notFound() {
        testGetMessageCategory_notFound();
        verify(messageCategoryRepository, never()).delete(any(MessageCategory.class));
    }

    @Test
    void testDeleteBlacklistEntry_idor() {
        testGetMessageCategory_idor();
        verify(messageCategoryRepository, never()).delete(any(MessageCategory.class));
    }
}
