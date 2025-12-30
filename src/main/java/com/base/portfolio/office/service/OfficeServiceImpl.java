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
import com.base.core.command.data.LogData;
import com.base.core.exception.NotFoundException;
import com.base.portfolio.office.controller.OfficeConstants;
import com.base.portfolio.office.dto.OfficeDTO;
import com.base.portfolio.office.mapper.OfficeMapper;
import com.base.portfolio.office.model.Office;
import com.base.portfolio.office.repository.OfficeRepository;
import com.base.portfolio.office.validation.OfficeDataValidation;
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
public class OfficeServiceImpl implements OfficeService {

    private final MessageSource messageSource;
    private final OfficeRepository repository;
    private final OfficeDataValidation validator;
    private final OfficeMapper officeMapper;

    @Autowired
    public OfficeServiceImpl(final MessageSource messageSource,
                             final OfficeRepository repository,
                             final OfficeDataValidation validator,
                             final OfficeMapper officeMapper) {
        this.messageSource = messageSource;
        this.repository = repository;
        this.validator = validator;
        this.officeMapper = officeMapper;
    }

    @Override
    @CacheEvict(value = "offices", allEntries = true)
    public Map<String, Object> createOffice(JsonCommand command) {
        this.validator.create(command.getJson());

        final var parentId = command.extractLong(OfficeConstants.PARENT_ID);
        final var parent = parentId != null
                ? this.repository.findById(parentId)
                        .orElseThrow(() -> new NotFoundException("msg.not.found", parentId))
                : null;

        final var nameEn = command.extractString(OfficeConstants.NAME_EN);
        final var nameKm = command.extractString(OfficeConstants.NAME_KM);
        final var nameZh = command.extractString(OfficeConstants.NAME_ZH);

        var data = Office.builder()
                .parent(parent)
                .nameEn(nameEn)
                .nameKm(nameKm)
                .nameZh(nameZh)
                .build();

        var office = this.repository.saveAndFlush(data);
        data.generateHierarchy();
        this.repository.save(data);

        return LogData.builder()
                .id(office.getId())
                .success("msg.success", messageSource)
                .build()
                .claims();
    }

    @Override
    @CacheEvict(value = "offices", allEntries = true)
    public Map<String, Object> updateOffice(Long id,
                                            JsonCommand command) {
        var exist = this.repository.findById(id)
                .orElseThrow(() -> new NotFoundException("msg.not.found", id));

        this.validator.update(command.getJson());

        var changes = exist.changed(command);
        if (!changes.isEmpty()) {
            if (changes.containsKey(OfficeConstants.PARENT_ID)) {
                final var parentId = command.extractLong(OfficeConstants.PARENT_ID);
                final Office parent = this.repository.findById(parentId)
                        .orElseThrow(() -> new NotFoundException("msg.not.found", id));
                exist.setParent(parent);
            }
            this.repository.save(exist);
        }

        return LogData.builder()
                .id(id)
                .changes(changes)
                .success("msg.success", messageSource)
                .build()
                .claims();
    }

    @Override
    @CacheEvict(value = "offices", allEntries = true)
    public Map<String, Object> deleteOffice(Long id) {

        var exist = this.repository.findById(id)
                .orElseThrow(() -> new NotFoundException("msg.not.found", id));

        this.repository.delete(exist);
        this.repository.flush();

        return LogData.builder()
                .id(id)
                .success("msg.success", messageSource)
                .build()
                .claims();
    }

    @Override
    @Cacheable(value = "offices", key = "#id")
    public OfficeDTO getOfficeById(Long id) {
        var office = this.repository.findById(id)
                .orElseThrow(() -> new NotFoundException("msg.not.found", id));
        return officeMapper.toDTOWithProfile(office);
    }

    @Override
    @Cacheable(value = "offices", key = "#page + '-' + #size + '-' + #search")
    public Page<OfficeDTO> listOffices(Integer page,
                                       Integer size,
                                       String search) {
        Specification<Office> specification = (root,
                                               _,
                                               sp) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search != null && !search.isEmpty()) {
                var likeSearch = "%" + search.toLowerCase() + "%";
                predicates.add(sp.or(sp.like(sp.lower(root.get("nameEn")), likeSearch), sp.like(sp.lower(root.get("nameKm")), likeSearch), sp.like(sp.lower(root
                        .get("nameZh")), likeSearch)));
            }
            return sp.and(predicates.toArray(new Predicate[0]));
        };
        var sort = Sort.by("hierarchy")
                .ascending();
        if (page == null || size == null) {
            var allOffices = repository.findAll(specification, sort);
            List<OfficeDTO> dto = officeMapper.toDTOList(allOffices);
            return new PageImpl<>(dto, Pageable.unpaged(), allOffices.size());
        }

        var pageable = PageRequest.of(page, size, sort);
        var officesPage = repository.findAll(specification, pageable);
        return officeMapper.toDTOPage(officesPage);
    }

}
