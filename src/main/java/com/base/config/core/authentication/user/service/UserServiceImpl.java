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
package com.base.config.core.authentication.user.service;


import com.base.config.core.authentication.user.controller.UserConstants;
import com.base.config.core.authentication.user.data.UserDTO;
import com.base.config.core.authentication.role.model.Role;
import com.base.config.core.authentication.user.model.User;
import com.base.config.core.authentication.role.repository.RoleRepository;
import com.base.config.core.authentication.user.repository.UserRepository;
import com.base.config.core.command.data.JsonCommand;
import com.base.config.core.command.data.LogData;
import com.base.config.core.exception.BadRequestException;
import com.base.config.core.exception.NotFoundException;
import com.base.config.security.service.SecurityContext;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author YISivlay
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final SecurityContext securityContext;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(final SecurityContext securityContext,
                           final UserRepository userRepository,
                           final RoleRepository roleRepository,
                           final PasswordEncoder passwordEncoder) {
        this.securityContext = securityContext;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LogData createUser(JsonCommand command) {

        final var username = command.extractString(UserConstants.USERNAME);
        final var password = command.extractString(UserConstants.PASSWORD);
        final var name = command.extractString(UserConstants.NAME);
        final var email = command.extractString(UserConstants.EMAIL);
        final var roleNames = command.extractArrayAs(UserConstants.ROLES, String.class);

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        var data = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .enabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .name(name)
                .email(email)
                .roles(resolveRoles(roleNames))
                .build();

        var user = userRepository.save(data);
        return LogData.builder()
                .id(user.getId())
                .build();
    }

    @Override
    public UserDTO getUserById(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("msg.not.found.user", id));
        return convertToDto(user);
    }

    @Override
    public Page<UserDTO> listUsers(Integer page, Integer size, String search) {
        Specification<User> specification = (root, _, sp) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search != null && !search.isBlank()) {
                predicates.add(sp.like(sp.lower(root.get("username")), "%" + search.toLowerCase() + "%"));
            }
            if (!securityContext.isAdmin()) {
                predicates.add(sp.isTrue(root.get("enabled")));
            }
            return sp.and(predicates.toArray(new Predicate[0]));
        };

        if (page == null || size == null) {
            var users = userRepository.findAll(specification, Sort.by("username").ascending());
            return new PageImpl<>(users.stream().map(this::convertToDto).toList());
        }

        var pageable = PageRequest.of(page, size, Sort.by("username").ascending());
        var usersPage = userRepository.findAll(specification, pageable);
        return usersPage.map(this::convertToDto);
    }

    @Override
    public LogData updateUser(Long id, JsonCommand command) {
        var user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("msg.not.found.user", id));
        final var username = command.extractString(UserConstants.USERNAME);
        if (!user.getUsername().equals(username) && userRepository.existsByUsername(username)) {
            throw new BadRequestException("Username already exists");
        }
        var changes = user.changed(this.passwordEncoder, this.roleRepository, command);
        if (!changes.isEmpty()) {
            userRepository.save(user);
        }
        return LogData.builder()
                .id(id)
                .changes(changes)
                .build();
    }

    @Override
    public LogData deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("msg.not.found.user", id);
        }
        userRepository.deleteById(id);

        return LogData.builder()
                .id(id)
                .build();
    }

    private Set<Role> resolveRoles(Set<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            return Collections.emptySet();
        }

        Set<Role> roles = new HashSet<>();
        for (var roleName : roleNames) {
            var role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new NotFoundException("msg.not.found.role", roleName));
            roles.add(role);
        }
        return roles;
    }

    private UserDTO convertToDto(User user) {

        var roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .roles(roleNames)
                .enabled(user.isEnabled())
                .build();
    }
}
