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
package com.base.core.authentication.user.dto;

import com.base.core.authentication.role.model.Role;
import com.base.core.authentication.user.model.User;
import com.base.core.exception.NotFoundException;
import com.base.portfolio.file.repository.FileUtils;
import com.base.portfolio.file.service.FileService;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author YISivlay
 */
public class UserDTO {

    private Long id;

    private String username;

    private String name;

    private String email;

    private Set<String> roles;

    private boolean enabled;

    private boolean accountNonExpired;

    private boolean accountNonLocked;

    private boolean credentialsNonExpired;

    private String profile;

    protected UserDTO() {
    }

    public UserDTO(Builder builder) {
        this.id = builder.id;
        this.username = builder.username;
        this.name = builder.name;
        this.email = builder.email;
        this.roles = builder.roles;
        this.enabled = builder.enabled;
        this.accountNonExpired = builder.accountNonExpired;
        this.accountNonLocked = builder.accountNonLocked;
        this.credentialsNonExpired = builder.credentialsNonExpired;
        this.profile = builder.profile;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static UserDTO toDTO(User user,
                                FileService fileService) {
        var roleNames = user.getRoles()
                            .stream()
                            .map(Role::getName)
                            .collect(Collectors.toSet());

        return UserDTO.builder()
                      .id(user.getId())
                      .username(user.getUsername())
                      .name(user.getName())
                      .email(user.getEmail())
                      .roles(roleNames)
                      .enabled(user.isEnabled())
                      .accountNonExpired(user.isAccountNonExpired())
                      .accountNonLocked(user.isAccountNonLocked())
                      .credentialsNonExpired(user.isCredentialsNonExpired())
                      .profile(profile(fileService,
                                       user.getId()))
                      .build();
    }

    private static String profile(FileService fileService,
                                  Long id) {
        if (fileService == null) {
            return null;
        }
        String profile = null;
        try {
            profile = fileService.fileURL(FileUtils.ENTITY.USER.toString(),
                                          id)
                                 .get("file") != null ? fileService.fileURL(FileUtils.ENTITY.USER.toString(),
                                                                            id)
                                                                   .toString() : null;
        } catch (NotFoundException ignored) {
        }
        return profile;
    }

    public static class Builder {

        private Long id;

        private String username;

        private String name;

        private String email;

        private Set<String> roles;

        private boolean enabled;

        private boolean accountNonExpired;

        private boolean accountNonLocked;

        private boolean credentialsNonExpired;

        private String profile;

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

        public Builder authorities(Set<String> authorities) {
            this.roles = authorities;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder accountNonExpired(boolean accountNonExpired) {
            this.accountNonExpired = accountNonExpired;
            return this;
        }

        public Builder accountNonLocked(boolean accountNonLocked) {
            this.accountNonLocked = accountNonLocked;
            return this;
        }

        public Builder credentialsNonExpired(boolean credentialsNonExpired) {
            this.credentialsNonExpired = credentialsNonExpired;
            return this;
        }

        public Builder profile(String profile) {
            this.profile = profile;
            return this;
        }

    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj != null && this.getClass() == obj.getClass()) {
            UserDTO that = (UserDTO) obj;
            return this.getId()
                       .equals(that.getId());
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.getId()
                   .hashCode();
    }

    public Long getId() { return id; }

    public String getUsername() { return username; }

    public String getName() { return name; }

    public String getEmail() { return email; }

    public Set<String> getRoles() { return roles; }

    public boolean isEnabled() { return enabled; }

    public boolean isAccountNonExpired() { return accountNonExpired; }

    public boolean isAccountNonLocked() { return accountNonLocked; }

    public boolean isCredentialsNonExpired() { return credentialsNonExpired; }

    public String getProfile() { return profile; }

}
