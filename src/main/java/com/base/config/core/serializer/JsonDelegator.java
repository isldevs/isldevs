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
package com.base.config.core.serializer;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Set;

/**
 * @author YISivlay
 */
@Component
public class JsonDelegator {

    public JsonElement parseString(String json) {
        JsonElement element = null;
        if (StringUtils.hasText(json)) {
            try {
                element = JsonParser.parseString(json);
            } catch (Exception e) {
                System.err.println("Error parsing JSON: " + e.getMessage());
            }
        }
        return element;
    }

    public boolean hasParameter(String json, String parameter) {
        if (StringUtils.hasText(json) && StringUtils.hasText(parameter)) {
            var element = parseString(json);
            return element != null && element.getAsJsonObject().has(parameter);
        }
        return false;
    }

    public Long extractLong(String parameter, JsonElement jsonElement) {
        Long value = null;
        if (jsonElement.isJsonObject()) {
            final var object = jsonElement.getAsJsonObject();
            if (object.has(parameter) && object.get(parameter).isJsonPrimitive()) {
                final var jsonPrimitive = object.get(parameter).getAsJsonPrimitive();
                if (StringUtils.hasText(jsonPrimitive.getAsString())) {
                    value = Long.valueOf(jsonPrimitive.getAsString());
                }
            }
        }
        return value;
    }

    public String extractString(String parameter, JsonElement jsonElement) {
        String value = null;
        if (jsonElement.isJsonObject()) {
            final var object = jsonElement.getAsJsonObject();
            if (object.has(parameter) && object.get(parameter).isJsonPrimitive()) {
                final var jsonPrimitive = object.get(parameter).getAsJsonPrimitive();
                if (StringUtils.hasText(jsonPrimitive.getAsString())) {
                    value = jsonPrimitive.getAsString();
                }
            }
        }
        return value;
    }

    public Set<String> extractStringArrays(String parameter, JsonElement jsonElement) {
        Set<String> value = new java.util.HashSet<>();
        if (jsonElement != null && jsonElement.isJsonObject()) {
            var object = jsonElement.getAsJsonObject();
            if (object.has(parameter) && object.get(parameter).isJsonArray()) {
                object.get(parameter).getAsJsonArray().forEach(elem -> {
                    if (elem.isJsonPrimitive() && elem.getAsJsonPrimitive().isString()) {
                        value.add(elem.getAsString());
                    }
                });
            }
        }
        return value;
    }

    public BigDecimal extractBigDecimal(String parameter, JsonElement jsonElement) {
        BigDecimal value = null;
        if (jsonElement != null && jsonElement.isJsonObject()) {
            var object = jsonElement.getAsJsonObject();
            if (object.has(parameter) && object.get(parameter).isJsonPrimitive()) {
                var primitive = object.get(parameter).getAsJsonPrimitive();
                if (primitive.isNumber()) {
                    value = primitive.getAsBigDecimal();
                } else if (primitive.isString() && StringUtils.hasText(primitive.getAsString())) {
                    try {
                        value = new BigDecimal(primitive.getAsString());
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return value;
    }

    public Boolean extractBoolean(String parameter, JsonElement jsonElement) {
        Boolean value = null;
        if (jsonElement != null && jsonElement.isJsonObject()) {
            var object = jsonElement.getAsJsonObject();
            if (object.has(parameter) && object.get(parameter).isJsonPrimitive()) {
                var primitive = object.get(parameter).getAsJsonPrimitive();
                if (primitive.isBoolean()) {
                    value = primitive.getAsBoolean();
                } else if (primitive.isString() && StringUtils.hasText(primitive.getAsString())) {
                    value = Boolean.parseBoolean(primitive.getAsString());
                }
            }
        }
        return value;
    }

    public Integer extractInteger(String parameter, JsonElement jsonElement) {
        Integer value = null;
        if (jsonElement != null && jsonElement.isJsonObject()) {
            var object = jsonElement.getAsJsonObject();
            if (object.has(parameter) && object.get(parameter).isJsonPrimitive()) {
                var primitive = object.get(parameter).getAsJsonPrimitive();
                try {
                    value = primitive.getAsInt();
                } catch (NumberFormatException | UnsupportedOperationException ignored) {}
            }
        }
        return value;
    }

    public Double extractDouble(String parameter, JsonElement jsonElement) {
        Double value = null;
        if (jsonElement != null && jsonElement.isJsonObject()) {
            var object = jsonElement.getAsJsonObject();
            if (object.has(parameter) && object.get(parameter).isJsonPrimitive()) {
                var primitive = object.get(parameter).getAsJsonPrimitive();
                try {
                    value = primitive.getAsDouble();
                } catch (NumberFormatException | UnsupportedOperationException ignored) {}
            }
        }
        return value;
    }

    public Float extractFloat(String parameter, JsonElement jsonElement) {
        Float value = null;
        if (jsonElement != null && jsonElement.isJsonObject()) {
            var object = jsonElement.getAsJsonObject();
            if (object.has(parameter) && object.get(parameter).isJsonPrimitive()) {
                var primitive = object.get(parameter).getAsJsonPrimitive();
                try {
                    value = primitive.getAsFloat();
                } catch (NumberFormatException | UnsupportedOperationException ignored) {}
            }
        }
        return value;
    }
}
