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
package com.base.config.security.data;


import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author YISivlay
 */
public class JwtBearerAuthenticationToken extends AbstractAuthenticationToken {

    private final String clientId;
    private final String assertion;
    private final Set<String> scopes;
    private Object principal;

    public JwtBearerAuthenticationToken(String clientId, String assertion) {
        this(clientId, assertion, Collections.emptySet());
    }

    public JwtBearerAuthenticationToken(String clientId, String assertion, Set<String> scopes) {
        super(null);
        this.clientId = clientId;
        this.assertion = assertion;
        this.scopes = scopes == null ? Collections.emptySet() : Collections.unmodifiableSet(scopes);
        this.setAuthenticated(false);
        this.principal = clientId;
    }

    public JwtBearerAuthenticationToken(Object principal, Set<String> scopes, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.clientId = null;
        this.assertion = null;
        this.scopes = scopes == null ? Collections.emptySet() : Collections.unmodifiableSet(scopes);
        this.setAuthenticated(true);
        this.principal = principal;
    }

    public String getClientId() {
        return clientId;
    }

    public String getAssertion() {
        return assertion;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    @Override
    public Object getCredentials() {
        return assertion;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }


    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        // Convert scopes to authorities with prefix "SCOPE_"
        if (super.getAuthorities() != null && !super.getAuthorities().isEmpty()) {
            return super.getAuthorities();
        }

        return scopes.stream()
                .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                .collect(Collectors.toSet());
    }
}
