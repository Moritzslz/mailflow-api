package de.flowsuite.mailflow.api.blacklist;

import de.flowsuite.mailflow.common.entity.BlacklistEntry;

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
class BlacklistResource {

    private static final Logger LOG = LoggerFactory.getLogger(BlacklistResource.class);
    private static final String NOTIFY_BLACKLIST_URI = "/notifications/users/{userId}/blacklist";

    private final BlacklistService blacklistService;
    private final RestClient mailboxServiceRestClient;

    BlacklistResource(
            BlacklistService blacklistService,
            @Qualifier("mailboxServiceRestClient") RestClient mailboxServiceRestClient) {
        this.blacklistService = blacklistService;
        this.mailboxServiceRestClient = mailboxServiceRestClient;
    }

    @PostMapping("/{customerId}/users/{userId}/blacklist")
    ResponseEntity<BlacklistEntry> createBlacklistEntry(
            @PathVariable long customerId,
            @PathVariable long userId,
            @RequestBody @Valid BlacklistEntry blacklistEntry,
            @AuthenticationPrincipal Jwt jwt,
            UriComponentsBuilder uriBuilder) {
        BlacklistEntry createdBlacklistEntry =
                blacklistService.createBlacklistEntry(customerId, userId, blacklistEntry, jwt);

        URI location =
                uriBuilder
                        .path("/customers/{customerId}/users/{userId}/blacklist/{id}")
                        .buildAndExpand(customerId, userId, createdBlacklistEntry.getId())
                        .toUri();

        CompletableFuture.runAsync(() -> notifyMailboxService(customerId, userId, jwt));

        return ResponseEntity.created(location).body(createdBlacklistEntry);
    }

    @GetMapping("/{customerId}/users/{userId}/blacklist/{id}")
    ResponseEntity<BlacklistEntry> getBlacklistEntry(
            @PathVariable long customerId,
            @PathVariable long userId,
            @PathVariable long id,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(blacklistService.getBlacklistEntry(customerId, userId, id, jwt));
    }

    @GetMapping("/{customerId}/users/{userId}/blacklist")
    ResponseEntity<List<BlacklistEntry>> listBlacklistEntries(
            @PathVariable long customerId,
            @PathVariable long userId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(blacklistService.listBlacklistEntries(customerId, userId, jwt));
    }

    @DeleteMapping("/{customerId}/users/{userId}/blacklist/{id}")
    ResponseEntity<Void> deleteBlacklistEntry(
            @PathVariable long customerId,
            @PathVariable long userId,
            @PathVariable long id,
            @AuthenticationPrincipal Jwt jwt) {
        blacklistService.deleteBlacklistEntry(customerId, userId, id, jwt);
        CompletableFuture.runAsync(() -> notifyMailboxService(customerId, userId, jwt));
        return ResponseEntity.noContent().build();
    }

    private void notifyMailboxService(long customerId, long userId, Jwt jwt) {
        LOG.debug("Notifying mailbox service of blacklist change");

        List<BlacklistEntry> blacklistEntries =
                blacklistService.listBlacklistEntries(customerId, userId, jwt);

        mailboxServiceRestClient
                .put()
                .uri(NOTIFY_BLACKLIST_URI, userId)
                .body(blacklistEntries)
                .retrieve()
                .toBodilessEntity();
    }
}
