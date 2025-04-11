package de.flowsuite.mailflowapi.common.util;

import de.flowsuite.mailflowapi.common.exception.InvalidEmailAddressException;

import java.security.SecureRandom;
import java.util.Base64;

public class Util {

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
        String secondLevelDomain = domainParts[0];
        String topLevelDomain = domainParts[1];

        if (secondLevelDomain.isBlank() || topLevelDomain.isBlank()) {
            throw new InvalidEmailAddressException();
        }
    }

    public static String generateRandomUrlSafeToken() {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
