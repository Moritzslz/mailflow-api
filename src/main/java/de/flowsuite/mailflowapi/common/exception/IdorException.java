package de.flowsuite.mailflowapi.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN)
public class IdorException extends RuntimeException {

    public IdorException() {
        super("Access denied: Not authorised to access or modify this data.");
    }
}
