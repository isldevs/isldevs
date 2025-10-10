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
package com.base.core.authentication.user.model;

import com.base.core.auditable.CustomAbstractPersistable;
import jakarta.persistence.*;

/**
 * @author YISivlay
 */
@Entity
@Table(name = "authorities")
public class Authority extends CustomAbstractPersistable {

    @Column(nullable = false)
    private String authority;

    public Authority() {
    }

    public Authority(Builder builder) {
        this.authority = builder.authority;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String authority;

        public Authority build() {
            return new Authority(this);
        }

        public Builder authority(String authority) {
            this.authority = authority;
            return this;
        }

    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

}
