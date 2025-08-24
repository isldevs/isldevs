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
import com.base.core.authentication.role.data.RoleDTO;
import com.base.core.authentication.role.model.Role;
import com.base.core.authentication.role.repository.RoleRepository;
import com.base.core.authentication.role.validation.RoleDataValidator;
import com.base.core.authentication.user.model.Authority;
import com.base.core.command.data.JsonCommand;
import com.base.core.command.data.LogData;
import com.base.core.exception.NotFoundException;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author YISivlay
 */
@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleDataValidator validator;

    @Autowired
    public RoleServiceImpl(final RoleRepository roleRepository,
                           final RoleDataValidator validator) {
        this.roleRepository = roleRepository;
        this.validator = validator;
    }

    @Override
    public LogData createRole(JsonCommand command) {

        this.validator.create(command.getJson());

        final var name = command.extractString(RoleConstants.NAME);
        final var authorities = command.extractArrayAs(RoleConstants.AUTHORITIES, String.class);
        Set<Authority> allAuthorities = authorities.stream()
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
                .build();
    }

    @Override
    public LogData updateRole(Long id, JsonCommand command) {

        Role exist = this.roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("msg.not.found.role", id));

        this.validator.update(command.getJson());

        var changes = exist.changed(command);
        if (!changes.isEmpty()) {
            this.roleRepository.save(exist);
        }

        return LogData.builder()
                .id(id)
                .changes(changes)
                .build();
    }

    @Override
    public LogData deleteRole(Long id) {

        final var role = this.roleRepository.findById(id).orElseThrow(() -> new NotFoundException("msg.not.found.role", id));
        this.roleRepository.delete(role);
        this.roleRepository.flush();

        return LogData.builder().id(id).build();
    }

    @Override
    public RoleDTO getRoleById(Long id) {

        var role = this.roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("msg.not.found.role", id));

        return RoleDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .authorities(role.getAuthorities().stream().map(Authority::getAuthority).collect(Collectors.toSet()))
                .build();
    }

    @Override
    public Page<RoleDTO> listRoles(Integer page, Integer size, String search) {
        Specification<Role> specification = (root, query, sp) -> {
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
        var rolesPage = this.roleRepository.findAll(specification, pageable);
        return rolesPage.map(this::convertToDTO);
    }

    private RoleDTO convertToDTO(Role role) {
        return RoleDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .authorities(role.getAuthorities().stream().map(Authority::getAuthority).collect(Collectors.toSet()))
                .build();
    }
}
