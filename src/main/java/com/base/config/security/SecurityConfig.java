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

import com.base.config.core.authentication.service.CustomUserDetailsService;
import com.base.config.security.filter.HttpAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.*;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2RefreshTokenAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.*;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2AuthorizationCodeAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2ClientCredentialsAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2RefreshTokenAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.DelegatingAuthenticationConverter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author YISivlay
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.issuer-uri}")
    private String issuerUri;

    @Autowired
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(AuthenticationProvider authenticationProvider,
                                                                      OAuth2AuthorizationService authorizationService,
                                                                      @Qualifier("delegatingOAuth2TokenGenerator") OAuth2TokenGenerator<?> tokenGenerator,
                                                                      RegisteredClientRepository registeredClientRepository,
                                                                      HttpAuthenticationFilter httpAuthenticationFilter,
                                                                      HttpSecurity http) throws Exception {
        var configurer = OAuth2AuthorizationServerConfigurer.authorizationServer();
        http
                .securityMatcher(configurer.getEndpointsMatcher())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        new AntPathRequestMatcher("/api/v1/oauth2/**"),
                        new AntPathRequestMatcher("/api/v1/public/**")
                ))
                .with(configurer, (authorizationServer) -> authorizationServer
                        .oidc(Customizer.withDefaults())
                        .tokenEndpoint(tokenEndpoint -> tokenEndpoint
                                .accessTokenRequestConverter(
                                        new DelegatingAuthenticationConverter(
                                                Arrays.asList(
                                                        new OAuth2PasswordAuthenticationConverter(),
                                                        new OAuth2RefreshTokenAuthenticationConverter(),
                                                        new OAuth2AuthorizationCodeAuthenticationConverter(),
                                                        new OAuth2ClientCredentialsAuthenticationConverter()
                                                )
                                        ))
                                .authenticationProviders(providers -> {
                                    providers.add(new OAuth2PasswordAuthenticationProvider(
                                            authenticationProvider,
                                            authorizationService,
                                            tokenGenerator,
                                            registeredClientRepository
                                    ));
                                    providers.add(new OAuth2RefreshTokenAuthenticationProvider(authorizationService, tokenGenerator));
                                })
                        ))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/v1/oauth2/**", "/api/v1/public/**").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .formLogin(Customizer.withDefaults())
                .addFilterBefore(httpAuthenticationFilter, BasicAuthenticationFilter.class)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                );
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/oauth2/**")).permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                );

        return http.build();
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().issuer(issuerUri).build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(CustomUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        var provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
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
        Stream.of(webClient(), serviceClient(), microserviceClient(), deviceClient()).forEach(client -> {
            if (repository.findByClientId(client.getClientId()) == null) {
                repository.save(client);
            }
        });

        return repository;
    }

    @Bean
    public RegisteredClient webClient() {
        return RegisteredClient
                .withId(UUID.randomUUID().toString())
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
                    scopes.add(OidcScopes.PROFILE);
                    scopes.add(OidcScopes.EMAIL);
                    scopes.add("read");
                    scopes.add("write");
                })
                .clientSettings(ClientSettings
                        .builder()
                        .requireProofKey(true)
                        .requireAuthorizationConsent(true)
                        .build())
                .tokenSettings(TokenSettings
                        .builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(30))
                        .refreshTokenTimeToLive(Duration.ofDays(7))
                        .reuseRefreshTokens(true)
                        .idTokenSignatureAlgorithm(SignatureAlgorithm.RS256)
                        .build())
                .build();
    }

    @Bean
    public RegisteredClient serviceClient() {
        return RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientId("service-account")
                .clientSecret(passwordEncoder().encode("secret"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scopes(scopes -> {
                    scopes.add("api.internal");
                    scopes.add("monitoring.read");
                })
                .tokenSettings(
                        TokenSettings.builder()
                                .accessTokenTimeToLive(Duration.ofHours(1))
                                .build()
                )
                .build();
    }

    @Bean
    public RegisteredClient microserviceClient() {
        return RegisteredClient
                .withId(UUID.randomUUID().toString())
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
    public RegisteredClient deviceClient() {
        return RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientId("iot-device")
                .clientSecret(passwordEncoder().encode("secret"))
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
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public OAuth2AuthorizationService authorizationService(JdbcOperations jdbcOperations, RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationService(jdbcOperations, registeredClientRepository);
    }

    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService(JdbcOperations jdbcOperations, RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcOperations, registeredClientRepository);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

}
