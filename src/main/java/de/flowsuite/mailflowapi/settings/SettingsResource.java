package de.flowsuite.mailflowapi.settings;

import de.flowsuite.mailflow.common.entity.Settings;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.ZonedDateTime;

@RestController
@RequestMapping("/customers")
class SettingsResource {

    private final SettingsService settingsService;

    public SettingsResource(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @PostMapping("/{customerId}/users/{userId}/settings")
    ResponseEntity<Settings> createSettings(
            @PathVariable long customerId,
            @PathVariable long userId,
            @RequestBody @Valid Settings settings,
            @AuthenticationPrincipal Jwt jwt,
            UriComponentsBuilder uriBuilder) {
        Settings createdSettings =
                settingsService.createSettings(customerId, userId, settings, jwt);

        URI location =
                uriBuilder
                        .path("/customers/{customerId}/users/{userId}")
                        .buildAndExpand(
                                createdSettings.getCustomerId(), createdSettings.getUserId())
                        .toUri();

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
        return ResponseEntity.ok(settingsService.updateSettings(customerId, userId, request, jwt));
    }

    @PutMapping("/{customerId}/users/{userId}/settings/mailbox-password")
    ResponseEntity<Settings> updateMailboxPassword(
            @PathVariable long customerId,
            @PathVariable long userId,
            @RequestBody @Valid UpdateMailboxPasswordRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                settingsService.updateMailboxPassword(customerId, userId, request, jwt));
    }

    record UpdateSettingsRequest(
            @NotNull Long userId,
            @NotNull Long customerId,
            boolean isExecutionEnabled,
            boolean isAutoReplyEnabled,
            boolean isResponseRatingEnabled,
            @Min(168) @Max(744) int crawlFrequencyInHours,
            ZonedDateTime lastCrawlAt,
            ZonedDateTime nextCrawlAt,
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
