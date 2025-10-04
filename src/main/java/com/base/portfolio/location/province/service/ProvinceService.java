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
package com.base.portfolio.location.province.service;

import com.base.core.command.data.JsonCommand;
import com.base.portfolio.location.province.dto.ProvinceDTO;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * @author YISivlay
 */
public interface ProvinceService {

    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'CREATE_PROVINCE')")
    Map<String, Object> createProvince(JsonCommand command);

    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'UPDATE_PROVINCE')")
    Map<String, Object> updateProvince(Long id,
                                       JsonCommand command);

    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'DELETE_PROVINCE')")
    Map<String, Object> deleteProvince(Long id);

    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'READ_PROVINCE')")
    ProvinceDTO getProvinceById(Long id);

    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'READ_PROVINCE')")
    Page<ProvinceDTO> listProvinces(Integer page,
                                    Integer size,
                                    String search);

}
