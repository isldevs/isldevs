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
package com.base.portfolio.location.village.service;

import com.base.core.command.data.JsonCommand;
import com.base.portfolio.location.village.dto.VillageDTO;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * @author YISivlay
 */
public interface VillageService {

	@PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'CREATE_VILLAGE')")
	Map<String, Object> createVillage(JsonCommand command);

	@PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'UPDATE_VILLAGE')")
	Map<String, Object> updateVillage(Long id, JsonCommand command);

	@PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'DELETE_VILLAGE')")
	Map<String, Object> deleteVillage(Long id);

	@PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'READ_VILLAGE')")
	VillageDTO getVillageById(Long id);

	@PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'READ_VILLAGE')")
	Page<VillageDTO> listVillages(Integer page, Integer size, String search);

}
