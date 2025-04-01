package de.flowsuite.mailflowapi.security;

import static org.springframework.security.oauth2.core.authorization.OAuth2AuthorizationManagers.hasAnyScope;
import static org.springframework.security.oauth2.core.authorization.OAuth2AuthorizationManagers.hasScope;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import de.flowsuite.mailflowapi.common.entity.Authorities;
import de.flowsuite.mailflowapi.user.UserService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Configuration
@EnableWebSecurity
class SecurityConfig {

    private final String mailboxClientSecret;
    private final String aiCompletionClientSecret;
    private final String ragClientSecret;
    private final String reCaptchaHttpHeader;
    private final ReCaptchaFilter reCaptchaFilter;
    private final UserService userService;

    SecurityConfig(
            @Value("${client.mailbox.secret}") String mailboxClientSecret,
            @Value("${client.ai-completion.secret}") String aiCompletionClientSecret,
            @Value("${client.rag.secret}") String ragClientSecret,
            @Value("${google.recaptcha.http-header}") String reCaptchaHttpHeader,
            ReCaptchaService reCaptchaService,
            UserService userService) {
        this.mailboxClientSecret = mailboxClientSecret;
        this.aiCompletionClientSecret = aiCompletionClientSecret;
        this.ragClientSecret = ragClientSecret;
        this.reCaptchaHttpHeader = reCaptchaHttpHeader;
        this.reCaptchaFilter = new ReCaptchaFilter(reCaptchaHttpHeader, reCaptchaService);
        this.userService = userService;
    }

