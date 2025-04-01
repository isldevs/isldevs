/**
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
package com.base.config.core.authentication.model;

import jakarta.persistence.*;
import org.springframework.data.jpa.domain.AbstractPersistable;

/**
 * @author YISivlay
 */
@Entity
@Table(name = "authorities")
public class Authority extends AbstractPersistable<Long> {

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(nullable = false)
    private String authority;

    protected Authority() {
    }

    public Authority(Builder builder) {
        this.role = builder.role;
        this.authority = builder.authority;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Role role;
        private String authority;

        public Authority build() {
            return new Authority(this);
        }

        public Builder role(Role role) {
            this.role = role;
            return this;
        }

        public Builder authority(String authority) {
            this.authority = authority;
            return this;
        }
    }

    public Role getRole() {
        return role;
    }

    public String getAuthority() {
        return authority;
    }
}
