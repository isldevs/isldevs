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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author YISivlay
 */
@Entity
@Table(name = "users")
public class User extends AbstractPersistable<Long> implements UserDetails {

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String provider = "LOCAL";

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "locale")
    private String locale;

    @Column(name = "access_token")
    private String accessToken;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "token_expiry")
    private String tokenExpiry;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "is_account_non_expired", nullable = false)
    private boolean isAccountNonExpired;

    @Column(name = "is_account_non_locked", nullable = false)
    private boolean isAccountNonLocked;

    @Column(name = "is_credentials_non_expired", nullable = false)
    private boolean isCredentialsNonExpired;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    protected User() {
    }

    public User(Builder builder) {
        this.username = builder.username;
        this.password = builder.password;
        this.provider = builder.provider != null ? builder.provider : "LOCAL";
        this.providerId = builder.providerId;
        this.name = builder.name;
        this.email = builder.email;
        this.avatarUrl = builder.avatarUrl;
        this.locale = builder.locale;
        this.accessToken = builder.accessToken;
        this.refreshToken = builder.refreshToken;
        this.tokenExpiry = builder.tokenExpiry;
        this.enabled = builder.enabled;
        this.isAccountNonExpired = builder.isAccountNonExpired;
        this.isAccountNonLocked = builder.isAccountNonLocked;
        this.isCredentialsNonExpired = builder.isCredentialsNonExpired;
        this.roles = builder.roles;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String username;
        private String password;
        private String provider;
        private String providerId;
        private String name;
        private String email;
        private String avatarUrl;
        private String locale;
        private String accessToken;
        private String refreshToken;
        private String tokenExpiry;
        private boolean enabled;
        private boolean isAccountNonExpired;
        private boolean isAccountNonLocked;
        private boolean isCredentialsNonExpired;
        private Set<Role> roles;

        public User build() {
            return new User(this);
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder provider(String provider) {
            this.provider = provider;
            return this;
        }

        public Builder providerId(String providerId) {
            this.providerId = providerId;
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

        public Builder avatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public Builder locale(String locale) {
            this.locale = locale;
            return this;
        }

        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public Builder tokenExpiry(String tokenExpiry) {
            this.tokenExpiry = tokenExpiry;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder isAccountNonExpired(boolean accountNonExpired) {
            isAccountNonExpired = accountNonExpired;
            return this;
        }

        public Builder isAccountNonLocked(boolean accountNonLocked) {
            isAccountNonLocked = accountNonLocked;
            return this;
        }

        public Builder isCredentialsNonExpired(boolean credentialsNonExpired) {
            isCredentialsNonExpired = credentialsNonExpired;
            return this;
        }

        public Builder roles(Set<Role> roles) {
            this.roles = roles;
            return this;
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .flatMap(role -> role.getAuthorities().stream())
                .map(authority -> new SimpleGrantedAuthority(authority.getAuthority()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }


    @Override
    public boolean isAccountNonExpired() {
        return this.isAccountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.isAccountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.isCredentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    public Set<Role> getRoles() {
        return roles;
    }
}
