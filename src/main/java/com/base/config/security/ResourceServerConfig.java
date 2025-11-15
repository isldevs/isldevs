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

import com.base.config.security.converter.CustomJwtAuthenticationConverter;
import com.base.config.security.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * @author YISivlay
 */
@Configuration
@Order(2)
public class ResourceServerConfig {

    private final JwtDecoder jwtDecoder;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final OAuth2AuthorizedClientService oauth2AuthorizedClientService;
    private final OAuth2UserServiceImpl oauth2UserService;
    private final OidcUserServiceImpl oidcUserService;
    private final FederatedIdentityAuthenticationSuccessHandler authenticationSuccessHandler;
    private final CorsConfigurationSource corsConfigurationSource;
    private final AccessTokenResponseClient accessTokenResponseClient;

    @Autowired
    public ResourceServerConfig(final JwtDecoder jwtDecoder,
                                final CustomAuthenticationEntryPoint authenticationEntryPoint,
                                final ClientRegistrationRepository clientRegistrationRepository,
                                final OAuth2AuthorizedClientService oauth2AuthorizedClientService,
                                final OAuth2UserServiceImpl oauth2UserService,
                                final OidcUserServiceImpl oidcUserService,
                                final FederatedIdentityAuthenticationSuccessHandler authenticationSuccessHandler,
                                final CorsConfigurationSource corsConfigurationSource,
                                final AccessTokenResponseClient accessTokenResponseClient) {
        this.jwtDecoder = jwtDecoder;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.oauth2AuthorizedClientService = oauth2AuthorizedClientService;
        this.oauth2UserService = oauth2UserService;
        this.oidcUserService = oidcUserService;
        this.authenticationSuccessHandler = authenticationSuccessHandler;
        this.corsConfigurationSource = corsConfigurationSource;
        this.accessTokenResponseClient = accessTokenResponseClient;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain deviceCodeSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/v1/oauth2/device_verification")
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest()
                        .permitAll())
                .csrf(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder))
                        .authenticationEntryPoint(authenticationEntryPoint));
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/oauth2/**", "/css/**", "/js/**", "/api/v1/login/**", "/login/oauth2/**", "/api/v1/error/**", "/api/v1/public/**")
                .permitAll()
                .anyRequest()
                .authenticated())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers("/api/v1/oauth2/token", "/api/v1/oauth2/token/introspect", "/api/v1/oauth2/token/revoke"))
                .httpBasic(Customizer.withDefaults())
                .formLogin(form -> form.loginPage("/login")
                        .defaultSuccessUrl("/home", true)
                        .successHandler(authenticationSuccessHandler)
                        .permitAll())
                .oauth2Login(oauth2 -> oauth2.clientRegistrationRepository(clientRegistrationRepository)
                        .authorizedClientService(oauth2AuthorizedClientService)
                        .tokenEndpoint(tokenEndpointConfig -> tokenEndpointConfig.accessTokenResponseClient(accessTokenResponseClient))
                        .userInfoEndpoint(userInfo -> userInfo.userService(oauth2UserService)
                                .oidcUserService(oidcUserService))
                        .successHandler(authenticationSuccessHandler)
                        .loginPage("/login")
                        .permitAll()
                        .defaultSuccessUrl("/home", true)
                        .failureUrl("/login?error=true"))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder)
                        .jwtAuthenticationConverter(new CustomJwtAuthenticationConverter()))
                        .authenticationEntryPoint(authenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation()
                        .migrateSession()
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(true))
                .logout(logout -> logout.logoutUrl("/logout")
                        .deleteCookies("JSESSIONID", "SESSION")
                        .clearAuthentication(true)
                        .invalidateHttpSession(true))
                .headers(header -> header.cacheControl(HeadersConfigurer.CacheControlConfig::disable));
        return http.build();
    }

}
