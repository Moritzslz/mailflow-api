package de.flowsuite.mailflowapi.settings;

import de.flowsuite.mailflowapi.common.entity.Customer;
import de.flowsuite.mailflowapi.common.entity.Settings;
import de.flowsuite.mailflowapi.common.exception.EntityNotFoundException;
import de.flowsuite.mailflowapi.common.exception.IdConflictException;
import de.flowsuite.mailflowapi.common.exception.InvalidValueException;
import de.flowsuite.mailflowapi.common.util.security.AesUtil;
import de.flowsuite.mailflowapi.common.util.security.AuthorisationUtil;

import org.springframework.stereotype.Service;

@Service
class SettingsService {

    private final SettingsRepository settingsRepository;

    SettingsService(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    Settings createSettings(long customerId, long userId, Settings settings) {
        AuthorisationUtil.checkCustomerAllowed(customerId);
        AuthorisationUtil.checkUserAllowed(userId);

        if (userId != settings.getUserId() || customerId != settings.getCustomerId()) {
            throw new IdConflictException();
        }

        settings.setMailboxPassword(AesUtil.encrypt(settings.getMailboxPassword()));

        return settingsRepository.save(settings);
    }

    Settings getSettings(long customerId, long userId) {
        AuthorisationUtil.checkCustomerAllowed(customerId);
        AuthorisationUtil.checkUserAllowed(userId);

        return settingsRepository
                .findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(Settings.class.getSimpleName()));
    }

    Settings updateSettings(
            long customerId, long userId, SettingsResource.UpdateSettingsRequest request) {
        AuthorisationUtil.checkCustomerAllowed(customerId);
        AuthorisationUtil.checkUserAllowed(userId);

        if (userId != request.userId() || customerId != request.customerId()) {
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

        return settingsRepository.save(settings);
    }

    Settings updateMailboxPassword(
            long customerId, long userId, SettingsResource.UpdateMailboxPasswordRequest request) {
        AuthorisationUtil.checkCustomerAllowed(customerId);
        AuthorisationUtil.checkUserAllowed(userId);

        if (userId != request.userId() || customerId != request.customerId()) {
            throw new IdConflictException();
        }

        Settings settings =
                settingsRepository
                        .findById(userId)
                        .orElseThrow(
                                () -> new EntityNotFoundException(Customer.class.getSimpleName()));

        String encryptedCurrentPassword = AesUtil.encrypt(request.currentPassword());

        if (!settings.getMailboxPassword().equals(encryptedCurrentPassword)) {
            throw new InvalidValueException();
        }

        settings.setMailboxPassword(AesUtil.encrypt(request.updatedPassword()));

        return settingsRepository.save(settings);
    }
}
