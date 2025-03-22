package de.flowsuite.mailflowapi.customersettings;

import de.flowsuite.mailflowapi.common.entity.CustomerSettings;
import de.flowsuite.mailflowapi.common.exception.IdMismatchException;
import de.flowsuite.mailflowapi.common.exception.NotFoundException;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customer-settings")
class CustomerSettingsResource {

    private final CustomerSettingsService customerSettingsService;

    public CustomerSettingsResource(CustomerSettingsService customerSettingsService) {
        this.customerSettingsService = customerSettingsService;
    }

    @PostMapping
    ResponseEntity<CustomerSettings> createCustomerSettings(
            @RequestBody @Valid CustomerSettings customerSettings) {
        return ResponseEntity.ok(customerSettingsService.createCustomerSettings(customerSettings));
    }

    @GetMapping("/{customerId}")
    ResponseEntity<CustomerSettings> getCustomerSettingsByCustomerId(
            @PathVariable long customerId) {
        return customerSettingsService
                .getCustomerSettingsByCustomerId(customerId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new NotFoundException(CustomerSettings.class.getSimpleName()));
    }

    @PutMapping("/{customerId}")
    ResponseEntity<CustomerSettings> updateCustomerSettings(
            @PathVariable long customerId, @RequestBody @Valid CustomerSettings customerSettings) {
        if (customerId != customerSettings.getCustomer().getId()) {
            throw new IdMismatchException(customerId, customerSettings.getCustomer().getId());
        } else {
            return ResponseEntity.ok(
                    customerSettingsService.updateCustomerSettings(customerSettings));
        }
    }
}
