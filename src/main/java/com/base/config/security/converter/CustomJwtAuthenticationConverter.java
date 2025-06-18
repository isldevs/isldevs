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
package com.base.config.security.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author YISivlay
 */
@Component
public class CustomJwtAuthenticationConverter implements Converter<Jwt, JwtAuthenticationToken> {

    private static final String SCOPE_CLAIM = "scope";
    private static final String SCOPES_CLAIM = "scp";
    private static final String AUTHORITIES_CLAIM = "authorities";
    private static final String CLIENT_ID_CLAIM = "client_id";
    private static final String USER_ID_CLAIM = "user_id";

    @Override
    public JwtAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        String principalName = Optional.ofNullable(jwt.getClaimAsString(USER_ID_CLAIM))
                .orElseGet(() -> Optional.ofNullable(jwt.getClaimAsString(CLIENT_ID_CLAIM))
                        .orElse(jwt.getSubject()));

        return new JwtAuthenticationToken(jwt, authorities, principalName);
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Set<String> roles = new HashSet<>();

        Map<String, Object> claims = jwt.getClaims();
        if (claims.containsKey(SCOPE_CLAIM)) {
            Object scope = jwt.getClaim(SCOPE_CLAIM);
            roles.addAll(parseScopeClaim(scope));
        } else if (claims.containsKey(SCOPES_CLAIM)) {
            Object scopes = jwt.getClaim(SCOPES_CLAIM);
            roles.addAll(parseScopeClaim(scopes));
        }

        if (claims.containsKey(AUTHORITIES_CLAIM)) {
            Object authClaim = jwt.getClaim(AUTHORITIES_CLAIM);
            roles.addAll(parseScopeClaim(authClaim));
        }

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(STR."SCOPE_\{role}"))
                .collect(Collectors.toSet());
    }

    private List<String> parseScopeClaim(Object claim) {
        if (claim instanceof String scopeStr) {
            return Arrays.stream(scopeStr.split("\\s+")).filter(s -> !s.isBlank()).toList();
        } else if (claim instanceof Collection<?> collection) {
            return collection.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .toList();
        }
        return Collections.emptyList();
    }
}
