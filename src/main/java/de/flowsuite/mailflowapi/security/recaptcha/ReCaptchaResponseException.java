package de.flowsuite.mailflowapi.security.recaptcha;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
class ReCaptchaResponseException extends RuntimeException {

    ReCaptchaResponseException() {
        super("Failed to verify reCAPTCHA: No response from verification server.");
    }
}
