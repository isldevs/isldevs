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


import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import org.springframework.data.domain.Auditable;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

/**
 * @author YISivlay
 */
@MappedSuperclass
public abstract class CustomAbstractAuditable<U, PK extends Serializable> extends AbstractPersistable<PK> implements Auditable<U, PK, LocalDateTime> {

    @ManyToOne
    @Nullable
    private U createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Nullable
    private Date createdDate;

    @ManyToOne
    @Nullable
    private U lastModifiedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Nullable
    private Date lastModifiedDate;

    public CustomAbstractAuditable() {
    }

    public Optional<U> getCreatedBy() {
        return Optional.ofNullable(this.createdBy);
    }

    public void setCreatedBy(U createdBy) {
        this.createdBy = createdBy;
    }

    public Optional<LocalDateTime> getCreatedDate() {
        return this.createdDate == null ? Optional.empty() : Optional.of(LocalDateTime.ofInstant(this.createdDate.toInstant(), ZoneId.systemDefault()));
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = Date.from(createdDate.atZone(ZoneId.systemDefault()).toInstant());
    }

    public Optional<U> getLastModifiedBy() {
        return Optional.ofNullable(this.lastModifiedBy);
    }

    public void setLastModifiedBy(U lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Optional<LocalDateTime> getLastModifiedDate() {
        return this.lastModifiedDate == null ? Optional.empty() : Optional.of(LocalDateTime.ofInstant(this.lastModifiedDate.toInstant(), ZoneId.systemDefault()));
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = Date.from(lastModifiedDate.atZone(ZoneId.systemDefault()).toInstant());
    }

}
