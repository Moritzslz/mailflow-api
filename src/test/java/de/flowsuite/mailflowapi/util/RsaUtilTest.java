package de.flowsuite.mailflowapi.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.flowsuite.mailflowcommon.util.RsaUtil;

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
