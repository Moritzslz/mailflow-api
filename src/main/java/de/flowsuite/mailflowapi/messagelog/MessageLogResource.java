package de.flowsuite.mailflowapi.messagelog;


import de.flowsuite.mailflowapi.common.entity.MessageLogEntry;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.Date;

@RestController
@RequestMapping("/api/v1/customers")
class MessageLogResource {
    private final MessageLogService messageLogService;

    MessageLogResource(MessageLogService messageLogService) {
        this.messageLogService = messageLogService;
    }

    @PostMapping("{customerId}/message-log")
    ResponseEntity<MessageLogEntry> createMessageLogEntry(
            @PathVariable long customerId, @RequestBody @Valid MessageLogEntry messageLogEntry) {
        return ResponseEntity.ok(messageLogService.createMessageLogEntry(messageLogEntry, customerId));
    }

    @GetMapping("{customerId}/message-log")
    ResponseEntity<Iterable<MessageLogEntry>> getMessageLogEntriesByCustomerId(
            @PathVariable long customerId) {
        return ResponseEntity.ok(messageLogService.getMessageLogEntriesByCustomerId(customerId));
    }

    @GetMapping("{customerId}/message-log/{messageId}")
    ResponseEntity<MessageLogEntry> getMessageLogEntryByMessageId(
            @PathVariable long customerId, @PathVariable long messageId) {
        return ResponseEntity.ok(messageLogService.getMessageLogEntryByMessageId(customerId, messageId));
    }

    @PutMapping("{customerId}/message-log/{messageId}")
    ResponseEntity<MessageLogEntry> updateMessageLogEntry(
            @PathVariable long customerId, @PathVariable long messageId, @RequestBody @Valid MessageLogEntry messageLogEntry) {
        return ResponseEntity.ok(messageLogService.updateMessageLogEntry(customerId, messageId, messageLogEntry));
    }

    @DeleteMapping("{customerId}/message-log/{messageId}")
    ResponseEntity<Void> deleteMessageLogEntry(
            @PathVariable long customerId, @PathVariable long messageId) {
        MessageLogEntry messageLogEntry = messageLogService.getMessageLogEntryByMessageId(customerId, messageId);
        messageLogService.deleteMessageLogEntry(messageLogEntry);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("{customerId}/message-log/analytics")
    ResponseEntity<?> getMessageLogAnalytics(
            @PathVariable long customerId,
            @RequestParam(required = false) Date startDate,
            @RequestParam(required = false) Date endDate,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String timeframe) {
        return ResponseEntity.ok(
                messageLogService.getMessageLogAnalytics(customerId, startDate, endDate, category, timeframe));
    }
}
