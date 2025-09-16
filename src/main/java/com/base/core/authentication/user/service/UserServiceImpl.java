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
package com.base.core.authentication.user.service;


import com.base.core.authentication.user.controller.UserConstants;
import com.base.core.authentication.user.dto.UserDTO;
import com.base.core.authentication.user.model.User;
import com.base.core.authentication.role.repository.RoleRepository;
import com.base.core.authentication.user.repository.UserRepository;
import com.base.core.authentication.user.validation.UserDataValidation;
import com.base.core.command.data.JsonCommand;
import com.base.core.command.data.LogData;
import com.base.core.exception.ErrorException;
import com.base.core.exception.NotFoundException;
import com.base.config.security.service.SecurityContext;
import com.base.portfolio.file.service.FileService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author YISivlay
 */
@Service
public class UserServiceImpl implements UserService {

    private final MessageSource messageSource;
    private final SecurityContext securityContext;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDataValidation validation;
    private final FileService fileService;

    @Autowired
    public UserServiceImpl(final MessageSource messageSource,
                           final SecurityContext securityContext,
                           final UserRepository userRepository,
                           final RoleRepository roleRepository,
                           final PasswordEncoder passwordEncoder,
                           final UserDataValidation validation,
                           final FileService fileService) {
        this.messageSource = messageSource;
        this.securityContext = securityContext;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.validation = validation;
        this.fileService = fileService;
    }

    @Override
    @CacheEvict(value = "users", allEntries = true)
    public Map<String, Object> createUser(JsonCommand command) {
        this.validation.create(command.getJson());

        final var username = command.extractString(UserConstants.USERNAME);

        if (userRepository.existsByUsername(username)) {
            throw new NotFoundException("msg.username.exist", username);
        }

        var data = User.from(command, username, passwordEncoder, roleRepository);

        var user = userRepository.save(data);
        return LogData.builder()
                .id(user.getId())
                .success("msg.success", messageSource)
                .build()
                .claims();
    }

    @Override
    @Cacheable(value = "users", key = "#id")
    public UserDTO getUserById(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("msg.not.found.user", id));
        return UserDTO.toDTO(user, fileService);
    }

    @Override
    @Cacheable(value = "users", key = "#page + '-' + #size + '-' + #search")
    public Page<UserDTO> listUsers(Integer page, Integer size, String search) {
        Specification<User> specification = (root, query, sp) -> {
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
            return new PageImpl<>(users.stream().map(user -> UserDTO.toDTO(user, null)).toList());
        }

        var pageable = PageRequest.of(page, size, Sort.by("username").ascending());
        var usersPage = userRepository.findAll(specification, pageable);
        return usersPage.map(user -> UserDTO.toDTO(user, null));
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    public Map<String, Object> updateUser(Long id, JsonCommand command) {
        var user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("msg.not.found.user", id));

        this.validation.update(command.getJson());

        final var username = command.extractString(UserConstants.USERNAME);
        if (!user.getUsername().equals(username) && userRepository.existsByUsername(username)) {
            throw new ErrorException("msg.username.exist", username);
        }
        var changes = user.changed(this.passwordEncoder, this.roleRepository, command);
        if (!changes.isEmpty()) {
            userRepository.save(user);
        }
        return LogData.builder()
                .id(id)
                .changes(changes)
                .success("msg.success", messageSource)
                .build()
                .claims();
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    public Map<String, Object> deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("msg.not.found.user", id);
        }
        userRepository.deleteById(id);

        return LogData.builder()
                .id(id)
                .success("msg.success", messageSource)
                .build()
                .claims();
    }
}
