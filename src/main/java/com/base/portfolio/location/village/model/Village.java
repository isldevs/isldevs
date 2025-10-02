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
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author YISivlay
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(
    name = "village",
    uniqueConstraints = {
      @UniqueConstraint(
          columnNames = {"commune_id", "postal_code"},
          name = "idx_village_postal_code_key")
    })
public class Village extends CustomAbstractAuditable {

  @ManyToOne
  @JoinColumn(name = "commune_id")
  private Commune commune;

  @Column(name = "name_en", nullable = false)
  private String nameEn;

  @Column(name = "name_km", nullable = false)
  private String nameKm;

  @Column(name = "name_zh", nullable = false)
  private String nameZh;

  @Column(name = "postal_code", nullable = false)
  private String postalCode;

  protected Village() {}

  public static Village fromJson(final Commune commune, final JsonCommand command) {

    final var nameEn = command.extractString(VillageConstants.NAME_EN);
    final var nameKm = command.extractString(VillageConstants.NAME_KM);
    final var nameZh = command.extractString(VillageConstants.NAME_ZH);
    final var postalCode = command.extractString(VillageConstants.POSTAL_CODE);

    return Village.builder()
        .commune(commune)
        .nameEn(nameEn)
        .nameKm(nameKm)
        .nameZh(nameZh)
        .postalCode(postalCode)
        .build();
  }

  public Map<String, Object> changed(JsonCommand command) {

    final Map<String, Object> changes = new HashMap<>(7);

    if (command.isChangeAsLong(
        VillageConstants.COMMUNE, this.commune == null ? null : this.commune.getId())) {
      final var commune = command.extractLong(VillageConstants.COMMUNE);
      changes.put(VillageConstants.COMMUNE, commune);
    }
    if (command.isChangeAsString(VillageConstants.NAME_EN, this.nameEn)) {
      final var nameEn = command.extractString(VillageConstants.NAME_EN);
      this.nameEn = nameEn;
      changes.put(VillageConstants.NAME_EN, nameEn);
    }
    if (command.isChangeAsString(VillageConstants.NAME_KM, this.nameKm)) {
      final var nameKm = command.extractString(VillageConstants.NAME_KM);
      this.nameKm = nameKm;
      changes.put(VillageConstants.NAME_KM, nameKm);
    }
    if (command.isChangeAsString(VillageConstants.NAME_ZH, this.nameZh)) {
      final var nameZh = command.extractString(VillageConstants.NAME_ZH);
      this.nameZh = nameZh;
      changes.put(VillageConstants.NAME_ZH, nameZh);
    }
    if (command.isChangeAsString(VillageConstants.POSTAL_CODE, this.postalCode)) {
      final var postalCode = command.extractString(VillageConstants.POSTAL_CODE);
      this.postalCode = postalCode;
      changes.put(VillageConstants.POSTAL_CODE, postalCode);
    }

    return changes;
  }
}
