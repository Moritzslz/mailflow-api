package de.flowsuite.mailflowapi.mail;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Autowired private MailService mailService;
    private static final long USER_ID = 1L;
    private static final String FIRST_NAME = "Test";
    private static final String EMAIL_ADDRESS = "test@flow-suite.de";
    private static final String TOKEN = "someToken";

    @Test
    @Disabled
    void testSendDoubleOptInEmail() {
        mailService.sendDoubleOptInEmail(USER_ID, FIRST_NAME, EMAIL_ADDRESS, TOKEN, 6);
    }

    @Test
    @Disabled
    void testSendPasswordResetEmail() {
        mailService.sendPasswordResetEmail(USER_ID, FIRST_NAME, EMAIL_ADDRESS, TOKEN, 30);
    }

    @Test
    @Disabled
    void testSendPasswordResetExpiredEmail() {
        mailService.sendPasswordResetExpiredEmail(USER_ID, FIRST_NAME, EMAIL_ADDRESS);
    }

    @Test
    @Disabled
    void testSendWelcomeEmail() {
        mailService.sendWelcomeEmail(USER_ID, FIRST_NAME, EMAIL_ADDRESS);
    }
}
