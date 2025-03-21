package de.flowsuite.mailflowapi.customer;

import de.flowsuite.mailflowapi.common.entity.Customer;

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
        return customerRepository.findById(id).orElse(null);
    }

    Customer updateCustomer(Customer customer) {
        return customerRepository.save(customer);
    }
}
