package de.flowsuite.mailflowapi.security.recaptcha;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
class MissingReCaptchaTokenException extends RuntimeException {

    MissingReCaptchaTokenException() {
        super("Missing reCAPTCHA token in the request header.");
    }
}
