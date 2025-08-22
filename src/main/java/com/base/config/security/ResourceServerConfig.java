package com.base.config.security;


import com.base.config.security.converter.CustomConverter;
import com.base.config.security.service.CustomAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @author YISivlay
 */
@Configuration
public class ResourceServerConfig {

    private final JwtDecoder jwtDecoder;
    private final SessionRegistry sessionRegistry;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    public ResourceServerConfig(final JwtDecoder jwtDecoder,
                                final SessionRegistry sessionRegistry,
                                final CustomAuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtDecoder = jwtDecoder;
        this.sessionRegistry = sessionRegistry;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/oauth2/**",
                                "/api/v1/device/**",
                                "/api/v1/public/**"
                        ).permitAll()
                        .requestMatchers("/api/v1/jwt/**").authenticated()
                        .anyRequest().authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults())
                .oauth2ResourceServer(
                        oauth2 -> oauth2.jwt(jwt -> jwt
                                        .decoder(jwtDecoder)
                                        .jwtAuthenticationConverter(new CustomConverter())
                                )
                                .authenticationEntryPoint(authenticationEntryPoint)
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
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                );

        return http.build();
    }
}
