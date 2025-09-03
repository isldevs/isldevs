package com.base.config.security.service;


import com.base.core.authentication.user.repository.UserRepository;
import com.base.core.exception.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * @author YISivlay
 */
@Component
public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {

    private final AuthenticationService authenticationService;
    private final SessionRegistry sessionRegistry;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public AuthenticationSuccessHandlerImpl(final AuthenticationService authenticationService,
                                            final SessionRegistry sessionRegistry,
                                            final UserRepository userRepository,
                                            final ObjectMapper objectMapper) {
        this.authenticationService = authenticationService;
        this.sessionRegistry = sessionRegistry;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        HttpSession session = request.getSession(true);
        sessionRegistry.registerNewSession(session.getId(), authentication.getPrincipal());

        String username = extractUsernameFromAuthentication(authentication);

        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + username));

        String token = authenticationService.generateToken(user);

        boolean isApiClient = isApiRequest(request);

        if (isApiClient) {
            sendJsonResponse(response, token);
        } else {
            setAuthCookie(response, token);
            response.sendRedirect("/home");
        }
    }

    private String extractUsernameFromAuthentication(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof OidcUser oidcUser) {
            return oidcUser.getSubject();
        } else if (principal instanceof OAuth2User oauth2User) {
            return extractUsernameFromOAuth2User(oauth2User);
        } else {
            return authentication.getName();
        }
    }

    private String extractUsernameFromOAuth2User(OAuth2User oauth2User) {
        String username = (String) Optional.ofNullable(oauth2User.getAttribute("login")).orElse(oauth2User.getAttribute("sub"));

        if (username == null) {
            username = oauth2User.getAttribute("id");
        }

        if (username == null) {
            throw new AuthenticationServiceException("Unable to extract username from OAuth2 user");
        }

        return username;
    }

    private boolean isApiRequest(HttpServletRequest request) {
        String acceptHeader = Optional.ofNullable(request.getHeader("Accept")).orElse("");
        String contentTypeHeader = Optional.ofNullable(request.getHeader("Content-Type")).orElse("");

        return acceptHeader.contains("application/json") ||
                contentTypeHeader.contains("application/json") ||
                request.getRequestURI().startsWith("/api/v1/");
    }

    private void sendJsonResponse(HttpServletResponse response, String token) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> responseBody = Map.of(
                "access_token", token,
                "token_type", "Bearer",
                "expires_in", "3600"
        );

        objectMapper.writeValue(response.getWriter(), responseBody);
    }

    private void setAuthCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // Always use secure cookies in production
        cookie.setPath("/");
        cookie.setMaxAge(3600);
        cookie.setAttribute("SameSite", "Lax"); // Prevent CSRF
        response.addCookie(cookie);
    }
}

