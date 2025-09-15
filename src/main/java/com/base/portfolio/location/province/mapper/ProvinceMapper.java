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
package com.base.portfolio.location.province.mapper;


import com.base.portfolio.location.province.dto.ProvinceDTO;
import com.base.portfolio.location.province.model.Province;
import org.mapstruct.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;

/**
 * @author YISivlay
 */
@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ProvinceMapper {

    protected ProvinceMapper() {
    }

    @Named("toDTO")
    public abstract ProvinceDTO toDTO(Province province);

    @IterableMapping(qualifiedByName = "toDTO")
    public abstract List<ProvinceDTO> toDTOList(List<Province> allProvinces);

    public Page<ProvinceDTO> toDTOPage(Page<Province> page) {
        if (page == null || !page.hasContent()) {
            return Page.empty();
        }
        List<ProvinceDTO> content = toDTOList(page.getContent());
        return new PageImpl<>(content, page.getPageable(), page.getTotalElements());
    }
}
