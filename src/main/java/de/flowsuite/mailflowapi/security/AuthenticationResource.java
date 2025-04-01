package de.flowsuite.mailflowapi.security;

import de.flowsuite.mailflowapi.common.entity.User;
import de.flowsuite.mailflowapi.user.UserService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
class AuthenticationResource {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationResource.class);

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

    @PostMapping("/token")
    ResponseEntity<TokenResponse> getToken(@RequestBody @Valid UserLoginRequest loginRequest) {
        LOG.debug(loginRequest.toString());
        Authentication authentication = authenticationService.authenticateUser(loginRequest);
        User user = authenticationService.extractUser(authentication);
        return ResponseEntity.ok(authenticationService.generateTokens(user));
    }

    @PostMapping("/token/refresh")
    ResponseEntity<TokenResponse> refreshAccessToken(
            @RequestBody @Valid RefreshTokenRequest refreshTokenRequest) {
        Jwt jwt = jwtDecoder.decode(refreshTokenRequest.refreshToken);
        authenticationService.validateRefreshToken(jwt);
        User user = userService.findByEmailAddress(jwt.getSubject());
        return ResponseEntity.ok(
                authenticationService.generateTokens(user, refreshTokenRequest.refreshToken));
    }

    record UserLoginRequest(@NotBlank String username, @NotBlank String password) {}

    record RefreshTokenRequest(@NotBlank String refreshToken) {}

    record TokenResponse(@NotBlank String accessToken, @NotBlank String refreshToken) {}
}
