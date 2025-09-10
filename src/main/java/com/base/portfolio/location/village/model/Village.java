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
package com.base.portfolio.location.village.model;


import com.base.core.auditable.CustomAbstractAuditable;
import com.base.core.command.data.JsonCommand;
import com.base.portfolio.location.commune.model.Commune;
import com.base.portfolio.location.village.controller.VillageConstants;
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
@Table(name = "village", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"commune_id","postal_code"}, name = "idx_village_postal_code_key")
})
public class Village extends CustomAbstractAuditable {

    @ManyToOne
    @JoinColumn(name = "commune_id")
    private Commune commune;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    protected Village() {
    }

    public static Village fromJson(final Commune commune, final JsonCommand command) {

        final var name = command.extractString(VillageConstants.NAME);
        final var postalCode = command.extractString(VillageConstants.POSTAL_CODE);

        return Village.builder()
                .commune(commune)
                .name(name)
                .postalCode(postalCode)
                .build();
    }

    public Map<String, Object> changed(JsonCommand command) {

        final Map<String, Object> changes = new HashMap<>(7);

        if (command.isChangeAsLong(VillageConstants.COMMUNE, this.commune == null ? null : this.commune.getId())) {
            final var commune = command.extractLong(VillageConstants.COMMUNE);
            changes.put(VillageConstants.COMMUNE, commune);
        }
        if (command.isChangeAsString(VillageConstants.NAME, this.name)) {
            final var name = command.extractString(VillageConstants.NAME);
            this.name = name;
            changes.put(VillageConstants.NAME, name);
        }
        if (command.isChangeAsString(VillageConstants.POSTAL_CODE, this.postalCode)) {
            final var postalCode = command.extractString(VillageConstants.POSTAL_CODE);
            this.postalCode = postalCode;
            changes.put(VillageConstants.POSTAL_CODE, postalCode);
        }

        return changes;
    }
}
