package de.flowsuite.mailflowapi.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class UpdateConflictException extends RuntimeException {

    public UpdateConflictException() {
        super("Unable to update due to a conflict with existing data.");
    }
}
