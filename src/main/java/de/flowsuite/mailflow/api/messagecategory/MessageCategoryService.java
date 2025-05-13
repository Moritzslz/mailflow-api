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
    private static final String NO_REPLY_CATEGORY = "No Reply";

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

        if (messageCategoryRepository.countByCustomerId(customerId) >= 10) {
            throw new MessageCategoryLimitException();
        }

        if (messageCategory.getDescription().length() < 100) {
            throw new MessageCategoryDescriptionException();
        }

        return messageCategoryRepository.save(messageCategory);
    }

    public void createDefaultMessageCategories(long customerId) {
        MessageCategory defaultMessageCategory =
                MessageCategory.builder()
                        .customerId(customerId)
                        .category(DEFAULT_CATEGORY)
                        .isReply(true)
                        .isFunctionCall(false)
                        .description(
                                "This is the default/fallback category for actionable emails that"
                                    + " do not fit into any other defined category. If an email"
                                    + " does not match any other category, it will be assigned"
                                    + " here. This category is useful for handling edge cases and"
                                    + " ensuring no email is left uncategorised.")
                        .build();

        MessageCategory noReplyMessageCategory =
                MessageCategory.builder()
                        .customerId(customerId)
                        .category(NO_REPLY_CATEGORY)
                        .isReply(false)
                        .isFunctionCall(false)
                        .description(
                                "This category is for emails that do not require a response and are"
                                    + " not actionable. This includes newsletters, promotional"
                                    + " offers, automated notifications, and any other"
                                    + " informational or unimportant emails that should not be"
                                    + " replied to. Security-related emails such as one-time codes"
                                    + " and password reset requests should NOT be categorized here,"
                                    + " as they are actionable and important.")
                        .build();

        messageCategoryRepository.save(defaultMessageCategory);
        messageCategoryRepository.save(noReplyMessageCategory);
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

        if (messageCategory.getCategory().equalsIgnoreCase(NO_REPLY_CATEGORY)) {
            throw new DeleteConflictException("Unable to delete no reply category.");
        }

        messageCategoryRepository.delete(messageCategory);
    }
}
