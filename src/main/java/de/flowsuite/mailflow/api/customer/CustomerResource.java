package de.flowsuite.mailflow.api.customer;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.flowsuite.mailflow.common.entity.Customer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/customers")
class CustomerResource {

    private final Logger LOG = LoggerFactory.getLogger(CustomerResource.class);
    private static final String NOTIFY_CUSTOMERS_URI = "/notifications/customers/{customerId}";

    private final CustomerService customerService;
    private final RestClient llmServiceRestClient;

    CustomerResource(
            CustomerService customerService,
            @Qualifier("llmServiceRestClient") RestClient llmServiceRestClient) {
        this.customerService = customerService;
        this.llmServiceRestClient = llmServiceRestClient;
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

    @GetMapping("/{id}/test-version")
    ResponseEntity<Boolean> getCustomerTestVersion(
            @PathVariable long id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(customerService.getCustomerTestVersion(id, jwt));
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
        Customer updatedCustomer = customerService.updateCustomer(id, customer, jwt);
        CompletableFuture.runAsync(() -> notifyLlmService(id, updatedCustomer));
        return ResponseEntity.ok(updatedCustomer);
    }

    @PutMapping("/{id}/test-version")
    ResponseEntity<Customer> updateCustomerTestVersion(
            @PathVariable long id, @RequestBody @Valid UpdateCustomerTestVersionRequest request) {
        // TODO notify mailbox service
        return ResponseEntity.ok(customerService.updateCustomerTestVersion(id, request));
    }

    private void notifyLlmService(long customerId, Customer customer) {
        LOG.debug("Notifying llm service of customer change");

        llmServiceRestClient
                .put()
                .uri(NOTIFY_CUSTOMERS_URI, customerId)
                .body(customer)
                .retrieve()
                .toBodilessEntity();
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
            boolean testVersion,
            @Email String ionosUsername,
            String ionosPassword,
            String defaultImapHost,
            String defaultSmtpHost,
            Integer defaultImapPort,
            Integer defaultSmtpPort) {}

    record UpdateCustomerTestVersionRequest(
            @NotNull Long id,
            boolean testVersion,
            @Email String ionosUsername,
            String ionosPassword) {}
}
