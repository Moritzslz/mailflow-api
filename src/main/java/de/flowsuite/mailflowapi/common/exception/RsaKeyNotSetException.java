package de.flowsuite.mailflowapi.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class RsaKeyNotSetException extends RuntimeException {

    public RsaKeyNotSetException(String rsaKeyName) {
        super(rsaKeyName + " has not been set.");
    }
}
