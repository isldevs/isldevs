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

import com.base.core.authentication.role.dto.RoleDTO;
import com.base.core.command.data.JsonCommand;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author YISivlay
 */
public interface RoleService {

    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'CREATE_ROLE')")
    Map<String, Object> createRole(JsonCommand command);

    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'UPDATE_ROLE')")
    Map<String, Object> updateRole(Long id,
                                   JsonCommand command);

    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'DELETE_ROLE')")
    Map<String, Object> deleteRole(Long id);

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'READ_ROLE')")
    RoleDTO getRoleById(Long id);

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'READ_ROLE')")
    Page<RoleDTO> listRoles(Integer page,
                            Integer size,
                            String search);

}
