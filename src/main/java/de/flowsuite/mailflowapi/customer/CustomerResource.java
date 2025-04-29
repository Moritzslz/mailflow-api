package de.flowsuite.mailflowapi.customer;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.flowsuite.mailflowcommon.entity.Customer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
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
            @RequestBody @Valid CreateCustomerRequest request, UriComponentsBuilder uriBuilder) {
        Customer createdCustomer = customerService.createCustomer(request);

        URI location =
                uriBuilder.path("/customers/{id}").buildAndExpand(createdCustomer.getId()).toUri();

        return ResponseEntity.created(location).body(createdCustomer);
    }

    @GetMapping("/{id}")
    ResponseEntity<Customer> getCustomer(@PathVariable long id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(customerService.getCustomer(id, jwt));
    }

    @GetMapping()
    ResponseEntity<List<Customer>> listCustomers() {
        return ResponseEntity.ok(customerService.listCustomers());
    }

    @PutMapping("/{id}")
    ResponseEntity<Customer> updateCustomer(
            @PathVariable long id,
            @RequestBody @Valid Customer customer,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(customerService.updateCustomer(id, customer, jwt));
    }

    @PutMapping("/{id}/test-version")
    ResponseEntity<Customer> updateCustomerTestVersion(
            @PathVariable long id, @RequestBody @Valid UpdateCustomerTestVersionRequest request) {
        return ResponseEntity.ok(customerService.updateCustomerTestVersion(id, request));
    }

    record CreateCustomerRequest(
            @NotBlank String company,
            @NotBlank String street,
            @NotBlank String houseNumber,
            @NotBlank String postalCode,
            @NotBlank String city,
            @Email @NotBlank String billingEmailAddress,
            @NotBlank String openaiApiKey,
            @JsonIgnore String sourceOfContact,
            String websiteUrl,
            String privacyPolicyUrl,
            String ctaUrl,
            boolean isTestVersion,
            @Email String ionosUsername,
            String ionosPassword) {}

    record UpdateCustomerTestVersionRequest(
            @NotNull Long id,
            boolean isTestVersion,
            @Email String ionosUsername,
            String ionosPassword) {}
}
