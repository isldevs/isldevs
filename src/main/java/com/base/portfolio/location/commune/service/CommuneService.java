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
package com.base.portfolio.location.commune.service;

import com.base.core.command.data.JsonCommand;
import com.base.portfolio.location.commune.dto.CommuneDTO;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * @author YISivlay
 */
public interface CommuneService {

	@PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'CREATE_COMMUNE')")
	Map<String, Object> createCommune(JsonCommand command);

	@PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'UPDATE_COMMUNE')")
	Map<String, Object> updateCommune(Long id, JsonCommand command);

	@PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'DELETE_COMMUNE')")
	Map<String, Object> deleteCommune(Long id);

	@PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'READ_COMMUNE')")
	CommuneDTO getCommuneById(Long id);

	@PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'READ_COMMUNE')")
	Page<CommuneDTO> listCommunes(Integer page, Integer size, String search);

}
