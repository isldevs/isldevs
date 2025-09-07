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
package com.base.entity.location.province.model;


import com.base.core.auditable.CustomAbstractAuditable;
import com.base.core.command.data.JsonCommand;
import com.base.entity.location.province.controller.ProvinceConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "province", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"postal_code"})
})
public class Province extends CustomAbstractAuditable {

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    protected Province() {
    }

    public Province(final String type,
                    final String name,
                    final String postalCode) {
        this.type = type;
        this.name = name;
        this.postalCode = postalCode;
    }


    public static Province fromJson(JsonCommand command) {

        final var type = command.extractString(ProvinceConstants.TYPE);
        final var name = command.extractString(ProvinceConstants.NAME);
        final var postalCode = command.extractString(ProvinceConstants.POSTAL_CODE);

        return Province.builder()
                .name(name)
                .postalCode(postalCode)
                .type(type)
                .build();
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
