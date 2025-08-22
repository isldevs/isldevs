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
package com.base.core.exception;


import com.base.core.data.ErrorData;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author YISivlay
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorData> handleIllegalArgumentException(IllegalArgumentException ex, Locale locale) {
        var message  = messageSource.getMessage(ex.getMessage(), null, ex.getMessage(), locale);
        return buildResponseEntity(HttpStatus.BAD_REQUEST, message, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorData> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, Locale locale) {
        var message  = messageSource.getMessage(ex.getMessage(), null, ex.getMessage(), locale);
        return buildResponseEntity(HttpStatus.METHOD_NOT_ALLOWED, message, null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorData> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, Locale locale) {
        var message  = messageSource.getMessage(ex.getMessage(), null, ex.getMessage(), locale);
        return buildResponseEntity(HttpStatus.UNSUPPORTED_MEDIA_TYPE, message, null);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorData> handleBadRequestException(BadRequestException ex, Locale locale) {
        var message  = messageSource.getMessage(ex.getMessage(), ex.getArgs(), ex.getMessage(), locale);
        return buildResponseEntity(HttpStatus.BAD_REQUEST, message, ex.getArgs());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorData> handleGenericException(Exception ex, Locale locale) {
        var message  = messageSource.getMessage(ex.getMessage(), null, ex.getMessage(), locale);
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, message, null);
    }

    @ExceptionHandler(value = {AuthorizationDeniedException.class})
    public ResponseEntity<ErrorData> handleAccessDeniedException(AuthorizationDeniedException ex, Locale locale) {
        var headers = new HttpHeaders();

        List<String> args = extract(ex);
        String message = messageSource.getMessage("msg.access.denied",args.toArray(),ex.getMessage(),locale);

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");

        var errorData = ErrorData.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .description("Access Denied")
                .message(message)
                .args(args)
                .build();

        return new ResponseEntity<>(errorData, headers, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorData> handleAuthenticationException(AuthenticationException ex, Locale locale) {
        var message  = messageSource.getMessage(ex.getMessage(), null, ex.getMessage(), locale);
        return buildResponseEntity(HttpStatus.UNAUTHORIZED, message, null);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorData> handleNotFoundException(NotFoundException ex, Locale locale) {
        var message  = messageSource.getMessage(ex.getMessage(), ex.getArgs(), ex.getMessage(), locale);
        return buildResponseEntity(HttpStatus.NOT_FOUND, message, ex.getArgs());
    }

    private ResponseEntity<ErrorData> buildResponseEntity(HttpStatus status, String message, Object[] args) {

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");

        var errorData = ErrorData.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .args(args)
                .build();

        return new ResponseEntity<>(errorData, headers, status);
    }

    private List<String> extract(AuthorizationDeniedException ex) {
        String msg = ex.getMessage();
        if (msg == null) return null;

        List<String> result = new ArrayList<>();
        Pattern singlePattern = Pattern.compile("has(Role|Authority)\\('([^']+)'\\)");
        Matcher singleMatcher = singlePattern.matcher(ex.getAuthorizationResult().toString());
        while (singleMatcher.find()) {
            result.add(singleMatcher.group(2));
        }

        Pattern multiplePattern = Pattern.compile("hasAny(Role|Authority)\\('([^']+)'(?:,'([^']+)')*\\)");
        Matcher multipleMatcher = multiplePattern.matcher(ex.getAuthorizationResult().toString());
        while (multipleMatcher.find()) {
            String group2 = multipleMatcher.group(2);
            if (group2 != null) result.add(group2);
            String group3 = multipleMatcher.group(3);
            if (group3 != null) result.add(group3);
        }

        return result.isEmpty() ? null : result;
    }

}
