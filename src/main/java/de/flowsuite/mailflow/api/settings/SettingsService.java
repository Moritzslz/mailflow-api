package de.flowsuite.mailflow.api.settings;

import de.flowsuite.mailflow.api.customer.CustomerService;
import de.flowsuite.mailflow.common.entity.Customer;
import de.flowsuite.mailflow.common.entity.Settings;
import de.flowsuite.mailflow.common.exception.*;
import de.flowsuite.mailflow.common.util.AesUtil;
import de.flowsuite.mailflow.common.util.AuthorisationUtil;
import de.flowsuite.mailflow.common.util.HmacUtil;
import de.flowsuite.mailflow.common.util.Util;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
class SettingsService {

    private final SettingsRepository settingsRepository;
    private final CustomerService customerService;

    SettingsService(SettingsRepository settingsRepository, CustomerService customerService) {
        this.settingsRepository = settingsRepository;
        this.customerService = customerService;
    }

    Settings createSettings(
            long customerId, long userId, SettingsResource.CreateSettingsRequest request, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);
        AuthorisationUtil.validateAccessToUser(userId, jwt);

        if (!request.userId().equals(userId) || !request.customerId().equals(customerId)) {
            throw new IdConflictException();
        }

        if (settingsRepository.existsByUserId(userId)) {
            throw new EntityAlreadyExistsException(Settings.class.getSimpleName());
        }

        Customer customer = customerService.getCustomer(customerId, jwt);

        Settings settings = new Settings();
        settings.setUserId(userId);
        settings.setCustomerId(customerId);
        settings.setExecutionEnabled(true);
        settings.setAutoReplyEnabled(false);
        settings.setResponseRatingEnabled(true);
        settings.setMoveToManualReviewEnabled(true);
        settings.setMailboxPasswordHash(HmacUtil.hash(request.mailboxPassword()));
        settings.setMailboxPassword(AesUtil.encrypt(request.mailboxPassword()));
        settings.setImapHost(customer.getDefaultImapHost());
        settings.setSmtpHost(customer.getDefaultSmtpHost());
        settings.setImapPort(customer.getDefaultImapPort());
        settings.setSmtpPort(customer.getDefaultSmtpPort());

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

        settings.setExecutionEnabled(request.executionEnabled());
        settings.setAutoReplyEnabled(request.autoReplyEnabled());
        settings.setResponseRatingEnabled(request.responseRatingEnabled());
        settings.setMoveToManualReviewEnabled(request.moveToManualReviewEnabled());

        Customer customer = customerService.getCustomer(customerId, jwt);

        if (!customer.isTestVersion()) {
            settings.setImapHost(request.imapHost());
            settings.setSmtpHost(request.smtpHost());
            settings.setImapPort(request.imapPort());
            settings.setSmtpPort(request.smtpPort());
        }

        Util.validateMailboxSettings(
                request.imapHost(), request.smtpHost(), request.imapPort(), request.smtpPort());

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
