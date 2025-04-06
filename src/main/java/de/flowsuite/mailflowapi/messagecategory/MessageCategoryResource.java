package de.flowsuite.mailflowapi.messagecategory;

import de.flowsuite.mailflowapi.common.entity.MessageCategory;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
class MessageCategoryResource {

    private final MessageCategoryService messageCategoryService;

    MessageCategoryResource(MessageCategoryService messageCategoryService) {
        this.messageCategoryService = messageCategoryService;
    }

    @PostMapping("/{customerId}/users/{userId}/message-categories")
    ResponseEntity<MessageCategory> createMessageCategory(
            @PathVariable long customerId,
            @PathVariable long userId,
            @RequestBody @Valid MessageCategory messageCategory,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                messageCategoryService.createMessageCategory(
                        customerId, userId, messageCategory, jwt));
    }

    @GetMapping("/{customerId}/users/{userId}/message-categories")
    ResponseEntity<List<MessageCategory>> listMessageCategories(
            @PathVariable long customerId,
            @PathVariable long userId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                messageCategoryService.listMessageCategories(customerId, userId, jwt));
    }

    @PutMapping("/{customerId}/users/{userId}/message-categories/{categoryId}")
    ResponseEntity<MessageCategory> updateMessageCategory(
            @PathVariable long customerId,
            @PathVariable long userId,
            @PathVariable long categoryId,
            @RequestBody @Valid MessageCategory messageCategory,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                messageCategoryService.updateMessageCategory(
                        customerId, userId, categoryId, messageCategory, jwt));
    }

    @DeleteMapping("/{customerId}/users/{userId}/message-categories/{categoryId}")
    ResponseEntity<Void> deleteMessageCategory(
            @PathVariable long customerId,
            @PathVariable long userId,
            @PathVariable long categoryId,
            @AuthenticationPrincipal Jwt jwt) {
        messageCategoryService.deleteMessageCategory(customerId, userId, categoryId, jwt);
        return ResponseEntity.noContent().build();
    }
}
