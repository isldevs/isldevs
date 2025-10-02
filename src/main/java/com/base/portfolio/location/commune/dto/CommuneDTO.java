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
package com.base.portfolio.location.commune.dto;

import com.base.portfolio.location.village.dto.VillageDTO;
import java.util.List;
import lombok.*;

/**
 * @author YISivlay
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommuneDTO {

  private Long id;
  private Long districtId;
  private String type;
  private String nameEn;
  private String nameKm;
  private String nameZh;
  private String postalCode;
  private List<VillageDTO> villages;
}
