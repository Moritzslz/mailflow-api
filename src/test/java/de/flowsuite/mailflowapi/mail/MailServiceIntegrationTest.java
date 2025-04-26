package de.flowsuite.mailflowapi.mail;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled
@SpringBootTest
class MailServiceIntegrationTest {

    private static final long USER_ID = 1L;
    private static final String FIRST_NAME = "Vorname";
    private static final String EMAIL_ADDRESS = "test@flow-suite.de";
    private static final String TOKEN = "token";
    private static final int TOKEN_TTL_HOURS = 6;
    private static final int TOKEN_TTL_MINUTES = 30;

    @Autowired private MailService mailService;

    @Test
    void testSendDoubleOptInEmail() {
        mailService.sendDoubleOptInEmail(FIRST_NAME, EMAIL_ADDRESS, TOKEN, TOKEN_TTL_HOURS);
    }

    @Test
    void testSendRegistrationExpiredEmail() {
        mailService.sendRegistrationExpiredEmail(USER_ID, FIRST_NAME, EMAIL_ADDRESS);
    }

    @Test
    void testSendPasswordResetEmail() {
        mailService.sendPasswordResetEmail(
                USER_ID, FIRST_NAME, EMAIL_ADDRESS, TOKEN, TOKEN_TTL_MINUTES);
    }

    @Test
    void testSendPasswordResetExpiredEmail() {
        mailService.sendPasswordResetExpiredEmail(USER_ID, FIRST_NAME, EMAIL_ADDRESS);
    }

    @Test
    void testSendWelcomeEmail() {
        mailService.sendWelcomeEmail(USER_ID, FIRST_NAME, EMAIL_ADDRESS);
    }
}
