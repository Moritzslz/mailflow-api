package de.flowsuite.mailflowapi.blacklist;

import de.flowsuite.mailflowapi.common.entity.BlacklistEntry;
import de.flowsuite.mailflowapi.common.exception.EntityNotFoundException;
import de.flowsuite.mailflowapi.common.exception.IdConflictException;
import org.springframework.stereotype.Service;

@Service
class BlacklistService {

    private final BlacklistRepository blacklistRepository;

    BlacklistService(BlacklistRepository blacklistRepository) {
        this.blacklistRepository = blacklistRepository;
    }

    BlacklistEntry createBlacklistEntry(BlacklistEntry blacklistEntry, long customerId) {
        if(blacklistEntry.getCustomerId() != customerId) {
            throw new IdConflictException();
        }
        return blacklistRepository.save(blacklistEntry);
    }

    BlacklistEntry getBlacklistEntryByCustomerId(long customerId) {
        return blacklistRepository
                .findById(customerId)
                .orElseThrow(
                        () -> new EntityNotFoundException(BlacklistEntry.class.getSimpleName()));
    }

    Iterable<BlacklistEntry> getBlacklistEntriesByCustomerId(long customerId) {
        return blacklistRepository.findByCustomerId(customerId);
    }

    void deleteBlacklistEntry(BlacklistEntry blacklistEntry) {
        blacklistRepository.delete(blacklistEntry);
    }

    BlacklistEntry getBlacklistEntryByBlacklistId(long customerId, long blacklistId) {
        BlacklistEntry blacklistEntry = blacklistRepository
                .findById(blacklistId)
                .orElseThrow(
                        () -> new EntityNotFoundException(BlacklistEntry.class.getSimpleName()));
        if (blacklistEntry.getCustomerId() != customerId) {
            throw new IdConflictException();
        }
        return blacklistEntry;
    }
}
