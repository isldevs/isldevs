/*
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
package com.base.config.security;


import com.base.config.GlobalConfig;
import com.base.config.security.service.JdbcClientRegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * @author YISivlay
 */
@Configuration
public class ClientConfig {

    @Value("${spring.security.oauth2.issuer-uri:https://localhost:8443/api/v1}")
    private String issuerUri;

    private final GlobalConfig config;
    private final PasswordEncoder passwordEncoder;
    private final JdbcClientRegistrationRepository jdbcClientRegistrationRepository;

    @Autowired
    public ClientConfig(final GlobalConfig config,
                        final PasswordEncoder passwordEncoder,
                        final JdbcClientRegistrationRepository jdbcClientRegistrationRepository) {
        this.config = config;
        this.passwordEncoder = passwordEncoder;
        this.jdbcClientRegistrationRepository = jdbcClientRegistrationRepository;
    }

    /**
     * Web app: public clients using PKCE (requireProofKey = true).
     * Two valid choices:
     * - Public client (no client_secret): ClientAuthenticationMethod.NONE + requireProofKey(true)
     * - Confidential client (client_secret): keep client secret and disallow PKCE by requireProofKey(false)
     *
     * Recommendation: For single page/mobile apps use PKCE (public client). For server-side web apps use confidential client with client secret.
     */
    @Bean
    public RegisteredClient webAppClient() {
        return RegisteredClient.withId(UUID.randomUUID()
                .toString())
                .clientName("Web && Mobile")
                .clientId("web-app")
                // PUBLIC CLIENT
                // Browser/Mobile apps never store secrets
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                // USER-BASED FLOW ONLY
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://127.0.0.1:8080/login/oauth2/code/web-app")
                .postLogoutRedirectUri("http://127.0.0.1:8080/")
                .scopes(scopes -> {
                    scopes.add(OidcScopes.OPENID);
                    scopes.add(OidcScopes.EMAIL);
                    scopes.add(OidcScopes.PROFILE);
                    scopes.add(OidcScopes.PHONE);
                    scopes.add(OidcScopes.ADDRESS);
                    scopes.add("read");
                    scopes.add("offline_access");
                })
                .clientSettings(ClientSettings.builder()
                        // requireProofKey(true) is recommended for public/browser clients using PKCE.
                        .requireProofKey(true)
                        .requireAuthorizationConsent(true)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        // This format SELF_CONTAINED not work revoke token with access_token,
                        // only work with refresh_token
                        .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                        .accessTokenTimeToLive(Duration.ofMinutes(15))
                        .refreshTokenTimeToLive(Duration.ofDays(30))
                        // reuseRefreshTokens(false) -> rotate refresh tokens. Recommended for security.
                        .reuseRefreshTokens(false)
                        .build())
                .build();
    }

    /**
     * API Development / Internal Testing Client
     * Purpose:
     * - Used by developers to test protected APIs directly from tools like Postman or curl
     * without going through a browser login or OAuth2 authorization UI.
     *
     * Security Model:
     * - Permissions are granted via scopes/authorities assigned to the client
     * (e.g. api.development, FULL_ACCESS)
     * - Authorities must be mapped from scopes for method-level security
     *
     * What this client MUST NOT be used for:
     * - End-user authentication
     * - Web or mobile applications
     * - Acting on behalf of a specific user
     */
    @Bean
    public RegisteredClient apiDevelopmentClient() {
        return RegisteredClient.withId(UUID.randomUUID()
                .toString())
                .clientId("api-development")
                .clientName("API Development")
                .clientSecret(passwordEncoder.encode("secret"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                // MACHINE-ONLY FLOW
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scopes(scopes -> {
                    scopes.add("api.development");
                    scopes.add("FULL_ACCESS");
                })
                .tokenSettings(TokenSettings.builder()
                        .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                        .accessTokenTimeToLive(Duration.ofMinutes(10))
                        .build())
                .build();
    }

    /**
     * Token-exchange client (RFC 8693)
     * - Use this client when a service needs to swap a subject token (e.g. user access token) for a new token that contains
     * a different audience/scopes (e.g., impersonation, service acting on behalf).
     * - Not a replacement for password grant. It is used for delegation / token-on-behalf-of.
     */
    @Bean
    public RegisteredClient tokenExchangeClient() {
        return RegisteredClient.withId(UUID.randomUUID()
                .toString())
                .clientId("token-exchange")
                .clientSecret(passwordEncoder.encode("secret"))
                .clientName("Token Exchange")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.TOKEN_EXCHANGE)
                .scopes(scopes -> {
                    scopes.add("user.read");
                    scopes.add("user.write");
                    scopes.add("role.manage");
                })
                .tokenSettings(TokenSettings.builder()
                        .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                        .accessTokenTimeToLive(Duration.ofMinutes(30))
                        .refreshTokenTimeToLive(Duration.ofHours(12))
                        .build())
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(false)
                        .build())
                .build();
    }

