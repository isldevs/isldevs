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
package com.base.core.data;

/**
 * @author YISivlay
 */
public final class ErrorData {

	private final Integer status;

	private final String error;

	private final String description;

	private final String message;

	private final Object[] args;

	public ErrorData(Builder builder) {
		this.status = builder.status;
		this.error = builder.error;
		this.description = builder.description;
		this.message = builder.message;
		this.args = builder.args;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private Integer status;

		private String error;

		private String description;

		private String message;

		private Object[] args;

		public ErrorData build() {
			return new ErrorData(this);
		}

		public Builder status(Integer status) {
			this.status = status;
			return this;
		}

		public Builder error(String error) {
			this.error = error;
			return this;
		}

		public Builder description(String description) {
			this.description = description;
			return this;
		}

		public Builder message(String message) {
			this.message = message;
			return this;
		}

		public Builder args(Object... args) {
			this.args = args;
			return this;
		}

	}

	public Integer getStatus() {
		return status;
	}

	public String getError() {
		return error;
	}

	public String getDescription() {
		return description;
	}

	public String getMessage() {
		return message;
	}

	public Object[] getArgs() {
		return args;
	}

}
