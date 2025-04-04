package de.flowsuite.mailflowapi.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class MissingReCaptchaTokenException extends RuntimeException {

    public MissingReCaptchaTokenException() {
        super("Missing reCAPTCHA token in the request header.");
    }
}
