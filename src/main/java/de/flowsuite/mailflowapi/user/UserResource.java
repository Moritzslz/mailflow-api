package de.flowsuite.mailflowapi.user;

import de.flowsuite.mailflowapi.common.entity.User;

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
    ResponseEntity<User> createUser(@RequestBody @Valid CreateUserRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @GetMapping("/users/enable")
    ResponseEntity<String> enableUser(@RequestParam @NotBlank String token) {
        return ResponseEntity.ok(userService.enableUser(token));
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
            boolean subscribedToNewsletter) {}
}
