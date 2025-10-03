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
package com.base.core.authentication.role.mapper;

import com.base.core.authentication.role.dto.RoleDTO;
import com.base.core.authentication.role.model.Role;
import com.base.core.authentication.user.model.Authority;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * @author YISivlay
 */
@Mapper(componentModel = "spring")
public abstract class RoleMapper {

	@Named("toDTO")
	@Mapping(target = "authorities", expression = "java(mapAuthorities(role.getAuthorities()))")
	public abstract RoleDTO toDTO(Role role);

	@IterableMapping(qualifiedByName = "toDTO")
	public abstract List<RoleDTO> toDTOList(List<Role> roles);

	public Page<RoleDTO> toDTOPage(Page<Role> rolePage) {
		if (rolePage == null || !rolePage.hasContent()) {
			return Page.empty();
		}
		List<RoleDTO> content = toDTOList(rolePage.getContent());
		return new PageImpl<>(content, rolePage.getPageable(), rolePage.getTotalElements());
	}

	@Named("mapAuthorities")
	protected Set<String> mapAuthorities(Set<Authority> authorities) {
		if (authorities == null) {
			return Set.of();
		}
		return authorities.stream().map(Authority::getAuthority).collect(Collectors.toSet());
	}

}
