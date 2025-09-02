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
import com.base.core.exception.ErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author YISivlay
 */
@Component
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final FacebookOAuth2UserService facebookOAuth2UserService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public OAuth2UserService(final FacebookOAuth2UserService facebookOAuth2UserService,
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
            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            OAuth2User oauth2User;
            if ("facebook".equals(registrationId)) {
                oauth2User = facebookOAuth2UserService.loadUser(userRequest);
            } else {
                oauth2User = super.loadUser(userRequest);
            }

            var username = (String) oauth2User.getAttributes().get("login");
            var fullName = (String) oauth2User.getAttributes().get("name");
            var email = (String) oauth2User.getAttributes().get("email");
            var avatarUrl = (String) oauth2User.getAttributes().get("avatar_url");
            //User user = userRepository.findByUsername(username).orElseGet(() -> {
            //    User newUser = User.builder()
            //            .username(username)
            //            .email(email)
            //            .name(fullName)
            //            .password(passwordEncoder.encode(UUID.randomUUID().toString()))
            //            .providerId(oauth2User.getName())
            //            .provider(registrationId.toUpperCase())
            //            .providerAvatarUrl(avatarUrl)
            //            .build();
            //        });

            return oauth2User;
        } catch (Exception e) {
            throw new ErrorException(HttpStatus.FORBIDDEN, "msg.internal.error", "OAuth2 user loading fails", e.getMessage());
        }
    }
}
