package de.flowsuite.mailflowapi.messagelog;

import de.flowsuite.mailflowapi.common.entity.MessageLogEntry;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

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
            @RequestBody @Valid MessageLogEntry messageLogEntry,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                messageLogService.createMessageLogEntry(customerId, userId, messageLogEntry, jwt));
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
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date from,
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
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date to,
            @RequestParam(required = false) Timeframe timeframe,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                messageLogService.getMessageLogAnalyticsForUser(
                        customerId, userId, from, to, timeframe, jwt));
    }

    record MessageLogAnalyticsResponse(
            double avgProcessingTimeInSeconds,
            double responseRate,
            Map<String, Map<String, Long>> messageLogAnalytics) {}

    public enum Timeframe {
        DAILY,
        WEEKLY,
        MONTHLY,
        YEARLY
    }
}
