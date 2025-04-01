package de.flowsuite.mailflowapi.settings;

import de.flowsuite.mailflowapi.common.entity.Settings;
import de.flowsuite.mailflowapi.common.exception.EntityNotFoundException;
import de.flowsuite.mailflowapi.common.exception.IdConflictException;
import de.flowsuite.mailflowapi.common.util.security.AuthorisationUtil;
import de.flowsuite.mailflowapi.common.util.security.RsaUtil;

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

        if (userId != settings.getUserId() || customerId != settings.getCustomer().getId()) {
            throw new IdConflictException();
        } else {
            settings.setMailboxPassword(
                    RsaUtil.encrypt(settings.getMailboxPassword(), RsaUtil.getPublicKey()));
            return settingsRepository.save(settings);
        }
    }

    Settings getSettings(long customerId, long userId) {
        AuthorisationUtil.checkCustomerAllowed(customerId);
        AuthorisationUtil.checkUserAllowed(userId);

        Settings settings =
                settingsRepository
                        .findById(userId)
                        .orElseThrow(
                                () -> new EntityNotFoundException(Settings.class.getSimpleName()));
        if (customerId != settings.getCustomer().getId()) {
            throw new IdConflictException();
        } else {
            return settings;
        }
    }

    Settings updateSettings(long customerId, long userId, Settings settings) {
        AuthorisationUtil.checkCustomerAllowed(customerId);
        AuthorisationUtil.checkUserAllowed(userId);

        if (userId != settings.getUserId() || customerId != settings.getCustomer().getId()) {
            throw new IdConflictException();
        } else {
            return settingsRepository.save(settings);
        }
    }
}
