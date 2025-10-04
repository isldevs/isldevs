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
package com.base.config.security.filter;

import com.base.config.data.RequestLog;
import com.base.config.serialization.ToApiJsonSerializer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * @author YISivlay
 */
@Component
public class HttpAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpAuthenticationFilter.class);

    private static final String TENANT_HEADER = "Tenant-Type";

    private static final String EXPECTED_TENANT = "iSLDevs";

    private static final String TOKEN_ENDPOINT = "/api/v1/oauth2/token";

    private final ToApiJsonSerializer<Object> toApiJsonSerializer;

    private final PasswordEncoder passwordEncoder;

    private final RegisteredClientRepository registeredClientRepository;

    @Autowired
    public HttpAuthenticationFilter(final ToApiJsonSerializer<Object> toApiJsonSerializer,
                                    final PasswordEncoder passwordEncoder,
                                    final RegisteredClientRepository registeredClientRepository) {
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.passwordEncoder = passwordEncoder;
        this.registeredClientRepository = registeredClientRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws IOException {
        var stopWatch = new StopWatch();
        stopWatch.start();
        try {
            if ("POST".equals(request.getMethod()) && request.getRequestURL()
                                                             .toString()
                                                             .contains(TOKEN_ENDPOINT)) {
                if (!"OPTIONS".equals(request.getMethod())) {
                    var tenantHeader = request.getHeader(TENANT_HEADER);
                    if (StringUtils.isBlank(tenantHeader)) {
                        tenantHeader = request.getParameter(TENANT_HEADER);
                    }
                    if (StringUtils.isBlank(tenantHeader)) {
                        throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST),
                                                                "Missing Header Tenant-Type");
                    }
                    if (!EXPECTED_TENANT.equalsIgnoreCase(tenantHeader)) {
                        throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST),
                                                                "Tenant-Type Value Mismatch");
                    }
                    String clientId = request.getParameter("client_id");
                    if (StringUtils.isBlank(clientId)) {
                        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
                        if (StringUtils.isNotBlank(authHeader) && authHeader.startsWith("Basic ")) {
                            String decoded = new String(Base64.getDecoder()
                                                              .decode(authHeader.substring("Basic ".length())
                                                                                .trim()),
                                                        StandardCharsets.UTF_8);
                            String[] parts = decoded.split(":",
                                                           2);
                            if (parts.length == 2) {
                                clientId = parts[0];
                            }
                        }
                    }

                    if (StringUtils.isBlank(clientId)) {
                        throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT),
                                                                "Missing client_id");
                    }

                    var registeredClient = registeredClientRepository.findByClientId(clientId);
                    if (registeredClient == null) {
                        throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT),
                                                                "Client not found");
                    }

                    if (registeredClient.getClientAuthenticationMethods()
                                        .contains(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)) {
                        final var authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
                        if (StringUtils.isBlank(authHeader) || !authHeader.startsWith("Basic ")) {
                            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST),
                                                                    "Missing Basic auth for client_secret_basic client");
                        }
                        final var base64Credentials = authHeader.substring("Basic ".length())
                                                                .trim();
                        final var decoded = new String(Base64.getDecoder()
                                                             .decode(base64Credentials),
                                                       StandardCharsets.UTF_8);
                        final var parts = decoded.split(":",
                                                        2);
                        if (parts.length != 2) {
                            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST),
                                                                    "Invalid Basic authorization format");
                        }
                        final var clientSecret = parts[1];
                        if (!passwordEncoder.matches(clientSecret,
                                                     registeredClient.getClientSecret())) {
                            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT),
                                                                    "Invalid client credentials");
                        }
                    }
                }
            }
            filterChain.doFilter(request,
                                 response);
        } catch (OAuth2AuthenticationException ex) {
            SecurityContextHolder.clearContext();
            clearCookies(response);
            noCacheHeaders(response);
            response.addHeader("WWW-Authenticate",
                               "Basic realm=\"" + EXPECTED_TENANT + "\"");

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            Map<String, Object> errorResponse = Map.of("error",
                                                       ex.getError()
                                                         .getErrorCode(),
                                                       "error_description",
                                                       ex.getMessage());
            response.getWriter()
                    .write(toApiJsonSerializer.serialize(errorResponse));
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            Map<String, Object> errorResponse = Map.of("error",
                                                       "Internal Server Error",
                                                       "error_description",
                                                       ex.getMessage());
            response.getWriter()
                    .write(toApiJsonSerializer.serialize(errorResponse));
        } finally {
            stopWatch.stop();
            var log = RequestLog.builder()
                                .startTime(stopWatch.getStartInstant()
                                                    .getEpochSecond())
                                .stopTime(stopWatch.getStopInstant()
                                                   .getEpochSecond())
                                .method(request.getMethod())
                                .url(request.getRequestURL()
                                            .toString())
                                .parameters(parameters(request))
                                .build();
            LOGGER.info(this.toApiJsonSerializer.serialize(log));
        }
    }

    private void clearCookies(HttpServletResponse response) {
        Cookie cookie = new Cookie("JSESSIONID",
                                   null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private void noCacheHeaders(HttpServletResponse response) {
        response.setHeader("Cache-Control",
                           "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma",
                           "no-cache");
        response.setHeader("Expires",
                           "0");
    }

    private Map<String, String[]> parameters(HttpServletRequest request) {
        final var parameters = new HashMap<>(request.getParameterMap());
        parameters.remove("password");
        parameters.remove("_");
        return parameters;
    }

}
