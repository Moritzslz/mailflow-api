package de.flowsuite.mailflowapi.security.recaptcha;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
class InvalidReCaptchaTokenException extends RuntimeException {

    InvalidReCaptchaTokenException() {
        super("Invalid reCAPTCHA token: Verification failed or score too low.");
    }
}
