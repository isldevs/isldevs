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


import com.base.core.authentication.role.repository.RoleRepository;
import com.base.core.authentication.user.model.User;
import com.base.core.authentication.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.base.core.authentication.user.model.User.resolveRoles;

/**
 * @author YISivlay
 */
@Component
public class OidcUserServiceImpl implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public OidcUserServiceImpl(final UserRepository userRepository,
                               final RoleRepository roleRepository,
                               final PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {

        OidcUserService oidcDelegate = new OidcUserService();
        OidcUser oidcUser = oidcDelegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        Map<String, Object> attributes = oidcUser.getAttributes();

        String username = extractUsername(attributes, registrationId);
        String email = (String) attributes.get("email");
        String fullName = (String) attributes.get("name");
        String avatarUrl = extractAvatarUrl(attributes, registrationId);

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            user = User.builder()
                    .username(username)
                    .email(email)
                    .name(fullName)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .providerId(username)
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
        return oidcUser;
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
