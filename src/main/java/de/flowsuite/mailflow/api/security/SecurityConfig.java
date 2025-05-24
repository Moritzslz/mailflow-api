package de.flowsuite.mailflow.api.security;

import static org.springframework.security.oauth2.core.authorization.OAuth2AuthorizationManagers.hasAnyScope;
import static org.springframework.security.oauth2.core.authorization.OAuth2AuthorizationManagers.hasScope;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import de.flowsuite.mailflow.api.client.ClientService;
import de.flowsuite.mailflow.api.user.UserService;
import de.flowsuite.mailflow.common.constant.Authorities;
import de.flowsuite.mailflow.common.exception.MissingEnvVarException;
import de.flowsuite.mailflow.common.util.RsaUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
class SecurityConfig {

    private final String reCaptchaHttpHeader;
    private final ReCaptchaFilter reCaptchaFilter;
    private final UserService userService;
    private final ClientService clientService;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    SecurityConfig(
            @Value("${google.recaptcha.http-header}") String reCaptchaHttpHeader,
            ReCaptchaService reCaptchaService,
            UserService userService,
            ClientService clientService,
            PasswordEncoder passwordEncoder,
            Environment environment) {
        this.reCaptchaHttpHeader = reCaptchaHttpHeader;
        this.reCaptchaFilter = new ReCaptchaFilter(reCaptchaHttpHeader, reCaptchaService);
        this.userService = userService;
        this.clientService = clientService;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    // spotless:off
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        boolean isProd = Arrays.asList(environment.getActiveProfiles()).contains("prod");

        if (isProd) {
            http.requiresChannel(channel -> channel.anyRequest().requiresSecure());
        }

        return http.authorizeHttpRequests(auth -> auth
                        // Auth Resource
                        .requestMatchers(HttpMethod.POST, "/auth/**").permitAll()
                        // Microservice Resource
                        .requestMatchers(HttpMethod.POST, "/clients").access(hasScope(Authorities.ADMIN.getAuthority()))
                        .requestMatchers(HttpMethod.GET, "/clients/*").access(hasScope(Authorities.ADMIN.getAuthority()))
                        .requestMatchers(HttpMethod.GET, "/clients").access(hasScope(Authorities.ADMIN.getAuthority()))
                        // User Resource
                        .requestMatchers(HttpMethod.POST, "/customers/users/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/customers/users/enable").permitAll()
                        .requestMatchers(HttpMethod.POST, "/customers/users/password-reset").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/customers/users/password-reset").permitAll()
                        .requestMatchers(HttpMethod.GET, "/customers/users").access(hasAnyScope(Authorities.USERS_LIST.getAuthority(), Authorities.ADMIN.getAuthority()))
                        .requestMatchers(HttpMethod.GET, "/customers/*/users/*/decrypted").access(hasScope(Authorities.ADMIN.getAuthority()))
                        .requestMatchers(HttpMethod.GET, "/customers/*/users/*").access(hasAnyScope(Authorities.USERS_READ.getAuthority(), Authorities.ADMIN.getAuthority()))
                        .requestMatchers(HttpMethod.PUT, "/customers/*/users/*").access(hasAnyScope(Authorities.USERS_WRITE.getAuthority(), Authorities.ADMIN.getAuthority()))
                        // Customer Resource
                        .requestMatchers(HttpMethod.POST, "/customers").access(hasScope(Authorities.ADMIN.getAuthority()))
                        .requestMatchers(HttpMethod.GET, "/customers").access(hasAnyScope(Authorities.CUSTOMERS_LIST.getAuthority(), Authorities.ADMIN.getAuthority()))
                        .requestMatchers(HttpMethod.PUT, "/customers/*/test-version").access(hasScope(Authorities.ADMIN.getAuthority()))
                        .requestMatchers(HttpMethod.GET, "/customers/*").access(hasAnyScope(Authorities.CUSTOMERS_READ.getAuthority(), Authorities.ADMIN.getAuthority()))
                        .requestMatchers(HttpMethod.PUT, "/customers/*").access(hasAnyScope(Authorities.CUSTOMERS_WRITE.getAuthority(), Authorities.MANAGER.getAuthority(), Authorities.ADMIN.getAuthority()))
                        // CustomerSettings Resource
                        .requestMatchers(HttpMethod.POST, "/customers/*/users/*/settings").access(hasAnyScope(Authorities.SETTINGS_WRITE.getAuthority(), Authorities.ADMIN.getAuthority()))
                        .requestMatchers(HttpMethod.GET, "/customers/*/users/*/settings").access(hasAnyScope(Authorities.SETTINGS_READ.getAuthority(), Authorities.ADMIN.getAuthority()))
                        .requestMatchers(HttpMethod.PUT, "/customers/*/users/*/settings/*").access(hasAnyScope(Authorities.SETTINGS_WRITE.getAuthority(), Authorities.ADMIN.getAuthority()))
                        // RagUrls Resource
                        .requestMatchers(HttpMethod.POST, "/customers/*/rag-urls").access(hasAnyScope(Authorities.MANAGER.getAuthority(), Authorities.ADMIN.getAuthority()))
                        .requestMatchers(HttpMethod.GET, "/customers/*/rag-urls").access(hasAnyScope(Authorities.RAG_URLS_LIST.getAuthority(), Authorities.ADMIN.getAuthority()))
                        .requestMatchers(HttpMethod.GET, "/customers/*/rag-urls/*").access(hasAnyScope(Authorities.RAG_URLS_READ.getAuthority(), Authorities.ADMIN.getAuthority()))
                        .requestMatchers(HttpMethod.DELETE, "/customers/*/rag-urls/*").access(hasAnyScope(Authorities.MANAGER.getAuthority(), Authorities.ADMIN.getAuthority()))
                        // Blacklist Resource
                        .requestMatchers(HttpMethod.POST, "/customers/*/users/*/blacklist").access(hasAnyScope(Authorities.BLACKLIST_WRITE.getAuthority(), Authorities.ADMIN.getAuthority()))
                        .requestMatchers(HttpMethod.GET, "/customers/*/users/*/blacklist").access(hasAnyScope(Authorities.BLACKLIST_LIST.getAuthority(), Authorities.ADMIN.getAuthority()))
                        .requestMatchers(HttpMethod.GET, "/customers/*/users/*/blacklist/*").access(hasAnyScope(Authorities.BLACKLIST_READ.getAuthority(), Authorities.ADMIN.getAuthority()))
                        .requestMatchers(HttpMethod.DELETE, "/customers/*/users/*/blacklist/*").access(hasAnyScope(Authorities.BLACKLIST_WRITE.getAuthority(), Authorities.ADMIN.getAuthority()))
                        // MessageCategories Resource
                        .requestMatchers(HttpMethod.POST, "/customers/*/message-categories").access(hasAnyScope(Authorities.MANAGER.getAuthority(), Authorities.ADMIN.getAuthority()))
                        .requestMatchers(HttpMethod.GET, "/customers/*/message-categories").access(hasAnyScope(Authorities.MESSAGE_CATEGORIES_LIST.getAuthority(), Authorities.ADMIN.getAuthority()))
                        .requestMatchers(HttpMethod.GET, "/customers/*/message-categories/*").access(hasAnyScope(Authorities.MESSAGE_CATEGORIES_READ.getAuthority(), Authorities.ADMIN.getAuthority()))
                        .requestMatchers(HttpMethod.PUT, "/customers/*/message-categories").access(hasAnyScope(Authorities.MESSAGE_CATEGORIES_WRITE.getAuthority(), Authorities.ADMIN.getAuthority()))
                        .requestMatchers(HttpMethod.DELETE, "/customers/*/message-categories").access(hasAnyScope(Authorities.MANAGER.getAuthority(), Authorities.ADMIN.getAuthority()))
                        // MessageLog Resource
                        .requestMatchers(HttpMethod.POST, "/customers/*/users/*/message-log").access(hasAnyScope(Authorities.MESSAGE_LOG_WRITE.getAuthority(), Authorities.ADMIN.getAuthority()))
                        .requestMatchers(HttpMethod.GET, "/customers/*/users/*/message-log").access(hasScope(Authorities.ADMIN.getAuthority()))
                        .requestMatchers(HttpMethod.GET, "/customers/*/users/*/message-log/*").access(hasAnyScope(Authorities.MESSAGE_LOG_READ.getAuthority(), Authorities.ADMIN.getAuthority()))
                        .requestMatchers(HttpMethod.GET, "/customers/*/message-log/analytics").access(hasAnyScope(Authorities.MESSAGE_LOG_LIST.getAuthority(), Authorities.ADMIN.getAuthority()))
                        .requestMatchers(HttpMethod.GET, "/customers/*/message-log").access(hasScope(Authorities.ADMIN.getAuthority()))
                        .requestMatchers(HttpMethod.GET, "/customers/*/users/*/message-log/analytics").access(hasAnyScope(Authorities.MESSAGE_LOG_LIST.getAuthority(), Authorities.ADMIN.getAuthority()))
                        // ResponseRatings Resource
                        .requestMatchers(HttpMethod.POST, "/customers/users/response-ratings").permitAll()
                        .requestMatchers(HttpMethod.GET, "/customers/*/response-ratings/*").access(hasAnyScope(Authorities.RESPONSE_RATINGS_READ.getAuthority(), Authorities.ADMIN.getAuthority()))
                        .requestMatchers(HttpMethod.GET, "/customers/*/response-ratings").access(hasAnyScope(Authorities.RESPONSE_RATINGS_LIST.getAuthority(), Authorities.ADMIN.getAuthority()))
                        .requestMatchers(HttpMethod.GET, "/customers/*/users/*/response-ratings").access(hasAnyScope(Authorities.RESPONSE_RATINGS_LIST.getAuthority(), Authorities.ADMIN.getAuthority()))
                        .requestMatchers(HttpMethod.GET, "/customers/*/response-ratings/analytics").access(hasAnyScope(Authorities.RESPONSE_RATINGS_LIST.getAuthority(), Authorities.ADMIN.getAuthority()))
                        .requestMatchers(HttpMethod.GET, "/customers/*/users/*/response-ratings/analytics").access(hasAnyScope(Authorities.RESPONSE_RATINGS_LIST.getAuthority(), Authorities.ADMIN.getAuthority()))
                        // Authenticate any request
                        .anyRequest()
                        .authenticated())
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .formLogin(AbstractHttpConfigurer::disable)
                .build();
    }
    // spotless:on

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(false);
        configuration.setAllowedHeaders(
                List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE, reCaptchaHttpHeader));
        configuration.setAllowedMethods(
                Arrays.asList(
                        HttpMethod.POST.name(),
                        HttpMethod.GET.name(),
                        HttpMethod.PUT.name(),
                        HttpMethod.DELETE.name(),
                        HttpMethod.OPTIONS.name()));
        configuration.addAllowedOriginPattern("http://localhost:*");
        configuration.addAllowedOriginPattern("https://*.mail-flow.com");
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
        if (RsaUtil.privateKey == null) {
            throw new MissingEnvVarException("RSA private key");
        }
        JWK jwk = new RSAKey.Builder(RsaUtil.publicKey).privateKey(RsaUtil.privateKey).build();
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    @Primary
    AuthenticationManager userAuthenticationManager() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authenticationProvider);
    }

    @Bean
    AuthenticationManager clientAuthenticationManager() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(clientService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authenticationProvider);
    }

    @Bean
    FilterRegistrationBean<ReCaptchaFilter> reCaptchaFilterRegistration() {
        FilterRegistrationBean<ReCaptchaFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(reCaptchaFilter);
        registrationBean.addUrlPatterns(
                "/auth/token/users",
                "/customers/users/register",
                "/customers/users/enable",
                "/customers/users/password-reset",
                "/customers/users/response-ratings");
        return registrationBean;
    }
}
