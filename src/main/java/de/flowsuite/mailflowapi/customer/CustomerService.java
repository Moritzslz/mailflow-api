package de.flowsuite.mailflowapi.customer;

import de.flowsuite.mailflowapi.common.entity.Customer;

import org.springframework.stereotype.Service;

@Service
class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    Customer createCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    Customer getCustomerById(long id) {
        return customerRepository.findById(id).orElse(null);
    }

    Customer updateCustomer(Customer customer) {
        return customerRepository.save(customer);
    }
}
