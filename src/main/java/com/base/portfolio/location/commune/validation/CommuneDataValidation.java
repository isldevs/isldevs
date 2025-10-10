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
package com.base.portfolio.location.commune.validation;

import com.base.config.serialization.JsonHelper;
import com.base.core.exception.ApiDataValidator;
import com.base.portfolio.location.commune.controller.CommuneConstants;
import com.google.gson.reflect.TypeToken;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author YISivlay
 */
@Component
public class CommuneDataValidation {

    private final JsonHelper jsonHelper;
    private final ApiDataValidator validator;

    @Autowired
    public CommuneDataValidation(final JsonHelper jsonHelper,
                                 final ApiDataValidator validator) {
        this.jsonHelper = jsonHelper;
        this.validator = validator;
    }

    public void create(String json) {

        final var typeOfMap = new TypeToken<Map<String, String>>() {
        }.getType();
        this.jsonHelper.unsupportedParameters(typeOfMap, json, CommuneConstants.SUPPORTED_PARAMETERS);

        final var jsonElement = this.jsonHelper.parse(json);

        final var district = this.jsonHelper.extractLong(CommuneConstants.DISTRICT, jsonElement);
        validator.parameter(CommuneConstants.DISTRICT, district)
                .isNumber()
                .notNull()
                .maxLength(20);

        final var type = this.jsonHelper.extractString(CommuneConstants.TYPE, jsonElement);
        validator.parameter(CommuneConstants.TYPE, type)
                .isString()
                .notEmpty()
                .maxLength(50);

        final var nameEn = this.jsonHelper.extractString(CommuneConstants.NAME_EN, jsonElement);
        validator.parameter(CommuneConstants.NAME_EN, nameEn)
                .isString()
                .notEmpty()
                .maxLength(100);

        final var nameKm = this.jsonHelper.extractString(CommuneConstants.NAME_KM, jsonElement);
        validator.parameter(CommuneConstants.NAME_KM, nameKm)
                .isString()
                .notEmpty()
                .maxLength(100);

        final var nameZh = this.jsonHelper.extractString(CommuneConstants.NAME_ZH, jsonElement);
        validator.parameter(CommuneConstants.NAME_ZH, nameZh)
                .isString()
                .notEmpty()
                .maxLength(100);

        final var postalCode = this.jsonHelper.extractString(CommuneConstants.POSTAL_CODE, jsonElement);
        validator.parameter(CommuneConstants.POSTAL_CODE, postalCode)
                .isString()
                .notEmpty()
                .maxLength(50);
    }

    public void update(String json) {
        final var typeOfMap = new TypeToken<Map<String, String>>() {
        }.getType();
        this.jsonHelper.unsupportedParameters(typeOfMap, json, CommuneConstants.SUPPORTED_PARAMETERS);

        final var jsonElement = this.jsonHelper.parse(json);

        if (this.jsonHelper.parameterExists(CommuneConstants.DISTRICT, jsonElement)) {
            final var district = this.jsonHelper.extractLong(CommuneConstants.DISTRICT, jsonElement);
            validator.parameter(CommuneConstants.DISTRICT, district)
                    .isNumber()
                    .notNull()
                    .maxLength(20);
        }
        if (this.jsonHelper.parameterExists(CommuneConstants.TYPE, jsonElement)) {
            final var type = this.jsonHelper.extractString(CommuneConstants.TYPE, jsonElement);
            validator.parameter(CommuneConstants.TYPE, type)
                    .isString()
                    .notEmpty()
                    .maxLength(50);
        }
        if (this.jsonHelper.parameterExists(CommuneConstants.NAME_EN, jsonElement)) {
            final var nameEn = this.jsonHelper.extractString(CommuneConstants.NAME_EN, jsonElement);
            validator.parameter(CommuneConstants.NAME_EN, nameEn)
                    .isString()
                    .notEmpty()
                    .maxLength(100);
        }
        if (this.jsonHelper.parameterExists(CommuneConstants.NAME_KM, jsonElement)) {
            final var nameKm = this.jsonHelper.extractString(CommuneConstants.NAME_KM, jsonElement);
            validator.parameter(CommuneConstants.NAME_KM, nameKm)
                    .isString()
                    .notEmpty()
                    .maxLength(100);
        }
        if (this.jsonHelper.parameterExists(CommuneConstants.NAME_ZH, jsonElement)) {
            final var nameZh = this.jsonHelper.extractString(CommuneConstants.NAME_ZH, jsonElement);
            validator.parameter(CommuneConstants.NAME_ZH, nameZh)
                    .isString()
                    .notEmpty()
                    .maxLength(100);
        }
        if (this.jsonHelper.parameterExists(CommuneConstants.POSTAL_CODE, jsonElement)) {
            final var postalCode = this.jsonHelper.extractString(CommuneConstants.POSTAL_CODE, jsonElement);
            validator.parameter(CommuneConstants.POSTAL_CODE, postalCode)
                    .isString()
                    .notEmpty()
                    .maxLength(50);
        }
    }

}
