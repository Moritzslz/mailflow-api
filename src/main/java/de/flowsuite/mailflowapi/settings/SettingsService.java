package de.flowsuite.mailflowapi.settings;

import de.flowsuite.mailflowapi.common.entity.Customer;
import de.flowsuite.mailflowapi.common.entity.Settings;
import de.flowsuite.mailflowapi.common.exception.EntityNotFoundException;
import de.flowsuite.mailflowapi.common.exception.IdConflictException;
import de.flowsuite.mailflowapi.common.exception.UpdateConflictException;
import de.flowsuite.mailflowapi.common.util.AesUtil;
import de.flowsuite.mailflowapi.common.util.AuthorisationUtil;
import de.flowsuite.mailflowapi.common.util.HmacUtil;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
class SettingsService {

    private final SettingsRepository settingsRepository;

    SettingsService(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    Settings createSettings(long customerId, long userId, Settings settings, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);
        AuthorisationUtil.validateAccessToUser(userId, jwt);

        if (!settings.getUserId().equals(userId) || !settings.getCustomerId().equals(customerId)) {
            throw new IdConflictException();
        }

        settings.setMailboxPasswordHash(HmacUtil.hash(settings.getMailboxPassword()));
        settings.setMailboxPassword(AesUtil.encrypt(settings.getMailboxPassword()));

        return settingsRepository.save(settings);
    }

    Settings getSettings(long customerId, long userId, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);
        AuthorisationUtil.validateAccessToUser(userId, jwt);

        return settingsRepository
                .findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(Settings.class.getSimpleName()));
    }

    Settings updateSettings(
            long customerId, long userId, SettingsResource.UpdateSettingsRequest request, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);
        AuthorisationUtil.validateAccessToUser(userId, jwt);

        if (!request.userId().equals(userId) || !request.customerId().equals(customerId)) {
            throw new IdConflictException();
        }

        Settings settings =
                settingsRepository
                        .findById(userId)
                        .orElseThrow(
                                () -> new EntityNotFoundException(Customer.class.getSimpleName()));

        settings.setExecutionEnabled(request.isExecutionEnabled());
        settings.setAutoReplyEnabled(request.isAutoReplyEnabled());
        settings.setResponseRatingEnabled(request.isResponseRatingEnabled());
        settings.setCrawlFrequencyInHours(request.crawlFrequencyInHours());
        settings.setImapHost(request.imapHost());
        settings.setSmtpHost(request.smtpHost());
        settings.setImapPort(request.imapPort());
        settings.setSmtpPort(request.smtpPort());

        if (request.lastCrawlAt() != null) {
            if (settings.getLastCrawlAt() == null
                    || request.lastCrawlAt().isAfter(settings.getLastCrawlAt())) {
                settings.setLastCrawlAt(request.lastCrawlAt());
            }
        }
        if (request.nextCrawlAt() != null) {
            if (settings.getNextCrawlAt() == null
                    || request.nextCrawlAt().isAfter(settings.getNextCrawlAt())) {
                settings.setNextCrawlAt(request.nextCrawlAt());
            }
        }

        return settingsRepository.save(settings);
    }

    Settings updateMailboxPassword(
            long customerId,
            long userId,
            SettingsResource.UpdateMailboxPasswordRequest request,
            Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);
        AuthorisationUtil.validateAccessToUser(userId, jwt);

        if (!request.userId().equals(userId) || !request.customerId().equals(customerId)) {
            throw new IdConflictException();
        }

        Settings settings =
                settingsRepository
                        .findById(userId)
                        .orElseThrow(
                                () -> new EntityNotFoundException(Customer.class.getSimpleName()));

        if (!settings.getMailboxPasswordHash().equals(HmacUtil.hash(request.currentPassword()))) {
            throw new UpdateConflictException();
        }

        settings.setMailboxPasswordHash(HmacUtil.hash(request.updatedPassword()));
        settings.setMailboxPassword(AesUtil.encrypt(request.updatedPassword()));

        return settingsRepository.save(settings);
    }
}
