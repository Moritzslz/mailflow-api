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

    Settings updateSettings(long customerId, long userId, Settings updatedSettings) {
        AuthorisationUtil.checkCustomerAllowed(customerId);
        AuthorisationUtil.checkUserAllowed(userId);

        if (userId != updatedSettings.getUserId() || customerId != updatedSettings.getCustomerId()) {
            throw new IdConflictException();
        }

        Settings settings =
                settingsRepository
                        .findById(userId)
                        .orElseThrow(
                                () -> new EntityNotFoundException(Customer.class.getSimpleName()));

        if (!settings.getMailboxPassword().equals(updatedSettings.getMailboxPassword())) {
            updatedSettings.setMailboxPassword(AesUtil.encrypt(updatedSettings.getMailboxPassword()));
        }

        return settingsRepository.save(settings);
    }
}
