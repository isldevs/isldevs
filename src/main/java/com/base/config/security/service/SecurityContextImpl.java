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

import com.base.config.core.authentication.user.model.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

/**
 * @author YISivlay
 */
@Service
public class SecurityContextImpl implements SecurityContext {

    @Override
    public User authenticatedUser() {
        org.springframework.security.core.context.SecurityContext context = SecurityContextHolder.getContext();
        if (context == null || context.getAuthentication() == null || context.getAuthentication().getPrincipal() == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN, "Unauthenticated user.", null));
        }
        Object principal = context.getAuthentication().getPrincipal();
        if (principal instanceof User user) {
            return user;
        } else if (principal instanceof Jwt jwt) {
            User user = new User();
            user.setUsername(jwt.getClaimAsString("user_id")); // or "username" or whatever claim you use
            return user;
        } else {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN, "Unsupported principal type.", null));
        }
    }

    @Override
    public boolean isAdmin() {
        User user = this.authenticatedUser();
        if (user == null || user.getAuthorities() == null) {
            return false;
        }
        return user.getRoles().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getName()));
    }

    @Override
    public boolean hasAuthority(String authority) {
        User user = this.authenticatedUser();
        return user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
    }

}
