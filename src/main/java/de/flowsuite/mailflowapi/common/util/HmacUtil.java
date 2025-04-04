package de.flowsuite.mailflowapi.common.util;

import de.flowsuite.mailflowapi.common.exception.MissingEnvVarException;

import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class HmacUtil {

    private static final SecretKey key = loadHmacKey();

    private static SecretKey loadHmacKey() {
        String envVar = "HMAC_B64_SECRET_KEY";

        String b64SecretKey = System.getenv(envVar);
        if (b64SecretKey == null || b64SecretKey.isBlank()) {
            throw new MissingEnvVarException(envVar);
        }

        byte[] decodedKey = Base64.getDecoder().decode(b64SecretKey);
        return new SecretKeySpec(decodedKey, "HmacSHA256");
    }

    public static String hash(String input) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            byte[] hash = mac.doFinal(input.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        String adminEmail = "test@flow-suite.de";
        String userEmail = "user@flow-suite.de";
        System.out.printf("%s: %s\n", adminEmail, hash(adminEmail));
        System.out.printf("%s: %s\n", userEmail, hash(userEmail));
    }
}
