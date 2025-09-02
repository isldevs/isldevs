/**
 * Copyright 2025 iSLDevs
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.base.config.security;

import com.base.config.GlobalConfig;
import com.base.config.security.data.ClientAssertionJwtDecoderFactory;
import com.base.config.security.keypairs.RSAKeyPairRepository;
import com.base.config.security.provider.JwtAuthenticationProvider;
import com.base.config.security.provider.JwtBearerAuthenticationProvider;
import com.base.config.security.service.JdbcClientRegistrationRepository;
import com.base.core.authentication.user.model.User;
import com.base.core.authentication.user.service.CustomUserDetailsService;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.collection.spi.PersistentSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.function.client.WebClient;

import javax.sql.DataSource;
import java.time.Clock;
import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author YISivlay
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.issuer-uri}")
    private String issuerUri;

    private final GlobalConfig config;
    private final CustomUserDetailsService userDetailsService;

    @Autowired
    public SecurityConfig(final GlobalConfig config,
                          final CustomUserDetailsService userDetailsService) {
        this.config = config;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public RegisteredClient webAppClient() {
        return RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientName("Web && Mobile")
                .clientId("web-app")
                .clientSecret(passwordEncoder().encode("secret"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.PASSWORD)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .redirectUri("http://127.0.0.1:8080/login/oauth2/code/web-app")
                .postLogoutRedirectUri("http://127.0.0.1:8080/")
                .scopes(scopes -> {
                    scopes.add(OidcScopes.OPENID);
                    scopes.add(OidcScopes.EMAIL);
                    scopes.add(OidcScopes.PROFILE);
                    scopes.add(OidcScopes.PHONE);
                    scopes.add(OidcScopes.ADDRESS);
                    scopes.add("read");
                    scopes.add("write");
                })
                .clientSettings(ClientSettings
                        .builder()
                        .requireProofKey(true)
                        .requireAuthorizationConsent(false)
                        .build())
                .tokenSettings(TokenSettings
                        .builder()
                        .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                        .accessTokenTimeToLive(Duration.ofMinutes(30))
                        .refreshTokenTimeToLive(Duration.ofDays(7))
                        .reuseRefreshTokens(false)
                        .idTokenSignatureAlgorithm(SignatureAlgorithm.RS256)
                        .build())
                .build();
    }

    @Bean
    public RegisteredClient serviceM2MClient() {
        return RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientName("Machine to Machine")
                .clientId("m2m")
                .clientSecret(passwordEncoder().encode("secret"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scopes(scopes -> {
                    scopes.add("api.internal");
                    scopes.add("monitoring.read");
                })
                .tokenSettings(
                        TokenSettings.builder()
                                .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                                .accessTokenTimeToLive(Duration.ofHours(1))
                                .build()
                )
                .build();
    }

    @Bean
    public RegisteredClient microserviceClient() {
        return RegisteredClient
                .withId(UUID.randomUUID().toString())
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

    @Bean
    public RegisteredClient deviceClient() {
        return RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientName("IOT Device")
                .clientId("iot-device")
                .clientSecret(passwordEncoder().encode("secret"))
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
    public ClientRegistrationRepository clientRegistrationRepository(JdbcClientRegistrationRepository clientRegistrationRepository) {
        if (clientRegistrationRepository.findByRegistrationId("github") == null) {
            if (githubClientRegistration() != null) {
                clientRegistrationRepository.save(githubClientRegistration());
            }
        }
        if (clientRegistrationRepository.findByRegistrationId("google") == null) {
            if (googleClientRegistration() != null) {
                clientRegistrationRepository.save(googleClientRegistration());
            }
        }
        if (clientRegistrationRepository.findByRegistrationId("facebook") == null) {
            if (googleClientRegistration() != null) {
                clientRegistrationRepository.save(facebookClientRegistration());
            }
        }
        return clientRegistrationRepository;
    }

    @Bean
    @Primary
    public OAuth2AuthorizedClientService authorizedClientService(JdbcTemplate jdbcTemplate,
                                                                 ClientRegistrationRepository clientRegistrationRepository) {
        return new JdbcOAuth2AuthorizedClientService(jdbcTemplate, clientRegistrationRepository);
    }


    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().issuer(issuerUri).build();
    }

    @Bean
    public JwtBearerAuthenticationProvider jwtBearerAuthenticationProvider(JwtDecoder jwtDecoder,
                                                                           RegisteredClientRepository registeredClientRepository,
                                                                           OAuth2AuthorizationService authorizationService,
                                                                           OAuth2TokenGenerator<?> tokenGenerator) {
        return new JwtBearerAuthenticationProvider(jwtDecoder, registeredClientRepository, authorizationService, tokenGenerator);
    }

    @Bean
    public JwtAuthenticationProvider jwtAuthenticationProvider(RegisteredClientRepository registeredClientRepository,
                                                               RSAKeyPairRepository rsaKeyPairRepository) {
        return new JwtAuthenticationProvider(registeredClientRepository, new ClientAssertionJwtDecoderFactory(rsaKeyPairRepository));
    }

    @Bean
    public AuthenticationManager authenticationManager(JwtBearerAuthenticationProvider jwtBearerAuthenticationProvider,
                                                       AuthenticationProvider authenticationProvider) {
        return new ProviderManager(List.of(jwtBearerAuthenticationProvider, authenticationProvider));
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("https://127.0.0.1:8443", "http://127.0.0.1:8080", "http://127.0.0.1:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        var repository = new JdbcRegisteredClientRepository(jdbcTemplate);
        try {
            Stream.of(webAppClient(), serviceM2MClient(), microserviceClient(), deviceClient()).forEach(client -> {
                if (repository.findByClientId(client.getClientId()) == null) {
                    repository.save(client);
                }
            });
        } catch (BadSqlGrammarException ignored) {
        }

        return repository;
    }

    @Bean
    public OAuth2AuthorizationService authorizationService(JdbcOperations jdbcOperations, RegisteredClientRepository registeredClientRepository) throws ClassNotFoundException {
        ObjectMapper objectMapper = new ObjectMapper();
        ClassLoader classLoader = JdbcOAuth2AuthorizationService.class.getClassLoader();
        List<Module> securityModules = SecurityJackson2Modules.getModules(classLoader);
        objectMapper.registerModules(securityModules);
        objectMapper.registerModule(new OAuth2AuthorizationServerJackson2Module());
        objectMapper.addMixIn(Class.forName("com.base.core.authentication.user.model.User"), User.class);
        objectMapper.addMixIn(Class.forName("org.hibernate.collection.spi.PersistentSet"), PersistentSet.class);
        objectMapper.addMixIn(Class.forName("java.util.HashSet"), HashSet.class);
        objectMapper.addMixIn(Class.forName("java.util.Collections$UnmodifiableSet"), Collections.class);
        objectMapper.addMixIn(Class.forName("java.util.ImmutableCollections$ListN"), ImmutableCollections.class);

        JdbcOAuth2AuthorizationService.OAuth2AuthorizationRowMapper rowMapper = new JdbcOAuth2AuthorizationService.OAuth2AuthorizationRowMapper(registeredClientRepository);
        rowMapper.setObjectMapper(objectMapper);
        JdbcOAuth2AuthorizationService authorizationService = new JdbcOAuth2AuthorizationService(jdbcOperations, registeredClientRepository);
        authorizationService.setAuthorizationRowMapper(rowMapper);

        return authorizationService;
    }

    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService(JdbcOperations jdbcOperations, RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcOperations, registeredClientRepository);
    }

    @Bean
    public AuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        var provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public WebClient rest(ClientRegistrationRepository clients, OAuth2AuthorizedClientRepository auth) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2 = new ServletOAuth2AuthorizedClientExchangeFilterFunction(clients, auth);
        return WebClient.builder().filter(oauth2).build();
    }

    abstract static class ImmutableCollections {
    }
}
