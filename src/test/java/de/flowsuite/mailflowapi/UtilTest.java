package de.flowsuite.mailflowapi;

import static org.junit.jupiter.api.Assertions.*;

import de.flowsuite.mailflowapi.common.exception.InvalidEmailAddressException;
import de.flowsuite.mailflowapi.common.exception.InvalidUrlException;
import de.flowsuite.mailflowapi.common.util.Util;

import org.junit.jupiter.api.Test;

class UtilTest {

    @Test
    void testValidateEmailAddress_validEmails() {
        assertDoesNotThrow(() -> Util.validateEmailAddress("test@example.com"));
        assertDoesNotThrow(() -> Util.validateEmailAddress("user.name@sub.domain.org"));
        assertDoesNotThrow(() -> Util.validateEmailAddress("simple.email@domain.com"));
    }

    @Test
    void testValidateEmailAddress_invalidEmails() {
        assertThrows(InvalidEmailAddressException.class, () -> Util.validateEmailAddress(null));
        assertThrows(InvalidEmailAddressException.class, () -> Util.validateEmailAddress(""));
        assertThrows(
                InvalidEmailAddressException.class,
                () -> Util.validateEmailAddress("noatsymbol.com"));
        assertThrows(
                InvalidEmailAddressException.class, () -> Util.validateEmailAddress("nodomain@"));
        assertThrows(
                InvalidEmailAddressException.class, () -> Util.validateEmailAddress("@nodomain"));
        assertThrows(
                InvalidEmailAddressException.class, () -> Util.validateEmailAddress("blank@.com"));
        assertThrows(
                InvalidEmailAddressException.class,
                () -> Util.validateEmailAddress("blank@domain."));
        assertThrows(
                InvalidEmailAddressException.class,
                () -> Util.validateEmailAddress("blank@domain..com"));
        assertThrows(
                InvalidEmailAddressException.class,
                () -> Util.validateEmailAddress("blank@.domain.com"));
    }

    @Test
    void testValidateUrl_validUrls() {
        assertDoesNotThrow(() -> Util.validateUrl("https://example.com"));
        assertDoesNotThrow(() -> Util.validateUrl("https://sub.domain.org/path"));
    }

    @Test
    void testValidateUrl_invalidUrls() {
        assertThrows(InvalidUrlException.class, () -> Util.validateUrl(null));
        assertThrows(InvalidUrlException.class, () -> Util.validateUrl(""));
        assertThrows(InvalidUrlException.class, () -> Util.validateUrl("invalid-url"));
        assertThrows(InvalidUrlException.class, () -> Util.validateUrl("ftp://example.com"));
        assertThrows(InvalidUrlException.class, () -> Util.validateUrl("http://example.com"));
    }

    @Test
    void testGenerateRandomUrlSafeToken() {
        String token1 = Util.generateRandomUrlSafeToken();
        String token2 = Util.generateRandomUrlSafeToken();

        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2);

        assertTrue(token1.matches("^[A-Za-z0-9_-]+$"));
        assertTrue(token2.matches("^[A-Za-z0-9_-]+$"));
    }
}
