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
package com.base.portfolio.location.district.validation;

import com.base.config.serialization.JsonHelper;
import com.base.core.exception.ApiDataValidator;
import com.base.portfolio.location.district.controller.DistrictConstants;
import com.google.gson.reflect.TypeToken;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author YISivlay
 */
@Component
public class DistrictDataValidation {

    private final JsonHelper jsonHelper;

    private final ApiDataValidator validator;

    @Autowired
    public DistrictDataValidation(final JsonHelper jsonHelper,
                                  final ApiDataValidator validator) {
        this.jsonHelper = jsonHelper;
        this.validator = validator;
    }

    public void create(String json) {

        final var typeOfMap = new TypeToken<Map<String, Object>>() {
        }.getType();
        this.jsonHelper.unsupportedParameters(typeOfMap,
                                              json,
                                              DistrictConstants.SUPPORTED_PARAMETERS);

        final var jsonElement = this.jsonHelper.parse(json);

        final var province = this.jsonHelper.extractLong(DistrictConstants.PROVINCE,
                                                         jsonElement);
        validator.parameter(DistrictConstants.PROVINCE,
                            province)
                 .isNumber()
                 .notNull()
                 .maxLength(20);

        final var type = this.jsonHelper.extractString(DistrictConstants.TYPE,
                                                       jsonElement);
        validator.parameter(DistrictConstants.TYPE,
                            type)
                 .isString()
                 .notEmpty()
                 .maxLength(50);

        final var nameEn = this.jsonHelper.extractString(DistrictConstants.NAME_EN,
                                                         jsonElement);
        validator.parameter(DistrictConstants.NAME_EN,
                            nameEn)
                 .isString()
                 .notEmpty()
                 .maxLength(100);

        final var nameKm = this.jsonHelper.extractString(DistrictConstants.NAME_KM,
                                                         jsonElement);
        validator.parameter(DistrictConstants.NAME_KM,
                            nameKm)
                 .isString()
                 .notEmpty()
                 .maxLength(100);

        final var nameZh = this.jsonHelper.extractString(DistrictConstants.NAME_ZH,
                                                         jsonElement);
        validator.parameter(DistrictConstants.NAME_ZH,
                            nameZh)
                 .isString()
                 .notEmpty()
                 .maxLength(100);

        final var postalCode = this.jsonHelper.extractString(DistrictConstants.POSTAL_CODE,
                                                             jsonElement);
        validator.parameter(DistrictConstants.POSTAL_CODE,
                            postalCode)
                 .isString()
                 .notEmpty()
                 .maxLength(50);

        if (this.jsonHelper.parameterExists(DistrictConstants.COMMUNE,
                                            jsonElement)) {
            final var communes = this.jsonHelper.extractArrayAsObject(DistrictConstants.COMMUNE,
                                                                      jsonElement);
            validator.parameter(DistrictConstants.COMMUNE,
                                communes)
                     .notEmptyCollection();
        }
    }

    public void update(String json) {
        final var typeOfMap = new TypeToken<Map<String, Object>>() {
        }.getType();
        this.jsonHelper.unsupportedParameters(typeOfMap,
                                              json,
                                              DistrictConstants.SUPPORTED_PARAMETERS);

        final var jsonElement = this.jsonHelper.parse(json);

        if (this.jsonHelper.parameterExists(DistrictConstants.PROVINCE,
                                            jsonElement)) {
            final var province = this.jsonHelper.extractLong(DistrictConstants.PROVINCE,
                                                             jsonElement);
            validator.parameter(DistrictConstants.PROVINCE,
                                province)
                     .isNumber()
                     .notNull()
                     .maxLength(20);
        }
        if (this.jsonHelper.parameterExists(DistrictConstants.TYPE,
                                            jsonElement)) {
            final var type = this.jsonHelper.extractString(DistrictConstants.TYPE,
                                                           jsonElement);
            validator.parameter(DistrictConstants.TYPE,
                                type)
                     .isString()
                     .notEmpty()
                     .maxLength(50);
        }
        if (this.jsonHelper.parameterExists(DistrictConstants.NAME_EN,
                                            jsonElement)) {
            final var nameEn = this.jsonHelper.extractString(DistrictConstants.NAME_EN,
                                                             jsonElement);
            validator.parameter(DistrictConstants.NAME_EN,
                                nameEn)
                     .isString()
                     .notEmpty()
                     .maxLength(100);
        }
        if (this.jsonHelper.parameterExists(DistrictConstants.NAME_KM,
                                            jsonElement)) {
            final var nameKm = this.jsonHelper.extractString(DistrictConstants.NAME_KM,
                                                             jsonElement);
            validator.parameter(DistrictConstants.NAME_KM,
                                nameKm)
                     .isString()
                     .notEmpty()
                     .maxLength(100);
        }
        if (this.jsonHelper.parameterExists(DistrictConstants.NAME_ZH,
                                            jsonElement)) {
            final var nameZh = this.jsonHelper.extractString(DistrictConstants.NAME_ZH,
                                                             jsonElement);
            validator.parameter(DistrictConstants.NAME_ZH,
                                nameZh)
                     .isString()
                     .notEmpty()
                     .maxLength(100);
        }
        if (this.jsonHelper.parameterExists(DistrictConstants.POSTAL_CODE,
                                            jsonElement)) {
            final var postalCode = this.jsonHelper.extractString(DistrictConstants.POSTAL_CODE,
                                                                 jsonElement);
            validator.parameter(DistrictConstants.POSTAL_CODE,
                                postalCode)
                     .isString()
                     .notEmpty()
                     .maxLength(50);
        }
    }

}
