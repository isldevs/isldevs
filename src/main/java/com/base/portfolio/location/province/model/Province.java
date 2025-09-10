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
package com.base.portfolio.location.province.model;


import com.base.core.auditable.CustomAbstractAuditable;
import com.base.core.command.data.JsonCommand;
import com.base.portfolio.location.district.model.District;
import com.base.portfolio.location.province.controller.ProvinceConstants;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author YISivlay
 */
@Getter
@Setter
@Entity
@Table(name = "province", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"postal_code"}, name = "idx_province_postal_code_key")
})
public class Province extends CustomAbstractAuditable {

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @OneToMany(mappedBy = "province", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<District> districts = new HashSet<>();

    protected Province() {
    }

    public Province(final String type,
                    final String name,
                    final String postalCode,
                    final Set<District> districts) {
        this.type = type;
        this.name = name;
        this.postalCode = postalCode;
        this.districts = districts;
        if (this.districts != null && !this.districts.isEmpty()) {
            this.districts.forEach(district -> district.setProvince(this));
        }
    }

    public static Province fromJson(JsonCommand command) {

        final var type = command.extractString(ProvinceConstants.TYPE);
        final var name = command.extractString(ProvinceConstants.NAME);
        final var postalCode = command.extractString(ProvinceConstants.POSTAL_CODE);
        final Set<District> districts = command.extractArrayAs(ProvinceConstants.DISTRICT, District.class);

        return new Province(type, name, postalCode, districts);
    }

    public Map<String, Object> changed(JsonCommand command) {

        final Map<String, Object> changes = new HashMap<>(7);

        if (command.isChangeAsString(ProvinceConstants.TYPE, this.type)) {
            final var type = command.extractString(ProvinceConstants.TYPE);
            this.type = type;
            changes.put(ProvinceConstants.TYPE, type);
        }
        if (command.isChangeAsString(ProvinceConstants.NAME, this.name)) {
            final var name = command.extractString(ProvinceConstants.NAME);
            this.name = name;
            changes.put(ProvinceConstants.NAME, name);
        }
        if (command.isChangeAsString(ProvinceConstants.POSTAL_CODE, this.postalCode)) {
            final var postalCode = command.extractString(ProvinceConstants.POSTAL_CODE);
            this.postalCode = postalCode;
            changes.put(ProvinceConstants.POSTAL_CODE, postalCode);
        }

        return changes;
    }
}
