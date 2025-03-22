package de.flowsuite.mailflowapi.customersettings;

import de.flowsuite.mailflowapi.common.entity.CustomerSettings;
import de.flowsuite.mailflowapi.common.exception.IdMismatchException;
import de.flowsuite.mailflowapi.common.exception.NotFoundException;
import de.flowsuite.mailflowapi.common.util.CryptoUtil;

import org.springframework.stereotype.Service;

@Service
class CustomerSettingsService {

    private final CustomerSettingsRepository customerSettingsRepository;

    CustomerSettingsService(CustomerSettingsRepository customerSettingsRepository) {
        this.customerSettingsRepository = customerSettingsRepository;
    }

    CustomerSettings createCustomerSettings(CustomerSettings customerSettings) {
        customerSettings.setMailboxPassword(
                CryptoUtil.encrypt(customerSettings.getMailboxPassword()));
        return customerSettingsRepository.save(customerSettings);
    }

    CustomerSettings getCustomerSettingsByCustomerId(long customerId) {
        return customerSettingsRepository
                .findById(customerId)
                .orElseThrow(() -> new NotFoundException(CustomerSettings.class.getSimpleName()));
    }

    CustomerSettings updateCustomerSettings(long customerId, CustomerSettings customerSettings) {
        if (customerId != customerSettings.getCustomerId()) {
            throw new IdMismatchException(customerId, customerSettings.getCustomerId());
        } else {
            return customerSettingsRepository.save(customerSettings);
        }
    }
}
