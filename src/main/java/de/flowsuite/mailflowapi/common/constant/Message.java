package de.flowsuite.mailflowapi.common.constant;

import jakarta.validation.constraints.NotBlank;

public record Message(@NotBlank String message) {
    // spotless:off
    public static final String CREATE_USER_MSG =
            "Your account has been created. Please check your inbox to enable your account.";
    public static final String ENABLE_USER_MSG =
            "Your account has been enabled.";
    public static final String REQUEST_PASSWORD_RESET_MSG =
            "A password reset link will be sent shortly.";
    public static final String COMPLETE_PASSWORD_RESET_MSG =
            "Your password has been updated successfully.";
    public static final String PASSWORDS_DO_NOT_MATCH_MSG =
            "The passwords do not match.";
    public static final String PASSWORD_TOO_SHORT_MSG =
            "The password must be at least %d characters long.";
    public static final String MISSING_CASE_MSG =
            "The password must contain at least one uppercase and one lowercase letter.";
    public static final String MISSING_DIGIT_MSG =
            "The password must contain at least one digit.";
    public static final String MISSING_SPECIAL_CHARACTER_MSG =
            "The password must contain at least one special character.";
    // spotless:on
}
