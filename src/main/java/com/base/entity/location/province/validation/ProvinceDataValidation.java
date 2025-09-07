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
package com.base.entity.location.province.validation;


import com.base.config.serialization.JsonHelper;
import com.base.core.exception.ApiDataValidator;
import com.base.entity.location.province.controller.ProvinceConstants;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author YISivlay
 */
@Component
public class ProvinceDataValidation {

    private final JsonHelper jsonHelper;
    private final ApiDataValidator validator;

    @Autowired
    public ProvinceDataValidation(final JsonHelper jsonHelper,
                                  final ApiDataValidator validator) {
        this.jsonHelper = jsonHelper;
        this.validator = validator;
    }

    public void create(String json) {

        final var typeOfMap = new TypeToken<Map<String, String>>() {}.getType();
        this.jsonHelper.unsupportedParameters(typeOfMap, json, ProvinceConstants.SUPPORTED_PARAMETERS);

        final var jsonElement = this.jsonHelper.parse(json);

        final var type = this.jsonHelper.extractString(ProvinceConstants.TYPE, jsonElement);
        validator.parameter(ProvinceConstants.TYPE, type).isString().notEmpty().maxLength(50);

        final var name = this.jsonHelper.extractString(ProvinceConstants.NAME, jsonElement);
        validator.parameter(ProvinceConstants.NAME, name).isString().notEmpty().maxLength(100);

        final var postalCode = this.jsonHelper.extractString(ProvinceConstants.POSTAL_CODE, jsonElement);
        validator.parameter(ProvinceConstants.POSTAL_CODE, postalCode).isString().notEmpty().maxLength(50);
    }

    public void update(String json) {
        final var typeOfMap = new TypeToken<Map<String, String>>() {}.getType();
        this.jsonHelper.unsupportedParameters(typeOfMap, json, ProvinceConstants.SUPPORTED_PARAMETERS);

        final var jsonElement = this.jsonHelper.parse(json);

        if (this.jsonHelper.parameterExists(ProvinceConstants.TYPE, jsonElement)) {
            final var type = this.jsonHelper.extractString(ProvinceConstants.TYPE, jsonElement);
            validator.parameter(ProvinceConstants.TYPE, type).isString().notEmpty().maxLength(50);
        }

        if (this.jsonHelper.parameterExists(ProvinceConstants.NAME, jsonElement)) {
            final var name = this.jsonHelper.extractString(ProvinceConstants.NAME, jsonElement);
            validator.parameter(ProvinceConstants.NAME, name).isString().notEmpty().maxLength(100);
        }

        if (this.jsonHelper.parameterExists(ProvinceConstants.POSTAL_CODE, jsonElement)) {
            final var postalCode = this.jsonHelper.extractString(ProvinceConstants.POSTAL_CODE, jsonElement);
            validator.parameter(ProvinceConstants.POSTAL_CODE, postalCode).isString().notEmpty().maxLength(50);
        }
    }
}
