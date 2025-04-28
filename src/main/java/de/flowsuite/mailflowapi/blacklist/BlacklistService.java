package de.flowsuite.mailflowapi.blacklist;

import de.flowsuite.mailflowcommon.entity.BlacklistEntry;
import de.flowsuite.mailflowcommon.exception.EntityAlreadyExistsException;
import de.flowsuite.mailflowcommon.exception.EntityNotFoundException;
import de.flowsuite.mailflowcommon.exception.IdConflictException;
import de.flowsuite.mailflowcommon.exception.IdorException;
import de.flowsuite.mailflowcommon.util.AesUtil;
import de.flowsuite.mailflowcommon.util.AuthorisationUtil;
import de.flowsuite.mailflowcommon.util.HmacUtil;
import de.flowsuite.mailflowcommon.util.Util;

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

        if (blacklistEntry.getId() != null || !blacklistEntry.getUserId().equals(userId)) {
            throw new IdConflictException();
        }

        String emailAddress = blacklistEntry.getBlacklistedEmailAddress().toLowerCase();
        String emailAddressHash = HmacUtil.hash(emailAddress);

        Util.validateEmailAddress(emailAddress);

        if (blacklistRepository.existsByUserIdAndBlacklistedEmailAddressHash(
                userId, emailAddressHash)) {
            throw new EntityAlreadyExistsException(BlacklistEntry.class.getSimpleName());
        }

        blacklistEntry.setBlacklistedEmailAddressHash(emailAddressHash);
        blacklistEntry.setBlacklistedEmailAddress(AesUtil.encrypt(emailAddress));

        return blacklistRepository.save(blacklistEntry);
    }

    BlacklistEntry getBlacklistEntry(long customerId, long userId, long id, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);
        AuthorisationUtil.validateAccessToUser(userId, jwt);

        BlacklistEntry blacklistEntry =
                blacklistRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                BlacklistEntry.class.getSimpleName()));

        if (!blacklistEntry.getUserId().equals(userId)) {
            throw new IdorException();
        }

        blacklistEntry.setBlacklistedEmailAddress(
                AesUtil.decrypt(blacklistEntry.getBlacklistedEmailAddress()));

        return blacklistEntry;
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

    void deleteBlacklistEntry(long customerId, long userId, long id, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);
        AuthorisationUtil.validateAccessToUser(userId, jwt);

        BlacklistEntry blacklistEntry =
                blacklistRepository
                        .findById(id)
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
