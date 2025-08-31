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


import com.base.config.security.converter.CustomAuthenticationConverter;
import com.base.config.security.converter.OAuth2PasswordAuthenticationConverter;
import com.base.config.security.filter.HttpAuthenticationFilter;
import com.base.config.security.filter.JwtAuthenticationFilter;
import com.base.config.security.provider.JwtBearerAuthenticationProvider;
import com.base.config.security.provider.OAuth2PasswordAuthenticationProvider;
import com.base.config.security.service.CustomTokenErrorResponseHandler;
import com.base.config.security.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientCredentialsAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2RefreshTokenAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2AuthorizationCodeAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2ClientCredentialsAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2RefreshTokenAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.DelegatingAuthenticationConverter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.function.Function;

/**
 * @author YISivlay
 */
@Configuration
public class AuthorizationServerConfig {

    private final MessageSource messageSource;
    private final AuthenticationProvider authenticationProvider;
    private final OAuth2AuthorizationService authorizationService;
    private final OAuth2TokenGenerator<?> tokenGenerator;
    private final RegisteredClientRepository registeredClientRepository;
    private final HttpAuthenticationFilter httpAuthenticationFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtBearerAuthenticationProvider jwtBearerAuthenticationProvider;
    private final CorsConfigurationSource corsConfigurationSource;
    private final UserInfoService userInfoService;

    @Autowired
    public AuthorizationServerConfig(final MessageSource messageSource,
                                     final AuthenticationProvider authenticationProvider,
                                     final OAuth2AuthorizationService authorizationService,
                                     final OAuth2TokenGenerator<?> tokenGenerator,
                                     final RegisteredClientRepository registeredClientRepository,
                                     final HttpAuthenticationFilter httpAuthenticationFilter,
                                     final JwtAuthenticationFilter jwtAuthenticationFilter,
                                     final JwtBearerAuthenticationProvider jwtBearerAuthenticationProvider,
                                     final CorsConfigurationSource corsConfigurationSource,
                                     final UserInfoService userInfoService) {
        this.messageSource = messageSource;
        this.authenticationProvider = authenticationProvider;
        this.authorizationService = authorizationService;
        this.tokenGenerator = tokenGenerator;
        this.registeredClientRepository = registeredClientRepository;
        this.httpAuthenticationFilter = httpAuthenticationFilter;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtBearerAuthenticationProvider = jwtBearerAuthenticationProvider;
        this.corsConfigurationSource = corsConfigurationSource;
        this.userInfoService = userInfoService;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {

        Function<OidcUserInfoAuthenticationContext, OidcUserInfo> userInfoMapper = this.userInfoService::loadUser;
        var configurer = OAuth2AuthorizationServerConfigurer.authorizationServer();
        http
                .securityMatcher(configurer.getEndpointsMatcher())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        "/api/v1/oauth2/token",
                        "/api/v1/oauth2/device_authorization",
                        "/api/v1/oauth2/token/introspect",
                        "/api/v1/oauth2/token/revoke"
                ))
                .with(configurer, (authorizationServer) -> authorizationServer
                        .oidc((oidc) -> oidc.userInfoEndpoint((userInfo) -> userInfo.userInfoMapper(userInfoMapper)))
                        .clientAuthentication(Customizer.withDefaults())
                        .tokenEndpoint(tokenEndpoint -> tokenEndpoint
                                .accessTokenRequestConverter(
                                        new DelegatingAuthenticationConverter(
                                                Arrays.asList(
                                                        new CustomAuthenticationConverter(),
                                                        new OAuth2PasswordAuthenticationConverter(),
                                                        new OAuth2RefreshTokenAuthenticationConverter(),
                                                        new OAuth2AuthorizationCodeAuthenticationConverter(),
                                                        new OAuth2ClientCredentialsAuthenticationConverter()
                                                )
                                        ))
                                .errorResponseHandler(new CustomTokenErrorResponseHandler(messageSource))
                                .authenticationProviders(providers -> {
                                    providers.add(jwtBearerAuthenticationProvider);
                                    providers.add(new OAuth2PasswordAuthenticationProvider(
                                            authenticationProvider,
                                            authorizationService,
                                            tokenGenerator,
                                            registeredClientRepository
                                    ));
                                    providers.add(new OAuth2RefreshTokenAuthenticationProvider(authorizationService, tokenGenerator));
                                    providers.add(new OAuth2ClientCredentialsAuthenticationProvider(authorizationService, tokenGenerator));
                                })
                        ))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/api/v1/oauth2/token",
                                "/api/v1/oauth2/device_authorization",
                                "/api/v1/oauth2/token/introspect",
                                "/api/v1/oauth2/token/revoke",
                                "/.well-known/oauth-authorization-server",
                                "/.well-known/openid-configuration",
                                "/jwks",
                                "/api/v1/device/**",
                                "/api/v1/public/**").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .formLogin(Customizer.withDefaults())
                .addFilterBefore(httpAuthenticationFilter, BasicAuthenticationFilter.class)
                .addFilterAfter(jwtAuthenticationFilter, HttpAuthenticationFilter.class)
                .exceptionHandling((exception) -> exception.defaultAuthenticationEntryPointFor(
                        new LoginUrlAuthenticationEntryPoint("/api/v1/login"),
                        new MediaTypeRequestMatcher(MediaType.TEXT_HTML))
                )
                .headers(headers -> headers
                        .xssProtection(Customizer.withDefaults())
                        .cacheControl(Customizer.withDefaults())
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; " +
                                        "script-src 'self'; " +
                                        "style-src 'self'; " +
                                        "img-src 'self' data:; " +
                                        "font-src 'self'; " +
                                        "connect-src 'self'; " +
                                        "form-action 'self'; " +
                                        "frame-ancestors 'none'; " +
                                        "block-all-mixed-content")
                        )
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        .contentTypeOptions(Customizer.withDefaults())
                        .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                        .permissionsPolicyHeader(permissions -> permissions.policy("geolocation 'none'; midi 'none'; camera 'none'"))
                        .referrerPolicy(referrerPolicyConfig -> referrerPolicyConfig.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                )
                .requiresChannel(channel -> channel
                        .requestMatchers(r -> r.getHeader("X-Forwarded-Proto") != null)
                        .requiresSecure()
                );

        return http.build();
    }
}
