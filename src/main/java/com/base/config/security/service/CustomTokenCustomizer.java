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


import com.base.config.core.authentication.user.model.Authority;
import com.base.config.core.authentication.user.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author YISivlay
 */
@Component
public class CustomTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    private static final Logger logger = LoggerFactory.getLogger(CustomTokenCustomizer.class);

    @Override
    public void customize(JwtEncodingContext context) {
        Authentication principal = context.getPrincipal();

        if (context.getTokenType().equals(OAuth2TokenType.ACCESS_TOKEN)) {
            context.getClaims()
                    .subject(principal.getName())
                    .claim("user_id", principal.getName())
                    .claim("client_id", context.getRegisteredClient().getClientId())
                    .claim("scopes", String.join(" ", context.getAuthorizedScopes()))
                    .claim("issued_at", Instant.now().toString())
                    .claim("grant_type", context.getAuthorizationGrantType().getValue());

            if (principal.getPrincipal() instanceof User user) {
                List<Map<String, Object>> roles = user.getRoles().stream()
                        .map(role -> {
                            Map<String, Object> roleMap = new HashMap<>();
                            roleMap.put("name", "ROLE_" + role.getName());
                            roleMap.put("authorities", role.getAuthorities().stream()
                                    .map(Authority::getAuthority)
                                    .distinct()
                                    .toList());
                            return roleMap;
                        }).toList();

                context.getClaims().claim("roles", roles);
            }
            String clientId = context.getRegisteredClient().getClientId();
            context.getClaims().claim("client_type", clientId);

            if (context.getAuthorizedScopes().contains("read")) {
                context.getClaims().claim("can_read", true);
            }
        }
    }
}
