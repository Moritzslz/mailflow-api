package de.flowsuite.mailflow.api.security;

import de.flowsuite.mailflow.common.exception.InvalidReCaptchaTokenException;
import de.flowsuite.mailflow.common.exception.MissingReCaptchaTokenException;
import de.flowsuite.mailflow.common.exception.ReCaptchaResponseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
class ReCaptchaService {

    private static final Logger LOG = LoggerFactory.getLogger(ReCaptchaService.class);
    private final String secretKey;
    private final String verifyUrl;
    private final double threshold;
    private final RestTemplate restTemplate;

    ReCaptchaService(
            @Value("${google.recaptcha.secret-key}") String secretKey,
            @Value("${google.recaptcha.verify-url}") String verifyUrl,
            @Value("${google.recaptcha.threshold}") double threshold,
            RestTemplate restTemplate) {
        this.secretKey = secretKey;
        this.verifyUrl = verifyUrl;
        this.threshold = threshold;
        this.restTemplate = restTemplate;
    }

    void verifyToken(String reCaptchaToken) {
        if (reCaptchaToken == null || reCaptchaToken.isBlank()) {
            throw new MissingReCaptchaTokenException();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("secret", secretKey);
        map.add("response", reCaptchaToken);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        ResponseEntity<ReCaptchaResponse> response =
                restTemplate.exchange(verifyUrl, HttpMethod.POST, entity, ReCaptchaResponse.class);

        ReCaptchaResponse recaptchaResponse = response.getBody();

        if (recaptchaResponse == null) {
            throw new ReCaptchaResponseException();
        }

        LOG.debug("Google reCAPTCHA response: {}", recaptchaResponse);

        if (!recaptchaResponse.success() || recaptchaResponse.score() <= threshold) {
            throw new InvalidReCaptchaTokenException();
        }
    }

    private record ReCaptchaResponse(
            Boolean success, String hostname, Double score, String action) {}
}
