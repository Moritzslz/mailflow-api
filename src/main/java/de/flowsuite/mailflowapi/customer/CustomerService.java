package de.flowsuite.mailflowapi.customer;

import de.flowsuite.mailflow.common.entity.Customer;
import de.flowsuite.mailflow.common.exception.EntityAlreadyExistsException;
import de.flowsuite.mailflow.common.exception.EntityNotFoundException;
import de.flowsuite.mailflow.common.exception.IdConflictException;
import de.flowsuite.mailflow.common.exception.UpdateConflictException;
import de.flowsuite.mailflow.common.util.AesUtil;
import de.flowsuite.mailflow.common.util.AuthorisationUtil;
import de.flowsuite.mailflow.common.util.Util;

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
                        .build();

        if (request.ionosUsername() != null && !request.ionosUsername().isBlank()) {
            Util.validateEmailAddress(request.ionosUsername());
            customer.setIonosUsername(request.ionosUsername());
        }

        if (request.ionosPassword() != null && !request.ionosPassword().isBlank()) {
            customer.setIonosPassword(AesUtil.encrypt(request.ionosPassword()));
        }

        return customerRepository.save(customer);
    }

    List<Customer> listCustomers() {
        return (List<Customer>) customerRepository.findAll();
    }

    Customer getCustomer(long id, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(id, jwt);

        Customer customer =
                customerRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new EntityNotFoundException(Customer.class.getSimpleName()));

        if (customer.isTestVersion() && customer.getIonosPassword() != null) {
            customer.setIonosPassword(AesUtil.decrypt(customer.getIonosPassword()));
        }

        return customer;
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

        String billingEmailAddress = customer.getBillingEmailAddress().toLowerCase();

        if (!billingEmailAddress.equals(existingCustomer.getBillingEmailAddress())) {
            Util.validateEmailAddress(billingEmailAddress);

            if (customerRepository.existsByBillingEmailAddress(billingEmailAddress)) {
                throw new EntityAlreadyExistsException(Customer.class.getSimpleName());
            }

            customer.setBillingEmailAddress(billingEmailAddress);
        }

        customer.setTestVersion(existingCustomer.isTestVersion());

        if (existingCustomer.isTestVersion()) {
            customer.setIonosUsername(existingCustomer.getIonosUsername());
            customer.setIonosPassword(existingCustomer.getIonosPassword());
        } else {
            customer.setIonosUsername(null);
            customer.setIonosPassword(null);
        }

        return customerRepository.save(customer);
    }

    Customer updateCustomerTestVersion(
            long id, CustomerResource.UpdateCustomerTestVersionRequest request) {
        if (!request.id().equals(id)) {
            throw new IdConflictException();
        }

        String ionosUsername = request.ionosUsername();
        if (ionosUsername != null) {
            ionosUsername = ionosUsername.toLowerCase();
            Util.validateEmailAddress(ionosUsername);
        }

        Customer customer =
                customerRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new EntityNotFoundException(Customer.class.getSimpleName()));

        customer.setTestVersion(request.isTestVersion());

        if (request.isTestVersion()) {
            customer.setIonosUsername(ionosUsername);
            customer.setIonosPassword(AesUtil.encrypt(request.ionosPassword()));
        } else {
            customer.setIonosUsername(null);
            customer.setIonosPassword(null);
        }

        return customerRepository.save(customer);
    }
}
