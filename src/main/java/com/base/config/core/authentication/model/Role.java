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

import java.util.HashSet;
import java.util.Set;

/**
 * @author YISivlay
 */
@Entity
@Table(name = "roles")
public class Role extends AbstractPersistable<Long> {

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Authority> authorities = new HashSet<>();

    @Column(unique = true, nullable = false)
    private String name;

    protected Role() {}

    public Role(Builder builder) {
        this.name = builder.name;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String name;

        public Role build() {
            return new Role(this);
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }
    }

    public String getName() {
        return name;
    }

    public Set<Authority> getAuthorities() {
        return authorities;
    }
}
