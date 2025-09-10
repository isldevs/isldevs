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
import com.base.core.command.data.LogData;
import com.base.core.exception.NotFoundException;
import com.base.portfolio.location.province.dto.ProvinceDTO;
import com.base.portfolio.location.province.mapper.ProvinceMapper;
import com.base.portfolio.location.province.model.Province;
import com.base.portfolio.location.province.repository.ProvinceRepository;
import com.base.portfolio.location.province.validation.ProvinceDataValidation;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author YISivlay
 */
@Service
public class ProvinceServiceImpl implements ProvinceService {

    private final MessageSource messageSource;
    private final ProvinceRepository repository;
    private final ProvinceDataValidation validation;
    private final ProvinceMapper mapper;

    @Autowired
    public ProvinceServiceImpl(final MessageSource messageSource,
                               final ProvinceRepository repository,
                               final ProvinceDataValidation validation,
                               final ProvinceMapper mapper) {
        this.messageSource = messageSource;
        this.repository = repository;
        this.validation = validation;
        this.mapper = mapper;
    }


    @Override
    @CacheEvict(value = "provinces", allEntries = true)
    public Map<String, Object> createProvince(JsonCommand command) {
        this.validation.create(command.getJson());

        final var data = Province.fromJson(command);

        this.repository.save(data);
        return LogData.builder()
                .id(data.getId())
                .success("msg.success", messageSource)
                .build().claims();
    }

    @Override
    @CacheEvict(value = "provinces", key = "#id")
    public Map<String, Object> updateProvince(Long id, JsonCommand command) {
        var data = this.repository.findById(id)
                .orElseThrow(() -> new NotFoundException("msg.not.found", id));

        this.validation.update(command.getJson());

        var changes = data.changed(command);
        if (!changes.isEmpty()) {
            this.repository.save(data);
        }
        return LogData.builder()
                .id(data.getId())
                .changes(changes)
                .success("msg.success", messageSource)
                .build()
                .claims();
    }

    @Override
    @CacheEvict(value = "provinces", key = "#id")
    public Map<String, Object> deleteProvince(Long id) {
        final var data = this.repository.findById(id)
                .orElseThrow(() -> new NotFoundException("msg.not.found", id));
        this.repository.delete(data);
        this.repository.flush();
        return LogData.builder()
                .id(data.getId())
                .success("msg.success", messageSource)
                .build().claims();
    }

    @Override
    @Cacheable(value = "provinces", key = "#id")
    public ProvinceDTO getProvinceById(Long id) {
        final var data = this.repository.findById(id)
                .orElseThrow(() -> new NotFoundException("msg.not.found", id));
        return mapper.toDTO(data);
    }

    @Override
    public Page<ProvinceDTO> listProvinces(Integer page, Integer size, String search) {
        Specification<Province> specification = (root, query, sp) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search != null && !search.isEmpty()) {
                var likeSearch = "%" + search.toLowerCase() + "%";
                predicates.add(sp.or(
                        sp.like(sp.lower(root.get("name")), likeSearch),
                        sp.like(sp.lower(root.get("type")), likeSearch),
                        sp.like(sp.lower(root.get("postalCode")), likeSearch)
                ));
            }
            return sp.and(predicates.toArray(new Predicate[0]));
        };

        var sort = Sort.by("name").ascending();
        if (page == null || size == null) {
            var allProvinces = repository.findAll(specification, sort);
            List<ProvinceDTO> dto = mapper.toDTOList(allProvinces);
            return new PageImpl<>(dto, Pageable.unpaged(), allProvinces.size());
        }

        var pageable = PageRequest.of(page, size, sort);
        var provincePage = repository.findAll(specification, pageable);
        return mapper.toDTOPage(provincePage);
    }
}
