package de.flowsuite.mailflowapi.common.util;

import de.flowsuite.mailflowapi.common.exception.RsaKeyNotSetException;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

@Component
public class RsaUtil {

    private static RSAPublicKey publicKey = null;
    private static RSAPrivateKey privateKey = null;

    private static String decodeRsaKey(String b64Key) {
        byte[] decodedKey = Base64.getDecoder().decode(b64Key);
        return new String(decodedKey, StandardCharsets.UTF_8);
    }

    public static void setPrivateKey(String b64PrivateKey) {
        try {
            String key = decodeRsaKey(b64PrivateKey);
            key =
                    key.replace("-----BEGIN PRIVATE KEY-----", "")
                            .replace("-----END PRIVATE KEY-----", "")
                            .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(key);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            privateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setPublicKey(String b64PublicKey) {
        try {
            String key = decodeRsaKey(b64PublicKey);
            key =
                    key.replace("-----BEGIN PUBLIC KEY-----", "")
                            .replace("-----END PUBLIC KEY-----", "")
                            .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(key);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static RSAPublicKey getPublicKey() {
        if (publicKey == null) {
            throw new RsaKeyNotSetException(RSAPrivateKey.class.getSimpleName());
        } else {
            return publicKey;
        }
    }

    public static RSAPrivateKey getPrivateKey() {
        if (privateKey == null) {
            throw new RsaKeyNotSetException(RSAPublicKey.class.getSimpleName());
        } else {
            return privateKey;
        }
    }

    public static String encrypt(String plainText, RSAPublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String decrypt(String encryptedText, RSAPrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
