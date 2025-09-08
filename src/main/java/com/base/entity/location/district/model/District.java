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
package com.base.entity.location.district.model;


import com.base.core.auditable.CustomAbstractAuditable;
import com.base.core.command.data.JsonCommand;
import com.base.entity.location.district.controller.DistrictConstants;
import com.base.entity.location.province.model.Province;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author YISivlay
 */
@Builder
@Getter
@Setter
@Entity
@Table(name = "district", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"province_id","postal_code"}, name = "idx_district_postal_code_key")
})
public class District extends CustomAbstractAuditable {

    @ManyToOne
    @JoinColumn(name = "province_id")
    private Province province;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    protected District() {
    }

    public District(final Province province,
                    final String type,
                    final String name,
                    final String postalCode) {
        this.province = province;
        this.type = type;
        this.name = name;
        this.postalCode = postalCode;
    }


    public static District fromJson(final Province province, final JsonCommand command) {

        final var type = command.extractString(DistrictConstants.TYPE);
        final var name = command.extractString(DistrictConstants.NAME);
        final var postalCode = command.extractString(DistrictConstants.POSTAL_CODE);

        return District.builder()
                .province(province)
                .name(name)
                .postalCode(postalCode)
                .type(type)
                .build();
    }

    public Map<String, Object> changed(JsonCommand command) {

        final Map<String, Object> changes = new HashMap<>(7);

        if (command.isChangeAsLong(DistrictConstants.PROVINCE, this.province == null ? null : this.province.getId())) {
            final var province = command.extractLong(DistrictConstants.PROVINCE);
            changes.put(DistrictConstants.PROVINCE, province);
        }
        if (command.isChangeAsString(DistrictConstants.TYPE, this.type)) {
            final var type = command.extractString(DistrictConstants.TYPE);
            this.type = type;
            changes.put(DistrictConstants.TYPE, type);
        }
        if (command.isChangeAsString(DistrictConstants.NAME, this.name)) {
            final var name = command.extractString(DistrictConstants.NAME);
            this.name = name;
            changes.put(DistrictConstants.NAME, name);
        }
        if (command.isChangeAsString(DistrictConstants.POSTAL_CODE, this.postalCode)) {
            final var postalCode = command.extractString(DistrictConstants.POSTAL_CODE);
            this.postalCode = postalCode;
            changes.put(DistrictConstants.POSTAL_CODE, postalCode);
        }

        return changes;
    }
}
