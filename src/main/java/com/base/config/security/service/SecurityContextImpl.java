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

import com.base.core.authentication.user.model.User;
import com.base.core.authentication.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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

  @Autowired private UserRepository userRepository;

  @Override
  public User authenticatedUser() {
    org.springframework.security.core.context.SecurityContext context =
        SecurityContextHolder.getContext();
    if (context == null
        || context.getAuthentication() == null
        || context.getAuthentication().getPrincipal() == null) {
      throw new OAuth2AuthenticationException(
          new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN, "Unauthenticated user.", null));
    }
    Object principal = context.getAuthentication().getPrincipal();
    if (principal instanceof User user) {
      return user;
    } else if (principal instanceof Jwt jwt) {
      String username = jwt.getClaimAsString("sub");
      return userRepository
          .findByUsername(username)
          .orElseThrow(
              () ->
                  new OAuth2AuthenticationException(
                      new OAuth2Error(
                          OAuth2ErrorCodes.INVALID_TOKEN, "User not found: " + username, null)));
    } else {
      throw new OAuth2AuthenticationException(
          new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN, "Unsupported principal type.", null));
    }
  }

  @Override
  public boolean isAdmin() {
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication == null || !authentication.isAuthenticated()) {
        return false;
      }
      return authentication.getAuthorities().stream()
          .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    } catch (Exception e) {
      return false;
    }
  }

  public String getCurrentUsername() {
    var context = SecurityContextHolder.getContext();
    if (context == null || context.getAuthentication() == null) {
      return "system";
    }
    return context.getAuthentication().getName();
  }

  @Override
  public boolean hasAuthority(String authority) {
    User user = this.authenticatedUser();
    return user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(authority));
  }
}
