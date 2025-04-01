package de.flowsuite.mailflowapi.common.util.security;

import de.flowsuite.mailflowapi.common.exception.MissingEnvironmentVariableException;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AesUtil {

    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final GCMParameterSpec gcmParameterSpec = generateIV();
    private static final SecretKey key = loadAESKey();

    public AesUtil() {}

    private static SecretKey generateAESKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            return keyGenerator.generateKey();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static GCMParameterSpec generateIV() {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return new GCMParameterSpec(GCM_TAG_LENGTH, iv);
    }

    private static SecretKey loadAESKey() {
        String environmentVariable = "AES_B64_SECRET_KEY";
        String b64SecretKey = System.getenv(environmentVariable);
        if (b64SecretKey == null || b64SecretKey.isBlank()) {
            throw new MissingEnvironmentVariableException(environmentVariable);
        }

        byte[] decodedKey = Base64.getDecoder().decode(b64SecretKey);
        return new SecretKeySpec(decodedKey, "AES");
    }

    public static String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmParameterSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String decrypt(String encryptedText) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, gcmParameterSpec);
            byte[] decodedText = Base64.getDecoder().decode(encryptedText);
            byte[] plainText = cipher.doFinal(decodedText);
            return new String(plainText);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
