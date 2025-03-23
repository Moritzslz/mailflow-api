package de.flowsuite.mailflowapi.security.recaptcha;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class ReCaptchaService {

    private static final Logger LOG = LoggerFactory.getLogger(ReCaptchaService.class);

    @Value("${google.recaptcha.secret-key}")
    private String secretKey;

    @Value("${google.recaptcha.verify-url}")
    private String verifyUrl;

    @Value("${google.recaptcha.threshold}")
    private double threshold;

    private final RestTemplate restTemplate;

    public ReCaptchaService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void verifyToken(String reCaptchaToken) {
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

        LOG.debug(recaptchaResponse.toString());

        if (!recaptchaResponse.success() || recaptchaResponse.score() <= threshold) {
            throw new InvalidReCaptchaTokenException();
        }
    }
}
