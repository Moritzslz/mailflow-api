package de.flowsuite.mailflowapi.customer;

import de.flowsuite.mailflowapi.common.entity.Customer;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    Optional<Customer> getCustomerById(long id) {
        return customerRepository.findById(id);
    }

    Customer updateCustomer(Customer customer) {
        return customerRepository.save(customer);
    }
}
