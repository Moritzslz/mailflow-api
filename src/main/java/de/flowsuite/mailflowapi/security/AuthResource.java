package de.flowsuite.mailflowapi.security;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
class AuthResource {

    private final JwtService jwtService;

    AuthResource(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/token")
    ResponseEntity<String> getToken(Authentication authentication) {
        return ResponseEntity.ok(jwtService.generateToken(authentication));
    }

    @PostMapping("/refresh")
    ResponseEntity<String> refreshToken(Authentication authentication) {
        // TODO
        return ResponseEntity.ok("TODO");
    }
}
