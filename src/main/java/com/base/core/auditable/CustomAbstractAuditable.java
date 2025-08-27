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
package com.base.core.auditable;


import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Auditable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

/**
 * @author YISivlay
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class CustomAbstractAuditable extends CustomAbstractPersistable {

    @Nullable
    @CreatedBy
    @Column(name = "created_by")
    private String createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Nullable
    @CreatedDate
    @Column(name = "created_date")
    private Date createdDate;

    @Nullable
    @LastModifiedBy
    @Column(name = "last_modified_by")
    private String lastModifiedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Nullable
    @LastModifiedDate
    @Column(name = "last_modified_date")
    private Date lastModifiedDate;

    public CustomAbstractAuditable() {
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public Optional<LocalDateTime> getCreatedDate() {
        return this.createdDate == null ? Optional.empty() : Optional.of(LocalDateTime.ofInstant(this.createdDate.toInstant(), ZoneId.systemDefault()));
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = Date.from(createdDate.atZone(ZoneId.systemDefault()).toInstant());
    }

    public String getLastModifiedBy() {
        return this.lastModifiedBy;
    }

    public Optional<LocalDateTime> getLastModifiedDate() {
        return this.lastModifiedDate == null ? Optional.empty() : Optional.of(LocalDateTime.ofInstant(this.lastModifiedDate.toInstant(), ZoneId.systemDefault()));
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = Date.from(lastModifiedDate.atZone(ZoneId.systemDefault()).toInstant());
    }
}
