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
package com.base.core.configuration.model;

import com.base.core.auditable.CustomAbstractAuditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * @author YISivlay
 */
@Entity
@Table(name = "config", uniqueConstraints = { @UniqueConstraint(name = "config_code_key", columnNames = { "code" }) })
public class Config extends CustomAbstractAuditable {

	@Column(name = "name")
	private String name;

	@Column(name = "code")
	private String code;

	@Column(name = "value")
	private String value;

	@Column(name = "enabled")
	private Boolean enabled;

	public Config() {
	}

	public String getName() {
		return name;
	}

	public String getCode() {
		return code;
	}

	public String getValue() {
		return value;
	}

	public Boolean getEnabled() {
		return enabled;
	}

}
