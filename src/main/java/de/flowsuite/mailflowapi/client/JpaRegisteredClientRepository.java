package de.flowsuite.mailflowapi.client;

import de.flowsuite.mailflowapi.common.entity.Client;

import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class JpaRegisteredClientRepository implements RegisteredClientRepository {

    private final ClientRepository clientRepository;

    public JpaRegisteredClientRepository(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        Client entity =
                Client.builder()
                        .clientId(registeredClient.getClientId())
                        .clientSecret(registeredClient.getClientSecret())
                        .authenticationMethod(
                                registeredClient.getClientAuthenticationMethods().stream()
                                        .map(ClientAuthenticationMethod::getValue)
                                        .collect(Collectors.joining(",")))
                        .authorizationGrantTypes(
                                registeredClient.getAuthorizationGrantTypes().stream()
                                        .map(AuthorizationGrantType::getValue)
                                        .collect(Collectors.joining(",")))
                        .scopes(String.join(",", registeredClient.getScopes()))
                        .tokenSettings(
                                TokenSettings.builder()
                                        .accessTokenTimeToLive(Duration.ofHours(1))
                                        .reuseRefreshTokens(false)
                                        .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                                        .build()
                                        .toString())
                        .build();
        clientRepository.save(entity);
    }

    @Override
    public RegisteredClient findById(String id) {
        return clientRepository
                .findById(Long.parseLong(id))
                .map(this::convertToRegisteredClient)
                .orElse(null);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return clientRepository
                .findByClientId(clientId)
                .map(this::convertToRegisteredClient)
                .orElse(null);
    }

    private RegisteredClient convertToRegisteredClient(Client entity) {
        return RegisteredClient.withId(String.valueOf(entity.getId()))
                .clientId(entity.getClientId())
                .clientSecret(entity.getClientSecret())
                .clientAuthenticationMethod(
                        new ClientAuthenticationMethod(entity.getAuthenticationMethod()))
                .authorizationGrantType(
                        new AuthorizationGrantType(entity.getAuthorizationGrantTypes()))
                .scopes(scopes -> scopes.addAll(Arrays.asList(entity.getScopes().split(","))))
                .tokenSettings(TokenSettings.builder().build())
                .build();
    }
}
