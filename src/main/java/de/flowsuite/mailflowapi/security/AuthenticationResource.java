package de.flowsuite.mailflowapi.security;

import de.flowsuite.mailflowapi.common.entity.Client;
import de.flowsuite.mailflowapi.common.entity.User;
import de.flowsuite.mailflowapi.user.UserService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/token")
class AuthenticationResource {

    private final AuthenticationService authenticationService;
    private final JwtDecoder jwtDecoder;
    private final UserService userService;

    AuthenticationResource(
            AuthenticationService authenticationService,
            JwtDecoder jwtDecoder,
            UserService userService) {
        this.authenticationService = authenticationService;
        this.jwtDecoder = jwtDecoder;
        this.userService = userService;
    }

    @PostMapping("/user")
    ResponseEntity<UserTokenResponse> getUserTokens(
            @RequestBody @Valid UserLoginRequest loginRequest) {
        Authentication authentication = authenticationService.authenticateUser(loginRequest);
        User user = authenticationService.extractUser(authentication);
        return ResponseEntity.ok(authenticationService.generateUserTokens(user));
    }

    @PostMapping("/user/refresh")
    ResponseEntity<UserTokenResponse> refreshUserAccessToken(
            @RequestBody @Valid RefreshTokenRequest refreshTokenRequest) {
        Jwt jwt = jwtDecoder.decode(refreshTokenRequest.refreshToken);
        authenticationService.validateRefreshToken(jwt);
        User user = userService.getById(Long.parseLong(jwt.getSubject()));
        return ResponseEntity.ok(
                authenticationService.generateUserTokens(user, refreshTokenRequest.refreshToken));
    }

    @PostMapping("/client")
    ResponseEntity<ClientTokenResponse> getClientAccessToken(
            @RequestBody @Valid ClientLoginRequest loginRequest) {
        Authentication authentication = authenticationService.authenticateClient(loginRequest);
        Client client = authenticationService.extractClient(authentication);
        return ResponseEntity.ok(authenticationService.generateClientAccessToken(client));
    }

    record UserLoginRequest(@NotBlank String username, @NotBlank String password) {}

    record UserTokenResponse(@NotBlank String accessToken, @NotBlank String refreshToken) {}

    record RefreshTokenRequest(@NotBlank String refreshToken) {}

    record ClientLoginRequest(@NotBlank String clientName, @NotBlank String clientSecret) {}

    record ClientTokenResponse(@NotBlank String accessToken) {}
}
