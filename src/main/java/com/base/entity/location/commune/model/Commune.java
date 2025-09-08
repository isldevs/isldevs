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
package com.base.entity.location.commune.model;


import com.base.core.auditable.CustomAbstractAuditable;
import com.base.core.command.data.JsonCommand;
import com.base.entity.location.commune.controller.CommuneConstants;
import com.base.entity.location.district.model.District;
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
@Table(name = "commune", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"district_id","postal_code"}, name = "idx_commune_postal_code_key")
})
public class Commune extends CustomAbstractAuditable {

    @ManyToOne
    @JoinColumn(name = "district_id")
    private District district;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    protected Commune() {
    }

    public Commune(final District district,
                   final String type,
                   final String name,
                   final String postalCode) {
        this.district = district;
        this.type = type;
        this.name = name;
        this.postalCode = postalCode;
    }


    public static Commune fromJson(final District district, final JsonCommand command) {

        final var type = command.extractString(CommuneConstants.TYPE);
        final var name = command.extractString(CommuneConstants.NAME);
        final var postalCode = command.extractString(CommuneConstants.POSTAL_CODE);

        return Commune.builder()
                .district(district)
                .name(name)
                .postalCode(postalCode)
                .type(type)
                .build();
    }

    public Map<String, Object> changed(JsonCommand command) {

        final Map<String, Object> changes = new HashMap<>(7);

        if (command.isChangeAsLong(CommuneConstants.DISTRICT, this.district == null ? null : this.district.getId())) {
            final var district = command.extractLong(CommuneConstants.DISTRICT);
            changes.put(CommuneConstants.DISTRICT, district);
        }
        if (command.isChangeAsString(CommuneConstants.TYPE, this.type)) {
            final var type = command.extractString(CommuneConstants.TYPE);
            this.type = type;
            changes.put(CommuneConstants.TYPE, type);
        }
        if (command.isChangeAsString(CommuneConstants.NAME, this.name)) {
            final var name = command.extractString(CommuneConstants.NAME);
            this.name = name;
            changes.put(CommuneConstants.NAME, name);
        }
        if (command.isChangeAsString(CommuneConstants.POSTAL_CODE, this.postalCode)) {
            final var postalCode = command.extractString(CommuneConstants.POSTAL_CODE);
            this.postalCode = postalCode;
            changes.put(CommuneConstants.POSTAL_CODE, postalCode);
        }

        return changes;
    }
}
