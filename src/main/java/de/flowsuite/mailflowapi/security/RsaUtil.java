package de.flowsuite.mailflowapi.security;

import de.flowsuite.mailflowapi.common.exception.MissingEnvironmentVariableException;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

class RsaUtil {

    static final RSAPublicKey publicKey = loadPublicKey();
    static final RSAPrivateKey privateKey = loadPrivateKey();

    public RsaUtil() {}

    private static RSAPrivateKey loadPrivateKey() {
        String environmentVariable = "RSA_PRIVATE_KEY";

        String key = System.getenv(environmentVariable);
        if (key == null || key.isBlank()) {
            throw new MissingEnvironmentVariableException(environmentVariable);
        }

        try {
            key =
                    key.replace("-----BEGIN PRIVATE KEY-----", "")
                            .replace("-----END PRIVATE KEY-----", "")
                            .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(key);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static RSAPublicKey loadPublicKey() {
        String environmentVariable = "RSA_PUBLIC_KEY";

        String key = System.getenv(environmentVariable);
        if (key == null || key.isBlank()) {
            throw new MissingEnvironmentVariableException(environmentVariable);
        }

        try {
            key =
                    key.replace("-----BEGIN PUBLIC KEY-----", "")
                            .replace("-----END PUBLIC KEY-----", "")
                            .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(key);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
