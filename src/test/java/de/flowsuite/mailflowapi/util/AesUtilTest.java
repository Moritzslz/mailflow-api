package de.flowsuite.mailflowapi.util;

import static org.junit.jupiter.api.Assertions.*;

import de.flowsuite.mailflow.common.util.AesUtil;

import org.junit.jupiter.api.Test;

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
    void testDecrypt_nullEncryptedText_throwsException() {
        assertThrows(RuntimeException.class, () -> AesUtil.decrypt(null));
    }

    @Test
    void testDecrypt_invalidEncryptedText_throwsException() {
        assertThrows(RuntimeException.class, () -> AesUtil.decrypt(PLAIN_TEXT));
    }
}
