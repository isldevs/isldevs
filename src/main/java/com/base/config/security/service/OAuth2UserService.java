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


import com.base.core.exception.ErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

/**
 * @author YISivlay
 */
@Component
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final FacebookOAuth2UserService facebookOAuth2UserService;

    @Autowired
    public OAuth2UserService(FacebookOAuth2UserService facebookOAuth2UserService) {
        this.facebookOAuth2UserService = facebookOAuth2UserService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            if ("facebook".equals(registrationId)) {
                return facebookOAuth2UserService.loadUser(userRequest);
            }
            return super.loadUser(userRequest);
        } catch (Exception e) {
            throw new ErrorException(HttpStatus.FORBIDDEN, "msg.internal.error", "OAuth2 user loading fails", e.getMessage());
        }
    }
}
