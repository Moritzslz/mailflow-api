package de.flowsuite.mailflowapi.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class IdConflictException extends RuntimeException {

    public IdConflictException() {
        super("The provided Ids do not match the expected values.");
    }
}
