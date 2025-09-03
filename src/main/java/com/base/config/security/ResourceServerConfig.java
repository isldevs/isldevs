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
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * @author YISivlay
 */
@Configuration
public class ResourceServerConfig {

    private final JwtDecoder jwtDecoder;
    private final SessionRegistry sessionRegistry;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final OAuth2AuthorizedClientService oauth2AuthorizedClientService;
    private final CustomOAuth2AccessTokenResponseClient accessTokenResponseClient;
    private final OAuth2UserServiceImpl oauth2UserServiceImpl;
    private final OidcUserServiceImpl oidcUserService;
    private final AuthenticationSuccessHandlerImpl authenticationSuccessHandler;

    @Autowired
    public ResourceServerConfig(final JwtDecoder jwtDecoder,
                                final SessionRegistry sessionRegistry,
                                final CustomAuthenticationEntryPoint authenticationEntryPoint,
                                final ClientRegistrationRepository clientRegistrationRepository,
                                final OAuth2AuthorizedClientService oauth2AuthorizedClientService,
                                final CustomOAuth2AccessTokenResponseClient accessTokenResponseClient,
                                final OAuth2UserServiceImpl oauth2UserServiceImpl,
                                final OidcUserServiceImpl oidcUserService,
                                final AuthenticationSuccessHandlerImpl authenticationSuccessHandler) {
        this.jwtDecoder = jwtDecoder;
        this.sessionRegistry = sessionRegistry;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.oauth2AuthorizedClientService = oauth2AuthorizedClientService;
        this.accessTokenResponseClient = accessTokenResponseClient;
        this.oauth2UserServiceImpl = oauth2UserServiceImpl;
        this.oidcUserService = oidcUserService;
        this.authenticationSuccessHandler = authenticationSuccessHandler;
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/oauth2/**",
                                "/api/v1/device/**",
                                "/css/**",
                                "/js/**",
                                "/api/v1/login/**",
                                "/api/v1/error/**",
                                "/api/v1/public/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(c -> c.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .httpBasic(Customizer.withDefaults())
                .formLogin(form -> form.loginPage("/login")
                        .successHandler(authenticationSuccessHandler)
                        .permitAll()
                )
                .oauth2ResourceServer(
                        oauth2 -> oauth2.jwt(jwt -> jwt
                                        .decoder(jwtDecoder)
                                        .jwtAuthenticationConverter(new CustomJwtAuthenticationConverter())
                                )
                                .authenticationEntryPoint(authenticationEntryPoint)
                )
                .oauth2Login(oauth2 -> oauth2
                        .clientRegistrationRepository(clientRegistrationRepository)
                        .authorizedClientService(oauth2AuthorizedClientService)
                        .tokenEndpoint(tokenEndpoint -> tokenEndpoint.accessTokenResponseClient(accessTokenResponseClient))
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oauth2UserServiceImpl)
                                .oidcUserService(oidcUserService))
                        .successHandler(authenticationSuccessHandler)
                        .loginPage("/login").permitAll()
                        .defaultSuccessUrl("/home", true)
                        .failureUrl("/login?error=true")
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().migrateSession()
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(true)
                        .sessionRegistry(sessionRegistry)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .deleteCookies("JSESSIONID", "SESSION")
                        .clearAuthentication(true)
                        .invalidateHttpSession(true)
                )
                .headers(header -> header
                        .cacheControl(HeadersConfigurer.CacheControlConfig::disable)
                );

        return http.build();
    }
}
