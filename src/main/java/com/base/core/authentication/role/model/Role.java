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
package com.base.core.authentication.role.model;

import com.base.core.authentication.role.controller.RoleConstants;
import com.base.core.authentication.user.model.Authority;
import com.base.core.command.data.JsonCommand;
import jakarta.persistence.*;

import java.util.*;

/**
 * @author YISivlay
 */
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @SequenceGenerator(name = "role_id_seq", sequenceName = "role_id_seq", allocationSize = 1)
    private Long id;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_authorities",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "authority_id")
    )
    private Set<Authority> authorities = new HashSet<>();

    @Column(unique = true, nullable = false)
    private String name;

    public Role() {}

    public Role(Builder builder) {
        this.name = builder.name;
        this.authorities = builder.authorities;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String name;
        private Set<Authority> authorities = new HashSet<>();

        public Role build() {
            return new Role(this);
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder authorities(Set<Authority> authorities) {
            this.authorities = authorities;
            return this;
        }
    }

    public Map<String, Object> changed(final JsonCommand command) {
        final Map<String, Object> changes = new HashMap<>(7);

        if (command.isChangeAsString(RoleConstants.NAME, this.name)) {
            final var value = command.extractString(RoleConstants.NAME);
            this.name = value;
            changes.put(RoleConstants.NAME, value);
        }
        if (command.isChangeAsArray(RoleConstants.AUTHORITIES, this.authorities, Authority.class)) {
            final var value = command.extractArrayAs(RoleConstants.AUTHORITIES, Authority.class);
            this.authorities = value;
            changes.put(RoleConstants.AUTHORITIES, value);
        }

        return changes;

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<Authority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<Authority> authorities) {
        this.authorities = authorities;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
