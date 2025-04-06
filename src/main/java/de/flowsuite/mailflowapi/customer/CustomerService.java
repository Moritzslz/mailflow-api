package de.flowsuite.mailflowapi.customer;

import de.flowsuite.mailflowapi.common.entity.Customer;
import de.flowsuite.mailflowapi.common.exception.EntityNotFoundException;
import de.flowsuite.mailflowapi.common.exception.IdConflictException;
import de.flowsuite.mailflowapi.common.exception.UpdateConflictException;
import de.flowsuite.mailflowapi.common.util.AesUtil;
import de.flowsuite.mailflowapi.common.util.AuthorisationUtil;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class CustomerService {

    private final CustomerRepository customerRepository;

    CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    Customer createCustomer(Customer customer) {
        customer.setOpenaiApiKey(AesUtil.encrypt(customer.getOpenaiApiKey()));
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
