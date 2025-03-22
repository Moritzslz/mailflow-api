package de.flowsuite.mailflowapi.customersettings;

import de.flowsuite.mailflowapi.common.entity.CustomerSettings;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
class CustomerSettingsService {

    private final CustomerSettingsRepository customerSettingsRepository;

    CustomerSettingsService(CustomerSettingsRepository customerSettingsRepository) {
        this.customerSettingsRepository = customerSettingsRepository;
    }

    CustomerSettings createCustomerSettings(CustomerSettings customerSettings) {
        return customerSettingsRepository.save(customerSettings);
    }

    Optional<CustomerSettings> getCustomerSettingsByCustomerId(long customerId) {
        return customerSettingsRepository.findById(customerId);
    }

    CustomerSettings updateCustomerSettings(CustomerSettings customerSettings) {
        return customerSettingsRepository.save(customerSettings);
    }
}
