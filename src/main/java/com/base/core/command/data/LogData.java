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


import java.io.Serializable;
import java.util.Map;

/**
 * @author YISivlay
 */
public class LogData implements Serializable {

    private final Long id;
    private final Map<String, Object> changes;

    public LogData(Builder builder) {
        this.id = builder.id;
        this.changes = builder.changes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Long id;
        private Map<String, Object> changes;

        public LogData build() {
            return new LogData(this);
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder changes(Map<String, Object> changes) {
            this.changes = changes;
            return this;
        }
    }

    public Long getId() {
        return id;
    }

    public Map<String, Object> getChanges() {
        return changes;
    }
}
