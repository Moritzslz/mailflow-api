package de.flowsuite.mailflowapi.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.flowsuite.mailflowapi.common.auth.Authorities;
import de.flowsuite.mailflowapi.common.dto.Message;
import de.flowsuite.mailflowapi.common.entity.User;
import de.flowsuite.mailflowapi.common.exception.IdConflictException;
import de.flowsuite.mailflowapi.common.exception.IdorException;
import de.flowsuite.mailflowapi.common.util.AesUtil;
import de.flowsuite.mailflowapi.common.util.AuthorisationUtil;
import de.flowsuite.mailflowapi.common.util.HmacUtil;
import de.flowsuite.mailflowapi.mail.MailService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String ENCRYPTED_VALUE = "encrypted-value";
    private static final String DECRYPTED_VALUE = "decrypted-value";
    private static final String HASHED_VALUE = "hashed-value";
    private static final String VERIFICATION_TOKEN = "verification-token";

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private MailService mailService;

    @InjectMocks private UserService userService;

    private MockedStatic<AesUtil> aesUtilMock;
    private MockedStatic<AuthorisationUtil> authUtilMock;
    private MockedStatic<HmacUtil> hmacUtilMock;
    private Jwt jwtMock;
    private User testUser;

    private static final UserResource.CreateUserRequest createUserRequest =
            new UserResource.CreateUserRequest(
                    100L,
                    "Rick",
                    "Sanchez",
                    "rick.sanchez@test.de",
                    "Password123456789!",
                    "Password123456789!",
                    "0123456789",
                    "Smartest man in the universe.",
                    true);

    private static final UserResource.RequestPasswordResetRequest requestPasswordResetRequest =
            new UserResource.RequestPasswordResetRequest(createUserRequest.emailAddress());

    private static final UserResource.CompletePasswordResetRequest completePasswordResetRequest =
            new UserResource.CompletePasswordResetRequest(
                    "strongPassword!123", "strongPassword!123");

    private User buildTestUser() {
        return User.builder()
                .id(100L)
                .customerId(createUserRequest.customerId())
                .firstName(ENCRYPTED_VALUE)
                .lastName(ENCRYPTED_VALUE)
                .emailAddressHash(HASHED_VALUE)
                .emailAddress(ENCRYPTED_VALUE)
                .password(HASHED_VALUE)
                .phoneNumber(ENCRYPTED_VALUE)
                .position(createUserRequest.position())
                .role(Authorities.USER.getAuthority())
                .isAccountLocked(false)
                .isAccountEnabled(false)
                .isSubscribedToNewsletter(createUserRequest.isSubscribedToNewsletter())
                .verificationToken(VERIFICATION_TOKEN)
                .tokenExpiresAt(ZonedDateTime.now().plusMinutes(30))
                .build();
    }

    @BeforeEach
    void setup() {
        testUser = buildTestUser();
        aesUtilMock = mockStatic(AesUtil.class);
        hmacUtilMock = mockStatic(HmacUtil.class);
        jwtMock = mock(Jwt.class);
        when(AesUtil.encrypt(anyString())).thenReturn(ENCRYPTED_VALUE);
        when(AesUtil.decrypt(anyString())).thenReturn(DECRYPTED_VALUE);
        when(HmacUtil.hash(anyString())).thenReturn(HASHED_VALUE);
    }

    @AfterEach
    void tearDown() {
        aesUtilMock.close();
        hmacUtilMock.close();
    }

    @Test
    void testCreateUser_success() {
        when(passwordEncoder.encode(anyString())).thenReturn(HASHED_VALUE);
        when(userRepository.existsByEmailAddressHash(anyString())).thenReturn(false);

        Message message = userService.createUser(createUserRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        verify(mailService)
                .sendDoubleOptInEmail(
                        eq(createUserRequest.firstName()),
                        eq(createUserRequest.emailAddress()),
                        eq(savedUser.getVerificationToken()),
                        anyInt());

        assertEquals(UserService.CREATE_USER_MSG, message.message());
        assertFalse(savedUser.isEnabled());
        assertTrue(
                savedUser
                        .getTokenExpiresAt()
                        .isAfter(
                                ZonedDateTime.now()
                                        .plusHours(UserService.TOKEN_TTL_HOURS)
                                        .minusMinutes(1)));
        assertEquals(ENCRYPTED_VALUE, savedUser.getFirstName());
        assertEquals(ENCRYPTED_VALUE, savedUser.getLastName());
        assertEquals(ENCRYPTED_VALUE, savedUser.getEmailAddress());
        assertEquals(ENCRYPTED_VALUE, savedUser.getPhoneNumber());
        assertEquals(HASHED_VALUE, savedUser.getEmailAddressHash());
        assertEquals(HASHED_VALUE, savedUser.getPassword());
        assertEquals(Authorities.USER.getAuthority(), savedUser.getRole());
    }

    @Test
    void testCreateUser_alreadyExists() {
        when(userRepository.existsByEmailAddressHash(anyString())).thenReturn(true);

        Message message = userService.createUser(createUserRequest);

        verify(userRepository, never()).save(any());
        verify(mailService, never()).sendDoubleOptInEmail(any(), any(), any(), anyInt());

        assertEquals(UserService.CREATE_USER_MSG, message.message());
    }

    @Test
    void testEnableUser_success() {
        when(userRepository.findByVerificationToken(anyString())).thenReturn(Optional.of(testUser));

        Message message = userService.enableUser(testUser.getVerificationToken());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        verify(mailService)
                .sendWelcomeEmail(eq(savedUser.getId()), eq(DECRYPTED_VALUE), eq(DECRYPTED_VALUE));
        verify(mailService, never())
                .sendRegistrationExpiredEmail(anyLong(), anyString(), anyString());

        assertEquals(UserService.ENABLE_USER_MSG, message.message());
        assertTrue(savedUser.isEnabled());
    }

    @Test
    void testEnableUser_notFound() {
        when(userRepository.findByVerificationToken(anyString())).thenReturn(Optional.empty());

        Message message = userService.enableUser(testUser.getVerificationToken());

        verify(userRepository, never()).save(any());
        verify(mailService, never()).sendWelcomeEmail(anyLong(), anyString(), anyString());
        verify(mailService, never())
                .sendRegistrationExpiredEmail(anyLong(), anyString(), anyString());

        assertEquals(UserService.ENABLE_USER_MSG, message.message());
    }

    @Test
    void testEnableUser_tokenExpired() {
        testUser.setTokenExpiresAt(ZonedDateTime.now().minusMinutes(1));

        when(AesUtil.decrypt(anyString())).thenReturn(DECRYPTED_VALUE);
        when(userRepository.findByVerificationToken(anyString())).thenReturn(Optional.of(testUser));

        Message message = userService.enableUser(testUser.getVerificationToken());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).delete(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        verify(mailService, never()).sendWelcomeEmail(anyLong(), anyString(), anyString());
        verify(mailService)
                .sendRegistrationExpiredEmail(savedUser.getId(), DECRYPTED_VALUE, DECRYPTED_VALUE);

        assertEquals(UserService.ENABLE_USER_MSG, message.message());
        assertFalse(savedUser.isEnabled());
    }

    @Test
    void testEnableUser_alreadyEnabled() {
        testUser.setIsAccountEnabled(true);

        when(userRepository.findByVerificationToken(anyString())).thenReturn(Optional.of(testUser));

        Message message = userService.enableUser(testUser.getVerificationToken());

        verify(userRepository, never()).save(any());
        verify(mailService, never()).sendWelcomeEmail(anyLong(), anyString(), anyString());
        verify(mailService, never())
                .sendRegistrationExpiredEmail(anyLong(), anyString(), anyString());

        assertEquals(UserService.ENABLE_USER_MSG, message.message());
    }

    @Test
    void testRequestResetPassword_success() {
        when(userRepository.findByEmailAddressHash(anyString())).thenReturn(Optional.of(testUser));

        Message message = userService.requestPasswordReset(requestPasswordResetRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        verify(mailService)
                .sendPasswordResetEmail(
                        eq(savedUser.getId()),
                        eq(DECRYPTED_VALUE),
                        eq(requestPasswordResetRequest.emailAddress()),
                        eq(savedUser.getVerificationToken()),
                        anyInt());

        assertEquals(UserService.REQUEST_PASSWORD_RESET_MSG, message.message());
        assertTrue(
                savedUser
                        .getTokenExpiresAt()
                        .isAfter(
                                ZonedDateTime.now()
                                        .plusMinutes(UserService.TOKEN_TTL_MINUTES)
                                        .minusMinutes(1)));
    }

    @Test
    void testRequestResetPassword_notFound() {
        when(userRepository.findByEmailAddressHash(anyString())).thenReturn(Optional.empty());

        Message message = userService.requestPasswordReset(requestPasswordResetRequest);

        verify(userRepository, never()).save(any());
        verify(mailService, never())
                .sendPasswordResetEmail(anyLong(), anyString(), anyString(), anyString(), anyInt());

        assertEquals(UserService.REQUEST_PASSWORD_RESET_MSG, message.message());
    }

    @Test
    void testCompletePasswordReset_success() {
        when(passwordEncoder.encode(anyString())).thenReturn(HASHED_VALUE);
        when(userRepository.findByVerificationToken(anyString())).thenReturn(Optional.of(testUser));

        Message message =
                userService.completePasswordReset(
                        testUser.getVerificationToken(), completePasswordResetRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        verify(mailService, never())
                .sendPasswordResetExpiredEmail(anyLong(), anyString(), anyString());

        assertEquals(UserService.COMPLETE_PASSWORD_RESET_MSG, message.message());
        assertEquals(HASHED_VALUE, savedUser.getPassword());
    }

    @Test
    void testCompletePasswordReset_notFound() {
        when(userRepository.findByVerificationToken(anyString())).thenReturn(Optional.empty());

        Message message =
                userService.completePasswordReset(
                        testUser.getVerificationToken(), completePasswordResetRequest);

        verify(userRepository, never()).save(any());
        verify(mailService, never())
                .sendPasswordResetExpiredEmail(anyLong(), anyString(), anyString());

        assertEquals(UserService.COMPLETE_PASSWORD_RESET_MSG, message.message());
    }

    @Test
    void testCompletePasswordReset_tokenExpired() {
        testUser.setTokenExpiresAt(ZonedDateTime.now().minusMinutes(1));

        when(userRepository.findByVerificationToken(anyString())).thenReturn(Optional.of(testUser));

        Message message =
                userService.completePasswordReset(
                        testUser.getVerificationToken(), completePasswordResetRequest);

        verify(userRepository, never()).save(any());
        verify(mailService)
                .sendPasswordResetExpiredEmail(testUser.getId(), DECRYPTED_VALUE, DECRYPTED_VALUE);

        assertEquals(UserService.COMPLETE_PASSWORD_RESET_MSG, message.message());
    }

    @Test
    void testListUsers() {
        when(userRepository.findAll()).thenReturn(List.of(testUser));
        List<User> users = userService.listUsers();
        assertEquals(1, users.size());
        assertEquals(testUser, users.get(0));
    }

    @Test
    void testGetUser_success() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(jwtMock.getClaim(AuthorisationUtil.CLAIM_SCOPE))
                .thenReturn(Authorities.USER.getAuthority());
        when(jwtMock.getSubject()).thenReturn(String.valueOf(testUser.getId()));
        when(jwtMock.getClaim(AuthorisationUtil.CLAIM_CUSTOMER_ID))
                .thenReturn(testUser.getCustomerId());
        User user = userService.getUser(testUser.getCustomerId(), testUser.getId(), jwtMock);
        assertEquals(testUser, user);
    }

    @Test
    void testGetUser_idor() {
        when(jwtMock.getClaim(AuthorisationUtil.CLAIM_SCOPE))
                .thenReturn(Authorities.USER.getAuthority());
        when(jwtMock.getSubject()).thenReturn(String.valueOf(testUser.getId()));
        when(jwtMock.getClaim(AuthorisationUtil.CLAIM_CUSTOMER_ID))
                .thenReturn(testUser.getCustomerId());
        assertThrows(
                IdorException.class,
                () -> userService.getUser(testUser.getCustomerId() + 1, testUser.getId(), jwtMock));
        assertThrows(
                IdorException.class,
                () -> userService.getUser(testUser.getCustomerId(), testUser.getId() + 1, jwtMock));
    }

    @Test
    void testUpdateUser_success() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(jwtMock.getClaim(AuthorisationUtil.CLAIM_SCOPE))
                .thenReturn(Authorities.USER.getAuthority());
        when(jwtMock.getSubject()).thenReturn(String.valueOf(testUser.getId()));
        when(jwtMock.getClaim(AuthorisationUtil.CLAIM_CUSTOMER_ID))
                .thenReturn(testUser.getCustomerId());

        UserResource.UpdateUserRequest updateUserRequest =
                new UserResource.UpdateUserRequest(
                        testUser.getId(),
                        testUser.getCustomerId(),
                        "Morty",
                        "Smith",
                        null,
                        "Grandson",
                        false);

        userService.updateUser(
                testUser.getCustomerId(), testUser.getId(), updateUserRequest, jwtMock);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User updatedUser = userCaptor.getValue();

        assertEquals(ENCRYPTED_VALUE, updatedUser.getFirstName());
        assertEquals(ENCRYPTED_VALUE, updatedUser.getLastName());
        assertNull(updatedUser.getPhoneNumber());
        assertEquals(updateUserRequest.position(), updatedUser.getPosition());
        assertFalse(updatedUser.getIsSubscribedToNewsletter());
    }

    @Test
    void testUpdateUser_idConflict() {
        when(jwtMock.getClaim(AuthorisationUtil.CLAIM_SCOPE))
                .thenReturn(Authorities.USER.getAuthority());
        when(jwtMock.getSubject()).thenReturn(String.valueOf(testUser.getId()));
        when(jwtMock.getClaim(AuthorisationUtil.CLAIM_CUSTOMER_ID))
                .thenReturn(testUser.getCustomerId());

        UserResource.UpdateUserRequest updateUserRequest =
                new UserResource.UpdateUserRequest(
                        testUser.getId() + 1,
                        testUser.getCustomerId() + 1,
                        "Morty",
                        "Smith",
                        null,
                        "Grandson",
                        false);

        assertThrows(
                IdConflictException.class,
                () ->
                        userService.updateUser(
                                testUser.getCustomerId(),
                                testUser.getId(),
                                updateUserRequest,
                                jwtMock));
    }

    @Test
    void testUpdateUser_idor() {
        when(jwtMock.getClaim(AuthorisationUtil.CLAIM_SCOPE))
                .thenReturn(Authorities.USER.getAuthority());
        when(jwtMock.getSubject()).thenReturn(String.valueOf(testUser.getId()));
        when(jwtMock.getClaim(AuthorisationUtil.CLAIM_CUSTOMER_ID))
                .thenReturn(testUser.getCustomerId());
        assertThrows(
                IdorException.class,
                () -> userService.getUser(testUser.getCustomerId() + 1, testUser.getId(), jwtMock));
        assertThrows(
                IdorException.class,
                () -> userService.getUser(testUser.getCustomerId(), testUser.getId() + 1, jwtMock));
    }
}
