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

    @PostMapping("/{customerId}/message-categories")
    ResponseEntity<MessageCategory> createMessageCategory(
            @PathVariable long customerId,
            @RequestBody @Valid MessageCategory messageCategory,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                messageCategoryService.createMessageCategory(customerId, messageCategory, jwt));
    }

    @GetMapping("/{customerId}/message-categories")
    ResponseEntity<List<MessageCategory>> listMessageCategories(
            @PathVariable long customerId, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(messageCategoryService.listMessageCategories(customerId, jwt));
    }

    @PutMapping("/{customerId}/message-categories/{id}")
    ResponseEntity<MessageCategory> updateMessageCategory(
            @PathVariable long customerId,
            @PathVariable long id,
            @RequestBody @Valid MessageCategory messageCategory,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                messageCategoryService.updateMessageCategory(customerId, id, messageCategory, jwt));
    }

    @DeleteMapping("/{customerId}/message-categories/{id}")
    ResponseEntity<Void> deleteMessageCategory(
            @PathVariable long customerId,
            @PathVariable long id,
            @AuthenticationPrincipal Jwt jwt) {
        messageCategoryService.deleteMessageCategory(customerId, id, jwt);
        return ResponseEntity.noContent().build();
    }
}
