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
import com.base.config.security.converter.CustomJwtAuthenticationConverter;
import com.base.config.security.data.ClientAssertionJwtDecoderFactory;
import com.base.config.security.filter.HttpAuthenticationFilter;
import com.base.config.security.filter.JwtAuthenticationFilter;
import com.base.config.security.keypairs.RSAKeyPairService;
import com.base.config.security.provider.JwtAuthenticationProvider;
import com.base.config.security.provider.JwtBearerAuthenticationProvider;
import com.base.config.security.service.*;
import com.base.core.authentication.user.model.User;
import com.base.core.authentication.user.service.CustomUserDetailsService;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.Module;
import java.util.*;
import java.util.function.Function;

import org.hibernate.collection.spi.PersistentSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.*;
import org.springframework.security.oauth2.server.authorization.authentication.*;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationContext;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2AuthorizationCodeAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2ClientCredentialsAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2RefreshTokenAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2TokenExchangeAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.DelegatingAuthenticationConverter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

/**
 * @author YISivlay
 */
@Configuration
public class SecurityConfig {

    @Value("${spring.security.oauth2.issuer-uri:https://localhost:8443/api}")
    private String issuerUri;

    private final MessageSource messageSource;
    private final CustomUserDetailsService userDetailsService;
    private final UserInfoService userInfoService;
    private final JwtDecoder jwtDecoder;
    private final JdbcTemplate jdbcTemplate;
    private final AuthenticationService authenticationService;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final OAuth2TokenGenerator<?> tokenGenerator;
    private final RegisteredClientRepository registeredClientRepository;
    private final CorsConfigurationSource corsConfigurationSource;
    private final FederatedIdentityAuthenticationSuccessHandler authenticationSuccessHandler;
    private final HttpAuthenticationFilter httpAuthenticationFilter;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final OAuth2UserServiceImpl oauth2UserService;
    private final OidcUserServiceImpl oidcUserService;

    @Autowired
    public SecurityConfig(final MessageSource messageSource,
                          final CustomUserDetailsService userDetailsService,
                          final UserInfoService userInfoService,
                          final JwtDecoder jwtDecoder,
                          final JdbcTemplate jdbcTemplate,
                          final AuthenticationService authenticationService,
                          final CustomAuthenticationEntryPoint authenticationEntryPoint,
                          final OAuth2TokenGenerator<?> tokenGenerator,
                          final RegisteredClientRepository registeredClientRepository,
                          final CorsConfigurationSource corsConfigurationSource,
                          final FederatedIdentityAuthenticationSuccessHandler authenticationSuccessHandler,
                          final HttpAuthenticationFilter httpAuthenticationFilter,
                          final ClientRegistrationRepository clientRegistrationRepository,
                          final OAuth2UserServiceImpl oauth2UserService,
                          final OidcUserServiceImpl oidcUserService) {
        this.messageSource = messageSource;
        this.userDetailsService = userDetailsService;
        this.userInfoService = userInfoService;
        this.jwtDecoder = jwtDecoder;
        this.jdbcTemplate = jdbcTemplate;
        this.authenticationService = authenticationService;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.tokenGenerator = tokenGenerator;
        this.registeredClientRepository = registeredClientRepository;
        this.corsConfigurationSource = corsConfigurationSource;
        this.authenticationSuccessHandler = authenticationSuccessHandler;
        this.httpAuthenticationFilter = httpAuthenticationFilter;
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.oauth2UserService = oauth2UserService;
        this.oidcUserService = oidcUserService;
    }

