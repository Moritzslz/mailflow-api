package de.flowsuite.mailflowapi.customer;

import de.flowsuite.mailflowapi.common.entity.Customer;
import de.flowsuite.mailflowapi.common.exception.EntityNotFoundException;
import de.flowsuite.mailflowapi.common.exception.IdConflictException;
import de.flowsuite.mailflowapi.common.exception.InvalidValueException;
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

    Customer createCustomer(Customer customer) {
        customer.setOpenaiApiKey(AesUtil.encrypt(customer.getOpenaiApiKey()));
        return customerRepository.save(customer);
    }

    List<Customer> getAllCustomers() {
        return (List<Customer>) customerRepository.findAll();
    }

    Customer getCustomerById(long id) {
        AuthorisationUtil.checkCustomerAllowed(id);

        return customerRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Customer.class.getSimpleName()));
    }

    Customer updateCustomer(long id, Customer updatedCustomer) {
        AuthorisationUtil.checkCustomerAllowed(id);

        if (id != updatedCustomer.getId()) {
            throw new IdConflictException();
        }

        Customer customer =
                customerRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new EntityNotFoundException(Customer.class.getSimpleName()));

        if (!customer.getOpenaiApiKey().equals(updatedCustomer.getOpenaiApiKey())) {
            throw new InvalidValueException();
        }

        return customerRepository.save(updatedCustomer);
    }
}
