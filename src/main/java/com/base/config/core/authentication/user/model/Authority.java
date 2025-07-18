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
package com.base.config.core.authentication.user.model;

import com.base.config.core.authentication.role.model.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * @author YISivlay
 */
@Getter
@Setter
@Entity
@Table(name = "authorities")
public class Authority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @SequenceGenerator(name = "authority_id_seq", sequenceName = "authority_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    @JsonIgnore
    private Role role;

    @Column(nullable = false)
    private String authority;

    public Authority() {
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
}
