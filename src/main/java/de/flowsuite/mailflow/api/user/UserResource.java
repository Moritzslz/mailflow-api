package de.flowsuite.mailflow.api.user;

import de.flowsuite.mailflow.common.constant.Message;
import de.flowsuite.mailflow.common.entity.User;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
class UserResource {

    private final UserService userService;

    public UserResource(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users/register")
    ResponseEntity<Message> createUser(@RequestBody @Valid CreateUserRequest request) {
        return ResponseEntity.accepted().body(userService.createUser(request));
    }

    @GetMapping("/users/enable")
    ResponseEntity<Message> enableUser(@RequestParam @NotBlank String token) {
        // TODO notify mailbox-service
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
        return ResponseEntity.ok(userService.getUser(customerId, id, jwt));
    }

    @PutMapping("/{customerId}/users/{id}")
    ResponseEntity<User> updateUser(
            @PathVariable long customerId,
            @PathVariable long id,
            @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        // TODO notify mailbox-service
        return ResponseEntity.ok(userService.updateUser(customerId, id, request, jwt));
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
