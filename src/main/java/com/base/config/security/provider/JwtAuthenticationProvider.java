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


import com.base.config.security.data.ClientAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

/**
 * @author YISivlay
 */
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final RegisteredClientRepository clientRepository;
    private final JwtDecoderFactory<ClientAuthenticationToken> jwtDecoderFactory;

    public JwtAuthenticationProvider(RegisteredClientRepository clientRepository,
                                     JwtDecoderFactory<ClientAuthenticationToken> jwtDecoderFactory) {
        this.clientRepository = clientRepository;
        this.jwtDecoderFactory = jwtDecoderFactory;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        var token = (ClientAuthenticationToken) authentication;

        var client = clientRepository.findByClientId(token.getClientId());
        if (client == null) throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);

        var jwtDecoder = jwtDecoderFactory.createDecoder(token);
        var jwt = jwtDecoder.decode(token.getClientAssertion());

        return new OAuth2ClientAuthenticationToken(client.getClientId(), ClientAuthenticationMethod.PRIVATE_KEY_JWT, jwt, null);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return ClientAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
