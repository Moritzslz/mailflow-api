package de.flowsuite.mailflowapi.customer;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
class CustomerNotFoundException extends RuntimeException {

    CustomerNotFoundException(long id) {
        super("Customer not found for Id: " + id);
    }
}
