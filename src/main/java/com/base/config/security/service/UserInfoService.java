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

import com.base.core.authentication.role.model.Role;
import com.base.core.authentication.user.dto.UserInfoData;
import com.base.core.authentication.user.model.Authority;
import com.base.core.authentication.user.repository.UserRepository;
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
    var username = context.getAuthorization().getPrincipalName();

    var user =
        userRepository
            .findByUsername(username)
            .orElseThrow(
                () -> new UsernameNotFoundException("User not found with username: " + username));

    var roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
    var authorities =
        user.getRoles().stream()
            .flatMap(role -> role.getAuthorities().stream())
            .map(Authority::getAuthority)
            .collect(Collectors.toSet());

    var userInfo =
        UserInfoData.builder()
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
            .authorities(authorities)
            .build()
            .getClaims();

    return new OidcUserInfo(userInfo);
  }
}
