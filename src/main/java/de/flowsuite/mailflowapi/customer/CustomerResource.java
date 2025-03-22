package de.flowsuite.mailflowapi.customer;

import de.flowsuite.mailflowapi.common.entity.Customer;
import de.flowsuite.mailflowapi.common.exception.NotFoundException;
import de.flowsuite.mailflowapi.common.exception.IdMismatchException;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
class CustomerResource {

    private final CustomerService customerService;

    CustomerResource(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    ResponseEntity<Customer> createCustomer(@RequestBody @Valid Customer customer) {
        return ResponseEntity.ok(customerService.createCustomer(customer));
    }

    @GetMapping()
    ResponseEntity<List<Customer>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/{id}")
    ResponseEntity<Customer> getCustomerById(@PathVariable long id) {
        Customer customer = customerService.getCustomerById(id);
        if (customer == null) {
            throw new NotFoundException(Customer.class.getSimpleName());
        } else {
            return ResponseEntity.ok(customer);
        }
    }

    @PutMapping("/{id}")
    ResponseEntity<Customer> updateCustomer(
            @PathVariable long id, @RequestBody @Valid Customer customer) {
        if (id != customer.getId()) {
            throw new IdMismatchException(id, customer.getId());
        } else {
            return ResponseEntity.ok(customerService.updateCustomer(customer));
        }
    }
}
