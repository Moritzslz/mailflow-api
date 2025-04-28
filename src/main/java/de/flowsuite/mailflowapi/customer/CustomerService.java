package de.flowsuite.mailflowapi.customer;

import de.flowsuite.mailflowcommon.entity.Customer;
import de.flowsuite.mailflowcommon.exception.EntityAlreadyExistsException;
import de.flowsuite.mailflowcommon.exception.EntityNotFoundException;
import de.flowsuite.mailflowcommon.exception.IdConflictException;
import de.flowsuite.mailflowcommon.exception.UpdateConflictException;
import de.flowsuite.mailflowcommon.util.AesUtil;
import de.flowsuite.mailflowcommon.util.AuthorisationUtil;
import de.flowsuite.mailflowcommon.util.Util;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Optional<Customer> getByRegistrationToken(String registrationToken) {
        return customerRepository.findByRegistrationToken(registrationToken);
    }

    private String generateRegistrationToken() {
        String registrationToken;
        do {
            registrationToken = Util.generateRandomUrlSafeToken();
        } while (customerRepository.existsByRegistrationToken(registrationToken));
        return registrationToken;
    }

    Customer createCustomer(CustomerResource.CreateCustomerRequest request) {
        String billingEmailAddress = request.billingEmailAddress().toLowerCase();
        Util.validateEmailAddress(billingEmailAddress);

        if (customerRepository.existsByBillingEmailAddress(billingEmailAddress)) {
            throw new EntityAlreadyExistsException(Customer.class.getSimpleName());
        }

        if (request.websiteUrl() != null && !request.websiteUrl().isBlank()) {
            Util.validateUrl(request.websiteUrl());
        }
        if (request.privacyPolicyUrl() != null && !request.privacyPolicyUrl().isBlank()) {
            Util.validateUrl(request.privacyPolicyUrl());
        }
        if (request.ctaUrl() != null && !request.ctaUrl().isBlank()) {
            Util.validateUrl(request.ctaUrl());
        }

        String registrationToken = generateRegistrationToken();

        Customer customer =
                Customer.builder()
                        .company(request.company())
                        .street(request.street())
                        .houseNumber(request.houseNumber())
                        .postalCode(request.postalCode())
                        .city(request.city())
                        .billingEmailAddress(request.billingEmailAddress())
                        .openaiApiKey(AesUtil.encrypt(request.openaiApiKey()))
                        .sourceOfContact(request.sourceOfContact())
                        .websiteUrl(request.websiteUrl())
                        .privacyPolicyUrl(request.privacyPolicyUrl())
                        .ctaUrl(request.ctaUrl())
                        .registrationToken(registrationToken)
                        .isTestVersion(request.isTestVersion())
                        .ionosUsername(request.ionosUsername())
                        .build();

        return customerRepository.save(customer);
    }

    List<Customer> listCustomers() {
        return (List<Customer>) customerRepository.findAll();
    }

    Customer getCustomer(long id, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(id, jwt);

        return customerRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Customer.class.getSimpleName()));
    }

    Customer updateCustomer(long id, Customer customer, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(id, jwt);

        if (!customer.getId().equals(id)) {
            throw new IdConflictException();
        }

        Customer existingCustomer =
                customerRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new EntityNotFoundException(Customer.class.getSimpleName()));

        if (!existingCustomer.getOpenaiApiKey().equals(customer.getOpenaiApiKey())) {
            throw new UpdateConflictException();
        }

        return customerRepository.save(customer);
    }
}
