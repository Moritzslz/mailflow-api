package de.flowsuite.mailflowapi.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class InvalidReCaptchaTokenException extends RuntimeException {

    public InvalidReCaptchaTokenException() {
        super("Invalid reCAPTCHA token: Verification failed or score too low.");
    }
}
