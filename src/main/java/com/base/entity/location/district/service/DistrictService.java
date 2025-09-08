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
package com.base.entity.location.district.service;


import com.base.core.command.data.JsonCommand;
import com.base.entity.location.district.dto.DistrictDTO;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Map;

/**
 * @author YISivlay
 */
public interface DistrictService {

    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'CREATE_DISTRICT')")
    Map<String, Object> createDistrict(JsonCommand command);

    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'UPDATE_DISTRICT')")
    Map<String, Object> updateDistrict(Long id, JsonCommand command);

    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'DELETE_DISTRICT')")
    Map<String, Object> deleteDistrict(Long id);

    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'READ_DISTRICT')")
    DistrictDTO getDistrictById(Long id);

    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'READ_DISTRICT')")
    Page<DistrictDTO> listDistricts(Integer page, Integer size, String search);
}
