package de.flowsuite.mailflowapi.customer;

import de.flowsuite.mailflowapi.common.entity.Customer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
class CustomerResource {

    private final CustomerService customerService;

    CustomerResource(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    ResponseEntity<Customer> createCustomer(
            @RequestBody @Valid CreateCustomerRequest createCustomerRequest) {
        return ResponseEntity.ok(customerService.createCustomer(createCustomerRequest));
    }

    @GetMapping()
    ResponseEntity<List<Customer>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/{id}")
    ResponseEntity<GetCustomerResponse> getCustomerById(@PathVariable long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @PutMapping("/{id}")
    ResponseEntity<Customer> updateCustomer(
            @PathVariable long id, @RequestBody @Valid Customer customer) {
        return ResponseEntity.ok(customerService.updateCustomer(id, customer));
    }

    record CreateCustomerRequest(@Valid Customer customer, @NotBlank String openAiApiKey) {}

    record GetCustomerResponse(
            long id,
            String company,
            String street,
            String houseNumber,
            String postalCode,
            String city,
            String websiteUrl,
            String privacyPolicyUrl,
            String ctaUrl) {}
}
