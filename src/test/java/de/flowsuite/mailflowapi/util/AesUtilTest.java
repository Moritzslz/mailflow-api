package de.flowsuite.mailflowapi.util;

import de.flowsuite.mailflowapi.common.util.AesUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AesUtilTest {

    private static final String PLAIN_TEXT = "This is a secret message";

    @Test
    void testEncryptAndDecrypt_success() {
        String encrypted = AesUtil.encrypt(PLAIN_TEXT);

        assertNotNull(encrypted);
        assertNotEquals(PLAIN_TEXT, encrypted);

        String decrypted = AesUtil.decrypt(encrypted);

        assertEquals(PLAIN_TEXT, decrypted);
    }

    @Test
    void testEncrypt_nullPlainText_throwsException() {
        assertThrows(RuntimeException.class, () -> AesUtil.encrypt(null));
    }

    @Test
    void testDecrypt_invalidData_throwsException() {
        String invalidEncryptedData = "invalidEncryptedText";
        assertThrows(RuntimeException.class, () -> AesUtil.decrypt(invalidEncryptedData));
    }
}
