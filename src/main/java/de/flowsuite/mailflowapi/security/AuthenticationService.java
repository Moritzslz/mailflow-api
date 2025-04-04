package de.flowsuite.mailflowapi.security;

import static de.flowsuite.mailflowapi.common.util.AuthorisationUtil.*;

import de.flowsuite.mailflowapi.common.entity.Client;
import de.flowsuite.mailflowapi.common.entity.User;
import de.flowsuite.mailflowapi.common.exception.AuthenticationFailedException;
import de.flowsuite.mailflowapi.common.exception.InvalidRefreshTokenException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
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
    private static final String JWT_ISSUER_LOCATION = "self";
    private static final String CLAIM_TYPE = "type";
    private static final String CLAIM_TYPE_REFRESH = "refresh";
    private final JwtEncoder jwtEncoder;
    private final AuthenticationManager userAuthenticationManager;
    private final AuthenticationManager clientAuthenticationManager;

    AuthenticationService(
            JwtEncoder jwtEncoder,
            @Qualifier("userAuthenticationManager") AuthenticationManager userAuthenticationManager,
            @Qualifier("clientAuthenticationManager") AuthenticationManager clientAuthenticationManager) {
        this.jwtEncoder = jwtEncoder;
        this.userAuthenticationManager = userAuthenticationManager;
        this.clientAuthenticationManager = clientAuthenticationManager;
    }

    String generateAccessToken(String subject, String scope, long customerId) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Berlin"));
        JwtClaimsSet claims =
                JwtClaimsSet.builder()
                        .issuer(JWT_ISSUER_LOCATION)
                        .issuedAt(now.toInstant())
                        .expiresAt(now.plusHours(1).toInstant())
                        .subject(subject)
                        .claim(CLAIM_SCOPE, scope)
                        .claim(CLAIM_CUSTOMER_ID, customerId)
                        .build();

        LOG.debug("Access Token Claims: {}", claims);

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String generateRefreshToken(String subject) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Berlin"));
        JwtClaimsSet claims =
                JwtClaimsSet.builder()
                        .issuer(JWT_ISSUER_LOCATION)
                        .issuedAt(now.toInstant())
                        .expiresAt(now.plusDays(2).toInstant())
                        .subject(subject)
                        .claim(CLAIM_TYPE, CLAIM_TYPE_REFRESH)
                        .build();

        LOG.debug("Refresh Token Claims: {}", claims);

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    Authentication authenticateUser(AuthenticationResource.UserLoginRequest loginRequest) {
        return userAuthenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.username(), loginRequest.password()));
    }

    Authentication authenticateClient(AuthenticationResource.ClientLoginRequest loginRequest) {
        return clientAuthenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.clientName(), loginRequest.clientSecret()));
    }

    User extractUser(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof User user)) {
            throw new AuthenticationFailedException();
        }
        return user;
    }

    Client extractClient(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof Client client)) {
            throw new AuthenticationFailedException();
        }
        return client;
    }

    void validateRefreshToken(Jwt jwt) {
        if (!CLAIM_TYPE_REFRESH.equals(jwt.getClaim(CLAIM_TYPE))) {
            throw new InvalidRefreshTokenException();
        }
    }

    AuthenticationResource.UserTokenResponse generateUserTokens(User user) {
        return generateUserTokens(user, generateRefreshToken(String.valueOf(user.getId())));
    }

    AuthenticationResource.UserTokenResponse generateUserTokens(User user, String refreshToken) {
        String scope =
                user.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(" "));
        String accessToken =
                generateAccessToken(String.valueOf(user.getId()), scope, user.getCustomerId());
        return new AuthenticationResource.UserTokenResponse(accessToken, refreshToken);
    }

    AuthenticationResource.ClientTokenResponse generateClientAccessToken(Client client) {
        String scope =
                client.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(" "));
        String accessToken = generateAccessToken(client.getClientName(), scope, -1);
        return new AuthenticationResource.ClientTokenResponse(accessToken);
    }
}
