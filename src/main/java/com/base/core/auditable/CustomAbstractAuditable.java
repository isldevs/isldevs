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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.lang.Nullable;

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
	@Column(name = "created_at")
	private Date createdAt;

	@Nullable
	@LastModifiedBy
	@Column(name = "updated_by")
	private String updatedBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Nullable
	@LastModifiedDate
	@Column(name = "updated_at")
	private Date updatedAt;

	public CustomAbstractAuditable() {
	}

	public String getCreatedBy() {
		return this.createdBy;
	}

	public Optional<LocalDateTime> getCreatedAt() {
		return this.createdAt == null ? Optional.empty()
				: Optional.of(LocalDateTime.ofInstant(this.createdAt.toInstant(), ZoneId.systemDefault()));
	}

	public void setCreatedAt(LocalDateTime createdDate) {
		this.createdAt = Date.from(createdDate.atZone(ZoneId.systemDefault()).toInstant());
	}

	public String getUpdatedBy() {
		return this.updatedBy;
	}

	public Optional<LocalDateTime> getUpdatedAt() {
		return this.updatedAt == null ? Optional.empty()
				: Optional.of(LocalDateTime.ofInstant(this.updatedAt.toInstant(), ZoneId.systemDefault()));
	}

	public void setUpdatedAt(LocalDateTime lastModifiedDate) {
		this.updatedAt = Date.from(lastModifiedDate.atZone(ZoneId.systemDefault()).toInstant());
	}

}
