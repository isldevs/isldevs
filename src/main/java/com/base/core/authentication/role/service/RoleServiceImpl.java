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
package com.base.core.authentication.role.service;


import com.base.core.authentication.role.controller.RoleConstants;
import com.base.core.authentication.role.dto.RoleDTO;
import com.base.core.authentication.role.mapper.RoleMapper;
import com.base.core.authentication.role.model.Role;
import com.base.core.authentication.role.repository.RoleRepository;
import com.base.core.authentication.role.validation.RoleDataValidator;
import com.base.core.authentication.user.model.Authority;
import com.base.core.command.data.JsonCommand;
import com.base.core.command.data.LogData;
import com.base.core.exception.NotFoundException;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author YISivlay
 */
@Service
public class RoleServiceImpl implements RoleService {

    private final MessageSource messageSource;
    private final RoleRepository roleRepository;
    private final RoleDataValidator validator;
    private final RoleMapper roleMapper;

    @Autowired
    public RoleServiceImpl(final MessageSource messageSource,
                           final RoleRepository roleRepository,
                           final RoleDataValidator validator,
                           final RoleMapper roleMapper) {
        this.messageSource = messageSource;
        this.roleRepository = roleRepository;
        this.validator = validator;
        this.roleMapper = roleMapper;
    }

    @Override
    @CacheEvict(value = "roles", allEntries = true)
    public Map<String, Object> createRole(JsonCommand command) {

        this.validator.create(command.getJson());

        final var name = command.extractString(RoleConstants.NAME);
        final var authorities = command.extractArrayAs(RoleConstants.AUTHORITIES, String.class);
        var allAuthorities = authorities.stream()
                .map(auth -> {
                    Authority authority = new Authority();
                    authority.setAuthority(auth);
                    return authority;
                })
                .collect(Collectors.toSet());
        var data = Role.builder()
                .name(name)
                .authorities(allAuthorities)
                .build();
        var role = roleRepository.save(data);

        return LogData.builder()
                .id(role.getId())
                .success("msg.success", messageSource)
                .build()
                .claims();
    }

    @Override
    @CacheEvict(value = "roles", key = "T(java.util.Objects).hash(#id, #command)")
    public Map<String, Object> updateRole(Long id, JsonCommand command) {

        var exist = this.roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("msg.not.found.role", id));

        this.validator.update(command.getJson());

        var changes = exist.changed(command);
        if (!changes.isEmpty()) {
            this.roleRepository.save(exist);
        }

        return LogData.builder()
                .id(id)
                .changes(changes)
                .success("msg.success", messageSource)
                .build()
                .claims();
    }

    @Override
    @CacheEvict(value = "roles", key = "#id")
    public Map<String, Object> deleteRole(Long id) {

        final var role = this.roleRepository.findById(id).orElseThrow(() -> new NotFoundException("msg.not.found.role", id));
        this.roleRepository.delete(role);
        this.roleRepository.flush();

        return LogData.builder()
                .id(id)
                .success("msg.success", messageSource)
                .build()
                .claims();
    }

    @Override
    @Cacheable(value = "roles", key = "#id")
    public RoleDTO getRoleById(Long id) {

        var role = this.roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("msg.not.found.role", id));

        return roleMapper.toDTO(role);
    }

    @Override
    @Cacheable(value = "roles", key = "#page + '-' + #size + '-' + #search")
    public Page<RoleDTO> listRoles(Integer page, Integer size, String search) {
        Specification<Role> specification = (root, _, sp) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search != null && !search.isEmpty()) {
                predicates.add(sp.like(root.get("name"), "%" + search.toLowerCase() + "%"));
            }
            return sp.and(predicates.toArray(new Predicate[0]));
        };
        if (page == null || size == null) {
            var allRoles = this.roleRepository.findAll(specification, Sort.by("name").ascending());
            return new PageImpl<>(allRoles.stream().map(this::convertToDTO).toList());
        }
        var pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        var roles = this.roleRepository.findAll(specification, pageable);
        return roleMapper.toDTOPage(roles);
    }

    private RoleDTO convertToDTO(Role role) {
        return RoleDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .authorities(role.getAuthorities().stream().map(Authority::getAuthority).collect(Collectors.toSet()))
                .build();
    }
}
