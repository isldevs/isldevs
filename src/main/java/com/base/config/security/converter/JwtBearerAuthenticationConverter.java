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


import com.base.config.security.data.JwtBearerAuthenticationToken;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.StringUtils;

/**
 * @author YISivlay
 */
public class JwtBearerAuthenticationConverter implements AuthenticationConverter {
    @Override
    public Authentication convert(HttpServletRequest request) {
        var grantType = request.getParameter("grant_type");
        if (!"urn:ietf:params:oauth:grant-type:jwt-bearer".equals(grantType)) return null;

        var clientId = request.getParameter("client_id");
        var assertion = request.getParameter("assertion");

        if (StringUtils.hasText(clientId) && StringUtils.hasText(assertion)) {
            return new JwtBearerAuthenticationToken(clientId, assertion);
        }
        return null;
    }
}
