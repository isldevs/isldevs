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
import com.google.gson.*;
import java.lang.reflect.Type;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * @author YISivlay
 */
@Component
public class JsonHelper {

  private final Gson gson;
  private final MessageSource messageSource;

  public JsonHelper(MessageSource messageSource) {
    this.gson = new Gson();
    this.messageSource = messageSource;
  }

  /** Parse JSON string into JsonElement */
  public JsonElement parse(final String json) {
    if (StringUtils.isNotBlank(json)) {
      return JsonParser.parseString(json);
    }
    return null;
  }

  /** Convert JSON string to object of type T */
  public <T> T fromJson(final String json, final Class<T> classOfT) {
    return this.gson.fromJson(json, classOfT);
  }

  /** Check for unsupported parameters in JSON */
  public void unsupportedParameters(
      final Type typeOfMap, final String json, final Collection<String> supportedParams) {
    Locale locale = LocaleContextHolder.getLocale();
    if (StringUtils.isBlank(json)) {
      String message =
          messageSource.getMessage("validation.json.invalid", null, "Invalid JSON", locale);
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
      String message =
          messageSource.getMessage(
              "validation.unsupported.parameters",
              unsupportedParameterList.toArray(),
              "Unsupported parameters: " + String.join(", ", unsupportedParameterList),
              locale);
      throw new ErrorException(message, unsupportedParameterList.toArray());
    }
  }

  /** Extract a string value by field name from JsonElement */
  public String extractString(final String fieldName, final JsonElement element) {
    if (element == null
        || element.getAsJsonObject().get(fieldName) == null
        || element.getAsJsonObject().get(fieldName).isJsonNull()) {
      return null;
    }

    JsonElement valueElement = element.getAsJsonObject().get(fieldName);

    if (!valueElement.isJsonPrimitive() || !valueElement.getAsJsonPrimitive().isString()) {
      throw new ErrorException(
          HttpStatus.BAD_REQUEST, "msg.internal.error", "Value of param must be string", fieldName);
    }

    return valueElement.getAsString();
  }

  /** Extract a Set<Long> by field name from JsonElement */
  public Set<Long> extractArrayAsLong(final String fieldName, final JsonElement element) {
    if (element == null
        || element.getAsJsonObject().get(fieldName) == null
        || element.getAsJsonObject().get(fieldName).isJsonNull()) {
      return Collections.emptySet();
    }

    JsonElement valueElement = element.getAsJsonObject().get(fieldName);

    if (!valueElement.isJsonArray()) {
      throw new ErrorException(
          HttpStatus.BAD_REQUEST,
          "msg.internal.error",
          "Value of param must be an array of numbers",
          fieldName);
    }

    Set<Long> result = new HashSet<>();
    JsonArray array = valueElement.getAsJsonArray();

    for (JsonElement item : array) {
      if (!item.isJsonPrimitive() || !item.getAsJsonPrimitive().isNumber()) {
        throw new ErrorException(
            HttpStatus.BAD_REQUEST,
            "msg.internal.error",
            "All elements of array must be numbers",
            fieldName);
      }
      try {
        result.add(item.getAsLong());
      } catch (NumberFormatException ex) {
        throw new ErrorException(
            HttpStatus.BAD_REQUEST,
            "msg.internal.error",
            "Invalid number format in array",
            fieldName);
      }
    }

    return result;
  }

  /** Extract a Long value by field name from JsonElement */
  public Long extractLong(final String fieldName, final JsonElement element) {
    if (element == null
        || element.getAsJsonObject().get(fieldName) == null
        || element.getAsJsonObject().get(fieldName).isJsonNull()) {
      return null;
    }

    JsonElement valueElement = element.getAsJsonObject().get(fieldName);

    if (!valueElement.isJsonPrimitive() || !valueElement.getAsJsonPrimitive().isNumber()) {
      throw new ErrorException(
          HttpStatus.BAD_REQUEST, "msg.internal.error", "Value of param must be number", fieldName);
    }

    try {
      return valueElement.getAsLong();
    } catch (NumberFormatException ex) {
      throw new ErrorException(
          HttpStatus.BAD_REQUEST, "msg.internal.error", "Value of param must be number", fieldName);
    }
  }

  /** Extract an Integer value by field name from JsonElement */
  public Integer extractInteger(final String fieldName, final JsonElement element) {
    if (element == null
        || element.getAsJsonObject().get(fieldName) == null
        || element.getAsJsonObject().get(fieldName).isJsonNull()) {
      return null;
    }

    JsonElement valueElement = element.getAsJsonObject().get(fieldName);

    if (!valueElement.isJsonPrimitive() || !valueElement.getAsJsonPrimitive().isNumber()) {
      throw new ErrorException(
          HttpStatus.BAD_REQUEST, "msg.internal.error", "Value of param must be number", fieldName);
    }

    try {
      return valueElement.getAsInt();
    } catch (NumberFormatException ex) {
      throw new ErrorException(
          HttpStatus.BAD_REQUEST, "msg.internal.error", "Value of param must be number", fieldName);
    }
  }

  /** Extract a boolean value by field name from JsonElement */
  public Boolean extractBoolean(final String fieldName, final JsonElement element) {
    if (element == null
        || element.getAsJsonObject().get(fieldName) == null
        || element.getAsJsonObject().get(fieldName).isJsonNull()) {
      return null;
    }
    return element.getAsJsonObject().get(fieldName).getAsBoolean();
  }

  /** Extract JsonElement by field name */
  public JsonElement extractJsonElement(final String fieldName, final JsonElement element) {
    if (element == null
        || element.getAsJsonObject().get(fieldName) == null
        || element.getAsJsonObject().get(fieldName).isJsonNull()) {
      return null;
    }
    return element.getAsJsonObject().get(fieldName);
  }

  /** Check if a parameter exists in the JSON */
  public boolean parameterExists(final String fieldName, final JsonElement element) {
    return element != null
        && element.getAsJsonObject().has(fieldName)
        && !element.getAsJsonObject().get(fieldName).isJsonNull();
  }

  /** Extract a List<JsonObject> by field name from JsonElement */
  public List<JsonObject> extractArrayAsObject(final String fieldName, final JsonElement element) {
    if (element == null
        || element.getAsJsonObject().get(fieldName) == null
        || element.getAsJsonObject().get(fieldName).isJsonNull()) {
      return Collections.emptyList();
    }

    JsonElement valueElement = element.getAsJsonObject().get(fieldName);

    if (!valueElement.isJsonArray()) {
      throw new ErrorException(
          HttpStatus.BAD_REQUEST,
          "msg.internal.error",
          "Value of param must be an array of objects",
          fieldName);
    }

    List<JsonObject> result = new ArrayList<>();
    JsonArray array = valueElement.getAsJsonArray();

    for (JsonElement item : array) {
      if (!item.isJsonObject()) {
        throw new ErrorException(
            HttpStatus.BAD_REQUEST,
            "msg.internal.error",
            "All elements of array must be objects",
            fieldName);
      }
      result.add(item.getAsJsonObject());
    }

    return result;
  }
}
