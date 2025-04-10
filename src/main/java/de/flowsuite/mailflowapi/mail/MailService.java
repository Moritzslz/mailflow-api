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

    // spotless:off
    private static final Logger LOG = LoggerFactory.getLogger(MailService.class);
    private static final String FROM_EMAIL_ADDRESS = "noreply@flow-suite.de";
    private static final String FROM_PERSONAL = "MailFlow";
    private static final String DOUBLE_OPT_IN_EMAIL_PATH = "classpath:templates/DoubleOptInEmail.html";
    private static final String RESET_PASSWORD_EMAIL_PATH = "classpath:templates/ResetPasswordEmail.html";
    private static final String RESET_PASSWORD_EXPIRED_EMAIL_PATH = "classpath:templates/ResetPasswordExpiredEmail.html";
    private static final String WELCOME_EMAIL_PATH = "classpath:templates/WelcomeEmail.html";
    private static final String DOUBLE_OPT_IN_EMAIL_SUBJECT = "Bitte bestätige deine Registrierung 🤝";
    private static final String RESET_PASSWORD_EMAIL_SUBJECT = "Dein Link zum Zurücksetzen deines Passwort 🔐";
    private static final String RESET_PASSWORD_EXPIRED_EMAIL_SUBJECT = "Dein Link zum Passwort Zurücksetzen ist abgelaufen ⏳";
    private static final String WELCOME_EMAIL_SUBJECT = "Willkommen bei MailFlow – schön, dass du dabei bist! 🥳";
    private final String contextPath;
    private final String mailFlowFrontendUrl;
    private final JavaMailSender mailSender;
    private final String doubleOptInEmail;
    private final String resetPasswordEmail;
    private final String resetPasswordExpiredEmail;
    private final String welcomeEmail;
    // spotless:on

    public MailService(
            @Value("${server.servlet.context-path}") String contextPath,
            @Value("${mailflow.frontend.url}") String mailFlowFrontendUrl,
            JavaMailSender mailSender,
            ResourceLoader resourceLoader) {
        this.contextPath = contextPath;
        this.mailFlowFrontendUrl = mailFlowFrontendUrl;
        this.mailSender = mailSender;
        this.doubleOptInEmail = MailUtil.readFile(resourceLoader, DOUBLE_OPT_IN_EMAIL_PATH);
        this.resetPasswordEmail = MailUtil.readFile(resourceLoader, RESET_PASSWORD_EMAIL_PATH);
        this.resetPasswordExpiredEmail =
                MailUtil.readFile(resourceLoader, RESET_PASSWORD_EXPIRED_EMAIL_PATH);
        this.welcomeEmail = MailUtil.readFile(resourceLoader, WELCOME_EMAIL_PATH);
    }

    public void sendDoubleOptInEmail(
            String fistName, String emailAddress, String token, int hours) {
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

        URI uri =
                UriComponentsBuilder.fromUriString(baseUrl + contextPath)
                        .path("/customers/users/enable")
                        .queryParam("token", token)
                        .build()
                        .toUri();

        String emailContent =
                MailUtil.replacePlaceholder(doubleOptInEmail, "TITLE", DOUBLE_OPT_IN_EMAIL_SUBJECT);
        emailContent = MailUtil.replacePlaceholder(emailContent, "FIRST_NAME", fistName);
        emailContent = MailUtil.replacePlaceholder(emailContent, "URL", uri.toString());
        emailContent = MailUtil.replacePlaceholder(emailContent, "HOURS", String.valueOf(hours));

        LOG.info("Sending Double-Opt-In email.");
        sendEmail(emailContent, emailAddress, DOUBLE_OPT_IN_EMAIL_SUBJECT);
    }

    public void sendResetPasswordEmail(
            long userId, String firstName, String emailAddress, String token, int minutes) {
        URI uri =
                UriComponentsBuilder.fromUriString(mailFlowFrontendUrl)
                        .path("/reset-password")
                        .queryParam("token", token)
                        .build()
                        .toUri();

        String emailContent =
                MailUtil.replacePlaceholder(
                        resetPasswordEmail, "TITLE", RESET_PASSWORD_EMAIL_SUBJECT);
        emailContent = MailUtil.replacePlaceholder(emailContent, "FIRST_NAME", firstName);
        emailContent = MailUtil.replacePlaceholder(emailContent, "URL", uri.toString());
        emailContent =
                MailUtil.replacePlaceholder(emailContent, "MINUTES", String.valueOf(minutes));

        LOG.info("Sending reset password email to: {}.", userId);
        sendEmail(emailContent, emailAddress, RESET_PASSWORD_EMAIL_SUBJECT);
    }

    public void sendResetPasswordExpiredEmail(long userId, String firstName, String emailAddress) {
        URI uri =
                UriComponentsBuilder.fromUriString(mailFlowFrontendUrl)
                        .path("/reset-password/request")
                        .build()
                        .toUri();

        String emailContent =
                MailUtil.replacePlaceholder(
                        resetPasswordExpiredEmail, "TITLE", RESET_PASSWORD_EXPIRED_EMAIL_SUBJECT);
        emailContent = MailUtil.replacePlaceholder(emailContent, "FIRST_NAME", firstName);
        emailContent = MailUtil.replacePlaceholder(emailContent, "URL", uri.toString());

        LOG.info("Sending reset password expired email to: {}.", userId);
        sendEmail(emailContent, emailAddress, RESET_PASSWORD_EXPIRED_EMAIL_SUBJECT);
    }

    public void sendWelcomeEmail(long userId, String firstName, String emailAddress) {
        URI uri = UriComponentsBuilder.fromUriString(mailFlowFrontendUrl).build().toUri();

        String emailContent =
                MailUtil.replacePlaceholder(welcomeEmail, "TITLE", WELCOME_EMAIL_SUBJECT);
        emailContent = MailUtil.replacePlaceholder(emailContent, "FIRST_NAME", firstName);
        emailContent = MailUtil.replacePlaceholder(emailContent, "URL", uri.toString());

        LOG.info("Sending welcome email to: {}.", userId);
        sendEmail(emailContent, emailAddress, WELCOME_EMAIL_SUBJECT);
    }

    @Async
    void sendEmail(String emailContent, String emailAddress, String subject) {
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
