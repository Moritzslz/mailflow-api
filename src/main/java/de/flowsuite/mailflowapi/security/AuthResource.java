package de.flowsuite.mailflowapi.security;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
class AuthResource {

    private final JwtService jwtService;

    private static final Map<String, String> CLIENT_CREDENTIALS =
            Map.of(
                    "core-service", "core-secret",
                    "message-service", "message-secret");

    AuthResource(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/user/token")
    ResponseEntity<String> getTokenForUser(Authentication authentication) {
        String subject = authentication.getName();
        String scope =
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(" "));
        return ResponseEntity.ok(jwtService.generateToken(subject, scope));
    }

    @PostMapping("/service/token") // TODO
    ResponseEntity<String> getTokenForService(Map<String, String> request) {
        String clientId = request.get("client_id");
        String clientSecret = request.get("client_secret");

        // Validate client credentials
        if (clientId == null
                || clientSecret == null
                || !CLIENT_CREDENTIALS.containsKey(clientId)
                || !CLIENT_CREDENTIALS.get(clientId).equals(clientSecret)) {
            return ResponseEntity.status(401).body("Invalid client credentials");
        }

        return ResponseEntity.ok(jwtService.generateToken(clientId, "service"));
    }

    @PostMapping("/refresh")
    ResponseEntity<String> refreshToken(Authentication authentication) {
        // TODO
        return ResponseEntity.ok("TODO");
    }
}
