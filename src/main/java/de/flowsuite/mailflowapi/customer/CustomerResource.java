package de.flowsuite.mailflowapi.customer;

import de.flowsuite.mailflowapi.common.entity.Customer;

import jakarta.validation.Valid;

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
    ResponseEntity<Customer> createCustomer(@RequestBody @Valid Customer customer, UriComponentsBuilder uriBuilder) {
        Customer createdCustomer = customerService.createCustomer(customer);

        URI location =
                uriBuilder
                        .path("/customers/{id}")
                        .buildAndExpand(createdCustomer.getId())
                        .toUri();

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
}
