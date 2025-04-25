package de.flowsuite.mailflowapi.messagelog;

import de.flowsuite.mailflowapi.common.constant.Timeframe;
import de.flowsuite.mailflowapi.common.entity.MessageLogEntry;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customers")
class MessageLogResource {

    private final MessageLogService messageLogService;

    MessageLogResource(MessageLogService messageLogService) {
        this.messageLogService = messageLogService;
    }

    @PostMapping("/{customerId}/users/{userId}/message-log")
    ResponseEntity<MessageLogEntry> createMessageLogEntry(
            @PathVariable long customerId,
            @PathVariable long userId,
            @RequestBody @Valid CreateMessageLogEntryRequest request,
            @AuthenticationPrincipal Jwt jwt,
            UriComponentsBuilder uriBuilder) {

        MessageLogEntry createdMessageLogEntry =
                messageLogService.createMessageLogEntry(customerId, userId, request, jwt);

        URI location =
                uriBuilder
                        .path("/customers/{customerId}/users/{userId}/message-log/{id}")
                        .buildAndExpand(
                                createdMessageLogEntry.getCustomerId(),
                                createdMessageLogEntry.getUserId(),
                                createdMessageLogEntry.getId())
                        .toUri();

        return ResponseEntity.created(location).body(createdMessageLogEntry);
    }

    @GetMapping("/{customerId}/message-log")
    ResponseEntity<List<MessageLogEntry>> listMessageLogEntries(
            @PathVariable long customerId, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                messageLogService.listMessageLogEntriesByCustomer(customerId, jwt));
    }

    @GetMapping("/{customerId}/users/{userId}/message-log")
    ResponseEntity<List<MessageLogEntry>> listMessageLogEntries(
            @PathVariable long customerId,
            @PathVariable long userId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                messageLogService.listMessageLogEntriesByUser(customerId, userId, jwt));
    }

    @GetMapping("/{customerId}/users/{userId}/message-log/{id}")
    ResponseEntity<MessageLogEntry> getMessageLogEntryByMessageId(
            @PathVariable long customerId,
            @PathVariable long userId,
            @PathVariable long id,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(messageLogService.getMessageLogEntry(customerId, userId, id, jwt));
    }

    @GetMapping("/{customerId}/message-log/analytics")
    ResponseEntity<MessageLogAnalyticsResponse> getMessageLogAnalyticsForCustomer(
            @PathVariable long customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    Date from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date to,
            @RequestParam(required = false) Timeframe timeframe,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                messageLogService.getMessageLogAnalyticsForCustomer(
                        customerId, from, to, timeframe, jwt));
    }

    @GetMapping("/{customerId}/users/{userId}/message-log/analytics")
    ResponseEntity<MessageLogAnalyticsResponse> getMessageLogAnalyticsForUser(
            @PathVariable long customerId,
            @PathVariable long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    Date from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date to,
            @RequestParam(required = false) Timeframe timeframe,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                messageLogService.getMessageLogAnalyticsForUser(
                        customerId, userId, from, to, timeframe, jwt));
    }

    record CreateMessageLogEntryRequest(
            @NotNull Long userId,
            @NotNull Long customerId,
            boolean isReplied,
            @NotBlank String category,
            @NotBlank String language,
            String fromEmailAddress,
            String subject,
            @NotNull ZonedDateTime receivedAt,
            @NotNull ZonedDateTime processedAt,
            @NotNull Integer processingTimeInSeconds,
            @NotBlank String llmUsed,
            @NotNull Integer inputTokens,
            @NotNull Integer outputTokens,
            @NotNull Integer totalTokens) {}

    record MessageLogAnalyticsResponse(
            double avgProcessingTimeInSeconds,
            double responseRate,
            Map<String, Map<String, Long>> messageLogAnalytics) {}
}
