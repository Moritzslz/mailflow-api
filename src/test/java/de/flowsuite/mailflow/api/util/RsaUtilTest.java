package de.flowsuite.mailflow.api.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.flowsuite.mailflow.common.util.RsaUtil;

import org.junit.jupiter.api.Test;

class RsaUtilTest {

    @Test
    void testLoadPrivateKey() {
        assertNotNull(RsaUtil.privateKey);
    }

    @Test
    void testLoadPublicKey() {
        assertNotNull(RsaUtil.publicKey);
    }
}
