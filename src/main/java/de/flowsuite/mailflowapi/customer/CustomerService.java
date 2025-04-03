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
        customer.setOpenaiApiKeyEnc(AesUtil.encrypt(customer.getOpenaiApiKeyEnc()));
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

    Customer updateCustomer(long id, Customer updatedCustomer, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(id, jwt);

        if (id != updatedCustomer.getId()) {
            throw new IdConflictException();
        }

        Customer customer =
                customerRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new EntityNotFoundException(Customer.class.getSimpleName()));

        if (!customer.getOpenaiApiKeyEnc().equals(updatedCustomer.getOpenaiApiKeyEnc())) {
            throw new UpdateConflictException();
        }

        return customerRepository.save(updatedCustomer);
    }
}
