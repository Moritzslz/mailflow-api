package de.flowsuite.mailflowapi.user;

import de.flowsuite.mailflowapi.common.auth.Authorities;
import de.flowsuite.mailflowapi.common.dto.Message;
import de.flowsuite.mailflowapi.common.entity.User;
import de.flowsuite.mailflowapi.common.exception.EntityAlreadyExistsException;
import de.flowsuite.mailflowapi.common.util.AesUtil;
import de.flowsuite.mailflowapi.common.util.HmacUtil;
import de.flowsuite.mailflowapi.common.util.Util;
import de.flowsuite.mailflowapi.mail.MailService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    // spotless:off
    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);
    private static final int TOKEN_TTL_HOURS = 6;
    private static final int TOKEN_TTL_MINUTES = 30;
    private static final String CREATE_USER_MSG = "Please check your inbox to enable your account.";
    private static final String ENABLE_USER_MSG = "Thank you! Your account has been enabled.";
    private static final String REQUEST_PASSWORD_RESET_MSG = "A password reset link will be sent shortly.";
    private static final String COMPLETE_PASSWORD_RESET_MSG = "Your password has been updated successfully.";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    // spotless:on

    UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            MailService mailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    @Override
    public UserDetails loadUserByUsername(String emailAddress) throws UsernameNotFoundException {
        return getByEmailAddress(emailAddress);
    }

    public User getByEmailAddress(String emailAddress) {
        return userRepository
                .findByEmailAddressHash(HmacUtil.hash(emailAddress.toLowerCase()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }

    public User getById(long id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }

    private String generateVerificationToken() {
        String verificationToken;
        do {
            verificationToken = Util.generateRandomUrlSafeToken();
        } while (userRepository.existsByVerificationToken(verificationToken));
        return verificationToken;
    }

    Message createUser(UserResource.CreateUserRequest request) {
        String emailAddress = request.emailAddress().toLowerCase();

        Util.validateEmailAddress(emailAddress);
        UserUtil.validatePassword(request.password(), request.confirmationPassword());

        String emailAddressHash = HmacUtil.hash(emailAddress);
        if (!userRepository.existsByEmailAddressHash(emailAddressHash)) {
            String passwordHash = passwordEncoder.encode(request.password());

            String verificationToken = generateVerificationToken();
            ZonedDateTime tokenExpiresAt =
                    ZonedDateTime.now(ZoneId.of("Europe/Berlin")).plusHours(TOKEN_TTL_HOURS);

            LOG.debug("Creating new user.");
            LOG.debug("Verification token: {}", verificationToken);

            String phoneNumberEncrypted = null;
            if (request.phoneNumber() != null && !request.phoneNumber().isBlank()) {
                phoneNumberEncrypted = AesUtil.encrypt(request.phoneNumber());
            }

            User user =
                    User.builder()
                            .customerId(request.customerId())
                            .firstName(AesUtil.encrypt(request.firstName()))
                            .lastName(AesUtil.encrypt(request.lastName()))
                            .emailAddressHash(emailAddressHash)
                            .emailAddress(AesUtil.encrypt(emailAddress))
                            .password(passwordHash)
                            .phoneNumber(phoneNumberEncrypted)
                            .position(request.position())
                            .role(Authorities.USER.getAuthority())
                            .isAccountLocked(false)
                            .isAccountEnabled(false)
                            .isSubscribedToNewsletter(request.isSubscribedToNewsletter())
                            .verificationToken(verificationToken)
                            .tokenExpiresAt(tokenExpiresAt)
                            .build();

            userRepository.save(user);
            mailService.sendDoubleOptInEmail(
                    request.firstName(), emailAddress, verificationToken, TOKEN_TTL_HOURS);
        }

        return new Message(CREATE_USER_MSG);
    }

    Message enableUser(String token) {
        Optional<User> optionalUser = userRepository.findByVerificationToken(token);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            String firstName = AesUtil.decrypt(user.getFirstName());
            String emailAddress = AesUtil.decrypt(user.getEmailAddress());
            ZonedDateTime tokenExpiresAt = user.getTokenExpiresAt();
            boolean isEnabled = user.isEnabled();

            LOG.debug("Enabling user: {}", user.getId());

            if (tokenExpiresAt.isBefore(ZonedDateTime.now(ZoneId.of("Europe/Berlin")))
                    && !isEnabled) {
                // Token expired => delete user account (GDPR data minimisation)
                userRepository.delete(user);
                mailService.sendRegistrationExpiredEmail(user.getId(), firstName, emailAddress);
            }

            if (!isEnabled) {
                user.setIsAccountEnabled(true);
                userRepository.save(user);
                mailService.sendWelcomeEmail(user.getId(), firstName, emailAddress);
            }
        }

        return new Message(ENABLE_USER_MSG);
    }

    Message requestPasswordReset(UserResource.RequestPasswordResetRequest request) {
        String emailAddress = request.emailAddress().toLowerCase();

        Util.validateEmailAddress(emailAddress);
        String emailAddressHash = HmacUtil.hash(emailAddress);

        Optional<User> optionalUser = userRepository.findByEmailAddressHash(emailAddressHash);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            String firstName = AesUtil.decrypt(user.getFirstName());

            LOG.debug("Processing password reset request for user: {}", user.getId());

            String verificationToken = generateVerificationToken();
            ZonedDateTime tokenExpiresAt =
                    ZonedDateTime.now(ZoneId.of("Europe/Berlin")).plusMinutes(TOKEN_TTL_MINUTES);

            user.setVerificationToken(verificationToken);
            user.setTokenExpiresAt(tokenExpiresAt);

            userRepository.save(user);
            mailService.sendPasswordResetEmail(
                    user.getId(), firstName, emailAddress, verificationToken, TOKEN_TTL_MINUTES);
        }

        return new Message(REQUEST_PASSWORD_RESET_MSG);
    }

    Message completePasswordReset(String token, UserResource.CompletePasswordResetRequest request) {
        Optional<User> optionalUser = userRepository.findByVerificationToken(token);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            String firstName = AesUtil.decrypt(user.getFirstName());
            String emailAddress = AesUtil.decrypt(user.getEmailAddress());
            ZonedDateTime tokenExpiresAt = user.getTokenExpiresAt();

            LOG.debug("Updating password for user: {}", user.getId());

            if (tokenExpiresAt.isBefore(ZonedDateTime.now(ZoneId.of("Europe/Berlin")))) {
                mailService.sendPasswordResetExpiredEmail(user.getId(), firstName, emailAddress);
            } else {
                UserUtil.validatePassword(request.password(), request.confirmationPassword());
                String passwordHash = passwordEncoder.encode(request.password());

                user.setPassword(passwordHash);
                userRepository.save(user);
            }
        }

        return new Message(COMPLETE_PASSWORD_RESET_MSG);
    }
}
