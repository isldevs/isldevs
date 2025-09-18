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

import com.base.core.auditable.CustomAbstractPersistable;
import com.base.core.authentication.role.model.Role;
import com.base.core.authentication.user.controller.UserConstants;
import com.base.core.authentication.role.repository.RoleRepository;
import com.base.core.command.data.JsonCommand;
import com.base.core.exception.NotFoundException;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author YISivlay
 */
@Entity
@Table(name = "users")
public class User extends CustomAbstractPersistable implements UserDetails, Serializable {

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "is_account_non_expired", nullable = false)
    private boolean accountNonExpired;

    @Column(name = "is_account_non_locked", nullable = false)
    private boolean accountNonLocked;

    @Column(name = "is_credentials_non_expired", nullable = false)
    private boolean credentialsNonExpired;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "provider")
    private String provider;

    @Column(name = "provider_avatar_url")
    private String providerAvatarUrl;

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
        this.name = builder.name;
        this.email = builder.email;
        this.enabled = builder.enabled;
        this.accountNonExpired = builder.accountNonExpired;
        this.accountNonLocked = builder.accountNonLocked;
        this.credentialsNonExpired = builder.credentialsNonExpired;
        this.providerId = builder.providerId;
        this.provider = builder.provider;
        this.providerAvatarUrl = builder.providerAvatarUrl;
        this.roles = builder.roles;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static User from(JsonCommand command, String username, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {

        final var password = command.extractString(UserConstants.PASSWORD);
        final var name = command.extractString(UserConstants.NAME);
        final var email = command.extractString(UserConstants.EMAIL);
        final var roleNames = command.extractArrayAs(UserConstants.ROLES, String.class);

        return User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .name(name)
                .email(email)
                .roles(resolveRoles(roleNames, roleRepository))
                .build();
    }

    public static Set<Role> resolveRoles(Set<String> roleNames, RoleRepository roleRepository) {
        if (roleNames == null || roleNames.isEmpty()) {
            return Collections.emptySet();
        }

        Set<Role> roles = new HashSet<>();
        for (var roleName : roleNames) {
            var role = roleRepository.findByName("ROLE_" + roleName)
                    .orElseThrow(() -> new NotFoundException("msg.not.found.role", roleName));
            roles.add(role);
        }
        return roles;
    }

    public static class Builder {

        private String username;
        private String password;
        private String name;
        private String email;
        private boolean enabled = true;
        private boolean accountNonExpired = true;
        private boolean accountNonLocked = true;
        private boolean credentialsNonExpired = true;
        private String providerId;
        private String provider;
        private String providerAvatarUrl;
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

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
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

        public Builder providerId(String providerId) {
            this.providerId = providerId;
            return this;
        }

        public Builder provider(String provider) {
            this.provider = provider;
            return this;
        }

        public Builder providerAvatarUrl(String providerAvatarUrl) {
            this.providerAvatarUrl = providerAvatarUrl;
            return this;
        }

        public Builder roles(Set<Role> roles) {
            this.roles = roles;
            return this;
        }

        public Builder authority(Collection<? extends GrantedAuthority> authorities) {
            this.roles = authorities.stream()
                    .map(grantedAuthority -> {
                        Role role = Role.builder().name(grantedAuthority.getAuthority()).build();
                        Set<Authority> authoritySet = new HashSet<>();
                        Authority authority = Authority.builder()
                                .authority(grantedAuthority.getAuthority())
                                .build();
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
                    .map(roleName -> roleRepository.findByName("ROLE_" + roleName)
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
        return this.accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return this.getId() != null && this.getId().equals(user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void enabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void accountNonExpired(boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    public void accountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public void credentialsNonExpired(boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProviderAvatarUrl() {
        return providerAvatarUrl;
    }

    public void setProviderAvatarUrl(String providerAvatarUrl) {
        this.providerAvatarUrl = providerAvatarUrl;
    }
}
