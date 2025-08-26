package com.base.entity.office.service;


import com.base.core.command.data.JsonCommand;
import com.base.entity.office.dto.OfficeDTO;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * @author YISivlay
 */
public interface OfficeService {

    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'CREATE_OFFICE')")
    Map<String, Object> createOffice(JsonCommand command);

    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'UPDATE_OFFICE')")
    Map<String, Object> updateOffice(Long id, JsonCommand command);

    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'DELETE_OFFICE')")
    Map<String, Object> deleteOffice(Long id);

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'READ_OFFICE')")
    OfficeDTO getOfficeById(Long id);

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'READ_OFFICE')")
    Page<OfficeDTO> listOffices(Integer page, Integer size, String search);

}
