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


import com.base.config.security.service.SecurityContext;
import com.base.core.data.ErrorData;
import com.base.entity.file.controller.FileConstants;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author YISivlay
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;
    private final SecurityContext securityContext;

    public GlobalExceptionHandler(MessageSource messageSource,
                                  SecurityContext securityContext) {
        this.messageSource = messageSource;
        this.securityContext = securityContext;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorData> handleIllegalArgumentException(IllegalArgumentException ex, Locale locale) {
        var localizedMessage  = messageSource.getMessage(ex.getMessage(), null, ex.getMessage(), locale);
        return buildResponseEntity(HttpStatus.BAD_REQUEST, localizedMessage, ex.getMessage(), null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorData> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, Locale locale) {
        var localizedMessage  = messageSource.getMessage(ex.getMessage(), null, ex.getMessage(), locale);
        return buildResponseEntity(HttpStatus.METHOD_NOT_ALLOWED, localizedMessage, ex.getMessage(), null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorData> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, Locale locale) {
        var localizedMessage  = messageSource.getMessage(ex.getMessage(), null, ex.getMessage(), locale);
        return buildResponseEntity(HttpStatus.UNSUPPORTED_MEDIA_TYPE, localizedMessage, ex.getMessage(), null);
    }

    @ExceptionHandler(ErrorException.class)
    public ResponseEntity<ErrorData> handleBadRequestException(ErrorException ex, Locale locale) {
        var localizedMessage  = messageSource.getMessage(ex.getMessage(), ex.getArgs(), ex.getMessage(), locale);

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");

        var errorData = ErrorData.builder()
                .status(ex.getStatus().value())
                .error(ex.getMessage())
                .description(ex.getDescription() != null ? ex.getDescription() : ex.getStatus().getReasonPhrase())
                .message(localizedMessage)
                .args(ex.getArgs())
                .build();

        return new ResponseEntity<>(errorData, headers, ex.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorData> handleGenericException(Exception ex, Locale locale) {
        var localizedMessage  = messageSource.getMessage(ex.getMessage(), null, ex.getMessage(), locale);
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, localizedMessage, ex.getMessage(), null);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorData> handleGenericException(IOException ex, Locale locale) {
        var localizedMessage  = messageSource.getMessage(ex.getMessage(), null, ex.getMessage(), locale);
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, localizedMessage, ex.getMessage(), null);
    }

    @ExceptionHandler(value = {AuthorizationDeniedException.class})
    public ResponseEntity<ErrorData> handleAccessDeniedException(AuthorizationDeniedException ex, Locale locale) {
        List<String> args = extract(ex);
        String localizedMessage = messageSource.getMessage("msg.access.denied",args != null ? args.toArray() : null,ex.getMessage(),locale);
        return buildResponseEntity(HttpStatus.FORBIDDEN, localizedMessage, ex.getMessage(), args != null ? args.toArray() : null);
    }

    @ExceptionHandler(value = {DataIntegrityViolationException.class})
    public ResponseEntity<ErrorData> handleDataIntegrityViolationException(DataIntegrityViolationException ex, Locale locale) {
        var cause = ex.getRootCause() != null ? ex.getRootCause().getMessage() : "";
        String localizedMessage;
        var args = new Object[1];
        if (cause.contains("duplicate key")) {
            var duplicateArgs = new Object[2];
            var result = extractDuplicateKeyAndValue(cause);
            duplicateArgs[1] = result[1];
            args = extractDuplicateArgs(duplicateArgs);
            localizedMessage = messageSource.getMessage("msg.data.integrity.unique", args, locale);
        } else if (cause.contains("foreign key")) {
            args[0] = extractColumnName(cause);
            localizedMessage = messageSource.getMessage("msg.data.integrity.foreign-key", args, locale);
        } else if (cause.contains("not-null")) {
            args[0] = extractColumnName(cause);
            localizedMessage = messageSource.getMessage("msg.data.integrity.not-null", args, locale);
        } else if (cause.contains("constraint")) {
            args[0] = extractColumnName(cause);
            localizedMessage = messageSource.getMessage("msg.data.integrity.constraint", args, locale);
        } else {
            args[0] = extractColumnName(cause);
            localizedMessage = messageSource.getMessage("msg.data.integrity.default", args, locale);
        }

        return buildResponseEntity(HttpStatus.BAD_REQUEST, localizedMessage, ex.getMessage(), args);
    }

    @ExceptionHandler(value = {UnexpectedRollbackException.class})
    public ResponseEntity<ErrorData> handleDataIntegrityViolationException(UnexpectedRollbackException ex, Locale locale) {
        String localizedMessage = messageSource.getMessage("msg.transaction.rollback", null, locale);
        return buildResponseEntity(HttpStatus.BAD_REQUEST, localizedMessage, ex.getMessage(), null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorData> handleAuthenticationException(AuthenticationException ex, Locale locale) {
        var localizedMessage  = messageSource.getMessage(ex.getMessage(), null, ex.getMessage(), locale);
        return buildResponseEntity(HttpStatus.UNAUTHORIZED, localizedMessage, ex.getMessage(), null);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorData> handleNotFoundException(NotFoundException ex, Locale locale) {
        var localizedMessage  = messageSource.getMessage(ex.getMessage(), ex.getArgs(), ex.getMessage(), locale);
        return buildResponseEntity(HttpStatus.NOT_FOUND, localizedMessage, ex.getMessage(), ex.getArgs());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorData> handleNotFoundException(MaxUploadSizeExceededException ex, Locale locale) {
        Object[] maxSizeMB = new Object[]{FileConstants.MAX_FILE_SIZE};
        var localizedMessage  = messageSource.getMessage("validation.file.size", maxSizeMB, ex.getMessage(), locale);
        return buildResponseEntity(HttpStatus.PAYLOAD_TOO_LARGE, localizedMessage, ex.getMessage(), maxSizeMB);
    }

    private Object[] extractDuplicateArgs(Object[] args) {
        if (args == null) {
            return Collections.emptyList().toArray();
        }
        return Arrays.stream(args)
                .filter(Objects::nonNull)
                .toList().toArray();
    }

    private String[] extractDuplicateKeyAndValue(String cause) {
        final Pattern DUPLICATE_KEY_PATTERN = Pattern.compile("Key \\(([^)]+)\\)=\\(([^)]+)\\)");
        Matcher matcher = DUPLICATE_KEY_PATTERN.matcher(cause);
        if (matcher.find()) {
            return new String[]{matcher.group(1), matcher.group(2)};
        }
        return new String[]{"", ""};
    }

    private String extractColumnName(String dbMessage) {
        var start = dbMessage.indexOf('"') + 1;
        var end = dbMessage.indexOf('"', start);
        if (start > 0 && end > start) {
            return dbMessage.substring(start, end);
        }
        return "field";
    }

    private ResponseEntity<ErrorData> buildResponseEntity(HttpStatus status, String message, String error, Object[] args) {

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");

        var errorData = ErrorData.builder()
                .status(status.value())
                .error(error)
                .description(status.getReasonPhrase())
                .message(message)
                .args(args)
                .build();

        return new ResponseEntity<>(errorData, headers, status);
    }

    private List<String> extractAuthoritiesFromExpression(String expr) {
        if (expr == null) return null;
        List<String> result = new ArrayList<>();

        Pattern rolePattern = Pattern.compile("hasRole\\('([^']+)'\\)");
        Matcher roleMatcher = rolePattern.matcher(expr);
        while (roleMatcher.find()) {
            result.add(roleMatcher.group(1)); // just "ADMIN"
        }

        Pattern authPattern = Pattern.compile("hasAuthority\\('([^']+)'\\)|hasAnyAuthority\\(([^)]+)\\)");
        Matcher authMatcher = authPattern.matcher(expr);
        while (authMatcher.find()) {
            if (authMatcher.group(1) != null) { // single authority
                result.add(authMatcher.group(1));
            } else if (authMatcher.group(2) != null) { // multiple authorities
                String[] authorities = authMatcher.group(2).replaceAll("'", "").split(",");
                result.addAll(Arrays.stream(authorities).map(String::trim).toList());
            }
        }

        return result.isEmpty() ? null : result;
    }


    private List<String> extract(AuthorizationDeniedException ex) {
        String msg = ex.getAuthorizationResult().toString();
        if (msg == null) return null;

        List<String> result = new ArrayList<>();

        var singlePattern = Pattern.compile("hasRole\\('([^']+)'\\)");
        var roleMatcher = singlePattern.matcher(ex.getAuthorizationResult().toString());
        while (roleMatcher.find()) {
            result.add(roleMatcher.group(1));
        }

        var multiplePattern = Pattern.compile("hasAuthority\\('([^']+)'\\)|hasAnyAuthority\\(([^)]+)\\)");
        var authorityMatcher = multiplePattern.matcher(ex.getAuthorizationResult().toString());
        while (authorityMatcher.find()) {
            if (authorityMatcher.group(1) != null) {
                result.add(authorityMatcher.group(1));
            } else if (authorityMatcher.group(2) != null) {
                String[] authorities = authorityMatcher.group(2).replaceAll("'", "").split(",");
                result.addAll(Arrays.stream(authorities).map(String::trim).toList());
            }
        }
        if (!this.securityContext.isAdmin() && !result.isEmpty()) {
            result.remove("FULL_ACCESS");
        }

        return result.isEmpty() ? null : result;
    }

}
