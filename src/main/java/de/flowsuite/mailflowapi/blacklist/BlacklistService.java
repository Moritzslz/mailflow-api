package de.flowsuite.mailflowapi.blacklist;

import de.flowsuite.mailflowapi.common.entity.BlacklistEntry;
import de.flowsuite.mailflowapi.common.exception.EntityExistsException;
import de.flowsuite.mailflowapi.common.exception.EntityNotFoundException;
import de.flowsuite.mailflowapi.common.exception.IdConflictException;
import de.flowsuite.mailflowapi.common.exception.IdorException;
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

        if (!blacklistEntry.getUserId().equals(userId)) {
            throw new IdConflictException();
        }

        String emailAddress = blacklistEntry.getBlacklistedEmailAddress().toLowerCase();
        String emailAddressHash = HmacUtil.hash(emailAddress);

        Util.validateEmailAddress(emailAddress);

        if (blacklistRepository.existsByUserIdAndBlacklistedEmailAddressHash(
                userId, emailAddressHash)) {
            throw new EntityExistsException(BlacklistEntry.class.getSimpleName());
        }

        blacklistEntry.setBlacklistedEmailAddressHash(emailAddressHash);
        blacklistEntry.setBlacklistedEmailAddress(AesUtil.encrypt(emailAddress));

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

        if (!blacklistEntry.getUserId().equals(userId)) {
            throw new IdorException();
        }

        blacklistRepository.delete(blacklistEntry);
    }
}
