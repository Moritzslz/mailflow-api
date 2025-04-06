package de.flowsuite.mailflowapi.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class EntityExistsException extends RuntimeException {

    public EntityExistsException(String entityName) {
        super(entityName + " entity already exists.");
    }
}
