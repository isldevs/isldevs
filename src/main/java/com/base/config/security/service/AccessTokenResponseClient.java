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
package com.base.config.security.service;


import com.base.core.exception.ErrorException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.*;

/**
 * @author YISivlay
 */
@Service
public class AccessTokenResponseClient implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public AccessTokenResponseClient(WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest authorizationGrantRequest) {
        ClientRegistration clientRegistration = authorizationGrantRequest.getClientRegistration();

        return webClient.post()
                .uri(clientRegistration.getProviderDetails().getTokenUri())
                .headers(headers -> {
                    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    headers.setBasicAuth(clientRegistration.getClientId(), clientRegistration.getClientSecret());
                })
                .body(BodyInserters.fromFormData(createTokenRequestParameters(authorizationGrantRequest)))
                .retrieve()
                //.onStatus(t -> HttpStatus.isError(t), response ->
                //        response.bodyToMono(String.class)
                //                .flatMap(errorBody -> Mono.error(new OAuth2AuthorizationException(
                //                        "An error occurred while attempting to retrieve the OAuth 2.0 Access Token: " + errorBody)))
                //)
                .bodyToMono(String.class)
                .map(responseBody -> extractTokenResponse(responseBody, clientRegistration))
                .block();
    }

    private MultiValueMap<String, String> createTokenRequestParameters(OAuth2AuthorizationCodeGrantRequest authorizationGrantRequest) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add(OAuth2ParameterNames.GRANT_TYPE, authorizationGrantRequest.getGrantType().getValue());
        parameters.add(OAuth2ParameterNames.CODE, authorizationGrantRequest.getAuthorizationExchange().getAuthorizationResponse().getCode());
        parameters.add(OAuth2ParameterNames.REDIRECT_URI, authorizationGrantRequest.getAuthorizationExchange().getAuthorizationRequest().getRedirectUri());
        parameters.add(OAuth2ParameterNames.CLIENT_ID, authorizationGrantRequest.getClientRegistration().getClientId());

        // Add PKCE parameters if available
        OAuth2AuthorizationRequest authorizationRequest = authorizationGrantRequest.getAuthorizationExchange().getAuthorizationRequest();
        String codeVerifier = (String) authorizationRequest.getAttribute(PkceParameterNames.CODE_VERIFIER);
        if (codeVerifier != null) {
            parameters.add(PkceParameterNames.CODE_VERIFIER, codeVerifier);
        }

        return parameters;
    }

    private OAuth2AccessTokenResponse extractTokenResponse(String responseBody, ClientRegistration clientRegistration) {
        try {
            Map<String, Object> tokenResponseMap = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});

            String accessToken = (String) tokenResponseMap.get(OAuth2ParameterNames.ACCESS_TOKEN);
            String tokenType = (String) tokenResponseMap.get(OAuth2ParameterNames.TOKEN_TYPE);
            long expiresIn = getExpiresIn(tokenResponseMap);
            Set<String> scopes = getScopes(tokenResponseMap);
            String refreshToken = (String) tokenResponseMap.get(OAuth2ParameterNames.REFRESH_TOKEN);
            Map<String, Object> additionalParameters = getAdditionalParameters(tokenResponseMap);

            return OAuth2AccessTokenResponse.withToken(accessToken)
                    .tokenType(new OAuth2AccessToken.TokenType(tokenType.toUpperCase()))
                    .expiresIn(expiresIn)
                    .scopes(scopes)
                    .refreshToken(refreshToken)
                    .additionalParameters(additionalParameters)
                    .build();

        } catch (IOException e) {
            throw new ErrorException("msg.internal.error", "Failed to parse token response", e);
        }
    }

    private long getExpiresIn(Map<String, Object> tokenResponseMap) {
        Object expiresInObj = tokenResponseMap.get(OAuth2ParameterNames.EXPIRES_IN);
        if (expiresInObj instanceof Number) {
            return ((Number) expiresInObj).longValue();
        } else if (expiresInObj instanceof String) {
            return Long.parseLong((String) expiresInObj);
        }
        return 3600L;
    }

    private Set<String> getScopes(Map<String, Object> tokenResponseMap) {
        Object scopeObj = tokenResponseMap.get(OAuth2ParameterNames.SCOPE);
        if (scopeObj instanceof String) {
            return new HashSet<>(Arrays.asList(((String) scopeObj).split(" ")));
        }
        return Collections.emptySet();
    }

    private Map<String, Object> getAdditionalParameters(Map<String, Object> tokenResponseMap) {
        Map<String, Object> additionalParameters = new HashMap<>();
        Set<String> standardParameters = Set.of(
                OAuth2ParameterNames.ACCESS_TOKEN,
                OAuth2ParameterNames.TOKEN_TYPE,
                OAuth2ParameterNames.EXPIRES_IN,
                OAuth2ParameterNames.REFRESH_TOKEN,
                OAuth2ParameterNames.SCOPE
        );

        for (Map.Entry<String, Object> entry : tokenResponseMap.entrySet()) {
            if (!standardParameters.contains(entry.getKey())) {
                additionalParameters.put(entry.getKey(), entry.getValue());
            }
        }

        return additionalParameters;
    }
}
