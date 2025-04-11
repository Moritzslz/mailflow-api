package de.flowsuite.mailflowapi.messagecategory;

import de.flowsuite.mailflowapi.common.entity.MessageCategory;
import de.flowsuite.mailflowapi.common.exception.EntityNotFoundException;
import de.flowsuite.mailflowapi.common.exception.IdConflictException;
import de.flowsuite.mailflowapi.common.exception.IdorException;
import de.flowsuite.mailflowapi.common.exception.UpdateConflictException;
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
            long customerId, long userId, MessageCategory messageCategory, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);
        AuthorisationUtil.validateAccessToUser(userId, jwt);

        if (!messageCategory.getUserId().equals(userId)) {
            throw new IdConflictException();
        }

        return messageCategoryRepository.save(messageCategory);
    }

    List<MessageCategory> listMessageCategories(long customerId, long userId, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);
        AuthorisationUtil.validateAccessToUser(userId, jwt);

        return messageCategoryRepository.findByUserId(userId);
    }

    MessageCategory updateMessageCategory(
            long customerId,
            long userId,
            long categoryId,
            MessageCategory messageCategory,
            Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);
        AuthorisationUtil.validateAccessToUser(userId, jwt);

        if (!messageCategory.getUserId().equals(userId)) {
            throw new IdConflictException();
        }

        MessageCategory existingCategory =
                messageCategoryRepository
                        .findById(categoryId)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                MessageCategory.class.getSimpleName()));

        if (!existingCategory.getUserId().equals(userId)) {
            throw new UpdateConflictException();
        }

        return messageCategoryRepository.save(messageCategory);
    }

    void deleteMessageCategory(long customerId, long userId, long categoryId, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);
        AuthorisationUtil.validateAccessToUser(userId, jwt);

        MessageCategory messageCategory =
                messageCategoryRepository
                        .findById(categoryId)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                MessageCategory.class.getSimpleName()));

        if (!messageCategory.getUserId().equals(userId)) {
            throw new IdorException();
        }

        messageCategoryRepository.delete(messageCategory);
    }
}
