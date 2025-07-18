package com.base.config.security.service;


import com.base.config.core.data.ErrorData;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Locale;

/**
 * @author YISivlay
 */
@Component
public class CustomTokenErrorResponseHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MessageSource messageSource;

    public CustomTokenErrorResponseHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        Locale locale = request.getLocale();
        String message = messageSource.getMessage("msg.unauthorized.description", null, "Please login again.", locale);

        ErrorData errorData = ErrorData.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Unauthorized")
                .description(exception.getMessage())
                .build();

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), errorData);
    }
}
