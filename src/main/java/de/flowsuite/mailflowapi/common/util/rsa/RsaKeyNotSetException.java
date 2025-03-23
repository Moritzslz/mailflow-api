package de.flowsuite.mailflowapi.common.util.rsa;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
class RsaKeyNotSetException extends RuntimeException {

    RsaKeyNotSetException(String rsaKeyName) {
        super(rsaKeyName + " has not been set.");
    }
}
