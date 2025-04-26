package de.flowsuite.mailflowapi.common.util;

import de.flowsuite.mailflowapi.common.exception.InvalidEmailAddressException;
import de.flowsuite.mailflowapi.common.exception.InvalidUrlException;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.time.ZoneId;
import java.util.Base64;

public class Util {

    public static final ZoneId BERLIN_ZONE = ZoneId.of("Europe/Berlin");

    public static void validateEmailAddress(String emailAddress) {
        if (emailAddress == null
                || emailAddress.isBlank()
                || !(emailAddress.contains("@") && emailAddress.contains("."))) {
            throw new InvalidEmailAddressException();
        }

        String[] emailAddressParts = emailAddress.split("@");
        if (emailAddressParts.length != 2) {
            throw new InvalidEmailAddressException();
        }

        String localPart = emailAddressParts[0];
        if (localPart.isBlank()) {
            throw new InvalidEmailAddressException();
        }

        String domain = emailAddressParts[1];
        String[] domainParts = domain.split("\\.");

        if (domainParts.length == 1) {
            throw new InvalidEmailAddressException();
        }

        for (String part : domainParts) {
            if (part.isBlank()) {
                throw new InvalidEmailAddressException();
            }
        }

        String secondLevelDomain = domainParts[0];
        String topLevelDomain = domainParts[1];

        if (secondLevelDomain.isBlank() || topLevelDomain.isBlank()) {
            throw new InvalidEmailAddressException();
        }
    }

    public static void validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new InvalidUrlException("URL must not be blank.");
        }

        URL parsedUrl;
        try {
            parsedUrl = new URL(url);
        } catch (MalformedURLException e) {
            throw new InvalidUrlException("The provided URL is invalid: " + e.getMessage());
        }

        if (!"https".equalsIgnoreCase(parsedUrl.getProtocol())) {
            throw new InvalidUrlException("Only https URLs are allowed.");
        }
    }

    public static String generateRandomUrlSafeToken() {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
