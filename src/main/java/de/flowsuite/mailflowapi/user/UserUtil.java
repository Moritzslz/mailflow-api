package de.flowsuite.mailflowapi.user;

import static de.flowsuite.mailflow.common.constant.Message.*;

import de.flowsuite.mailflow.common.exception.InvalidPasswordException;

import java.util.regex.Pattern;

class UserUtil {

    static final int MIN_PASSWORD_LENGTH = 14;
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHARACTER_PATTERN = Pattern.compile("[^a-zA-Z0-9]");

    static void validatePassword(String password, String confirmationPassword) {
        if (!password.equals(confirmationPassword)) {
            throw new InvalidPasswordException(PASSWORDS_DO_NOT_MATCH_MSG);
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new InvalidPasswordException(
                    String.format(PASSWORD_TOO_SHORT_MSG, MIN_PASSWORD_LENGTH));
        }

        boolean hasUppercase = UPPERCASE_PATTERN.matcher(password).find();
        boolean hasLowercase = LOWERCASE_PATTERN.matcher(password).find();
        boolean hasDigit = DIGIT_PATTERN.matcher(password).find();
        boolean hasSpecialCharacter = SPECIAL_CHARACTER_PATTERN.matcher(password).find();

        if (!hasUppercase || !hasLowercase) {
            throw new InvalidPasswordException(MISSING_CASE_MSG);
        }

        if (!hasDigit) {
            throw new InvalidPasswordException(MISSING_DIGIT_MSG);
        }

        if (!hasSpecialCharacter) {
            throw new InvalidPasswordException(MISSING_SPECIAL_CHARACTER_MSG);
        }
    }
}
