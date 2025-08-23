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
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;

/**
 * @author YISivlay
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final Logger log = LoggerFactory.getLogger(CustomJwtAuthenticationConverter.class);

    private static final String ROLES_CLAIM = "roles";
    private static final String AUTHORITIES_CLAIM = "authorities";
    private static final String SCOPE_CLAIM = "scope";
    private static final String SCOPES_CLAIM = "scopes";
    private static final String CLIENT_ID = "client_id";
    private static final String USER_ID = "user_id";

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        try {
            Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
            String principalName = getPrincipalName(jwt);
            return new UsernamePasswordAuthenticationToken(
                    principalName,
                    "N/A",
                    authorities
            );

        } catch (Exception e) {
            log.error("Failed to convert JWT to Authentication", e);
            return new UsernamePasswordAuthenticationToken(
                    jwt.getSubject(),
                    "N/A",
                    Collections.emptyList()
            );
        }
    }

    private String getPrincipalName(Jwt jwt) {
        return Optional.ofNullable(jwt.getClaimAsString(USER_ID))
                .or(() -> Optional.ofNullable(jwt.getClaimAsString(CLIENT_ID)))
                .orElse(jwt.getSubject());
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        extractSimpleAuthorities(jwt, authorities);
        extractRoles(jwt, authorities);
        extractScopes(jwt, authorities);
        return authorities;
    }

    private void extractSimpleAuthorities(Jwt jwt, Set<GrantedAuthority> authorities) {
        try {
            Object authoritiesClaim = jwt.getClaim(AUTHORITIES_CLAIM);
            if (authoritiesClaim instanceof Collection) {
                ((Collection<?>) authoritiesClaim).forEach(authority -> {
                    if (authority != null) {
                        authorities.add(new SimpleGrantedAuthority(authority.toString()));
                    }
                });
            }
        } catch (Exception e) {
            log.debug("No simple authorities claim found or error reading it");
        }
    }

    private void extractRoles(Jwt jwt, Set<GrantedAuthority> authorities) {
        try {
            Object rolesClaim = jwt.getClaim(ROLES_CLAIM);
            if (rolesClaim instanceof Collection) {
                ((Collection<?>) rolesClaim).forEach(role -> {
                    if (role != null) {
                        String roleName = role.toString().startsWith("ROLE_") ?
                                role.toString() : "ROLE_" + role;
                        authorities.add(new SimpleGrantedAuthority(roleName));
                    }
                });
            }
        } catch (Exception e) {
            log.debug("No roles claim found or error reading it");
        }
    }

    private void extractScopes(Jwt jwt, Set<GrantedAuthority> authorities) {
        try {
            String scopeClaim = jwt.getClaimAsString(SCOPE_CLAIM);
            if (scopeClaim != null && !scopeClaim.trim().isEmpty()) {
                Arrays.stream(scopeClaim.split(" "))
                        .filter(scope -> !scope.trim().isEmpty())
                        .forEach(scope -> authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope)));
            }
            Object scopesClaim = jwt.getClaim(SCOPES_CLAIM);
            if (scopesClaim instanceof Collection) {
                ((Collection<?>) scopesClaim).forEach(scope -> {
                    if (scope != null) {
                        authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope));
                    }
                });
            }
        } catch (Exception e) {
            log.debug("No scope/scopes claims found or error reading them");
        }
    }
}


