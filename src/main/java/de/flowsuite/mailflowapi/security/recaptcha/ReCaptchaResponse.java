package de.flowsuite.mailflowapi.security.recaptcha;

record ReCaptchaResponse(Boolean success, String hostname, Double score, String action) {}