    /**
     * Authorization Server filter chain.
     * - Registers endpoints for token, authorize, jwks, device, PAR, revocation, introspection.
     * - Adds custom token converters and authentication providers (refresh, code, client-credentials, jwt-bearer, token-exchange).
     * - Adds your custom filters: HttpAuthenticationFilter (pre-token sanity checks) and JwtAuthenticationFilter.
     *
     * NOTE: Keep endpointsMatcher() usage — this chain should only match authorization server endpoints.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {

        // map OIDC userinfo using your service
        Function<OidcUserInfoAuthenticationContext, OidcUserInfo> userInfoMapper = this.userInfoService::loadUser;

        var configurer = OAuth2AuthorizationServerConfigurer.authorizationServer();
        http.with(configurer, (authorizationServer) -> authorizationServer.oidc((oidc) -> oidc.userInfoEndpoint((userInfo) -> userInfo
                .userInfoMapper(userInfoMapper)))
                .clientAuthentication(Customizer.withDefaults())
                .tokenEndpoint(tokenEndpoint -> tokenEndpoint
                        // Delegating converter: accept refresh, code, client_credentials, etc.
                        .accessTokenRequestConverter(                                                   //
                                new DelegatingAuthenticationConverter(                                  //
                                        Arrays.asList(                                                  //
                                                new CustomAuthenticationConverter(),                    //
                                                new OAuth2RefreshTokenAuthenticationConverter(),        //
                                                new OAuth2AuthorizationCodeAuthenticationConverter(),   //
                                                new OAuth2ClientCredentialsAuthenticationConverter(),   //
                                                new OAuth2TokenExchangeAuthenticationConverter()        //
                                        )))
                        .errorResponseHandler(new CustomTokenErrorResponseHandler(messageSource))
                        .authenticationProviders(providers -> {
                            providers.add(jwtBearerAuthenticationProvider());
                            providers.add(new OAuth2RefreshTokenAuthenticationProvider(authorizationService(), tokenGenerator));
                            providers.add(new OAuth2ClientCredentialsAuthenticationProvider(authorizationService(), tokenGenerator));
                            providers.add(new OAuth2AuthorizationCodeAuthenticationProvider(authorizationService(), tokenGenerator));
                            providers.add(//
                                    new OAuth2AuthorizationCodeRequestAuthenticationProvider(   //
                                            registeredClientRepository,                         //
                                            authorizationService(),                             //
                                            authorizationConsentService()                       //
                            ));
                            providers.add(                                                      //
                                    new OAuth2AuthorizationConsentAuthenticationProvider(       //
                                            registeredClientRepository,                         //
                                            authorizationService(),                             //
                                            authorizationConsentService()                       //
                            ));
                            // custom token exchange provider (implements RFC 8693) - see bean below
                            providers.add(tokenExchangeAuthenticationProvider());
                        })));

        http.securityMatcher(configurer.getEndpointsMatcher())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf
                        // token endpoints and device endpoints are excluded from CSRF protection.
                        .ignoringRequestMatchers(                       //
                                "/oauth2/token",         // token endpoint
                                "/oauth2/device_authorization",  // device code request
                                "/oauth2/device_verification",   // device verification page
                                "/oauth2/token/introspect",      //
                                "/oauth2/token/revoke"           //
                        ))
                .authorizeHttpRequests(authorize -> authorize               //
                        .requestMatchers(                                   //
                                "/oauth2/token",                   //
                                "/oauth2/device_authorization",    //
                                "/oauth2/token/introspect",        //
                                "/oauth2/token/revoke",            //
                                "/.well-known/oauth-authorization-server",//
                                "/.well-known/openid-configuration",      //
                                "/jwks",                                  //
                                "/device/**",                      //
                                "/css/**",                                //
                                "/js/**",                                 //
                                "/login/**",                       //
                                "/error/**",                       //
                                "/public/**"                       //
                        )
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                // We keep formLogin so browser-based flows still work for interactive OIDC/OAuth2 login
                .httpBasic(Customizer.withDefaults())
                .formLogin(form -> form.loginPage("/login")
                        .loginProcessingUrl("/login") // POST endpoint for credentials
                        .defaultSuccessUrl("/home", true)
                        .successHandler(authenticationSuccessHandler)
                        .permitAll())
                // Your custom pre-token request validations (tenant header, client_secret checks when present)
                .addFilterBefore(httpAuthenticationFilter, BasicAuthenticationFilter.class)
                // JWT authentication filter sets SecurityContext from Authorization header for resource endpoints
                .addFilterAfter(jwtAuthenticationFilter(), HttpAuthenticationFilter.class)
                .exceptionHandling((exception) -> exception.defaultAuthenticationEntryPointFor(//
                        new LoginUrlAuthenticationEntryPoint("/login"),              //
                        new MediaTypeRequestMatcher(MediaType.TEXT_HTML)                       //
                ))
                .headers(headers -> headers.xssProtection(Customizer.withDefaults())
                        .cacheControl(Customizer.withDefaults())
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; " + "script-src 'self'; " + "style-src 'self'; " + "img-src 'self' data:; " + "font-src 'self'; " + "connect-src 'self'; " + "form-action 'self'; " + "frame-ancestors 'none'; " + "block-all-mixed-content"))
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        .contentTypeOptions(Customizer.withDefaults())
                        .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true)
                                .maxAgeInSeconds(31536000))
                        .permissionsPolicyHeader(permissions -> permissions.policy("geolocation 'none'; midi 'none'; camera 'none'"))
                        .referrerPolicy(referrerPolicyConfig -> referrerPolicyConfig
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation()
                        .migrateSession()
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false))
                .logout(logout -> logout.logoutUrl("/logout")
                        .deleteCookies("JSESSIONID", "SESSION")
                        .clearAuthentication(true)
                        .invalidateHttpSession(true))
                // If behind a proxy that sets X-Forwarded-Proto, redirectToHttps enforces https.
                .redirectToHttps(redirectToHttp -> redirectToHttp.requestMatchers(r -> r.getHeader("X-Forwarded-Proto") != null));

        return http.build();
    }

    /**
     * Default resource server filter chain (web + API)
     * - Permits auth endpoints and static resources
     * - Enables form login and oauth2Login for interactive users
     * - Enables jwt resource server for API endpoints
     *
     * IMPORTANT:
     * - This chain is stateful for browser users (IF_REQUIRED).
     * - Device verification uses above state-less chain so it won't redirect to /login.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth //
                .requestMatchers( //
                        "/oauth2/**", //
                        "/css/**",           //
                        "/js/**",            //
                        "/login/**",  //
                        "/login/oauth2/**",  //
                        "/error/**",  //
                        "/public/**") //
                .permitAll()
                .anyRequest()
                .authenticated())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // CSRF tokens stored in cookie for browser clients. Token endpoints that are not browser-based are excluded.
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers( //
                                "/oauth2/token",        //
                                "/oauth2/device_authorization", //
                                "/oauth2/device_verification",  //
                                "/oauth2/token/introspect",     //
                                "/oauth2/token/revoke"))        //
                .httpBasic(Customizer.withDefaults())
                .formLogin(form -> form.loginPage("/login")
                        .defaultSuccessUrl("/home", true)
                        .successHandler(authenticationSuccessHandler)
                        .permitAll())
                .oauth2Login(oauth2 -> oauth2.clientRegistrationRepository(clientRegistrationRepository)
                        .authorizedClientService(authorizedClientService())
                        .tokenEndpoint(tokenEndpointConfig -> tokenEndpointConfig.accessTokenResponseClient(accessTokenResponseClient()))
                        .userInfoEndpoint(userInfo -> userInfo.userService(oauth2UserService)
                                .oidcUserService(oidcUserService))
                        .successHandler(authenticationSuccessHandler)
                        .loginPage("/login")
                        .permitAll()
                        .defaultSuccessUrl("/home", true)
                        .failureUrl("/login?error=true"))
                // JWT resource server for API calls
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder)
                        .jwtAuthenticationConverter(new CustomJwtAuthenticationConverter()))
                        .authenticationEntryPoint(authenticationEntryPoint))
                // Session management: browser interactive users keep session. Device flows are handled by stateless chain above.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation()
                        .migrateSession()
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false))
                .logout(logout -> logout.logoutUrl("/logout")
                        .deleteCookies("JSESSIONID", "SESSION")
                        .clearAuthentication(true)
                        .invalidateHttpSession(true))
                .headers(header -> header.cacheControl(HeadersConfigurer.CacheControlConfig::disable));

        return http.build();
    }

    @Bean
    @Primary
    public OAuth2AuthorizedClientService authorizedClientService() {
        return new JdbcOAuth2AuthorizedClientService(jdbcTemplate, clientRegistrationRepository);
    }

    @Bean
    public JwtBearerAuthenticationProvider jwtBearerAuthenticationProvider() {
        return new JwtBearerAuthenticationProvider(jwtDecoder, registeredClientRepository, authorizationService(), tokenGenerator);
    }

    @Bean
    public JwtAuthenticationProvider jwtAuthenticationProvider(RSAKeyPairService rsaKeyPairService) {
        return new JwtAuthenticationProvider(registeredClientRepository, new ClientAssertionJwtDecoderFactory(rsaKeyPairService));
    }

    @Bean
    public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder) {
        return new ProviderManager(List.of(jwtBearerAuthenticationProvider(), authenticationProvider(passwordEncoder)));
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
    public OAuth2AuthorizationService authorizationService() {
        ObjectMapper objectMapper = new ObjectMapper();
        ClassLoader classLoader = JdbcOAuth2AuthorizationService.class.getClassLoader();
        List<Module> securityModules = SecurityJackson2Modules.getModules(classLoader);
        objectMapper.registerModules(securityModules);
        objectMapper.registerModule(new OAuth2AuthorizationServerJackson2Module());
        try {
            objectMapper.addMixIn(Class.forName("com.base.core.authentication.user.model.User"), User.class);
            objectMapper.addMixIn(Class.forName("org.hibernate.collection.spi.PersistentSet"), PersistentSet.class);
            objectMapper.addMixIn(Class.forName("java.util.HashSet"), HashSet.class);
            objectMapper.addMixIn(Class.forName("java.util.Collections$UnmodifiableSet"), Collections.class);
            objectMapper.addMixIn(Class.forName("java.util.ImmutableCollections$ListN"), ImmutableCollections.class);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        JdbcOAuth2AuthorizationService.OAuth2AuthorizationRowMapper rowMapper = new JdbcOAuth2AuthorizationService.OAuth2AuthorizationRowMapper(registeredClientRepository);
        rowMapper.setObjectMapper(objectMapper);
        JdbcOAuth2AuthorizationService authorizationService = new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
        authorizationService.setAuthorizationRowMapper(rowMapper);

        return authorizationService;
    }

    @Bean
    public OAuth2AuthorizedClientRepository authorizedClientRepository() {
        return new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(authorizedClientService());
    }

    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService() {
        return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository);
    }

    @Bean
    public AuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        var provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(authenticationService, userDetailsService, messageSource);
    }

    @Bean
    public WebClient webClient() {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2 = new ServletOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrationRepository, authorizedClientRepository());
        return WebClient.builder()
                .filter(oauth2)
                .build();
    }

    @Bean
    public AccessTokenResponseClient accessTokenResponseClient() {
        ObjectMapper objectMapper = new ObjectMapper();
        return new AccessTokenResponseClient(webClient(), objectMapper);
    }

    /**
     * Token exchange provider bean.
     * - Implements RFC 8693 (Token Exchange).
     * - Accepts subject_token + subject_token_type and issues a new token per token exchange rules.
     *
     * TODO: implement policy checks (what scopes can be granted, audience mapping, impersonation rules).
     */
    @Bean
    public OAuth2TokenExchangeAuthenticationProvider tokenExchangeAuthenticationProvider() {
        return new OAuth2TokenExchangeAuthenticationProvider(authorizationService(), tokenGenerator);
    }

    /**
     * Authorization server settings (issuer)
     * Keep issuer consistent with SecurityConfig.
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer(issuerUri)
                .build();
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
    public ViewResolver viewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".jsp");
        resolver.setViewClass(JstlView.class);
        return resolver;
    }

    @Bean
    public Validator validator() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setValidationMessageSource(messageSource);
        return validator;
    }

    abstract static class ImmutableCollections {

    }

}
