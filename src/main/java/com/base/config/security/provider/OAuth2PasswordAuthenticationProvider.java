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
package com.base.config.security.provider;

import com.base.config.security.data.OAuth2PasswordAuthenticationToken;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.stereotype.Service;

/**
 * @author YISivlay
 */
@Service
public class OAuth2PasswordAuthenticationProvider implements AuthenticationProvider {

    private final AuthenticationProvider authenticationProvider;
    private final OAuth2AuthorizationService authorizationService;
    private final OAuth2TokenGenerator<?> tokenGenerator;
    private final RegisteredClientRepository registeredClientRepository;

    @Autowired
    public OAuth2PasswordAuthenticationProvider(AuthenticationProvider authenticationProvider,
                                                OAuth2AuthorizationService authorizationService,
                                                OAuth2TokenGenerator<?> tokenGenerator,
                                                RegisteredClientRepository registeredClientRepository) {
        this.authenticationProvider = authenticationProvider;
        this.authorizationService = authorizationService;
        this.tokenGenerator = tokenGenerator;
        this.registeredClientRepository = registeredClientRepository;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        var authRequest = (OAuth2PasswordAuthenticationToken) authentication;

        var registeredClient = registeredClientRepository.findByClientId(authRequest.getClientId());
        if (registeredClient == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
        }
        var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword());
        var userAuthentication = authenticationProvider.authenticate(usernamePasswordAuthenticationToken);
        if (userAuthentication == null || !userAuthentication.isAuthenticated()) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT), "User authentication failed");
        }
        var authorizationBuilder = OAuth2Authorization.withRegisteredClient(registeredClient)
                .principalName(userAuthentication.getName())
                .authorizationGrantType(new AuthorizationGrantType("password"))
                .attribute(Principal.class.getName(), userAuthentication);

        Set<String> authorizedScopes = authRequest.getScopes();
        if (authorizedScopes == null || authorizedScopes.isEmpty()) {
            authorizedScopes = registeredClient.getScopes();
        }
        authorizationBuilder.attribute("authorized_scopes", authorizedScopes);

        var tokenContext = DefaultOAuth2TokenContext.builder()
                .registeredClient(registeredClient)
                .principal(userAuthentication)
                .authorizedScopes(authorizedScopes)
                .authorization(authorizationBuilder.build())
                .authorizationGrantType(new AuthorizationGrantType("password"))
                .authorizationGrant(authRequest)
                .tokenType(OAuth2TokenType.ACCESS_TOKEN)
                .authorizationServerContext(AuthorizationServerContextHolder.getContext())
                .build();

        var oAuth2Token = tokenGenerator.generate(tokenContext);
        if (oAuth2Token == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR), "Access token generation failed");
        }
        var accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, oAuth2Token.getTokenValue(), oAuth2Token.getIssuedAt(), oAuth2Token
                .getExpiresAt(), authorizedScopes);

        OAuth2RefreshToken refreshToken = null;
        if (registeredClient.getAuthorizationGrantTypes()
                .contains(AuthorizationGrantType.REFRESH_TOKEN)) {
            tokenContext = DefaultOAuth2TokenContext.builder()
                    .registeredClient(registeredClient)
                    .principal(userAuthentication)
                    .authorization(authorizationBuilder.build())
                    .authorizationGrantType(new AuthorizationGrantType("password"))
                    .authorizationGrant(authRequest)
                    .tokenType(OAuth2TokenType.REFRESH_TOKEN)
                    .build();

            refreshToken = (OAuth2RefreshToken) tokenGenerator.generate(tokenContext);
        }

        authorizationBuilder.accessToken(accessToken);
        if (refreshToken != null) {
            authorizationBuilder.refreshToken(refreshToken);
        }
        var authorization = authorizationBuilder.build();
        authorizationService.save(authorization);
        Map<String, Object> additionalParameters = new HashMap<>();
        additionalParameters.put("scope", String.join(" ", authorizedScopes));
        additionalParameters.put("client_id", registeredClient.getClientId());

        return new OAuth2AccessTokenAuthenticationToken(registeredClient, userAuthentication, accessToken, refreshToken, additionalParameters);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2PasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
