package de.flowsuite.mailflow.api.messagecategory;

import de.flowsuite.mailflow.common.entity.MessageCategory;
import de.flowsuite.mailflow.common.exception.*;
import de.flowsuite.mailflow.common.util.AuthorisationUtil;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageCategoryService {

    private static final String DEFAULT_CATEGORY = "Default";

    private final MessageCategoryRepository messageCategoryRepository;

    MessageCategoryService(MessageCategoryRepository messageCategoryRepository) {
        this.messageCategoryRepository = messageCategoryRepository;
    }

    MessageCategory createMessageCategory(
            long customerId, MessageCategory messageCategory, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);

        if (messageCategory.getId() != null
                || !messageCategory.getCustomerId().equals(customerId)) {
            throw new IdConflictException();
        }

        if (messageCategory.getCategory().equalsIgnoreCase(DEFAULT_CATEGORY)) {
            throw new EntityAlreadyExistsException(MessageCategory.class.getSimpleName());
        }

        if (messageCategoryRepository.existsByCustomerIdAndCategory(
                customerId, messageCategory.getCategory())) {
            throw new EntityAlreadyExistsException(MessageCategory.class.getSimpleName());
        }

        return messageCategoryRepository.save(messageCategory);
    }

    public void createDefaultMessageCategory(long customerId) {
        MessageCategory defaultMessageCategory =
                MessageCategory.builder()
                        .customerId(customerId)
                        .category(DEFAULT_CATEGORY)
                        .isReply(true)
                        .isFunctionCall(false)
                        .description(
                                "This is the default/fallback category for messages that do not fit"
                                        + " into any other defined category.")
                        .build();

        messageCategoryRepository.save(defaultMessageCategory);
    }

    MessageCategory getMessageCategory(long customerId, long id, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);

        MessageCategory messageCategory =
                messageCategoryRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                MessageCategory.class.getSimpleName()));

        if (!messageCategory.getCustomerId().equals(customerId)) {
            throw new IdorException();
        }

        return messageCategory;
    }

    List<MessageCategory> listMessageCategories(long customerId, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);

        return messageCategoryRepository.findByCustomerId(customerId);
    }

    MessageCategory updateMessageCategory(
            long customerId, long id, MessageCategory updatedMessageCategory, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);

        if (!updatedMessageCategory.getCustomerId().equals(customerId)
                || !updatedMessageCategory.getId().equals(id)) {
            throw new IdConflictException();
        }

        MessageCategory messageCategory =
                messageCategoryRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                MessageCategory.class.getSimpleName()));

        if (!messageCategory.getCustomerId().equals(customerId)) {
            throw new IdorException();
        }

        return messageCategoryRepository.save(updatedMessageCategory);
    }

    void deleteMessageCategory(long customerId, long id, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);

        MessageCategory messageCategory =
                messageCategoryRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                MessageCategory.class.getSimpleName()));

        if (!messageCategory.getCustomerId().equals(customerId)) {
            throw new IdorException();
        }

        if (messageCategory.getCategory().equalsIgnoreCase(DEFAULT_CATEGORY)) {
            throw new DeleteConflictException("Unable to delete default category.");
        }

        messageCategoryRepository.delete(messageCategory);
    }
}
