package de.flowsuite.mailflowapi.common.util;

import de.flowsuite.mailflowapi.common.exception.InvalidEmailAddressException;
import de.flowsuite.mailflowapi.common.exception.InvalidPasswordException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Util {

    private static final int MIN_PASSWORD_LENGTH = 14;
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHARACTER_PATTERN = Pattern.compile("[^a-zA-Z0-9]");

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

    public static void validatePassword(String password, String confirmationPassword) {
        if (!password.equals(confirmationPassword)) {
            throw new InvalidPasswordException("The passwords do not match.");
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new InvalidPasswordException(
                    "The password must be at least " + MIN_PASSWORD_LENGTH + " characters long.");
        }

        boolean hasUppercase = UPPERCASE_PATTERN.matcher(password).find();
        boolean hasLowercase = LOWERCASE_PATTERN.matcher(password).find();
        boolean hasDigit = DIGIT_PATTERN.matcher(password).find();
        boolean hasSpecialCharacter = SPECIAL_CHARACTER_PATTERN.matcher(password).find();

        if (!hasUppercase || !hasLowercase) {
            throw new InvalidPasswordException(
                    "The password must contain at least one uppercase and one lowercase letter.");
        }

        if (!hasDigit) {
            throw new InvalidPasswordException("The password must contain at least one digit.");
        }

        if (!hasSpecialCharacter) {
            throw new InvalidPasswordException(
                    "The password must contain at least one special character.");
        }
    }
}
