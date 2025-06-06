/**
 * Copyright 2025 iSLDevs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.base.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.authorization.*;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import javax.sql.DataSource;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author YISivlay
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.issuer-uri}")
    private String issuerUri;


    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http, JdbcTemplate jdbcTemplate) throws Exception {
        var authorizationServerConfigurer = OAuth2AuthorizationServerConfigurer.authorizationServer();
        return http
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .with(authorizationServerConfigurer, (authorizationServer) ->
                        authorizationServer
                                .registeredClientRepository(registeredClientRepository(jdbcTemplate, passwordEncoder()))
                                .authorizationService(authorizationService(jdbcTemplate, registeredClientRepository(jdbcTemplate, passwordEncoder())))
                                .authorizationConsentService(authorizationConsentService(jdbcTemplate, registeredClientRepository(jdbcTemplate, passwordEncoder())))
                                .authorizationServerSettings(authorizationServerSettings())
                                .tokenGenerator(tokenGenerator())
                                .oidc(Customizer.withDefaults()))
                .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain resourceServerFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/v1/**")
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/public/**",
                                "/api/v1/.well-known/**"
                        ).permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/user/**").hasRole("USER")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder(jwkSource()))));

        return http.build();
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer(issuerUri)
                .authorizationEndpoint("/api/v1/oauth2/authorize")
                .deviceAuthorizationEndpoint("/api/v1/oauth2/device_authorization")
                .deviceVerificationEndpoint("/api/v1/oauth2/device_verification")
                .tokenEndpoint("/api/v1/oauth2/token")
                .tokenIntrospectionEndpoint("/api/v1/oauth2/introspect")
                .tokenRevocationEndpoint("/api/v1/oauth2/revoke")
                .jwkSetEndpoint("/api/v1/oauth2/jwks")
                .oidcLogoutEndpoint("/api/v1/connect/logout")
                .oidcUserInfoEndpoint("/api/v1/connect/userinfo")
                .oidcClientRegistrationEndpoint("/api/v1/connect/register")
                .build();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate, PasswordEncoder encoder) {
        var repository = new JdbcRegisteredClientRepository(jdbcTemplate);
        Stream.of(webClient(encoder), serviceClient(encoder), microserviceClient(), deviceClient(encoder))
                .forEach(client -> {
                    if (repository.findByClientId(client.getClientId()) == null) {
                        repository.save(client);
                    }
                });

        return repository;
    }

    @Bean
    public RegisteredClient webClient(PasswordEncoder encoder) {
        return RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("web-app")
                .clientSecret(encoder.encode("secret"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("https://localhost:8443/api/v1/login/oauth2/code/web-app")
                .postLogoutRedirectUri("https://localhost:8443/api/v1/logged-out")
                .scopes(scopes -> {
                    scopes.add(OidcScopes.OPENID);
                    scopes.add(OidcScopes.PROFILE);
                    scopes.add(OidcScopes.EMAIL);
                    scopes.add("api.read");
                })
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(true)
                        .requireAuthorizationConsent(false)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(30))
                        .refreshTokenTimeToLive(Duration.ofDays(7))
                        .reuseRefreshTokens(false)
                        .idTokenSignatureAlgorithm(SignatureAlgorithm.RS256)
                        .build())
                .build();
    }

    @Bean
    public RegisteredClient serviceClient(PasswordEncoder encoder) {
        return RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("service-account")
                .clientSecret(encoder.encode("secret"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scopes(scopes -> {
                    scopes.add("api.internal");
                    scopes.add("monitoring.read");
                })
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofHours(1))
                        .build())
                .build();
    }

    @Bean
    public RegisteredClient microserviceClient() {
        return RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("microservice")
                .clientAuthenticationMethod(ClientAuthenticationMethod.PRIVATE_KEY_JWT)
                .authorizationGrantType(AuthorizationGrantType.JWT_BEARER)
                .scope("api.write")
                .clientSettings(ClientSettings.builder()
                        .jwkSetUrl(issuerUri + "/.well-known/jwks.json")
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(15))
                        .build())
                .build();
    }

    @Bean
    public RegisteredClient deviceClient(PasswordEncoder encoder) {
        return RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("iot-device")
                .clientSecret(encoder.encode("secret"))
                .authorizationGrantType(AuthorizationGrantType.DEVICE_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .scope(OidcScopes.OPENID)
                .scope("device.manage")
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofDays(1))
                        .refreshTokenTimeToLive(Duration.ofDays(30))
                        .build())
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        var rsaKey = generateRsa();
        var jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, _) -> jwkSelector.select(jwkSet);
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        var jwtProcessor = new DefaultJWTProcessor<>();
        jwtProcessor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSource));
        jwtProcessor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(new JWTClaimsSet.Builder().issuer(issuerUri).build(), new HashSet<>(Arrays.asList("sub", "iat", "exp"))));
        return new NimbusJwtDecoder(jwtProcessor);
    }

    @Bean
    public OAuth2TokenGenerator<?> tokenGenerator() {
        var jwtGenerator = new JwtGenerator(new NimbusJwtEncoder(jwkSource()));
        jwtGenerator.setJwtCustomizer(jwtCustomizer());
        var accessTokenGenerator = new OAuth2AccessTokenGenerator();
        var refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
        return new DelegatingOAuth2TokenGenerator(jwtGenerator, accessTokenGenerator, refreshTokenGenerator);
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
        final var clientSpecificClaims = Map.of(
                "web-app", "web-app",
                "service-account", "service-account",
                "microservice", "microservice",
                "iot-device", "iot-device"
        );

        return context -> {
            if (context.getTokenType().equals(OAuth2TokenType.ACCESS_TOKEN)) {
                try {
                    var principal = context.getPrincipal();
                    context.getClaims()
                            .subject(principal.getName())
                            .claim("user_id", principal.getName())
                            .claim("client_id", context.getAuthorizationGrant().getName())
                            .claim("scopes", context.getAuthorizedScopes())
                            .claim("issued_at", Instant.now().toString())
                            .claim("grant_type", context.getAuthorizationGrantType().getValue());

                    var authorities = principal.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toList());
                    context.getClaims().claim("authorities", authorities);

                    String clientId = context.getAuthorizationGrant().getName();
                    if (clientSpecificClaims.containsKey(clientId)) {
                        context.getClaims().claim("special_claim", clientSpecificClaims.get(clientId));
                    }

                    if (context.getAuthorizedScopes().contains("read")) {
                        context.getClaims().claim("can_read", true);
                    }
                } catch (Exception e) {
                    System.err.println("Error customizing access token claims: " + e.getMessage());

                }
            } else if (context.getTokenType().getValue().equals(OidcParameterNames.ID_TOKEN)) {
                try {
                    var principal = context.getPrincipal();
                    context.getClaims().subject(principal.getName()).claim("user_id", principal.getName());
                    var authorities = principal.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toList());
                    context.getClaims().claim("authorities", authorities);
                } catch (Exception e) {
                    System.err.println("Error customizing id token claims: " + e.getMessage());
                }
            }
        };
    }

    private static RSAKey generateRsa() {
        var keyPair = generateRsaKey();
        var publicKey = (RSAPublicKey) keyPair.getPublic();
        var privateKey = (RSAPrivateKey) keyPair.getPrivate();
        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
    }

    private static KeyPair generateRsaKey() {
        try {
            var keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate RSA key pair", ex);
        }
    }

    @Bean
    public OAuth2AuthorizationService authorizationService(JdbcTemplate jdbcTemplate, RegisteredClientRepository clientRepository) {
        return new JdbcOAuth2AuthorizationService(jdbcTemplate, clientRepository);
    }

    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService(JdbcTemplate jdbcTemplate, RegisteredClientRepository clientRepository) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, clientRepository);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}
