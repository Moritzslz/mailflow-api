package de.flowsuite.mailflowapi.customer;

import de.flowsuite.mailflowapi.common.entity.Customer;
import de.flowsuite.mailflowapi.common.exception.EntityNotFoundException;
import de.flowsuite.mailflowapi.common.exception.IdConflictException;
import de.flowsuite.mailflowapi.common.util.security.AesUtil;
import de.flowsuite.mailflowapi.common.util.security.AuthorisationUtil;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
class CustomerService {

    private final CustomerRepository customerRepository;

    CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    Customer createCustomer(CustomerResource.CreateCustomerRequest createCustomerRequest) {
        Customer customer = createCustomerRequest.customer();
        customer.setOpenAiApiKey(AesUtil.encrypt(createCustomerRequest.openAiApiKey()));
        return customerRepository.save(customer);
    }

    List<Customer> getAllCustomers() {
        return (List<Customer>) customerRepository.findAll();
    }

    CustomerResource.GetCustomerResponse getCustomerById(long id) {
        AuthorisationUtil.checkCustomerAllowed(id);

        return customerRepository
                .findById(id)
                .map(this::mapToGetCustomerResponse)
                .orElseThrow(() -> new EntityNotFoundException(Customer.class.getSimpleName()));
    }

    Customer updateCustomer(long id, Customer updatedCustomer) {
        AuthorisationUtil.checkCustomerAllowed(id);

        if (id != updatedCustomer.getId()) {
            throw new IdConflictException();
        } else {
            Customer customer =
                    customerRepository
                            .findById(id)
                            .orElseThrow(
                                    () ->
                                            new EntityNotFoundException(
                                                    Customer.class.getSimpleName()));
            updatedCustomer.setOpenAiApiKey(customer.getOpenAiApiKey());
            return customerRepository.save(updatedCustomer);
        }
    }

    private CustomerResource.GetCustomerResponse mapToGetCustomerResponse(Customer customer) {
        return new CustomerResource.GetCustomerResponse(
                customer.getId(),
                customer.getCompany(),
                customer.getStreet(),
                customer.getHouseNumber(),
                customer.getPostalCode(),
                customer.getCity(),
                customer.getWebsiteUrl(),
                customer.getPrivacyPolicyUrl(),
                customer.getCtaUrl());
    }
}
