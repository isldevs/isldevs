package com.base.config.security;


import com.base.config.security.converter.CustomAuthenticationConverter;
import com.base.config.security.converter.OAuth2PasswordAuthenticationConverter;
import com.base.config.security.filter.HttpAuthenticationFilter;
import com.base.config.security.filter.JwtAuthenticationFilter;
import com.base.config.security.provider.JwtBearerAuthenticationProvider;
import com.base.config.security.provider.OAuth2PasswordAuthenticationProvider;
import com.base.config.security.service.CustomTokenErrorResponseHandler;
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
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2RefreshTokenAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2AuthorizationCodeAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2ClientCredentialsAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2RefreshTokenAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.DelegatingAuthenticationConverter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;

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

    @Autowired
    public AuthorizationServerConfig(final MessageSource messageSource,
                                     final AuthenticationProvider authenticationProvider,
                                     final OAuth2AuthorizationService authorizationService,
                                     final OAuth2TokenGenerator<?> tokenGenerator,
                                     final RegisteredClientRepository registeredClientRepository,
                                     final HttpAuthenticationFilter httpAuthenticationFilter,
                                     final JwtAuthenticationFilter jwtAuthenticationFilter,
                                     final JwtBearerAuthenticationProvider jwtBearerAuthenticationProvider,
                                     final CorsConfigurationSource corsConfigurationSource) {
        this.messageSource = messageSource;
        this.authenticationProvider = authenticationProvider;
        this.authorizationService = authorizationService;
        this.tokenGenerator = tokenGenerator;
        this.registeredClientRepository = registeredClientRepository;
        this.httpAuthenticationFilter = httpAuthenticationFilter;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtBearerAuthenticationProvider = jwtBearerAuthenticationProvider;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {

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
                        .oidc(Customizer.withDefaults())
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
                        new LoginUrlAuthenticationEntryPoint("/login"),
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
