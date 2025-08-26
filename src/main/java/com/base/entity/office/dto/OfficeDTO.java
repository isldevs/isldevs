package com.base.entity.office.dto;


import java.util.List;

/**
 * @author YISivlay
 */
public record OfficeDTO(Long id,
                        OfficeDTO parent,
                        List<OfficeDTO> children,
                        String hierarchy,
                        String decorated,
                        String nameEn,
                        String nameKm,
                        String nameZh) {
}
