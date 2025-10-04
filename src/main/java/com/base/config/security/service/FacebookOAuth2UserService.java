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

import java.util.HashMap;
import java.util.Map;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

/**
 * @author YISivlay
 */
@Component
public class FacebookOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = delegate.loadUser(userRequest);

        var attributes = oauth2User.getAttributes();
        var convertedAttributes = convertFacebookAttributes(attributes);

        return new DefaultOAuth2User(oauth2User.getAuthorities(),
                                     convertedAttributes,
                                     "id");
    }

    private Map<String, Object> convertFacebookAttributes(Map<String, Object> attributes) {
        Map<String, Object> converted = new HashMap<>();

        converted.put("id",
                      attributes.get("id"));
        converted.put("name",
                      attributes.get("name"));
        converted.put("email",
                      attributes.get("email"));
        converted.put("first_name",
                      attributes.get("first_name"));
        converted.put("last_name",
                      attributes.get("last_name"));

        if (attributes.get("picture") instanceof Map<?, ?> picture) {
            if (picture.get("data") instanceof Map<?, ?> data) {
                converted.put("picture",
                              data.get("url"));
            }
        }

        return converted;
    }

}
