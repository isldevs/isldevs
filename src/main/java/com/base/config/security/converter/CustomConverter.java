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

import com.base.core.authentication.role.data.RoleDTO;
import com.base.core.authentication.user.model.Authority;
import com.base.core.authentication.role.model.Role;
import com.base.core.authentication.user.model.User;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author YISivlay
 */
public class CustomConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String ROLES = "roles";
    private static final String CLIENT_ID = "client_id";
    private static final String PRINCIPAL_NAME = "user_id";

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Set<Role> roles = extractAuthorities(jwt);

        String principalName = Optional.ofNullable(jwt.getClaimAsString(PRINCIPAL_NAME))
                .or(() -> Optional.ofNullable(jwt.getClaimAsString(CLIENT_ID)))
                .orElse(jwt.getSubject());

        User principal = User.builder()
                .name(principalName)
                .roles(roles)
                .build();

        Collection<GrantedAuthority> authorities = roles.stream()
                .flatMap(role -> role.getAuthorities().stream())
                .map((Authority authority) -> new SimpleGrantedAuthority(authority.getAuthority()))
                .collect(Collectors.toSet());


        return new UsernamePasswordAuthenticationToken(principal, jwt, authorities);
    }

    private Set<Role> extractAuthorities(Jwt jwt) {
        Set<Role> roles = new HashSet<>();
        Object rolesClaim = jwt.getClaims().get(ROLES);

        if (rolesClaim instanceof List<?> roleList) {
            for (Object roleObj : roleList) {
                if (roleObj instanceof Map<?, ?> roleMap) {
                    Object nameObj = roleMap.get("name");
                    Object authoritiesObj = roleMap.get("authorities");

                    if (nameObj != null && authoritiesObj instanceof Collection<?>) {
                        Role role = new Role();
                        role.setName(nameObj.toString());

                        Set<Authority> authorities = ((Collection<?>) authoritiesObj).stream()
                                .filter(Objects::nonNull)
                                .map(Object::toString)
                                .map(authStr -> {
                                    Authority authority = new Authority();
                                    authority.setAuthority(authStr);
                                    authority.setRole(role);
                                    return authority;
                                })
                                .collect(Collectors.toSet());

                        role.setAuthorities(authorities);

                        roles.add(role);
                    }
                }
            }
        }

        return roles;
    }

    private Role toEntityRole(RoleDTO dto) {
        Role role = new Role();
        role.setName(dto.getName());

        Set<Authority> authorities = dto.getAuthorities().stream()
                .map(authStr -> {
                    Authority authority = new Authority();
                    authority.setAuthority(authStr);
                    return authority;
                })
                .collect(Collectors.toSet());

        role.setAuthorities(authorities);
        return role;
    }
}


