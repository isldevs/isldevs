/**
 * Copyright 2025 iSLDevs
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.base.core.authentication.user.model;

import jakarta.persistence.*;

/**
 * @author YISivlay
 */
@Entity
@Table(name = "authorities")
public class Authority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @SequenceGenerator(name = "authority_id_seq", sequenceName = "authority_id_seq", allocationSize = 1)
    private Long id;

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

    public Long getId() {
        return id;
    }

    public String getAuthority() {
        return authority;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }
}
