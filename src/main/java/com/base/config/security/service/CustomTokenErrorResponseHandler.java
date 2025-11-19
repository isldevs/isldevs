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

import com.base.core.data.ErrorData;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

/**
 * @author YISivlay
 */
@Component
public class CustomTokenErrorResponseHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MessageSource messageSource;

    public CustomTokenErrorResponseHandler(final MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public void onAuthenticationFailure(final HttpServletRequest request,
                                        final HttpServletResponse response,
                                        final AuthenticationException exception) throws IOException {

        Locale locale = request.getLocale();

        String errorCode = "invalid_request";
        String message = messageSource.getMessage("msg.unauthorized.description", null, "Please login again.", locale);

        if (exception instanceof OAuth2AuthenticationException ex) {
            errorCode = ex.getError()
                    .getErrorCode();
            message = ex.getError()
                    .getDescription() != null
                            ? ex.getError()
                                    .getDescription()
                            : message;
        } else if (exception instanceof BadCredentialsException) {
            errorCode = "invalid_grant";
            message = messageSource.getMessage("msg.invalid.credentials", null, "Invalid username or password.", locale);
        }

        ErrorData errorData = ErrorData.builder()
                .status(HttpServletResponse.SC_BAD_REQUEST)
                .error(errorCode)
                .message(message)
                .description(exception.getMessage())
                .build();

        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        objectMapper.writeValue(response.getOutputStream(), errorData);
    }
}
