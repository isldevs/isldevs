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
package com.base.portfolio.office.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author YISivlay
 */
@Builder
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OfficeDTO {

    private Long id;
    private OfficeDTO parent;
    private String decorated;

    @JsonProperty("nameEn")
    private String nameEn;

    @JsonProperty("nameKm")
    private String nameKm;

    @JsonProperty("nameZh")
    private String nameZh;

    @JsonProperty("hierarchyEn")
    private String hierarchyEn;

    @JsonProperty("hierarchyKm")
    private String hierarchyKm;

    @JsonProperty("hierarchyZh")
    private String hierarchyZh;
    private String profile;

    public static String decorate(String hierarchy, String name) {
        if (hierarchy == null || hierarchy.isEmpty() || name == null) return name;
        var level = hierarchy.length() - hierarchy.replace(".", "").length() - 1;
        if (level <= 0) return name;
        return ".".repeat(level * 4) + name;
    }
}
