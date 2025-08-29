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
package com.base.entity.file.dto;


/**
 * @author YISivlay
 */
public class FileDTO {

    private final Long id;
    private final String entity;
    private final Long entityId;
    private final String name;
    private final Long size;
    private final String mimeType;
    private final String location;
    private final Integer storageType;
    private final String url;

    public FileDTO(Builder builder) {
        this.id = builder.id;
        this.entity = builder.entity;
        this.entityId = builder.entityId;
        this.name = builder.name;
        this.size = builder.size;
        this.mimeType = builder.mimeType;
        this.location = builder.location;
        this.storageType = builder.storageType;
        this.url = builder.url;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Long id;
        private String entity;
        private Long entityId;
        private String name;
        private Long size;
        private String mimeType;
        private String location;
        private Integer storageType;
        private String url;

        public FileDTO build() {
            return new FileDTO(this);
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }
        public Builder entity(String entity) {
            this.entity = entity;
            return this;
        }
        public Builder entityId(Long entityId) {
            this.entityId = entityId;
            return this;
        }
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        public Builder size(Long size) {
            this.size = size;
            return this;
        }
        public Builder mimeType(String mimeType) {
            this.mimeType = mimeType;
            return this;
        }
        public Builder location(String location) {
            this.location = location;
            return this;
        }
        public Builder storageType(Integer storageType) {
            this.storageType = storageType;
            return this;
        }
        public Builder url(String url) {
            this.url = url;
            return this;
        }
    }

    public Long getId() {
        return id;
    }

    public String getEntity() {
        return entity;
    }

    public Long getEntityId() {
        return entityId;
    }

    public String getName() {
        return name;
    }

    public Long getSize() {
        return size;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getLocation() {
        return location;
    }

    public String getUrl() {
        return url;
    }

    public Integer getStorageType() {
        return storageType;
    }
}
