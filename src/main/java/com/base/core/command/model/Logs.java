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
package com.base.core.command.model;

import com.base.core.auditable.CustomAbstractPersistable;
import jakarta.persistence.*;
import java.util.Date;

/**
 * @author YISivlay
 */
@Entity
@Table(name = "logs")
public class Logs extends CustomAbstractPersistable {

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "entity", nullable = false)
    private String entity;

    @Column(name = "href", nullable = false)
    private String href;

    @Column(name = "json", nullable = false)
    private String json;

    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    public Logs() {
    }

    public Logs(final Long entityId,
                final String action,
                final String entity,
                final String href,
                final String json,
                final String createdBy,
                final Date createdAt) {
        this.entityId = entityId;
        this.action = action;
        this.entity = entity;
        this.href = href;
        this.json = json;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

}
