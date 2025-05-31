package de.flowsuite.mailflow.api.security;

import static de.flowsuite.mailflow.common.util.AuthorisationUtil.CLAIM_CUSTOMER_ID;
import static de.flowsuite.mailflow.common.util.AuthorisationUtil.CLAIM_SCOPE;
import static de.flowsuite.mailflow.common.util.Util.BERLIN_ZONE;

import de.flowsuite.mailflow.api.user.UserService;
import de.flowsuite.mailflow.common.constant.Authorities;
import de.flowsuite.mailflow.common.entity.Client;
import de.flowsuite.mailflow.common.entity.User;
import de.flowsuite.mailflow.common.exception.AuthenticationFailedException;
import de.flowsuite.mailflow.common.exception.InvalidRefreshTokenException;

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

import java.time.ZonedDateTime;
import java.util.stream.Collectors;

@Service
class AuthenticationService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationService.class);
    private static final String JWT_ISSUER_LOCATION = "self";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TYPE = "type";
    private static final String CLAIM_TYPE_ACCESS = "access";
    private static final String CLAIM_TYPE_REFRESH = "refresh";
    private static final int JWT_TTL_HOURS = 1;
    private static final int JWT_TTL_DAYS = 2;
    private final JwtEncoder jwtEncoder;
    private final AuthenticationManager userAuthenticationManager;
    private final AuthenticationManager clientAuthenticationManager;
    private final UserService userService;

    AuthenticationService(
            JwtEncoder jwtEncoder,
            @Qualifier("userAuthenticationManager") AuthenticationManager userAuthenticationManager,
            @Qualifier("clientAuthenticationManager") AuthenticationManager clientAuthenticationManager,
            UserService userService) {
        this.jwtEncoder = jwtEncoder;
        this.userAuthenticationManager = userAuthenticationManager;
        this.clientAuthenticationManager = clientAuthenticationManager;
        this.userService = userService;
    }

    String generateAccessToken(String subject, String role, String scope, long customerId) {
        ZonedDateTime now = ZonedDateTime.now(BERLIN_ZONE);
        JwtClaimsSet claims =
                JwtClaimsSet.builder()
                        .issuer(JWT_ISSUER_LOCATION)
                        .issuedAt(now.toInstant())
                        .expiresAt(now.plusHours(JWT_TTL_HOURS).toInstant())
                        .subject(subject)
                        .claim(CLAIM_ROLE, role)
                        .claim(CLAIM_SCOPE, scope)
                        .claim(CLAIM_CUSTOMER_ID, customerId)
                        .claim(CLAIM_TYPE, CLAIM_TYPE_ACCESS)
                        .build();

        LOG.debug("Access Token Claims: {}", claims);

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String generateRefreshToken(String subject) {
        ZonedDateTime now = ZonedDateTime.now(BERLIN_ZONE);
        JwtClaimsSet claims =
                JwtClaimsSet.builder()
                        .issuer(JWT_ISSUER_LOCATION)
                        .issuedAt(now.toInstant())
                        .expiresAt(now.plusDays(JWT_TTL_DAYS).toInstant())
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
                generateAccessToken(
                        String.valueOf(user.getId()), user.getRole(), scope, user.getCustomerId());

        userService.updateLastLoginAt(user);

        return new AuthenticationResource.UserTokenResponse(accessToken, refreshToken);
    }

    AuthenticationResource.ClientTokenResponse generateClientAccessToken(Client client) {
        String scope =
                client.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(" "));
        String accessToken =
                generateAccessToken(
                        client.getClientName(), Authorities.CLIENT.getAuthority(), scope, -1);
        return new AuthenticationResource.ClientTokenResponse(accessToken);
    }
}
