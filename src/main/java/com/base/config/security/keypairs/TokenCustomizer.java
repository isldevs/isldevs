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
package com.base.config.security.keypairs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author YISivlay
 */
@Component
public class TokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    private final RSAKeyPairService rsaKeyPairService;

    @Autowired
    public TokenCustomizer(RSAKeyPairService rsaKeyPairService) {
        this.rsaKeyPairService = rsaKeyPairService;
    }

    @Override
    public void customize(JwtEncodingContext context) {
        rsaKeyPairService.findKeyPairs()
                .stream()
                .max(Comparator.comparing(RSAKeyPairService.RSAKeyPair::created))
                .ifPresent(keyPair -> context.getJwsHeader()
                        .keyId(keyPair.id()));

        if (!OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
            return;
        }
        Authentication principal = context.getPrincipal();

        Set<String> roles = new HashSet<>(principal.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .toList());
        roles.add("ROLE_" + principal.getName()
                .toUpperCase()
                .replace("-", "_"));

        List<String> authorities = new ArrayList<>(principal.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> !a.startsWith("ROLE_"))
                .toList());

        for (String scope : context.getAuthorizedScopes()) {
            authorities.add(scope.toUpperCase());
        }

        context.getClaims()
                .claim("roles", roles)
                .claim("authorities", authorities);
    }

}
