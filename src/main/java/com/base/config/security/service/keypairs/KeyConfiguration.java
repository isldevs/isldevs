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
package com.base.config.security.service.keypairs;


import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author YISivlay
 */
@Configuration
public class KeyConfiguration {

    @Bean
    ApplicationListener<ApplicationReadyEvent> applicationReadyListener(ApplicationEventPublisher publisher, RSAKeyPairRepository repository) {
        return _ -> {
            if (repository.findKeyPairs().isEmpty())
                publisher.publishEvent(new RSAKeyPairGenerationRequestEvent(Instant.now()));
        };
    }


    @Bean
    ApplicationListener<RSAKeyPairGenerationRequestEvent> keyPairGenerationRequestListener(Keys keys, RSAKeyPairRepository repository) {
        String keyId = UUID.randomUUID().toString();
        return _ -> repository.save(keys.generateKeyPair(keyId, new Timestamp(System.currentTimeMillis())));
    }

    @Bean
    TextEncryptor textEncryptor(@Value("${jwt.password}") String pw,
                                @Value("${jwt.salt}") String salt) {
        return Encryptors.text(pw, salt);
    }


    @Bean
    NimbusJwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
        final var clientSpecificClaims = Map.of(
                "web-app", "web-app",
                "service-account",
                "service-account",
                "microservice",
                "microservice",
                "iot-device",
                "iot-device"
        );

        return context -> {
            if (context.getTokenType().equals(OAuth2TokenType.ACCESS_TOKEN)) {
                try {
                    var principal = context.getPrincipal();
                    context.getClaims()
                            .subject(principal.getName())
                            .claim("user_id", principal.getName())
                            .claim("client_id", context.getRegisteredClient().getClientId())
                            .claim("scopes", context.getAuthorizedScopes())
                            .claim("issued_at", Instant.now().toString())
                            .claim("grant_type", context.getAuthorizationGrantType().getValue());

                    var authorities = principal.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
                    context.getClaims().claim("authorities", authorities);

                    String clientId = context.getAuthorizationGrant().getName();
                    if (clientSpecificClaims.containsKey(clientId)) {
                        context.getClaims().claim("client_type", clientSpecificClaims.get(clientId));
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
                    context.getClaims().subject(principal.getName())
                            .claim("user_id", principal.getName())
                            .claim("email", principal.getName())
                            .claim("name", principal.getName());
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

    @Bean
    OAuth2TokenGenerator<OAuth2Token> delegatingOAuth2TokenGenerator(JwtEncoder encoder,
                                                                     OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer) {
        var generator = new JwtGenerator(encoder);
        generator.setJwtCustomizer(jwtCustomizer);
        return new DelegatingOAuth2TokenGenerator(generator, new OAuth2AccessTokenGenerator(), new OAuth2RefreshTokenGenerator());
    }
}