    @Bean
    public RegisteredClient serviceM2MClient() {
        return RegisteredClient.withId(UUID.randomUUID()
                .toString())
                .clientName("Machine to Machine")
                .clientId("m2m")
                .clientSecret(passwordEncoder.encode("secret"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scopes(scopes -> {
                    scopes.add("api.internal");
                    scopes.add("monitoring.read");
                })
                .tokenSettings(TokenSettings.builder()
                        .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                        .accessTokenTimeToLive(Duration.ofHours(1))
                        .build())
                .build();
    }

    /**
     * Microservice uses private_key_jwt + jwt-bearer client auth for inter-service authentication.
     * - jwkSetUrl points to your issuer's JWKS or remote key set for the client
     */
    @Bean
    public RegisteredClient microserviceClient() {
        return RegisteredClient.withId(UUID.randomUUID()
                .toString())
                .clientName("Microservice")
                .clientId("microservice")
                .clientAuthenticationMethod(ClientAuthenticationMethod.PRIVATE_KEY_JWT)
                .authorizationGrantType(AuthorizationGrantType.JWT_BEARER)
                .scope("api.write")
                .clientSettings(ClientSettings.builder()
                        .jwkSetUrl(issuerUri + "/oauth2/jwks")
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                        .accessTokenTimeToLive(Duration.ofMinutes(15))
                        .build())
                .build();
    }

    /**
     * Device client: uses device_code grant
     */
    @Bean
    public RegisteredClient deviceClient() {
        return RegisteredClient.withId(UUID.randomUUID()
                .toString())
                .clientName("IOT Device")
                .clientId("iot-device")
                .clientSecret(passwordEncoder.encode("secret"))
                .authorizationGrantType(AuthorizationGrantType.DEVICE_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .scopes(scopes -> {
                    scopes.add(OidcScopes.OPENID);
                    scopes.add(OidcScopes.EMAIL);
                    scopes.add(OidcScopes.PROFILE);
                    scopes.add(OidcScopes.PHONE);
                    scopes.add(OidcScopes.ADDRESS);
                    scopes.add("device.manage");
                })
                .tokenSettings(TokenSettings.builder()
                        .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                        .accessTokenTimeToLive(Duration.ofDays(1))
                        .refreshTokenTimeToLive(Duration.ofDays(30))
                        .build())
                .build();
    }

    @Bean
    public ClientRegistration githubClientRegistration() {
        if (config.getConfigValue("GITHUB_CLIENT_ID") != null && config.getConfigValue("GITHUB_CLIENT_SECRET") != null) {
            return ClientRegistration.withRegistrationId("github")
                    .clientId(config.getConfigValue("GITHUB_CLIENT_ID"))
                    .clientSecret(config.getConfigValue("GITHUB_CLIENT_SECRET"))
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .scope("read:user", "user:email")
                    .authorizationUri("https://github.com/login/oauth/authorize")
                    .tokenUri("https://github.com/login/oauth/access_token")
                    .userInfoUri("https://api.github.com/user")
                    .userNameAttributeName("id")
                    .clientName("GitHub")
                    .build();
        }
        return null;
    }

    @Bean
    public ClientRegistration googleClientRegistration() {
        if (config.getConfigValue("GOOGLE_CLIENT_ID") != null && config.getConfigValue("GOOGLE_CLIENT_SECRET") != null) {
            return ClientRegistration.withRegistrationId("google")
                    .clientId(config.getConfigValue("GOOGLE_CLIENT_ID"))
                    .clientSecret(config.getConfigValue("GOOGLE_CLIENT_SECRET"))
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .scope("openid", "profile", "email")
                    .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                    .tokenUri("https://oauth2.googleapis.com/token")
                    .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                    .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                    .userNameAttributeName(IdTokenClaimNames.SUB)
                    .clientName("Google")
                    .build();
        }
        return null;
    }

    @Bean
    public ClientRegistration facebookClientRegistration() {
        if (config.getConfigValue("FACEBOOK_CLIENT_ID") != null && config.getConfigValue("FACEBOOK_CLIENT_SECRET") != null) {
            return ClientRegistration.withRegistrationId("facebook")
                    .clientId(config.getConfigValue("FACEBOOK_CLIENT_ID"))
                    .clientSecret(config.getConfigValue("FACEBOOK_CLIENT_SECRET"))
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .scope("public_profile", "email")
                    .authorizationUri("https://www.facebook.com/v23.0/dialog/oauth")
                    .tokenUri("https://graph.facebook.com/v23.0/oauth/access_token")
                    .userInfoUri("https://graph.facebook.com/me?fields=id,name,email,picture")
                    .userNameAttributeName("id")
                    .clientName("Facebook")
                    .build();
        }
        return null;
    }

    @Bean
    @Primary
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        var repository = new JdbcRegisteredClientRepository(jdbcTemplate);
        try {
            Stream.of(webAppClient(), apiDevelopmentClient(), tokenExchangeClient(), serviceM2MClient(), microserviceClient(), deviceClient())
                    .forEach(client -> {
                        if (repository.findByClientId(client.getClientId()) == null) {
                            repository.save(client);
                        }
                    });
        } catch (BadSqlGrammarException ignored) {
        }

        return repository;
    }

    @Bean
    @Primary
    public ClientRegistrationRepository clientRegistrationRepository() {
        if (jdbcClientRegistrationRepository.findByRegistrationId("github") == null) {
            if (githubClientRegistration() != null) {
                jdbcClientRegistrationRepository.save(githubClientRegistration());
            }
        }
        if (jdbcClientRegistrationRepository.findByRegistrationId("google") == null) {
            if (googleClientRegistration() != null) {
                jdbcClientRegistrationRepository.save(googleClientRegistration());
            }
        }
        if (jdbcClientRegistrationRepository.findByRegistrationId("facebook") == null) {
            if (facebookClientRegistration() != null) {
                jdbcClientRegistrationRepository.save(facebookClientRegistration());
            }
        }
        return jdbcClientRegistrationRepository;
    }
}
