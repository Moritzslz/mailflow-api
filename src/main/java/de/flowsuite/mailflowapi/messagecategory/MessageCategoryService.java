package de.flowsuite.mailflowapi.messagecategory;

import de.flowsuite.mailflowapi.common.entity.MessageCategory;
import de.flowsuite.mailflowapi.common.exception.*;
import de.flowsuite.mailflowapi.common.util.AuthorisationUtil;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class MessageCategoryService {

    private final MessageCategoryRepository messageCategoryRepository;

    MessageCategoryService(MessageCategoryRepository messageCategoryRepository) {
        this.messageCategoryRepository = messageCategoryRepository;
    }

    MessageCategory createMessageCategory(
            long customerId, MessageCategory messageCategory, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);

        if (messageCategory.getId() != null || !messageCategory.getCustomerId().equals(customerId)) {
            throw new IdConflictException();
        }

        if (messageCategoryRepository.existsByCategory(messageCategory.getCategory())) {
            throw new EntityAlreadyExistsException(MessageCategory.class.getSimpleName());
        }

        return messageCategoryRepository.save(messageCategory);
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
            long customerId, long id, MessageCategory messageCategory, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);

        if (!messageCategory.getCustomerId().equals(customerId)
                || !messageCategory.getId().equals(id)) {
            throw new IdConflictException();
        }

        MessageCategory existingCategory =
                messageCategoryRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                MessageCategory.class.getSimpleName()));

        if (!existingCategory.getCustomerId().equals(customerId)) {
            throw new UpdateConflictException();
        }

        return messageCategoryRepository.save(messageCategory);
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

        messageCategoryRepository.delete(messageCategory);
    }
}
