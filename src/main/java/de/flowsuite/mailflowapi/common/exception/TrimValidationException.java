package de.flowsuite.mailflowapi.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class TrimValidationException extends RuntimeException {

    public TrimValidationException() {
        super("Fields must not contain leading or trailing whitespace.");
    }

    public TrimValidationException(String fieldName) {
        super(fieldName + " must not contain leading or trailing whitespace.");
    }
}
