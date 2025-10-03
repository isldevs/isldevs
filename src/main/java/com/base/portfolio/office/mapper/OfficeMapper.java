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
package com.base.portfolio.office.mapper;

import com.base.core.exception.NotFoundException;
import com.base.portfolio.file.repository.FileUtils;
import com.base.portfolio.file.service.FileService;
import com.base.portfolio.office.dto.OfficeDTO;
import com.base.portfolio.office.model.Office;
import java.util.List;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * @author YISivlay
 */
@Mapper(componentModel = "spring", uses = { FileService.class }, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public abstract class OfficeMapper {

	@Autowired
	protected FileService fileService;

	protected OfficeMapper() {
	}

	@Named("toDTO")
	@Mapping(source = "office", target = "parent", qualifiedByName = "toParentDTO")
	@Mapping(target = "decorated", expression = "java(getDecoratedName(office))")
	@Mapping(target = "profile", ignore = true)
	@Mapping(target = "hierarchyEn", expression = "java(OfficeDTO.decorate(office.getHierarchy(), office.getNameEn()))")
	@Mapping(target = "hierarchyKm", expression = "java(OfficeDTO.decorate(office.getHierarchy(), office.getNameKm()))")
	@Mapping(target = "hierarchyZh", expression = "java(OfficeDTO.decorate(office.getHierarchy(), office.getNameZh()))")
	public abstract OfficeDTO toDTO(Office office);

	@IterableMapping(qualifiedByName = "toDTO")
	public abstract List<OfficeDTO> toDTOList(List<Office> offices);

	public Page<OfficeDTO> toDTOPage(Page<Office> officePage) {
		if (officePage == null || !officePage.hasContent()) {
			return Page.empty();
		}

		List<OfficeDTO> content = toDTOList(officePage.getContent());
		return new PageImpl<>(content, officePage.getPageable(), officePage.getTotalElements());
	}

	@Named("toParentDTO")
	protected OfficeDTO toParentDTO(Office office) {
		if (office == null || office.getParent() == null) {
			return null;
		}
		Office parent = office.getParent();
		return OfficeDTO.builder()
			.id(parent.getId())
			.nameEn(parent.getNameEn())
			.nameKm(parent.getNameKm())
			.nameZh(parent.getNameZh())
			.decorated(OfficeDTO.decorate(parent.getHierarchy(), parent.getNameEn()))
			.build();
	}

	protected String getDecoratedName(Office office) {
		if (office.getParent() != null) {
			return OfficeDTO.decorate(office.getParent().getHierarchy(), office.getParent().getNameEn());
		}
		return null;
	}

	public OfficeDTO toDTOWithProfile(Office office) {
		OfficeDTO dto = toDTO(office);
		dto.setProfile(getProfile(office));
		return dto;
	}

	private String getProfile(Office office) {
		if (fileService == null || office == null) {
			return null;
		}
		try {
			var fileInfo = fileService.fileURL(FileUtils.ENTITY.OFFICE.toString(), office.getId());
			return fileInfo != null && fileInfo.get("file") != null ? fileInfo.get("file").toString() : null;
		}
		catch (NotFoundException ignored) {
			return null;
		}
	}

}
