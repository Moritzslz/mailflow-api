package de.flowsuite.mailflowapi.user;

import de.flowsuite.mailflowapi.common.dto.Message;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
class UserResource {

    private final UserService userService;

    public UserResource(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    ResponseEntity<Message> createUser(@RequestBody @Valid CreateUserRequest request) {
        return ResponseEntity.accepted().body(userService.createUser(request));
    }

    @GetMapping("/users/enable")
    ResponseEntity<Message> enableUser(@RequestParam @NotBlank String token) {
        return ResponseEntity.ok(userService.enableUser(token));
    }

    @PostMapping("/users/password-reset")
    ResponseEntity<Message> requestPasswordReset(@RequestBody String emailAddress) {
        return ResponseEntity.accepted().body(userService.requestPasswordReset(emailAddress));
    }

    @PutMapping("/users/password-reset")
    ResponseEntity<Message> completePasswordReset(
            @RequestParam @NotBlank String token,
            @RequestBody @Valid UserResource.CompletePasswordResetRequest request) {
        return ResponseEntity.ok(userService.completePasswordReset(token, request));
    }

    record CreateUserRequest(
            @NotNull Long customerId,
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotBlank String emailAddress,
            @NotBlank String password,
            @NotBlank String confirmationPassword,
            String phoneNumber,
            String position,
            boolean isSubscribedToNewsletter) {}

    record CompletePasswordResetRequest(
            @NotBlank String password, @NotBlank String confirmationPassword) {}
}
