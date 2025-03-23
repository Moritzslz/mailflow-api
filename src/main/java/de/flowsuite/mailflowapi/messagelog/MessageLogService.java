package de.flowsuite.mailflowapi.messagelog;

import de.flowsuite.mailflowapi.common.entity.MessageLogEntry;
import de.flowsuite.mailflowapi.common.exception.EntityNotFoundException;
import de.flowsuite.mailflowapi.common.exception.IdConflictException;
import org.springframework.stereotype.Service;

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
}
