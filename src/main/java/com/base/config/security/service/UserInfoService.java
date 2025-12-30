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

import com.base.core.authentication.role.dto.RoleDTO;
import com.base.core.authentication.user.dto.UserInfoData;
import com.base.core.authentication.user.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationContext;
import org.springframework.stereotype.Service;

/**
 * @author YISivlay
 */
@Service
public class UserInfoService {

    private final UserRepository userRepository;

    @Autowired
    public UserInfoService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public OidcUserInfo loadUser(OidcUserInfoAuthenticationContext context) {
        var username = context.getAuthorization()
                .getPrincipalName();

        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        Set<RoleDTO> roles = user.toRoleDTO(user.getRoles());

        var userInfo = UserInfoData.builder()
                .id(user.getId())
                .username(username)
                .name(user.getName())
                .email(user.getEmail())
                .enabled(user.isEnabled())
                .authenticated(true)
                .isAccountNonExpired(user.isAccountNonExpired())
                .isAccountNonLocked(user.isAccountNonLocked())
                .isCredentialsNonExpired(user.isCredentialsNonExpired())
                .roles(roles)
                .build()
                .getClaims();
        Map<String, Object> snakeCaseClaims = toSnakeCaseMap(userInfo);
        return new OidcUserInfo(snakeCaseClaims);
    }

    /**
     * Recursively converts map keys to snake_case
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> toSnakeCaseMap(Map<String, Object> map) {
        return map.entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> toSnakeCase(entry.getKey()), entry -> {
                    Object value = entry.getValue();
                    if (value instanceof Map<?, ?> nestedMap) {
                        return toSnakeCaseMap((Map<String, Object>) nestedMap);
                    } else if (value instanceof Collection<?> collection) {
                        return collection.stream()
                                .map(item -> item instanceof Map<?, ?> m
                                        ? toSnakeCaseMap((Map<String, Object>) m)
                                        : item)
                                .collect(Collectors.toList());
                    } else {
                        return value;
                    }
                }, (_,
                    b) -> b, LinkedHashMap::new));
    }

    /**
     * Converts a camelCase string to snake_case
     */
    public static String toSnakeCase(String str) {
        if (str == null || str.isEmpty())
            return str;

        var sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (Character.isUpperCase(c)) {
                sb.append('_')
                        .append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

}
