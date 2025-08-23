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


import com.base.core.authentication.user.model.Authority;
import com.base.core.authentication.user.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author YISivlay
 */
@Component
public class CustomTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    @Override
    public void customize(JwtEncodingContext context) {
        Authentication principal = context.getPrincipal();

        if (context.getTokenType().equals(OAuth2TokenType.ACCESS_TOKEN)) {
            context.getClaims()
                    .subject(principal.getName())
                    .claim("jti", UUID.randomUUID().toString())
                    .claim("user_id", principal.getName())
                    .claim("client_id", context.getRegisteredClient().getClientId())
                    .claim("scopes", String.join(" ", context.getAuthorizedScopes()))
                    .claim("issued_at", Instant.now().toString())
                    .claim("grant_type", context.getAuthorizationGrantType().getValue());

            Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();
            List<String> roles = authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(authority -> authority.startsWith("ROLE_"))
                    .collect(Collectors.toList());

            List<String> nonRoleAuthorities = authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(authority -> !authority.startsWith("ROLE_"))
                    .toList();

            context.getClaims().claim("roles", roles);
            context.getClaims().claim("authorities", nonRoleAuthorities);

            List<String> allAuthorities = authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            context.getClaims().claim("all_authorities", allAuthorities);

            String clientId = context.getRegisteredClient().getClientId();
            context.getClaims().claim("client_type", clientId);

            if (context.getAuthorizedScopes().contains("read")) {
                context.getClaims().claim("can_read", true);
            }
        }
    }
}
