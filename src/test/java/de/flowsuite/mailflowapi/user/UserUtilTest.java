package de.flowsuite.mailflowapi.user;

import static de.flowsuite.mailflow.common.constant.Message.*;
import static de.flowsuite.mailflowapi.user.UserUtil.*;

import static org.junit.jupiter.api.Assertions.*;

import de.flowsuite.mailflow.common.exception.InvalidPasswordException;

import org.junit.jupiter.api.Test;

class UserUtilTest {

    @Test
    void testValidatePassword_success() {
        String password = "ValidPassword123!";
        assertDoesNotThrow(() -> UserUtil.validatePassword(password, password));
    }

    @Test
    void testValidatePassword_passwordsDoNotMatch() {
        InvalidPasswordException exception =
                assertThrows(
                        InvalidPasswordException.class,
                        () -> UserUtil.validatePassword("Password123!", "Different123!"));
        assertEquals(PASSWORDS_DO_NOT_MATCH_MSG, exception.getMessage());
    }

    @Test
    void testValidatePassword_tooShort() {
        InvalidPasswordException exception =
                assertThrows(
                        InvalidPasswordException.class,
                        () -> UserUtil.validatePassword("Short1!", "Short1!"));
        assertEquals(
                String.format(PASSWORD_TOO_SHORT_MSG, MIN_PASSWORD_LENGTH), exception.getMessage());
    }

    @Test
    void testValidatePassword_missingUppercase() {
        String password = "invalid_password123";
        InvalidPasswordException exception =
                assertThrows(
                        InvalidPasswordException.class,
                        () -> UserUtil.validatePassword(password, password));
        assertEquals(MISSING_CASE_MSG, exception.getMessage());
    }

    @Test
    void testValidatePassword_missingLowercase() {
        String password = "INVALID_PASSWORD123";
        InvalidPasswordException exception =
                assertThrows(
                        InvalidPasswordException.class,
                        () -> UserUtil.validatePassword(password, password));
        assertEquals(MISSING_CASE_MSG, exception.getMessage());
    }

    @Test
    void testValidatePassword_missingDigit() {
        String password = "InvalidPassword!";
        InvalidPasswordException exception =
                assertThrows(
                        InvalidPasswordException.class,
                        () -> UserUtil.validatePassword(password, password));
        assertEquals(MISSING_DIGIT_MSG, exception.getMessage());
    }

    @Test
    void testValidatePassword_missingSpecialCharacter() {
        String password = "InvalidPassword123";
        InvalidPasswordException exception =
                assertThrows(
                        InvalidPasswordException.class,
                        () -> UserUtil.validatePassword(password, password));
        assertEquals(MISSING_SPECIAL_CHARACTER_MSG, exception.getMessage());
    }
}
