package de.flowsuite.mailflowapi.customer;

import de.flowsuite.mailflowapi.common.entity.Customer;
import de.flowsuite.mailflowapi.common.exception.EntityNotFoundException;
import de.flowsuite.mailflowapi.common.exception.IdConflictException;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
class CustomerService {

    private final CustomerRepository customerRepository;

    CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    Customer createCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    List<Customer> getAllCustomers() {
        return (List<Customer>) customerRepository.findAll();
    }

    Customer getCustomerById(long id) {
        return customerRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Customer.class.getSimpleName()));
    }

    Customer updateCustomer(long id, Customer customer) {
        if (id != customer.getId()) {
            throw new IdConflictException();
        } else {
            return customerRepository.save(customer);
        }
    }
}
