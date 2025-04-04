package de.flowsuite.mailflowapi.blacklist;

import de.flowsuite.mailflowapi.common.entity.BlacklistEntry;
import de.flowsuite.mailflowapi.common.exception.EntityNotFoundException;
import de.flowsuite.mailflowapi.common.exception.IdConflictException;
import de.flowsuite.mailflowapi.common.util.AesUtil;
import de.flowsuite.mailflowapi.common.util.AuthorisationUtil;
import de.flowsuite.mailflowapi.common.util.HmacUtil;
import de.flowsuite.mailflowapi.common.util.Util;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class BlacklistService {

    private final BlacklistRepository blacklistRepository;

    BlacklistService(BlacklistRepository blacklistRepository) {
        this.blacklistRepository = blacklistRepository;
    }

    BlacklistEntry createBlacklistEntry(
            long customerId, long userId, BlacklistEntry blacklistEntry, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);
        AuthorisationUtil.validateAccessToUser(userId, jwt);

        if (userId != blacklistEntry.getUserId()) {
            throw new IdConflictException();
        }

        Util.validateEmailAddress(blacklistEntry.getBlacklistedEmailAddress());

        blacklistEntry.setBlacklistedEmailAddressHash(
                HmacUtil.hash(blacklistEntry.getBlacklistedEmailAddress()));

        return blacklistRepository.save(blacklistEntry);
    }

    List<BlacklistEntry> listBlacklistEntries(long customerId, long userId, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);
        AuthorisationUtil.validateAccessToUser(userId, jwt);

        return blacklistRepository.findByUserId(userId).stream()
                .map(
                        blacklistEntry -> {
                            blacklistEntry.setBlacklistedEmailAddress(
                                    AesUtil.decrypt(blacklistEntry.getBlacklistedEmailAddress()));
                            return blacklistEntry;
                        })
                .toList();
    }

    void deleteBlacklistEntry(long customerId, long userId, long blacklistEntryId, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);
        AuthorisationUtil.validateAccessToUser(userId, jwt);

        BlacklistEntry blacklistEntry =
                blacklistRepository
                        .findById(blacklistEntryId)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                BlacklistEntry.class.getSimpleName()));

        blacklistRepository.delete(blacklistEntry);
    }
}
