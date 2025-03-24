package de.flowsuite.mailflowapi.messagelog;

import de.flowsuite.mailflowapi.common.entity.MessageLogEntry;
import de.flowsuite.mailflowapi.common.exception.EntityNotFoundException;
import de.flowsuite.mailflowapi.common.exception.IdConflictException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.time.temporal.TemporalAdjusters.previousOrSame;

@Service
class MessageLogService {

    private final MessageLogRepository messageLogRepository;

    MessageLogService(MessageLogRepository messageLogRepository) {
        this.messageLogRepository = messageLogRepository;
    }

    MessageLogEntry createMessageLogEntry(MessageLogEntry messageLogEntry, long customerId) {
        if (messageLogEntry.getCustomerId() != customerId) {
            throw new IdConflictException();
        }
        return messageLogRepository.save(messageLogEntry);
    }

    MessageLogEntry getMessageLogEntryByMessageId(long customerId, long messageId) {
        MessageLogEntry messageLogEntry = messageLogRepository
                .findById(messageId)
                .orElseThrow(
                        () -> new EntityNotFoundException(MessageLogEntry.class.getSimpleName()));
        if (messageLogEntry.getCustomerId() != customerId) {
            throw new IdConflictException();
        }
        return messageLogEntry;
    }

    Iterable<MessageLogEntry> getMessageLogEntriesByCustomerId(long customerId) {
        return messageLogRepository.findByCustomerId(customerId);
    }

    Iterable<MessageLogEntry> getMessageLogEntries() {
        return messageLogRepository.findAll();
    }

    MessageLogEntry updateMessageLogEntry(long customerId, long messageId, MessageLogEntry messageLogEntry) {
        MessageLogEntry existingMessageLogEntry = getMessageLogEntryByMessageId(customerId, messageId);
        if (messageLogEntry.getCustomerId() != customerId) {
            throw new IdConflictException();
        }
        messageLogEntry.setId(messageId);
        return messageLogRepository.save(messageLogEntry);
    }

    void deleteMessageLogEntry(MessageLogEntry messageLogEntry) {
        messageLogRepository.delete(messageLogEntry);
    }

    public Map<String, Object> getMessageLogAnalytics(
            long customerId, String startDate, String endDate, String category, String timeframe) {
        List<MessageLogEntry> entries = (List<MessageLogEntry>) messageLogRepository.findByCustomerId(customerId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (startDate != null) {
            LocalDate start = LocalDate.parse(startDate, formatter);
            entries = entries.stream()
                    .filter(entry -> !entry.getReceivedAt().toLocalDate().isBefore(start))
                    .collect(Collectors.toList());
        }

        if (endDate != null) {
            LocalDate end = LocalDate.parse(endDate, formatter);
            entries = entries.stream()
                    .filter(entry -> !entry.getReceivedAt().toLocalDate().isAfter(end))
                    .collect(Collectors.toList());
        }

        if (category != null) {
            entries = entries.stream()
                    .filter(entry -> entry.getCategory().equalsIgnoreCase(category))
                    .collect(Collectors.toList());
        }

        int totalMessages = entries.size();
        int totalInputTokens = entries.stream().mapToInt(MessageLogEntry::getInputTokens).sum();
        int totalOutputTokens = entries.stream().mapToInt(MessageLogEntry::getOutputTokens).sum();
        double averageProcessingTimeSeconds = entries.stream().mapToInt(MessageLogEntry::getProcessingTimeInSeconds).average().orElse(0);

        Map<String, Long> messagesPerCategory = entries.stream()
                .collect(Collectors.groupingBy(MessageLogEntry::getCategory, Collectors.counting()));

        Map<LocalDate, Long> messagesPerDay = entries.stream()
                .collect(Collectors.groupingBy(entry -> entry.getReceivedAt().toLocalDate(), Collectors.counting()));

        Map<LocalDate, Long> messagesPerWeek = entries.stream()
                .collect(Collectors.groupingBy(entry -> entry.getReceivedAt().toLocalDate().with(previousOrSame(java.time.DayOfWeek.MONDAY)), Collectors.counting()));

        Map<LocalDate, Long> messagesPerMonth = entries.stream()
                .collect(Collectors.groupingBy(entry -> entry.getReceivedAt().toLocalDate().withDayOfMonth(1), Collectors.counting()));

        List<Map<String, Object>> messagesPerDayList = messagesPerDay.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", entry.getKey().toString());
                    map.put("count", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());

        List<Map<String, Object>> messagesPerWeekList = messagesPerWeek.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("week_start_date", entry.getKey().toString());
                    map.put("count", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());

        List<Map<String, Object>> messagesPerMonthList = messagesPerMonth.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("month", entry.getKey().toString());
                    map.put("count", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("total_messages", totalMessages);
        response.put("total_input_tokens", totalInputTokens);
        response.put("total_output_tokens", totalOutputTokens);
        response.put("average_processing_time_seconds", averageProcessingTimeSeconds);
        response.put("messages_per_category", messagesPerCategory);
        response.put("messages_per_day", messagesPerDayList);
        response.put("messages_per_week", messagesPerWeekList);
        response.put("messages_per_month", messagesPerMonthList);

        return response;
    }
}
