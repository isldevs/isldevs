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

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author YISivlay
 */
public class LogData implements Serializable {

    private Map<String, Object> claims;

    public LogData(final Map<String, Object> claims) {
        this.claims = claims;
    }

    public LogData() {
    }

    public Map<String, Object> claims() {
        return this.claims;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return (Long) this.claims.get("id");
    }

    public static class Builder {

        private final Map<String, Object> claims = new LinkedHashMap<>();

        public Builder() {
        }

        public Builder claim(String name, Object value) {
            this.claims.put(name, value);
            return this;
        }

        public LogData build() {
            return new LogData(this.claims);
        }

        public Builder id(Long id) {
            return this.claim("id", id);
        }

        public Builder file(Object  file) {
            return this.claim("file", file);
        }

        public Builder changes(Map<String, Object> changes) {
            return this.claim("changes", changes);
        }

        public Builder success(String msgCde, MessageSource messageSource, Object... args) {
            String localizedMessage = messageSource.getMessage(msgCde, args, LocaleContextHolder.getLocale());
            return this.claim("message", localizedMessage);
        }
    }
}
