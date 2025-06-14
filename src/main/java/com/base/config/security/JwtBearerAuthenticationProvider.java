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


import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author YISivlay
 */
public class JwtBearerAuthenticationProvider implements AuthenticationProvider {

    @Value("${spring.security.oauth2.issuer-uri}")
    private String issuerUri;

    private final JwtDecoder jwtDecoder;
    private final RegisteredClientRepository registeredClientRepository;
    private final OAuth2AuthorizationService authorizationService;
    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;

    public JwtBearerAuthenticationProvider(JwtDecoder jwtDecoder,
                                           RegisteredClientRepository registeredClientRepository,
                                           OAuth2AuthorizationService authorizationService,
                                           OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator) {
        this.jwtDecoder = jwtDecoder;
        this.registeredClientRepository = registeredClientRepository;
        this.authorizationService = authorizationService;
        this.tokenGenerator = tokenGenerator;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        var jwtBearerToken = (JwtBearerAuthenticationToken) authentication;

        var clientId = jwtBearerToken.getClientId();
        var assertion = jwtBearerToken.getAssertion();

        var registeredClient = registeredClientRepository.findByClientId(clientId);
        if (registeredClient == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT), "Invalid client");
        }
        Jwt jwt;
        try {
            jwt = jwtDecoder.decode(assertion);
        } catch (JwtException ex) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST), "Invalid JWT", ex);
        }
        if (!issuerUri.equals(jwt.getIssuer().toString())) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST), "Invalid token issuer");
        }
        if (!jwt.getSubject().contains(registeredClient.getClientId())) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST), "Invalid token audience");
        }
        if (jwt.getExpiresAt() == null || Instant.now().isAfter(jwt.getExpiresAt())) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN), "Token expired");
        }
        var accessTokenValue = UUID.randomUUID().toString();
        var authorizationBuilder = OAuth2Authorization.withRegisteredClient(registeredClient)
                .principalName(clientId)
                .authorizationGrantType(AuthorizationGrantType.JWT_BEARER)
                .attribute("scopes", registeredClient.getScopes())
                .token(new OAuth2AccessToken(
                        OAuth2AccessToken.TokenType.BEARER,
                        accessTokenValue,
                        Instant.now(),
                        Instant.now().plusSeconds(3600)), (_) -> {});
        var authorization = authorizationBuilder.build();
        authorizationService.save(authorization);
        var tokenContext = DefaultOAuth2TokenContext.builder()
                .registeredClient(registeredClient)
                .principal(new JwtAuthenticationToken(jwt))
                .authorization(authorizationBuilder.build())
                .authorizationGrantType(AuthorizationGrantType.JWT_BEARER)
                .authorizationGrant(new JwtBearerAuthenticationToken(clientId, assertion))
                .tokenType(OAuth2TokenType.ACCESS_TOKEN)
                .authorizationServerContext(AuthorizationServerContextHolder.getContext())
                .build();
        var oAuth2Token = tokenGenerator.generate(tokenContext);
        if (oAuth2Token == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR), "Failed to generate access token");
        }
        var accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, oAuth2Token.getTokenValue(), oAuth2Token.getIssuedAt(), oAuth2Token.getExpiresAt());
        Map<String, Object> additionalParameters = new HashMap<>();
        additionalParameters.put("scope", String.join(" ", registeredClient.getScopes()));
        additionalParameters.put("client_id", clientId);
        return new OAuth2AccessTokenAuthenticationToken(
                registeredClient,
                jwtBearerToken,
                accessToken,
                null,
                additionalParameters
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtBearerAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