    // spotless:off
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(auth -> auth
                        // Auth Resource
                        .requestMatchers(HttpMethod.POST, "/auth/**").permitAll()
                        // Customer Resource
                        .requestMatchers(HttpMethod.POST, "/customers").access(hasScope(Authorities.ADMIN.getAuthority()))
                        .requestMatchers(HttpMethod.GET, "/customers").access(hasAnyScope(Authorities.CUSTOMERS_LIST.getAuthority(), Authorities.ADMIN.getAuthority()))
                        .requestMatchers(HttpMethod.GET, "/customers/{id}").access(hasScope(Authorities.CUSTOMERS_READ.getAuthority()))
                        .requestMatchers(HttpMethod.PUT, "/customers/{id}").access(hasScope(Authorities.CUSTOMERS_WRITE.getAuthority()))
                        // User Resource
                        .requestMatchers(HttpMethod.POST, "/customers/users/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/customers/users/enable").permitAll()
                        .requestMatchers(HttpMethod.POST, "/customers/users/password").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/customers/users/password").permitAll()
                        .requestMatchers(HttpMethod.GET, "/customers/users").access(hasScope(Authorities.USERS_LIST.getAuthority()))
                        .requestMatchers(HttpMethod.GET, "/customers/*/users/*").access(hasScope(Authorities.USERS_READ.getAuthority()))
                        .requestMatchers(HttpMethod.PUT, "/customers/*/users/*").access(hasScope(Authorities.USERS_WRITE.getAuthority()))
                        // CustomerSettings Resource
                        .requestMatchers(HttpMethod.POST, "/customers/*/users/*/settings").access(hasScope(Authorities.SETTINGS_WRITE.getAuthority()))
                        .requestMatchers(HttpMethod.GET, "/customers/*/users/*/settings").access(hasScope(Authorities.SETTINGS_READ.getAuthority()))
                        .requestMatchers(HttpMethod.PUT, "/customers/*/users/*/settings").access(hasScope(Authorities.SETTINGS_WRITE.getAuthority()))
                        // RagUrls Resource
                        .requestMatchers(HttpMethod.POST, "/customers/*/rag-urls").access(hasScope(Authorities.RAG_URLS_WRITE.getAuthority()))
                        .requestMatchers(HttpMethod.GET, "/customers/*/rag-urls").access(hasScope(Authorities.RAG_URLS_LIST.getAuthority()))
                        .requestMatchers(HttpMethod.PUT, "/customers/*/rag-urls").access(hasScope(Authorities.RAG_URLS_WRITE.getAuthority()))
                        // Blacklist Resource
                        .requestMatchers(HttpMethod.POST, "/customers/*/blacklist").access(hasScope(Authorities.BLACKLIST_WRITE.getAuthority()))
                        .requestMatchers(HttpMethod.GET, "/customers/*/blacklist").access(hasScope(Authorities.BLACKLIST_LIST.getAuthority()))
                        .requestMatchers(HttpMethod.DELETE, "/customers/*/blacklist").access(hasScope(Authorities.BLACKLIST_WRITE.getAuthority()))
                        // MessageCategories Resource
                        .requestMatchers(HttpMethod.POST, "/customers/*/message-categories").access(hasScope(Authorities.MESSAGE_CATEGORIES_WRITE.getAuthority()))
                        .requestMatchers(HttpMethod.GET, "/customers/*/message-categories").access(hasScope(Authorities.MESSAGE_CATEGORIES_LIST.getAuthority()))
                        .requestMatchers(HttpMethod.PUT, "/customers/*/message-categories").access(hasScope(Authorities.MESSAGE_CATEGORIES_WRITE.getAuthority()))
                        // MessageLog Resource
                        .requestMatchers(HttpMethod.POST, "/customers/*/users/*/message-log").access(hasScope(Authorities.MESSAGE_LOG_WRITE.getAuthority()))
                        .requestMatchers(HttpMethod.GET, "/customers/*/users/*/message-log").access(hasScope(Authorities.MESSAGE_LOG_LIST.getAuthority()))
                        // ResponseRatings Resource
                        .requestMatchers(HttpMethod.POST, "/customers/users/response-ratings").permitAll()
                        .requestMatchers(HttpMethod.GET, "/customers/*/users/*/response-ratings").access(hasScope(Authorities.RESPONSE_RATINGS_LIST.getAuthority()))
                        // Authenticate any request
                        .anyRequest()
                        .authenticated())
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .build();
    }
    // spotless:on

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:*",
                        "http://flow-suite.de",
                        "https://flow-suite.de",
                        "http://*.flow-suite.de",
                        "https://*.flow-suite.de"));
        configuration.setAllowedHeaders(
                List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE, reCaptchaHttpHeader));
        configuration.setAllowedMethods(
                Arrays.asList(
                        HttpMethod.POST.name(),
                        HttpMethod.GET.name(),
                        HttpMethod.PUT.name(),
                        HttpMethod.DELETE.name(),
                        HttpMethod.OPTIONS.name()));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(RsaUtil.publicKey).build();
    }

    @Bean
    JwtEncoder jwtEncoder() {
        JWK jwk = new RSAKey.Builder(RsaUtil.publicKey).privateKey(RsaUtil.privateKey).build();
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public JdbcRegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        RegisteredClient mailboxClient =
                RegisteredClient.withId(UUID.randomUUID().toString())
                        .clientId("mailflow-mailbox-client")
                        .clientSecret(passwordEncoder().encode(mailboxClientSecret))
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                        .scope(Authorities.CLIENT.getAuthority())
                        .scope(Authorities.CUSTOMERS_LIST.getAuthority())
                        .scope(Authorities.CUSTOMERS_READ.getAuthority())
                        .scope(Authorities.USERS_LIST.getAuthority())
                        .scope(Authorities.USERS_READ.getAuthority())
                        .scope(Authorities.SETTINGS_READ.getAuthority())
                        .scope(Authorities.BLACKLIST_LIST.getAuthority())
                        .build();

        RegisteredClient aiCompletionClient =
                RegisteredClient.withId(UUID.randomUUID().toString())
                        .clientId("mailflow-ai-completion-client")
                        .clientSecret(passwordEncoder().encode(aiCompletionClientSecret))
                        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                        .scope(Authorities.CLIENT.getAuthority())
                        .scope(Authorities.SETTINGS_READ.getAuthority())
                        .scope(Authorities.MESSAGE_CATEGORIES_LIST.getAuthority())
                        .scope(Authorities.MESSAGE_LOG_WRITE.getAuthority())
                        .build();

        RegisteredClient ragClient =
                RegisteredClient.withId(UUID.randomUUID().toString())
                        .clientId("mailflow-rag-client")
                        .clientSecret(passwordEncoder().encode(ragClientSecret))
                        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                        .scope(Authorities.CLIENT.getAuthority())
                        .scope(Authorities.RAG_URLS_LIST.getAuthority())
                        .scope(Authorities.RAG_URLS_WRITE.getAuthority())
                        .build();

        JdbcRegisteredClientRepository registeredClientRepository =
                new JdbcRegisteredClientRepository(jdbcTemplate);
        registeredClientRepository.save(mailboxClient);
        registeredClientRepository.save(aiCompletionClient);
        registeredClientRepository.save(ragClient);

        return registeredClientRepository;
    }

    @Bean
    FilterRegistrationBean<ReCaptchaFilter> reCaptchaFilterRegistration() {
        FilterRegistrationBean<ReCaptchaFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(reCaptchaFilter);
        registrationBean.addUrlPatterns(
                "/auth/token",
                "/customers/users/register",
                "/customers/users/enable",
                "/customers/users/password",
                "/customers/users/response-ratings");
        return registrationBean;
    }
}
