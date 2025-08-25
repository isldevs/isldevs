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
package com.base.core.authentication.user.data;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author YISivlay
 */
public class UserDTO {

    private final Long id;
    private final String username;
    private final String name;
    private final String email;
    private final Set<String> roles;
    private final boolean enabled;
    private final Map<String, Object> claims;

    public UserDTO(Builder builder) {
        this.id = builder.id;
        this.username = builder.username;
        this.name = builder.name;
        this.email = builder.email;
        this.roles = builder.roles;
        this.enabled = builder.enabled;
        this.claims = builder.claims;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Long id;
        private String username;
        private String name;
        private String email;
        private Set<String> roles;
        private boolean enabled;
        private final Map<String, Object> claims = new LinkedHashMap();

        public Builder() {
        }

        public UserDTO build() {
            return new UserDTO(this);
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder roles(Set<String> roles) {
            this.roles = roles;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder claim(String name, Object value) {
            this.claims.put(name, value);
            return this;
        }

        public Builder authorities(Set<String> authorities) {
            this.claims.put("authorities", authorities);
            return this;
        }
    }

    public Map<String, Object> getClaims() {
        return this.claims;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj != null && this.getClass() == obj.getClass()) {
            UserDTO that = (UserDTO) obj;
            return this.getClaims().equals(that.getClaims());
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.getClaims().hashCode();
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public boolean enabled() {
        return enabled;
    }

}
