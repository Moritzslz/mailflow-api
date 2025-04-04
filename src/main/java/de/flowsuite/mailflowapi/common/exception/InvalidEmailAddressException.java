package de.flowsuite.mailflowapi.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class InvalidEmailAddressException extends RuntimeException {

    public InvalidEmailAddressException() {
        super("The email address is invalid.");
    }
}
