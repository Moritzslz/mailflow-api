package de.flowsuite.mailflowapi.user;

import de.flowsuite.mailflowapi.common.entity.Authorities;
import de.flowsuite.mailflowapi.common.entity.User;
import de.flowsuite.mailflowapi.common.exception.EntityExistsException;
import de.flowsuite.mailflowapi.common.util.AesUtil;
import de.flowsuite.mailflowapi.common.util.HmacUtil;
import de.flowsuite.mailflowapi.common.util.Util;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
            throw new EntityExistsException(User.class.getSimpleName());
        }

        String passwordHash = passwordEncoder.encode(request.password());

        String verificationToken = generateVerificationToken();
        ZonedDateTime tokenExpiresAt =
                ZonedDateTime.now(ZoneId.of("Europe/Berlin")).plusMinutes(30);

        User user =
                User.builder()
                        .customerId(request.customerId())
                        .firstName(AesUtil.encrypt(request.firstName()))
                        .lastName(AesUtil.encrypt(request.lastName()))
                        .emailAddressHash(emailAddressHash)
                        .emailAddress(AesUtil.encrypt(emailAddress))
                        .password(passwordHash)
                        .phoneNumber(request.phoneNumber())
                        .position(request.position())
                        .role(Authorities.USER.getAuthority())
                        .isAccountLocked(false)
                        .isAccountEnabled(false)
                        .isSubscribedToNewsletter(request.isSubscribedToNewsletter())
                        .verificationToken(verificationToken)
                        .tokenExpiresAt(tokenExpiresAt)
                        .build();

        // TODO send Double-Opt-In email

        return userRepository.save(user);
    }
}
