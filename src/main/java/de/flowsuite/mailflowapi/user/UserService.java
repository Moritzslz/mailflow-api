package de.flowsuite.mailflowapi.user;

import static de.flowsuite.mailflowapi.common.constant.Message.*;
import static de.flowsuite.mailflowapi.common.util.Util.BERLIN_ZONE;

import de.flowsuite.mailflowapi.common.constant.Authorities;
import de.flowsuite.mailflowapi.common.constant.Message;
import de.flowsuite.mailflowapi.common.entity.User;
import de.flowsuite.mailflowapi.common.exception.IdConflictException;
import de.flowsuite.mailflowapi.common.util.AesUtil;
import de.flowsuite.mailflowapi.common.util.AuthorisationUtil;
import de.flowsuite.mailflowapi.common.util.HmacUtil;
import de.flowsuite.mailflowapi.common.util.Util;
import de.flowsuite.mailflowapi.mail.MailService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);
    static final int TOKEN_TTL_HOURS = 6;
    static final int TOKEN_TTL_MINUTES = 30;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

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

    public void updateLastLoginAt(User user) {
        user.setLastLoginAt(ZonedDateTime.now(BERLIN_ZONE));
        userRepository.save(user);
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
                    ZonedDateTime.now(BERLIN_ZONE).plusHours(TOKEN_TTL_HOURS);

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

            if (tokenExpiresAt.isBefore(ZonedDateTime.now(BERLIN_ZONE)) && !isEnabled) {
                // Token expired => delete user account (GDPR data minimisation)
                userRepository.delete(user);
                mailService.sendRegistrationExpiredEmail(user.getId(), firstName, emailAddress);
                return new Message(ENABLE_USER_MSG);
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
                    ZonedDateTime.now(BERLIN_ZONE).plusMinutes(TOKEN_TTL_MINUTES);

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

            if (tokenExpiresAt.isBefore(ZonedDateTime.now(BERLIN_ZONE))) {
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

    List<User> listUsers() {
        return (List<User>) userRepository.findAll();
    }

    User getUser(long customerId, long id, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);
        AuthorisationUtil.validateAccessToUser(id, jwt);
        return getById(id);
    }

    User updateUser(long customerId, long id, UserResource.UpdateUserRequest request, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);
        AuthorisationUtil.validateAccessToUser(id, jwt);

        if (!request.userId().equals(id) || !request.customerId().equals(customerId)) {
            throw new IdConflictException();
        }

        User user = getById(id);

        user.setFirstName(AesUtil.encrypt(request.firstName()));
        user.setLastName(AesUtil.encrypt(request.lastName()));

        String phoneNumberEncrypted = null;
        if (request.phoneNumber() != null && !request.phoneNumber().isBlank()) {
            phoneNumberEncrypted = AesUtil.encrypt(request.phoneNumber());
        }

        user.setPhoneNumber(phoneNumberEncrypted);
        user.setPosition(request.position());
        user.setIsSubscribedToNewsletter(request.isSubscribedToNewsletter());

        return userRepository.save(user);
    }
}
