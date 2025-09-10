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
package com.base.portfolio.office.service;


import com.base.core.command.data.JsonCommand;
import com.base.portfolio.office.dto.OfficeDTO;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * @author YISivlay
 */
public interface OfficeService {

    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'CREATE_OFFICE')")
    Map<String, Object> createOffice(JsonCommand command);

    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'UPDATE_OFFICE')")
    Map<String, Object> updateOffice(Long id, JsonCommand command);

    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'DELETE_OFFICE')")
    Map<String, Object> deleteOffice(Long id);

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'READ_OFFICE')")
    OfficeDTO getOfficeById(Long id);

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'READ_OFFICE')")
    Page<OfficeDTO> listOffices(Integer page, Integer size, String search);

}
