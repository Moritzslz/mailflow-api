package de.flowsuite.mailflow.api.customer;

import de.flowsuite.mailflow.api.messagecategory.MessageCategoryService;
import de.flowsuite.mailflow.common.entity.Customer;
import de.flowsuite.mailflow.common.exception.*;
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
    private final MessageCategoryService messageCategoryService;

    CustomerService(
            CustomerRepository customerRepository, MessageCategoryService messageCategoryService) {
        this.customerRepository = customerRepository;
        this.messageCategoryService = messageCategoryService;
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
                        .testVersion(request.testVersion())
                        .crawlFrequencyInHours(168)
                        .defaultImapHost(request.defaultImapHost())
                        .defaultSmtpHost(request.defaultSmtpHost())
                        .defaultImapPort(request.defaultImapPort())
                        .defaultSmtpPort(request.defaultImapPort())
                        .build();

        if (request.ionosUsername() != null && !request.ionosUsername().isBlank()) {
            Util.validateEmailAddress(request.ionosUsername());
            customer.setIonosUsername(request.ionosUsername());
        }

        if (request.ionosPassword() != null && !request.ionosPassword().isBlank()) {
            customer.setIonosPassword(AesUtil.encrypt(request.ionosPassword()));
        }

        Util.validateMailboxSettings(request.defaultImapHost(), request.defaultSmtpHost(),
                request.defaultImapPort(), request.defaultSmtpPort());

        Customer createdCustomer = customerRepository.save(customer);

        messageCategoryService.createDefaultMessageCategories(createdCustomer.getId());

        return createdCustomer;
    }

    List<Customer> listCustomers() {
        return (List<Customer>) customerRepository.findAll();
    }

    public Customer getCustomer(long id, Jwt jwt) {
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

    Customer updateCustomer(long id, Customer updatedCustomer, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(id, jwt);

        if (!updatedCustomer.getId().equals(id)) {
            throw new IdConflictException();
        }

        Customer existingCustomer =
                customerRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new EntityNotFoundException(Customer.class.getSimpleName()));

        if (!existingCustomer.getOpenaiApiKey().equals(updatedCustomer.getOpenaiApiKey())) {
            throw new UpdateConflictException();
        }

        String billingEmailAddress = updatedCustomer.getBillingEmailAddress().toLowerCase();

        if (!billingEmailAddress.equals(existingCustomer.getBillingEmailAddress())) {
            Util.validateEmailAddress(billingEmailAddress);

            if (customerRepository.existsByBillingEmailAddress(billingEmailAddress)) {
                throw new EntityAlreadyExistsException(Customer.class.getSimpleName());
            }

            updatedCustomer.setBillingEmailAddress(billingEmailAddress);
        }

        updatedCustomer.setTestVersion(existingCustomer.isTestVersion());

        if (existingCustomer.isTestVersion()) {
            updatedCustomer.setIonosUsername(existingCustomer.getIonosUsername());
            updatedCustomer.setIonosPassword(existingCustomer.getIonosPassword());
        } else {
            updatedCustomer.setIonosUsername(null);
            updatedCustomer.setIonosPassword(null);
        }

        if (updatedCustomer.getLastCrawlAt() != null) {
            if (existingCustomer.getLastCrawlAt() == null
                    || updatedCustomer.getLastCrawlAt().isAfter(existingCustomer.getLastCrawlAt())) {
                existingCustomer.setLastCrawlAt(updatedCustomer.getLastCrawlAt());
            }
        }
        if (updatedCustomer.getNextCrawlAt() != null) {
            if (existingCustomer.getNextCrawlAt() == null
                    || updatedCustomer.getNextCrawlAt().isAfter(existingCustomer.getNextCrawlAt())) {
                existingCustomer.setNextCrawlAt(updatedCustomer.getNextCrawlAt());
            }
        }

        return customerRepository.save(updatedCustomer);
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

        customer.setTestVersion(request.testVersion());

        if (request.testVersion()) {
            customer.setIonosUsername(ionosUsername);
            customer.setIonosPassword(AesUtil.encrypt(request.ionosPassword()));
        } else {
            customer.setIonosUsername(null);
            customer.setIonosPassword(null);
        }

        return customerRepository.save(customer);
    }
}
