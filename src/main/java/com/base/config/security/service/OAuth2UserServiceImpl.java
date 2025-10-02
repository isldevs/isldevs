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

import static com.base.core.authentication.user.model.User.resolveRoles;

import com.base.core.authentication.role.repository.RoleRepository;
import com.base.core.authentication.user.model.User;
import com.base.core.authentication.user.repository.UserRepository;
import com.base.core.exception.ErrorException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

/**
 * @author YISivlay
 */
@Component
public class OAuth2UserServiceImpl implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

  private final FacebookOAuth2UserService facebookOAuth2UserService;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  @Autowired
  public OAuth2UserServiceImpl(
      final FacebookOAuth2UserService facebookOAuth2UserService,
      final UserRepository userRepository,
      final RoleRepository roleRepository,
      final PasswordEncoder passwordEncoder) {
    this.facebookOAuth2UserService = facebookOAuth2UserService;
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    try {
      String registrationId = userRequest.getClientRegistration().getRegistrationId().toLowerCase();

      OAuth2User oauth2User;
      if ("facebook".equals(registrationId)) {
        oauth2User = facebookOAuth2UserService.loadUser(userRequest);
      } else {
        oauth2User = new DefaultOAuth2UserService().loadUser(userRequest);
      }

      Map<String, Object> attributes = oauth2User.getAttributes();

      String username = extractUsername(attributes, registrationId);
      String fullName = (String) attributes.get("name");
      String email = (String) attributes.get("email");
      String avatarUrl = extractAvatarUrl(attributes, registrationId);

      User user = userRepository.findByUsername(username).orElse(null);
      if (user == null) {
        user =
            User.builder()
                .username(username)
                .email(email)
                .name(fullName)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .providerId(oauth2User.getName())
                .provider(registrationId.toUpperCase())
                .providerAvatarUrl(avatarUrl)
                .roles(resolveRoles(new HashSet<>(Set.of("USER")), roleRepository))
                .build();
      } else {
        user.setProviderAvatarUrl(avatarUrl);
        if (fullName != null) user.setName(fullName);
        if (email != null) user.setEmail(email);
      }

      userRepository.save(user);

      return oauth2User;
    } catch (Exception e) {
      throw new ErrorException(
          HttpStatus.FORBIDDEN, "msg.internal.error", "OAuth2 user loading fails", e.getMessage());
    }
  }

  private String extractUsername(Map<String, Object> attributes, String provider) {
    return switch (provider) {
      case "github" -> (String) attributes.get("login");
      case "google" -> (String) attributes.get("sub");
      case "facebook" -> (String) attributes.get("id");
      default -> UUID.randomUUID().toString();
    };
  }

  private String extractAvatarUrl(Map<String, Object> attributes, String provider) {
    return switch (provider) {
      case "github" -> (String) attributes.get("avatar_url");
      case "google", "facebook" -> (String) attributes.get("picture");
      default -> null;
    };
  }
}
