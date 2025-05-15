package de.flowsuite.mailflow.api.messagecategory;

import de.flowsuite.mailflow.common.entity.MessageCategory;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/customers")
class MessageCategoryResource {

    private static final Logger LOG = LoggerFactory.getLogger(MessageCategoryResource.class);
    private static final String NOTIFY_MESSAGE_CATEGORIES_URI = "/notifications/customers/{customerId}/message-categories";

    private final MessageCategoryService messageCategoryService;
    private final RestClient mailboxServiceRestClient;

    MessageCategoryResource(MessageCategoryService messageCategoryService, @Qualifier("mailboxServiceRestClient") RestClient mailboxServiceRestClient) {
        this.messageCategoryService = messageCategoryService;
        this.mailboxServiceRestClient = mailboxServiceRestClient;
    }

    @PostMapping("/{customerId}/message-categories")
    ResponseEntity<MessageCategory> createMessageCategory(
            @PathVariable long customerId,
            @RequestBody @Valid MessageCategory messageCategory,
            @AuthenticationPrincipal Jwt jwt,
            UriComponentsBuilder uriBuilder) {

        MessageCategory createdMessageCategory =
                messageCategoryService.createMessageCategory(customerId, messageCategory, jwt);

        URI location =
                uriBuilder
                        .path("/customers/{customerId}/message-categories/{id}")
                        .buildAndExpand(
                                createdMessageCategory.getCustomerId(),
                                createdMessageCategory.getId())
                        .toUri();

        CompletableFuture.runAsync(() -> notifyMailboxService(customerId, jwt));

        return ResponseEntity.created(location).body(createdMessageCategory);
    }

    @GetMapping("/{customerId}/message-categories/{id}")
    ResponseEntity<MessageCategory> getMessageCategory(
            @PathVariable long customerId,
            @PathVariable long id,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(messageCategoryService.getMessageCategory(customerId, id, jwt));
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
        MessageCategory updatedMessageCategory = messageCategoryService.updateMessageCategory(customerId, id, messageCategory, jwt);
        CompletableFuture.runAsync(() -> notifyMailboxService(customerId, jwt));
        return ResponseEntity.ok(updatedMessageCategory);
    }

    @DeleteMapping("/{customerId}/message-categories/{id}")
    ResponseEntity<Void> deleteMessageCategory(
            @PathVariable long customerId,
            @PathVariable long id,
            @AuthenticationPrincipal Jwt jwt) {
        messageCategoryService.deleteMessageCategory(customerId, id, jwt);
        CompletableFuture.runAsync(() -> notifyMailboxService(customerId, jwt));
        return ResponseEntity.noContent().build();
    }

    private void notifyMailboxService(long customerId, Jwt jwt) {
        LOG.debug("Notifying mailbox service of blacklist change");

        List<MessageCategory> messageCategories =
                messageCategoryService.listMessageCategories(customerId, jwt);

        mailboxServiceRestClient
                .put()
                .uri(NOTIFY_MESSAGE_CATEGORIES_URI, customerId)
                .body(messageCategories)
                .retrieve()
                .toBodilessEntity();
    }
}
