package de.flowsuite.mailflow.api.user;

import de.flowsuite.mailflow.common.constant.Message;
import de.flowsuite.mailflow.common.entity.User;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/customers")
class UserResource {

    private static final Logger LOG = LoggerFactory.getLogger(UserResource.class);
    private static final String NOTIFY_USER_URI = "/notifications/users/{userId}";

    private final UserService userService;
    private final RestClient mailboxServiceRestClient;

    UserResource(
            UserService userService,
            @Qualifier("mailboxServiceRestClient") RestClient mailboxServiceRestClient) {
        this.userService = userService;
        this.mailboxServiceRestClient = mailboxServiceRestClient;
    }

    @PostMapping("/users/register")
    ResponseEntity<Message> createUser(@RequestBody @Valid CreateUserRequest request) {
        return ResponseEntity.accepted().body(userService.createUser(request));
    }

    @GetMapping("/users/enable")
    ResponseEntity<Message> enableUser(@RequestParam @NotBlank String token) {
        return ResponseEntity.ok(userService.enableUser(token));
    }

    @PostMapping("/users/password-reset")
    ResponseEntity<Message> requestPasswordReset(
            @RequestBody @Valid RequestPasswordResetRequest request) {
        return ResponseEntity.accepted().body(userService.requestPasswordReset(request));
    }

    @PutMapping("/users/password-reset")
    ResponseEntity<Message> completePasswordReset(
            @RequestParam @NotBlank String token,
            @RequestBody @Valid CompletePasswordResetRequest request) {
        return ResponseEntity.ok(userService.completePasswordReset(token, request));
    }

    @GetMapping("/users")
    ResponseEntity<List<User>> listUsers() {
        return ResponseEntity.ok(userService.listUsers());
    }

    @GetMapping("/{customerId}/users/{id}")
    ResponseEntity<User> getUser(
            @PathVariable long customerId,
            @PathVariable long id,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(userService.getUser(customerId, id, false, jwt));
    }

    @GetMapping("/{customerId}/users/{id}/decrypted")
    ResponseEntity<User> getUserDecrypted(
            @PathVariable long customerId,
            @PathVariable long id,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(userService.getUser(customerId, id, true, jwt));
    }

    @PutMapping("/{customerId}/users/{id}")
    ResponseEntity<User> updateUser(
            @PathVariable long customerId,
            @PathVariable long id,
            @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        User updatedUser = userService.updateUser(customerId, id, request, jwt);
        CompletableFuture.runAsync(() -> notifyMailboxService(id, updatedUser));
        return ResponseEntity.ok(userService.updateUser(customerId, id, request, jwt));
    }

    private void notifyMailboxService(long userId, User user) {
        LOG.debug("Notifying mailbox service of user change");

        mailboxServiceRestClient
                .put()
                .uri(NOTIFY_USER_URI, userId)
                .body(user)
                .retrieve()
                .toBodilessEntity();
    }

    record CreateUserRequest(
            @NotNull String registrationToken,
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotBlank String emailAddress,
            @NotBlank String password,
            @NotBlank String confirmationPassword,
            String phoneNumber,
            String position,
            boolean isSubscribedToNewsletter) {}

    record RequestPasswordResetRequest(@NotBlank String emailAddress) {}

    record CompletePasswordResetRequest(
            @NotBlank String password, @NotBlank String confirmationPassword) {}

    record UpdateUserRequest(
            @NotNull Long userId,
            @NotNull Long customerId,
            @NotBlank String firstName,
            @NotBlank String lastName,
            String phoneNumber,
            String position,
            boolean isSubscribedToNewsletter) {}
}
