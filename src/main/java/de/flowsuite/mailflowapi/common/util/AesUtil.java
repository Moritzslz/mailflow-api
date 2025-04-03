package de.flowsuite.mailflowapi.common.util;

import de.flowsuite.mailflowapi.common.exception.MissingEnvVarException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AesUtil {

    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final SecretKey key = loadAesKey();

    public AesUtil() {}

    private static GCMParameterSpec generateIV() {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return new GCMParameterSpec(GCM_TAG_LENGTH, iv);
    }

    private static SecretKey loadAesKey() {
        String envVar = "AES_B64_SECRET_KEY";

        String b64SecretKey = System.getenv(envVar);
        if (b64SecretKey == null || b64SecretKey.isBlank()) {
            throw new MissingEnvVarException(envVar);
        }

        byte[] decodedKey = Base64.getDecoder().decode(b64SecretKey);
        return new SecretKeySpec(decodedKey, "AES");
    }

    public static String encrypt(String plainText) {
        try {
            GCMParameterSpec gcmParameterSpec = generateIV();

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmParameterSpec);

            byte[] encrypted = cipher.doFinal(plainText.getBytes());

            byte[] ivAndCipherText = new byte[IV_LENGTH + encrypted.length];
            System.arraycopy(gcmParameterSpec.getIV(), 0, ivAndCipherText, 0, IV_LENGTH);
            System.arraycopy(encrypted, 0, ivAndCipherText, IV_LENGTH, encrypted.length);

            return Base64.getEncoder().encodeToString(ivAndCipherText);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String decrypt(String encryptedText) {
        try {
            byte[] ivAndCipherText = Base64.getDecoder().decode(encryptedText);

            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(ivAndCipherText, 0, iv, 0, IV_LENGTH);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            byte[] cipherText = new byte[ivAndCipherText.length - IV_LENGTH];
            System.arraycopy(ivAndCipherText, IV_LENGTH, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, gcmParameterSpec);
            byte[] plainText = cipher.doFinal(cipherText);

            return new String(plainText);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
