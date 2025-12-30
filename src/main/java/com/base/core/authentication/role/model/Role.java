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
package com.base.core.authentication.role.model;

import com.base.core.auditable.CustomAbstractPersistable;
import com.base.core.authentication.role.controller.RoleConstants;
import com.base.core.authentication.user.model.Authority;
import com.base.core.authentication.user.repository.AuthorityRepository;
import com.base.core.command.data.JsonCommand;
import com.base.core.exception.NotFoundException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author YISivlay
 */
@Setter
@Getter
@Entity
@Table(name = "roles")
public class Role extends CustomAbstractPersistable {

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "role_authorities", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "authority_id"))
    private Set<Authority> authorities = new HashSet<>();

    @Column(unique = true, nullable = false)
    private String name;

    public Role() {
    }

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

    public Map<String, Object> changed(final JsonCommand command,
                                       AuthorityRepository authorityRepository) {
        final Map<String, Object> changes = new HashMap<>(7);

        if (command.isChangeAsString(RoleConstants.NAME, this.name)) {
            final var value = command.extractString(RoleConstants.NAME);
            this.name = value;
            changes.put(RoleConstants.NAME, value);
        }
        if (command.isChangeAsArray(RoleConstants.AUTHORITIES, this.authorities.stream()
                .map(Authority::getAuthority)
                .collect(Collectors.toSet()), String.class)) {
            final var value = command.extractArrayAs(RoleConstants.AUTHORITIES, String.class);
            this.authorities = value.stream()
                    .map(authority -> authorityRepository.findByAuthority(authority)
                            .orElseThrow(() -> new NotFoundException("msg.not.found", authority)))
                    .collect(Collectors.toSet());
            ;
            changes.put(RoleConstants.AUTHORITIES, value);
        }

        return changes;
    }

}
