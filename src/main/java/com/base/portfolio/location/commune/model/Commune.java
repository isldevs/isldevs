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
package com.base.portfolio.location.commune.model;


import com.base.core.auditable.CustomAbstractAuditable;
import com.base.core.command.data.JsonCommand;
import com.base.portfolio.location.commune.controller.CommuneConstants;
import com.base.portfolio.location.district.model.District;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
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

    @Column(name = "name_en", nullable = false)
    private String nameEn;

    @Column(name = "name_km", nullable = false)
    private String nameKm;

    @Column(name = "name_zh", nullable = false)
    private String nameZh;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    protected Commune() {
    }

    public static Commune fromJson(final District district, final JsonCommand command) {

        final var type = command.extractString(CommuneConstants.TYPE);
        final var nameEn = command.extractString(CommuneConstants.NAME_EN);
        final var nameKm = command.extractString(CommuneConstants.NAME_KM);
        final var nameZh = command.extractString(CommuneConstants.NAME_ZH);
        final var postalCode = command.extractString(CommuneConstants.POSTAL_CODE);

        return Commune.builder()
                .district(district)
                .nameEn(nameEn)
                .nameKm(nameKm)
                .nameZh(nameZh)
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
        if (command.isChangeAsString(CommuneConstants.NAME_EN, this.nameEn)) {
            final var nameEn = command.extractString(CommuneConstants.NAME_EN);
            this.nameEn = nameEn;
            changes.put(CommuneConstants.NAME_EN, nameEn);
        }
        if (command.isChangeAsString(CommuneConstants.NAME_KM, this.nameKm)) {
            final var nameKm = command.extractString(CommuneConstants.NAME_KM);
            this.nameKm = nameKm;
            changes.put(CommuneConstants.NAME_KM, nameKm);
        }
        if (command.isChangeAsString(CommuneConstants.NAME_ZH, this.nameZh)) {
            final var nameZh = command.extractString(CommuneConstants.NAME_ZH);
            this.nameZh = nameZh;
            changes.put(CommuneConstants.NAME_ZH, nameZh);
        }
        if (command.isChangeAsString(CommuneConstants.POSTAL_CODE, this.postalCode)) {
            final var postalCode = command.extractString(CommuneConstants.POSTAL_CODE);
            this.postalCode = postalCode;
            changes.put(CommuneConstants.POSTAL_CODE, postalCode);
        }

        return changes;
    }
}
