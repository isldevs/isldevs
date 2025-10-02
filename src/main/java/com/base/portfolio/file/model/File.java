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
package com.base.portfolio.file.model;

import com.base.core.auditable.CustomAbstractAuditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * @author YISivlay
 */
@Entity
@Table(
    name = "file",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "file_entity_entity_id_key",
          columnNames = {"entity", "entity_id"})
    })
public class File extends CustomAbstractAuditable {

  @Column(name = "entity")
  private String entity;

  @Column(name = "entity_id")
  private Long entityId;

  @Column(name = "name")
  private String name;

  @Column(name = "size")
  private Long size;

  @Column(name = "mime_type")
  private String mimeType;

  @Column(name = "location")
  private String location;

  @Column(name = "storage_type")
  private Integer storageType;

  protected File() {}

  public File(Builder builder) {
    this.entity = builder.entity;
    this.entityId = builder.entityId;
    this.name = builder.name;
    this.size = builder.size;
    this.mimeType = builder.mimeType;
    this.location = builder.location;
    this.storageType = builder.storageType;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String entity;
    private Long entityId;
    private String name;
    private Long size;
    private String mimeType;
    private String location;
    private Integer storageType;

    public File build() {
      return new File(this);
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

  public Integer getStorageType() {
    return storageType;
  }

  public void setEntity(String entity) {
    this.entity = entity;
  }

  public void setEntityId(Long entityId) {
    this.entityId = entityId;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setSize(Long size) {
    this.size = size;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public void setStorageType(Integer storageType) {
    this.storageType = storageType;
  }
}
