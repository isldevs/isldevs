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
package com.base.config.serialization;


import com.base.core.exception.ErrorException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.*;

/**
 * @author YISivlay
 */
@Component
public class JsonHelper {

    private final Locale locale;
    private final Gson gson;
    private final MessageSource messageSource;

    public JsonHelper(MessageSource messageSource) {
        this.locale = LocaleContextHolder.getLocale();
        this.gson = new Gson();
        this.messageSource = messageSource;
    }

    /**
     * Parse JSON string into JsonElement
     */
    public JsonElement parse(final String json) {
        if (StringUtils.isNotBlank(json)) {
            return JsonParser.parseString(json);
        }
        return null;
    }

    /**
     * Convert JSON string to object of type T
     */
    public <T> T fromJson(final String json, final Class<T> classOfT) {
        return this.gson.fromJson(json, classOfT);
    }

    /**
     * Check for unsupported parameters in JSON
     */
    public void unsupportedParameters(final Type typeOfMap,
                                      final String json,
                                      final Collection<String> supportedParams) {
        if (StringUtils.isBlank(json)) {
            String message = messageSource.getMessage("validation.json.invalid", null, "Invalid JSON", locale);
            throw new ErrorException(message);
        }

        final Map<String, Object> requestMap = this.gson.fromJson(json, typeOfMap);
        final List<String> unsupportedParameterList = new ArrayList<>();
        for (final String providedParameter : requestMap.keySet()) {
            if (!supportedParams.contains(providedParameter)) {
                unsupportedParameterList.add(providedParameter);
            }
        }

        if (!unsupportedParameterList.isEmpty()) {
            String message = messageSource.getMessage(
                    "validation.unsupported.parameters",
                    new Object[]{String.join(", ", unsupportedParameterList)},
                    "Unsupported parameters: " + String.join(", ", unsupportedParameterList),
                    locale
            );
            throw new ErrorException(message);
        }
    }

    /**
     * Extract a string value by field name from JsonElement
     */
    public String extractString(final String fieldName, final JsonElement element) {
        if (element == null || element.getAsJsonObject().get(fieldName) == null || element.getAsJsonObject().get(fieldName).isJsonNull()) {
            return null;
        }

        JsonElement valueElement = element.getAsJsonObject().get(fieldName);

        if (!valueElement.isJsonPrimitive() || !valueElement.getAsJsonPrimitive().isString()) {
            throw new ErrorException("validation.string", fieldName);
        }

        return valueElement.getAsString();
    }

    /**
     * Extract a boolean value by field name from JsonElement
     */
    public Boolean extractBoolean(final String fieldName, final JsonElement element) {
        if (element == null || element.getAsJsonObject().get(fieldName) == null || element.getAsJsonObject().get(fieldName).isJsonNull()) {
            return null;
        }
        return element.getAsJsonObject().get(fieldName).getAsBoolean();
    }

    /**
     * Extract JsonElement by field name
     */
    public JsonElement extractJsonElement(final String fieldName, final JsonElement element) {
        if (element == null || element.getAsJsonObject().get(fieldName) == null || element.getAsJsonObject().get(fieldName).isJsonNull()) {
            return null;
        }
        return element.getAsJsonObject().get(fieldName);
    }

    /**
     * Check if a parameter exists in the JSON
     */
    public boolean parameterExists(final String fieldName, final JsonElement element) {
        return element != null
                && element.getAsJsonObject().has(fieldName)
                && !element.getAsJsonObject().get(fieldName).isJsonNull();
    }
}
