package de.flowsuite.mailflowapi.customersettings;

import de.flowsuite.mailflowapi.common.entity.CustomerSettings;
import de.flowsuite.mailflowapi.common.exception.EntityNotFoundException;
import de.flowsuite.mailflowapi.common.exception.IdConflictException;
import de.flowsuite.mailflowapi.common.util.RsaUtil;

import org.springframework.stereotype.Service;

@Service
class CustomerSettingsService {

    private final CustomerSettingsRepository customerSettingsRepository;

    CustomerSettingsService(CustomerSettingsRepository customerSettingsRepository) {
        this.customerSettingsRepository = customerSettingsRepository;
    }

    CustomerSettings createCustomerSettings(CustomerSettings customerSettings) {
        customerSettings.setMailboxPassword(
                RsaUtil.encrypt(customerSettings.getMailboxPassword(), RsaUtil.getPublicKey()));
        return customerSettingsRepository.save(customerSettings);
    }

    CustomerSettings getCustomerSettingsByCustomerId(long customerId) {
        return customerSettingsRepository
                .findById(customerId)
                .map(
                        customerSettings -> {
                            customerSettings.setMailboxPassword(
                                    RsaUtil.decrypt(
                                            customerSettings.getMailboxPassword(),
                                            RsaUtil.getPrivateKey()));
                            return customerSettings;
                        })
                .orElseThrow(
                        () -> new EntityNotFoundException(CustomerSettings.class.getSimpleName()));
    }

    CustomerSettings updateCustomerSettings(long customerId, CustomerSettings customerSettings) {
        if (customerId != customerSettings.getCustomerId()) {
            throw new IdConflictException();
        } else {
            return customerSettingsRepository.save(customerSettings);
        }
    }
}
