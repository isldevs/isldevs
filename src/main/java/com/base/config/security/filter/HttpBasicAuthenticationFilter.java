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
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author YISivlay
 */
@Component
public class HttpBasicAuthenticationFilter extends OncePerRequestFilter {

    private final static Logger LOGGER = LoggerFactory.getLogger(HttpBasicAuthenticationFilter.class);
    private final static String TOKEN_ENDPOINT_URI = "/api/v1/oauth2/token";

    private final ToApiJsonSerializer toApiJsonSerializer;

    @Autowired
    public HttpBasicAuthenticationFilter(ToApiJsonSerializer toApiJsonSerializer) {
        this.toApiJsonSerializer = toApiJsonSerializer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        var stopWatch = new StopWatch();
        stopWatch.start();
        try {
            if ("POST".equals(request.getMethod()) && request.getRequestURL().toString().contains(TOKEN_ENDPOINT_URI)) {
                response.setHeader("Access-Control-Allow-Origin", "*");
                response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                final String requestHeader = request.getHeader("Access-Control-Request-Headers");
                if (null != requestHeader && !requestHeader.isEmpty()) {
                    response.setHeader("Access-Control-Allow-Headers", requestHeader);
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            SecurityContextHolder.getContext().setAuthentication(null);
            response.addHeader("WWW-Authenticate", "Basic realm=\"" + "iSLDevs" + "\"");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } finally {
            stopWatch.stop();
            final var log = RequestLog.builder()
                    .startTime(stopWatch.getStartInstant().toEpochMilli())
                    .stopTime(stopWatch.getStopInstant().toEpochMilli())
                    .duration(stopWatch.getDuration().toMillis())
                    .method(request.getMethod())
                    .url(request.getRequestURL().toString())
                    .parameters(parameters(request))
                    .build();
            LOGGER.info(this.toApiJsonSerializer.serialize(log));
        }
    }

    private Map<String, String[]> parameters(HttpServletRequest request) {
        final var parameters = new HashMap<>(request.getParameterMap());
        parameters.remove("password");
        parameters.remove("_");
        return parameters;
    }
}
