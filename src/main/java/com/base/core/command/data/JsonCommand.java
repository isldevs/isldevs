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
package com.base.core.command.data;

import com.base.core.serializer.JsonDelegator;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author YISivlay
 */
public final class JsonCommand {

    private final Long id;
    private final String action;
    private final String entity;
    private final String entityType;
    private final Long entityId;
    private final String permission;
    private final String href;
    private final MultipartFile file;
    private final String json;
    private final JsonElement jsonElement;
    private final JsonDelegator jsonDelegator;

    public static class Builder {

        private Long id;
        private String action;
        private String entity;
        private String entityType;
        private Long entityId;
        private String permission;
        private String href;
        private MultipartFile file;
        private String json;
        private JsonElement jsonElement;
        private JsonDelegator jsonDelegator;

        public JsonCommand build() {
            return new JsonCommand(this);
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }
        public Builder action(String action) {
            this.action = action;
            return this;
        }
        public Builder entity(String entity) {
            this.entity = entity;
            this.permission = action + "_" + entity;
            return this;
        }

        public Builder entityType(String entityType) {
            this.entityType = entityType;
            return this;
        }

        public Builder entityId(Long entityId) {
            this.entityId = entityId;
            return this;
        }

        public Builder permission(String permission) {
            this.permission = permission;
            return this;
        }
        public Builder href(String href) {
            this.href = href;
            return this;
        }
        public Builder file(MultipartFile file) {
            this.file = file;
            return this;
        }
        public Builder json(String json) {
            this.json = json;
            return this;
        }
        public Builder jsonElement(JsonElement jsonElement) {
            this.jsonElement = jsonElement;
            return this;
        }
        public Builder jsonDelegator(JsonDelegator jsonDelegator) {
            this.jsonDelegator = jsonDelegator;
            return this;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public JsonCommand(Builder builder) {
        this.id = builder.id;
        this.action = builder.action;
        this.entity = builder.entity;
        this.entityType = builder.entityType;
        this.entityId = builder.entityId;
        this.permission = builder.permission;
        this.href = builder.href;
        this.file = builder.file;
        this.json = builder.json;
        this.jsonElement = builder.jsonElement;
        this.jsonDelegator = builder.jsonDelegator;
    }

    public Long extractLong(String parameter) {
        return this.jsonDelegator.extractLong(parameter, this.jsonElement);
    }

    public String extractString(String parameter) {
        return this.jsonDelegator.extractString(parameter, this.jsonElement);
    }

    public Set<String> extractStringArrays(String parameter) {
        return this.jsonDelegator.extractStringArrays(parameter, this.jsonElement);
    }

    public BigDecimal extractBigDecimal(String parameter) {
        return this.jsonDelegator.extractBigDecimal(parameter, this.jsonElement);
    }

    public Boolean extractBoolean(String parameter) {
        return this.jsonDelegator.extractBoolean(parameter, this.jsonElement);
    }

    public Integer extractInteger(String parameter) {
        return this.jsonDelegator.extractInteger(parameter, this.jsonElement);
    }

    public Double extractDouble(String parameter) {
        return this.jsonDelegator.extractDouble(parameter, this.jsonElement);
    }

    public Float extractFloat(String parameter) {
        return this.jsonDelegator.extractFloat(parameter, this.jsonElement);
    }

    public <T> Set<T> extractArrayAs(String parameter, Class<T> type) {
        Set<T> result = new LinkedHashSet<>();
        if (this.jsonElement != null && this.jsonElement.isJsonObject()) {
            var object = this.jsonElement.getAsJsonObject();
            if (object.has(parameter) && object.get(parameter).isJsonArray()) {
                var array = object.get(parameter).getAsJsonArray();
                for (JsonElement element : array) {
                    T value = null;
                    if (element.isJsonPrimitive()) {
                        var primitive = element.getAsJsonPrimitive();
                        value = convertPrimitiveToType(primitive, type);
                    } else if (element.isJsonObject()) {
                        value = new Gson().fromJson(element, type);
                    }

                    if (value != null) {
                        result.add(value);
                    }
                }
            }
        }
        return result;
    }


    public boolean isChangeAsString(String parameter, String existing) {
        if (this.jsonDelegator.hasParameter(this.json, parameter)) {
            var value = this.extractString(parameter);
            return value != null && !value.equals(existing);
        }
        return false;
    }

    public boolean isChangePassword(String parameter, PasswordEncoder passwordEncoder, String existing) {
        if (this.jsonDelegator.hasParameter(this.json, parameter)) {
            var value = this.extractString(parameter);
            return value != null && existing != null && !passwordEncoder.matches(value, existing);
        }
        return false;
    }

    public boolean isChangeAsLong(String parameter, Long existing) {
        if (this.jsonDelegator.hasParameter(this.json, parameter)) {
            var value = this.extractLong(parameter);
            return value != null && existing != null && !value.equals(existing);
        }
        return false;
    }

    public boolean isChangeAsBigDecimal(String parameter, BigDecimal existing) {
        if (this.jsonDelegator.hasParameter(this.json, parameter)) {
            var value = this.extractBigDecimal(parameter);
            return value != null && existing != null && value.compareTo(existing) != 0;
        }
        return false;
    }

    public boolean isChangeAsBoolean(String parameter, Boolean existing) {
        if (this.jsonDelegator.hasParameter(this.json, parameter)) {
            var value = this.extractBoolean(parameter);
            return value != null && existing != null && !value.equals(existing);
        }
        return false;
    }

    public boolean isChangeAsInteger(String parameter, Integer existing) {
        if (this.jsonDelegator.hasParameter(this.json, parameter)) {
            var value = this.extractInteger(parameter);
            return value != null && existing != null && !value.equals(existing);
        }
        return false;
    }

    public boolean isChangeAsDouble(String parameter, Double existing) {
        if (this.jsonDelegator.hasParameter(this.json, parameter)) {
            var value = this.extractDouble(parameter);
            return value != null && existing != null && !value.equals(existing);
        }
        return false;
    }

    public boolean isChangeAsFloat(String parameter, Float existing) {
        if (this.jsonDelegator.hasParameter(this.json, parameter)) {
            var value = this.extractFloat(parameter);
            return value != null && existing != null && !value.equals(existing);
        }
        return false;
    }

    public <T> boolean isChangeAsArray(String parameter, Collection<T> existing, Class<T> type) {
        if (!jsonDelegator.hasParameter(this.json, parameter)) {
            return false;
        }
        var extracted = this.extractArrayAs(parameter, type);
        if (existing == null) {
            return true;
        }
        return !new HashSet<>(extracted).equals(new HashSet<>(existing));
    }

    @SuppressWarnings("unchecked")
    private <T> T convertPrimitiveToType(JsonPrimitive primitive, Class<T> type) {
        try {
            if (type == String.class) {
                return (T) primitive.getAsString();
            } else if (type == Integer.class) {
                return (T) Integer.valueOf(primitive.getAsInt());
            } else if (type == Long.class) {
                return (T) Long.valueOf(primitive.getAsLong());
            } else if (type == Double.class) {
                return (T) Double.valueOf(primitive.getAsDouble());
            } else if (type == Float.class) {
                return (T) Float.valueOf(primitive.getAsFloat());
            } else if (type == BigDecimal.class) {
                return (T) primitive.getAsBigDecimal();
            } else if (type == Boolean.class) {
                return (T) Boolean.valueOf(primitive.getAsBoolean());
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public Long getId() {
        return id;
    }

    public String getJson() {
        return json;
    }

    public MultipartFile getFile() {
        return file;
    }

    public String getHref() {
        return href;
    }

    public JsonDelegator getJsonDelegator() {
        return jsonDelegator;
    }

    public String getAction() {
        return action;
    }

    public String getEntity() {
        return entity;
    }

    public String getPermission() {
        return permission;
    }

    public JsonElement getJsonElement() {
        return jsonElement;
    }

    public String getEntityType() {
        return entityType;
    }

    public Long getEntityId() {
        return entityId;
    }
}
