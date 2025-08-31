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
package com.base.entity.office.dto;


import com.base.entity.file.repository.FileUtils;
import com.base.entity.file.service.FileService;
import com.base.entity.office.model.Office;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author YISivlay
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OfficeDTO {

    private final Long id;
    private final OfficeDTO parent;
    private final String decorated;
    private final String nameEn;
    private final String nameKm;
    private final String nameZh;
    private final String hierarchyEn;
    private final String hierarchyKm;
    private final String hierarchyZh;
    private final String profile;

    public OfficeDTO(Builder builder) {
        this.id = builder.id;
        this.parent = builder.parent;
        this.decorated = builder.decorated;
        this.nameEn = builder.nameEn;
        this.nameKm = builder.nameKm;
        this.nameZh = builder.nameZh;
        this.hierarchyEn = builder.hierarchyEn;
        this.hierarchyKm = builder.hierarchyKm;
        this.hierarchyZh = builder.hierarchyZh;
        this.profile = builder.profile;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static OfficeDTO toDTO(Office office, FileService fileService) {
        var parent = office.getParent();
        return OfficeDTO.builder()
                .id(office.getId())
                .parent(parent != null ? OfficeDTO.builder()
                        .id(parent.getId())
                        .nameEn(parent.getNameEn())
                        .nameKm(parent.getNameKm())
                        .nameZh(parent.getNameZh())
                        .decorated(decorate(parent.getHierarchy(), parent.getNameEn()))
                        .build()
                        : null)
                .nameEn(office.getNameEn())
                .nameKm(office.getNameKm())
                .nameZh(office.getNameZh())
                .hierarchyEn(decorate(office.getHierarchy(), office.getNameEn()))
                .hierarchyKm(decorate(office.getHierarchy(), office.getNameKm()))
                .hierarchyZh(decorate(office.getHierarchy(), office.getNameZh()))
                .profile(profile(fileService, office.getId()))
                .build();
    }

    private static String profile(FileService fileService, Long id) {
        if (fileService == null) {
            return null;
        }
        return fileService.fileURL(FileUtils.ENTITY.OFFICE.toString(), id).get("file") != null
                ? fileService.fileURL(FileUtils.ENTITY.OFFICE.toString(), id).get("file").toString()
                : null;
    }

    public static String decorate(String hierarchy, String name) {
        if (hierarchy == null || hierarchy.isEmpty() || name == null) return name;
        var level = hierarchy.length() - hierarchy.replace(".", "").length() - 1;
        if (level <= 0) return name;
        return ".".repeat(level * 4) + name;
    }

    public static class Builder {

        private Long id;
        private OfficeDTO parent;
        private String decorated;
        private String nameEn;
        private String nameKm;
        private String nameZh;
        private String hierarchyEn;
        private String hierarchyKm;
        private String hierarchyZh;
        private String profile;

        public OfficeDTO build() {
            return new OfficeDTO(this);
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder parent(OfficeDTO parent) {
            this.parent = parent;
            return this;
        }

        public Builder decorated(String decorated) {
            this.decorated = decorated;
            return this;
        }

        public Builder nameEn(String nameEn) {
            this.nameEn = nameEn;
            return this;
        }

        public Builder nameKm(String nameKm) {
            this.nameKm = nameKm;
            return this;
        }

        public Builder nameZh(String nameZh) {
            this.nameZh = nameZh;
            return this;
        }

        public Builder hierarchyEn(String hierarchyEn) {
            this.hierarchyEn = hierarchyEn;
            return this;
        }

        public Builder hierarchyKm(String hierarchyKm) {
            this.hierarchyKm = hierarchyKm;
            return this;
        }

        public Builder hierarchyZh(String hierarchyZh) {
            this.hierarchyZh = hierarchyZh;
            return this;
        }

        public Builder profile(String profile) {
            this.profile = profile;
            return this;
        }
    }

    public Long getId() {
        return id;
    }

    public OfficeDTO getParent() {
        return parent;
    }

    public String getDecorated() {
        return decorated;
    }

    public String getNameEn() {
        return nameEn;
    }

    public String getNameKm() {
        return nameKm;
    }

    public String getNameZh() {
        return nameZh;
    }

    public String getHierarchyEn() {
        return hierarchyEn;
    }

    public String getHierarchyKm() {
        return hierarchyKm;
    }

    public String getHierarchyZh() {
        return hierarchyZh;
    }

    public String getProfile() {
        return profile;
    }
}
