package de.flowsuite.mailflowapi.customersettings;

import de.flowsuite.mailflowapi.common.entity.CustomerSettings;

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
        return ResponseEntity.ok(
                customerSettingsService.getCustomerSettingsByCustomerId(customerId));
    }

    @PutMapping("/{customerId}")
    ResponseEntity<CustomerSettings> updateCustomerSettings(
            @PathVariable long customerId, @RequestBody @Valid CustomerSettings customerSettings) {
        return ResponseEntity.ok(
                customerSettingsService.updateCustomerSettings(customerId, customerSettings));
    }
}
