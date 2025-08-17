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
package com.base.config.core.authentication.user.model;

import com.base.config.core.authentication.role.model.Role;
import com.base.config.core.authentication.user.controller.UserConstants;
import com.base.config.core.authentication.role.repository.RoleRepository;
import com.base.config.core.command.data.JsonCommand;
import com.base.config.core.exception.NotFoundException;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author YISivlay
 */
@Entity
@Table(name = "users")
public class User implements UserDetails, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @SequenceGenerator(name = "user_id_seq", sequenceName = "user_id_seq", allocationSize = 1)
    private Long id;

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

    public User() {
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

        public Builder authority(Collection<? extends GrantedAuthority> authorities) {
            this.roles = authorities.stream()
                    .map(grantedAuthority -> {
                        Role role = new Role();
                        role.setName(grantedAuthority.getAuthority());
                        Set<Authority> authoritySet = new HashSet<>();
                        Authority authority = new Authority();
                        authority.setAuthority(grantedAuthority.getAuthority());
                        authoritySet.add(authority);
                        role.setAuthorities(authoritySet);
                        return role;
                    })
                    .collect(Collectors.toSet());
            return this;
        }
    }

    public Map<String, Object> changed(PasswordEncoder passwordEncoder, RoleRepository roleRepository, JsonCommand command) {
        Map<String, Object> changes = new HashMap<>(7);

        if (command.isChangeAsString(UserConstants.USERNAME, this.username)) {
            final String newUsername = command.extractString(UserConstants.USERNAME);
            this.username = newUsername;
            changes.put(UserConstants.USERNAME, newUsername);
        }
        if (command.isChangePassword(UserConstants.PASSWORD, passwordEncoder, this.password)) {
            final String newPassword = command.extractString(UserConstants.PASSWORD);
            this.password = passwordEncoder.encode(newPassword);
            changes.put(UserConstants.PASSWORD, "CHANGED");
        }
        if (command.isChangeAsString(UserConstants.NAME, this.name)) {
            final String newName = command.extractString(UserConstants.NAME);
            this.name = newName;
            changes.put(UserConstants.NAME, newName);
        }
        if (command.isChangeAsString(UserConstants.EMAIL, this.email)) {
            final String newEmail = command.extractString(UserConstants.EMAIL);
            this.email = newEmail;
            changes.put(UserConstants.EMAIL, newEmail);
        }
        if (command.isChangeAsArray(UserConstants.ROLES, this.roles.stream().map(Role::getName).collect(Collectors.toSet()), String.class)) {
            final Set<String> newRoles = command.extractArrayAs(UserConstants.ROLES, String.class);
            this.roles = newRoles.stream()
                    .map(roleName -> roleRepository.findByName(roleName)
                            .orElseThrow(() -> new NotFoundException("msg.not.found.role", roleName)))
                    .collect(Collectors.toSet());
            changes.put(UserConstants.ROLES, newRoles);
        }

        return changes;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User that = (User) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Long getId() {
        return id;
    }

    public String getProvider() {
        return provider;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getLocale() {
        return locale;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getTokenExpiry() {
        return tokenExpiry;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setTokenExpiry(String tokenExpiry) {
        this.tokenExpiry = tokenExpiry;
    }

    public void enabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void isAccountNonExpired(boolean accountNonExpired) {
        isAccountNonExpired = accountNonExpired;
    }

    public void isAccountNonLocked(boolean accountNonLocked) {
        isAccountNonLocked = accountNonLocked;
    }

    public void isCredentialsNonExpired(boolean credentialsNonExpired) {
        isCredentialsNonExpired = credentialsNonExpired;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}
