package de.flowsuite.mailflowapi.user;

import de.flowsuite.mailflowapi.common.entity.Authorities;
import de.flowsuite.mailflowapi.common.entity.User;
import de.flowsuite.mailflowapi.common.exception.EntityAlreadyExistsException;
import de.flowsuite.mailflowapi.common.exception.EntityNotFoundException;
import de.flowsuite.mailflowapi.common.exception.TokenExpiredException;
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

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);
    private static final int TOKEN_TTL_HOURS = 6;
    private static final int TOKEN_TTL_MINUTES = 30;
    private static final String ENABLE_USER_SUCCESS_MSG =
            "Your account has been enabled. You can close this window now.";
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

    private String generateVerificationToken() {
        String verificationToken;
        do {
            verificationToken = Util.generateRandomUrlSafeToken();
        } while (userRepository.existsByVerificationToken(verificationToken));
        return verificationToken;
    }

    User createUser(UserResource.CreateUserRequest request) {
        String emailAddress = request.emailAddress().toLowerCase();

        Util.validateEmailAddress(emailAddress);
        UserUtil.validatePassword(request.password(), request.confirmationPassword());

        String emailAddressHash = HmacUtil.hash(emailAddress);
        if (userRepository.existsByEmailAddressHash(emailAddressHash)) {
            throw new EntityAlreadyExistsException(User.class.getSimpleName());
        }

        String passwordHash = passwordEncoder.encode(request.password());

        String verificationToken = generateVerificationToken();
        ZonedDateTime tokenExpiresAt =
                ZonedDateTime.now(ZoneId.of("Europe/Berlin")).plusHours(TOKEN_TTL_HOURS);

        LOG.debug("Verification token: {}", verificationToken);

        String phoneNumberEncrypted = null;
        if (request.phoneNumber() != null && request.phoneNumber().isBlank()) {
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
                        .accountLocked(false)
                        .accountEnabled(false)
                        .subscribedToNewsletter(request.subscribedToNewsletter())
                        .verificationToken(verificationToken)
                        .tokenExpiresAt(tokenExpiresAt)
                        .build();

        mailService.sendDoubleOptInEmail(
                request.firstName(), emailAddress, verificationToken, TOKEN_TTL_HOURS);

        return userRepository.save(user);
    }

    String enableUser(String token) {
        Optional<User> optionalUser = userRepository.findByVerificationToken(token);

        if (optionalUser.isEmpty()) {
            throw new EntityNotFoundException(User.class.getSimpleName());
        }

        User user = optionalUser.get();
        boolean isEnabled = user.isEnabled();
        ZonedDateTime tokenExpiresAt = user.getTokenExpiresAt();

        if (tokenExpiresAt.isBefore(ZonedDateTime.now(ZoneId.of("Europe/Berlin"))) && !isEnabled) {
            // Token expired => delete user account (GDPR data minimisation)
            userRepository.delete(user);
            throw new TokenExpiredException(
                    "Your registration has expired. Please register again.");
        }

        if (!isEnabled) {
            user.setAccountEnabled(true);
            userRepository.save(user);

            String firstName = AesUtil.decrypt(user.getFirstName());
            String emailAddress = AesUtil.decrypt(user.getEmailAddress());

            mailService.sendWelcomeEmail(user.getId(), firstName, emailAddress);
        }

        return ENABLE_USER_SUCCESS_MSG;
    }
}
