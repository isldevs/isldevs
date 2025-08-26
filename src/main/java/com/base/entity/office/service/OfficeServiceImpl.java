package com.base.entity.office.service;


import com.base.core.command.data.JsonCommand;
import com.base.core.command.data.LogData;
import com.base.core.exception.NotFoundException;
import com.base.entity.office.controller.OfficeConstants;
import com.base.entity.office.dto.OfficeDTO;
import com.base.entity.office.model.Office;
import com.base.entity.office.repository.OfficeRepository;
import com.base.entity.office.validation.OfficeDataValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author YISivlay
 */
@Service
public class OfficeServiceImpl implements OfficeService {

    private final MessageSource messageSource;
    private final OfficeRepository repository;
    private final OfficeDataValidation validator;

    @Autowired
    public OfficeServiceImpl(final MessageSource messageSource,
                             final OfficeRepository repository,
                             final OfficeDataValidation validator) {
        this.messageSource = messageSource;
        this.repository = repository;
        this.validator = validator;
    }

    @Override
    public Map<String, Object> createOffice(JsonCommand command) {
        this.validator.create(command.getJson());

        final var parentId = command.extractLong(OfficeConstants.PARENT_ID);
        final Office parent = parentId != null ? this.repository.findById(parentId)
                .orElseThrow(() -> new NotFoundException("msg.not.found", parentId)) : null;

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
    public Map<String, Object> updateOffice(Long id, JsonCommand command) {
        Office exist = this.repository.findById(id)
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
    public Map<String, Object> deleteOffice(Long id) {
        return Map.of();
    }

    @Override
    public OfficeDTO getOfficeById(Long id) {
        return null;
    }

    @Override
    public Page<OfficeDTO> listOffices(Integer page, Integer size, String search) {
        return null;
    }
}
