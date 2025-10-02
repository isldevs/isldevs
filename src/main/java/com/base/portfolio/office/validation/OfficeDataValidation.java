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
package com.base.portfolio.office.validation;

import com.base.config.serialization.JsonHelper;
import com.base.core.exception.ApiDataValidator;
import com.base.portfolio.office.controller.OfficeConstants;
import com.google.gson.reflect.TypeToken;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author YISivlay
 */
@Component
public class OfficeDataValidation {

  private final JsonHelper jsonHelper;
  private final ApiDataValidator validator;

  @Autowired
  public OfficeDataValidation(final JsonHelper jsonHelper, final ApiDataValidator validator) {
    this.jsonHelper = jsonHelper;
    this.validator = validator;
  }

  public void create(String json) {

    final var typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
    this.jsonHelper.unsupportedParameters(typeOfMap, json, OfficeConstants.SUPPORTED_PARAMETER);

    final var jsonElement = this.jsonHelper.parse(json);

    final var nameEn = this.jsonHelper.extractString(OfficeConstants.NAME_EN, jsonElement);
    validator.parameter(OfficeConstants.NAME_EN, nameEn).isString().notEmpty().maxLength(100);

    if (this.jsonHelper.parameterExists(OfficeConstants.NAME_KM, jsonElement)) {
      final var nameKm = this.jsonHelper.extractString(OfficeConstants.NAME_KM, jsonElement);
      validator.parameter(OfficeConstants.NAME_KM, nameKm).isString().notEmpty().maxLength(150);
    }
    if (this.jsonHelper.parameterExists(OfficeConstants.NAME_ZH, jsonElement)) {
      final var nameZh = this.jsonHelper.extractString(OfficeConstants.NAME_ZH, jsonElement);
      validator.parameter(OfficeConstants.NAME_ZH, nameZh).isString().notEmpty().maxLength(100);
    }
  }

  public void update(String json) {

    final var typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
    this.jsonHelper.unsupportedParameters(typeOfMap, json, OfficeConstants.SUPPORTED_PARAMETER);

    final var jsonElement = this.jsonHelper.parse(json);

    if (this.jsonHelper.parameterExists(OfficeConstants.NAME_EN, jsonElement)) {
      final var nameEn = this.jsonHelper.extractString(OfficeConstants.NAME_EN, jsonElement);
      validator.parameter(OfficeConstants.NAME_EN, nameEn).isString().notEmpty().maxLength(100);
    }
    if (this.jsonHelper.parameterExists(OfficeConstants.NAME_KM, jsonElement)) {
      final var nameKm = this.jsonHelper.extractString(OfficeConstants.NAME_KM, jsonElement);
      validator.parameter(OfficeConstants.NAME_KM, nameKm).isString().notEmpty().maxLength(150);
    }
    if (this.jsonHelper.parameterExists(OfficeConstants.NAME_ZH, jsonElement)) {
      final var nameZh = this.jsonHelper.extractString(OfficeConstants.NAME_ZH, jsonElement);
      validator.parameter(OfficeConstants.NAME_ZH, nameZh).isString().notEmpty().maxLength(100);
    }
  }
}
