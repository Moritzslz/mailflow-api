package de.flowsuite.mailflowapi.security;

import de.flowsuite.mailflowapi.common.entity.User;
import de.flowsuite.mailflowapi.common.exception.AuthenticationException;
import de.flowsuite.mailflowapi.common.exception.InvalidRefreshTokenException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.stream.Collectors;

@Service
class AuthenticationService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationService.class);
    private final String jwtIssuerLocation;
    private final JwtEncoder jwtEncoder;
    private final AuthenticationManager userAuthenticationManager;

    AuthenticationService(
            @Value("${jwt.issuer.location}") String jwtIssuerLocation,
            JwtEncoder jwtEncoder,
            AuthenticationManager userAuthenticationManager) {
        this.jwtIssuerLocation = jwtIssuerLocation;
        this.jwtEncoder = jwtEncoder;
        this.userAuthenticationManager = userAuthenticationManager;
    }

    String generateAccessToken(String subject, String scope, long userId, long customerId) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Berlin"));
        JwtClaimsSet claims =
                JwtClaimsSet.builder()
                        .issuer(jwtIssuerLocation)
                        .issuedAt(now.toInstant())
                        .expiresAt(now.plusHours(1).toInstant())
                        .subject(subject)
                        .claim("scope", scope)
                        .claim("userId", userId)
                        .claim("customerId", customerId)
                        .build();

        LOG.debug("Access Token Claims: {}", claims);

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String generateRefreshToken(String subject) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Berlin"));
        JwtClaimsSet claims =
                JwtClaimsSet.builder()
                        .issuer(jwtIssuerLocation)
                        .issuedAt(now.toInstant())
                        .expiresAt(now.plusDays(2).toInstant())
                        .subject(subject)
                        .claim("type", "refresh")
                        .build();

        LOG.debug("Refresh Token Claims: {}", claims);

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    Authentication authenticateUser(AuthenticationResource.UserLoginRequest loginRequest) {
        return userAuthenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.username(), loginRequest.password()));
    }

    User extractUser(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof User user)) {
            throw new AuthenticationException();
        }
        return user;
    }

    void validateRefreshToken(Jwt jwt) {
        if (!"refresh".equals(jwt.getClaim("type"))) {
            throw new InvalidRefreshTokenException();
        }
    }

    AuthenticationResource.TokenResponse generateTokens(User user) {
        return generateTokens(user, generateRefreshToken(user.getEmailAddress()));
    }

    AuthenticationResource.TokenResponse generateTokens(User user, String refreshToken) {
        String scope =
                user.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(" "));
        String accessToken =
                generateAccessToken(
                        user.getEmailAddress(), scope, user.getId(), user.getCustomer().getId());
        return new AuthenticationResource.TokenResponse(accessToken, refreshToken);
    }
}
