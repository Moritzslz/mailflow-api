package de.flowsuite.mailflowapi.customer;

import de.flowsuite.mailflowapi.common.entity.Customer;
import de.flowsuite.mailflowapi.common.exception.IdMismatchException;
import de.flowsuite.mailflowapi.common.exception.NotFoundException;

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
                .orElseThrow(() -> new NotFoundException(Customer.class.getSimpleName()));
    }

    Customer updateCustomer(long id, Customer customer) {
        if (id != customer.getId()) {
            throw new IdMismatchException(id, customer.getId());
        } else {
            return customerRepository.save(customer);
        }
    }
}
