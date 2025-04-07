package de.flowsuite.mailflowapi.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URI;

@Service
public class MailService {

    private static final Logger LOG = LoggerFactory.getLogger(MailService.class);
    private static final String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
    private static final String FROM_EMAIL_ADDRESS = "noreply@flow-suite.de";
    private static final String FROM_PERSONAL = "MailFlow";
    private static final String DOUBLE_OPT_IN_EMAIL_PATH = "classpath:templates/DoubleOptInEmail.html";
    private static final String RESET_PASSWORD_EMAIL_PATH = "classpath:templates/ResetPasswordEmail.html";
    private static final String RESET_PASSWORD_ERROR_EMAIL_PATH = "classpath:templates/ResetPasswordErrorEmail.html";
    private static final String WELCOME_EMAIL_PATH = "classpath:templates/WelcomeEmail.html";
    private static final String DOUBLE_OPT_IN_EMAIL_SUBJECT = "Bitte bestätigen Sie Ihre Registrierung bei MailFlow";
    private static final String RESET_PASSWORD_EMAIL_SUBJECT = "Setzen Sie Ihr Passwort zurück";
    private static final String RESET_PASSWORD_ERROR_EMAIL_SUBJECT = "Ihr Passwort konnte nicht zurückgesetzt werden";
    private static final String WELCOME_EMAIL_SUBJECT = "Willkommen bei MailFlow! 👋🏼";
    private final String mailFlowFrontendUrl;
    private final JavaMailSender mailSender;
    private final String doubleOptInEmail;
    private final String resetPasswordEmail;
    private final String resetPasswordErrorEmail;
    private final String welcomeEmail;

    public MailService(@Value("${mailflow.frontend.url}") String mailFlowFrontendUrl, JavaMailSender mailSender, ResourceLoader resourceLoader) {
        this.mailFlowFrontendUrl = mailFlowFrontendUrl;
        this.mailSender = mailSender;
        this.doubleOptInEmail = MailUtil.readFile(resourceLoader, DOUBLE_OPT_IN_EMAIL_PATH);
        this.resetPasswordEmail = MailUtil.readFile(resourceLoader, RESET_PASSWORD_EMAIL_PATH);
        this.resetPasswordErrorEmail = MailUtil.readFile(resourceLoader, RESET_PASSWORD_ERROR_EMAIL_PATH);
        this.welcomeEmail = MailUtil.readFile(resourceLoader, WELCOME_EMAIL_PATH);
    }

    private String replacePlaceholder(String template, String placeholder, String value) {
        return template.replaceAll("\\{\\{" + placeholder + "\\}\\}", value);
    }

    public void sendDoubleOptInEmail(String fistName, String emailAddress, String token, int minutes) {
        URI uri = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/customers/users/enable")
                .queryParam("token", token)
                .build()
                .toUri();

        String emailContent = replacePlaceholder(doubleOptInEmail, "TITLE", DOUBLE_OPT_IN_EMAIL_SUBJECT);
        emailContent = replacePlaceholder(emailContent, "FIRST_NAME", fistName);
        emailContent = replacePlaceholder(emailContent, "URL", uri.toString());
        emailContent = replacePlaceholder(emailContent, "MINUTES", String.valueOf(minutes));

        LOG.info("Sending Double-Opt-In email.");
        sendEmail(emailContent, emailAddress, DOUBLE_OPT_IN_EMAIL_SUBJECT);
    }

    public void sendResetPasswordEmail(String firstName, String emailAddress, String token, int minutes) {
        URI uri = UriComponentsBuilder.fromUriString(mailFlowFrontendUrl)
                .path("/reset-password")
                .queryParam("token", token)
                .build()
                .toUri();

        String emailContent = replacePlaceholder(resetPasswordEmail, "TITLE", RESET_PASSWORD_EMAIL_SUBJECT);
        emailContent = replacePlaceholder(emailContent, "FIRST_NAME", firstName);
        emailContent = replacePlaceholder(emailContent, "URL", uri.toString());
        emailContent = replacePlaceholder(emailContent, "MINUTES", String.valueOf(minutes));

        LOG.info("Sending reset password email.");
        sendEmail(emailContent, emailAddress, RESET_PASSWORD_EMAIL_SUBJECT);
    }

    public void sendResetPasswordErrorEmail(String firstName, String userEmail) {
        URI uri = UriComponentsBuilder.fromUriString(mailFlowFrontendUrl)
                .path("/reset-password/request")
                .build()
                .toUri();


        String emailContent = replacePlaceholder(resetPasswordErrorEmail, "TITLE", RESET_PASSWORD_ERROR_EMAIL_SUBJECT);
        emailContent = replacePlaceholder(emailContent, "FIRST_NAME", firstName);
        emailContent = replacePlaceholder(emailContent, "URL", uri.toString());

        LOG.info("Sending reset password error email.");
        sendEmail(emailContent, userEmail, RESET_PASSWORD_ERROR_EMAIL_SUBJECT);
    }

    public void sendWelcomeEmail(String firstName, String userEmail, int tokens) {
        String emailContent = replacePlaceholder(resetPasswordErrorEmail, "TITLE", WELCOME_EMAIL_SUBJECT);
        emailContent = replacePlaceholder(emailContent, "FIRST_NAME", firstName);

        LOG.info("Sending welcome email.");
        sendEmail(emailContent, userEmail, WELCOME_EMAIL_SUBJECT);
    }

    @Async
    protected void sendEmail(String emailContent, String emailAddress, String subject) {
        try {

            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "UTF-8");
            mimeMessageHelper.setText(emailContent, true);
            mimeMessageHelper.setTo(emailAddress);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setFrom(FROM_EMAIL_ADDRESS, FROM_PERSONAL);

            mailSender.send(mimeMessage);

            LOG.info("Email sent successfully.");

        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
