package de.flowsuite.mailflow.api.settings;

import de.flowsuite.mailflow.api.user.UserService;
import de.flowsuite.mailflow.common.entity.Settings;
import de.flowsuite.mailflow.common.entity.User;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/customers")
class SettingsResource {

    private static final Logger LOG = LoggerFactory.getLogger(SettingsResource.class);
    private static final String NOTIFY_USER_URI = "/notifications/users/{userId}";

    private final SettingsService settingsService;
    private final UserService userService;
    private final RestClient mailboxServiceRestClient;

    public SettingsResource(
            SettingsService settingsService,
            UserService userService,
            @Qualifier("mailboxServiceRestClient") RestClient mailboxServiceRestClient) {
        this.settingsService = settingsService;
        this.userService = userService;
        this.mailboxServiceRestClient = mailboxServiceRestClient;
    }

    @PostMapping("/{customerId}/users/{userId}/settings")
    ResponseEntity<Settings> createSettings(
            @PathVariable long customerId,
            @PathVariable long userId,
            @RequestBody @Valid CreateSettingsRequest request,
            @AuthenticationPrincipal Jwt jwt,
            UriComponentsBuilder uriBuilder) {
        Settings createdSettings = settingsService.createSettings(customerId, userId, request, jwt);

        URI location =
                uriBuilder
                        .path("/customers/{customerId}/users/{userId}")
                        .buildAndExpand(
                                createdSettings.getCustomerId(), createdSettings.getUserId())
                        .toUri();

        CompletableFuture.runAsync(() -> notifyMailboxService(HttpMethod.POST, userId));

        return ResponseEntity.created(location).body(createdSettings);
    }

    @GetMapping("/{customerId}/users/{userId}/settings")
    ResponseEntity<Settings> getSettings(
            @PathVariable long customerId,
            @PathVariable long userId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(settingsService.getSettings(customerId, userId, jwt));
    }

    @PutMapping("/{customerId}/users/{userId}/settings")
    ResponseEntity<Settings> updateSettings(
            @PathVariable long customerId,
            @PathVariable long userId,
            @RequestBody @Valid UpdateSettingsRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        Settings updatedSettings = settingsService.updateSettings(customerId, userId, request, jwt);
        CompletableFuture.runAsync(() -> notifyMailboxService(HttpMethod.PUT, userId));
        return ResponseEntity.ok(updatedSettings);
    }

    @PutMapping("/{customerId}/users/{userId}/settings/mailbox-password")
    ResponseEntity<Settings> updateMailboxPassword(
            @PathVariable long customerId,
            @PathVariable long userId,
            @RequestBody @Valid UpdateMailboxPasswordRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        Settings updatedSettings =
                settingsService.updateMailboxPassword(customerId, userId, request, jwt);
        CompletableFuture.runAsync(() -> notifyMailboxService(HttpMethod.PUT, userId));
        return ResponseEntity.ok(updatedSettings);
    }

    private void notifyMailboxService(HttpMethod method, long userId) {
        LOG.debug("Notifying mailbox service of settings change ({})", method);

        User user = userService.getById(userId);

        mailboxServiceRestClient
                .method(method)
                .uri(NOTIFY_USER_URI, userId)
                .body(user)
                .retrieve()
                .toBodilessEntity();
    }

    record CreateSettingsRequest(
            @NotNull Long userId, @NotNull Long customerId, @NotBlank String mailboxPassword) {}

    record UpdateSettingsRequest(
            @NotNull Long userId,
            @NotNull Long customerId,
            boolean executionEnabled,
            boolean autoReplyEnabled,
            boolean responseRatingEnabled,
            String imapHost,
            String smtpHost,
            Integer imapPort,
            Integer smtpPort) {}

    record UpdateMailboxPasswordRequest(
            @NotNull Long userId,
            @NotNull Long customerId,
            @NotBlank String currentPassword,
            @NotBlank String updatedPassword) {}
}
