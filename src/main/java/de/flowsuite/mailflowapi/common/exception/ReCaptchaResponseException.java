package de.flowsuite.mailflowapi.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_GATEWAY)
public class ReCaptchaResponseException extends RuntimeException {

    public ReCaptchaResponseException() {
        super("Failed to verify reCAPTCHA: No response from verification server.");
    }
}
