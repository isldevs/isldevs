package com.base.config.core.authentication.service;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * @author YISivlay
 */
public class CustomTokenResponseHandler implements AuthenticationSuccessHandler {

    private final HttpMessageConverter<OAuth2AccessTokenResponse> converter = new OAuth2AccessTokenResponseHttpMessageConverter();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2AccessTokenAuthenticationToken tokenAuthentication = (OAuth2AccessTokenAuthenticationToken) authentication;
        OAuth2AccessToken accessToken = tokenAuthentication.getAccessToken();

        OAuth2AccessTokenResponse.Builder builder = OAuth2AccessTokenResponse
                .withToken(accessToken.getTokenValue())
                .tokenType(accessToken.getTokenType())
                .scopes(accessToken.getScopes());

        if (accessToken.getExpiresAt() != null) {
            builder.expiresIn(ChronoUnit.SECONDS.between(Instant.now(), accessToken.getExpiresAt()));
        }

        if (tokenAuthentication.getRefreshToken() != null) {
            builder.refreshToken(tokenAuthentication.getRefreshToken().getTokenValue());
        }

        builder.additionalParameters(Map.of(
                "issued_at", Instant.now().toString(),
                "issuer", "http://localhost:8080/api"
        ));

        ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
        converter.write(builder.build(), null, httpResponse);
    }
}
