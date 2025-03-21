package de.flowsuite.mailflowapi.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class IdMismatchException extends RuntimeException {

    public IdMismatchException(long pathId, long bodyId) {
        super("Path Id " + pathId + " does not equal body Id " + bodyId);
    }
}
