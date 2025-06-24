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
package com.base.config.core.authentication.service;


import com.base.config.core.authentication.controller.UserConstants;
import com.base.config.core.authentication.data.UserCreateDTO;
import com.base.config.core.authentication.data.UserDTO;
import com.base.config.core.authentication.model.Role;
import com.base.config.core.authentication.model.User;
import com.base.config.core.authentication.repository.RoleRepository;
import com.base.config.core.authentication.repository.UserRepository;
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
    public UserDTO createUser(UserCreateDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        var user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .enabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .name(dto.getName())
                .email(dto.getEmail())
                .roles(resolveRoles(dto.getRoles()))
                .build();

        var savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    @Override
    public UserDTO getUserById(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + id));
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
    public Map<String, Object> updateUser(Long id, UserCreateDTO dto) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + id));

        if (!user.getUsername().equals(dto.getUsername()) && userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        var changes = user.changed(passwordEncoder, dto);

        var newRoles = resolveRoles(dto.getRoles());
        if (!user.getRoles().equals(newRoles)) {
            user.setRoles(newRoles);
            changes.put(UserConstants.ROLES, dto.getRoles());
        }

        if (!changes.isEmpty()) {
            userRepository.save(user);
        }

        return changes;
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NoSuchElementException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    private Set<Role> resolveRoles(Set<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            return Collections.emptySet();
        }

        Set<Role> roles = new HashSet<>();
        for (var roleName : roleNames) {
            var role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new NoSuchElementException("Role not found: " + roleName));
            roles.add(role);
        }
        return roles;
    }

    private UserDTO convertToDto(User user) {

        var roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        var dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRoles(roleNames);
        dto.setEnabled(user.isEnabled());
        return dto;
    }
}
