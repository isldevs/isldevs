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
package com.base.core.authentication.role.validation;

import com.base.config.serialization.JsonHelper;
import com.base.core.authentication.role.controller.RoleConstants;
import com.base.core.exception.ApiDataValidator;
import com.google.gson.reflect.TypeToken;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author YISivlay
 */
@Component
public class RoleDataValidator {

    private final JsonHelper jsonHelper;
    private final ApiDataValidator validator;

    @Autowired
    public RoleDataValidator(final JsonHelper jsonHelper,
                             final ApiDataValidator validator) {
        this.jsonHelper = jsonHelper;
        this.validator = validator;
    }

    public void create(String json) {

        final var typeOfMap = new TypeToken<Map<String, Object>>() {
        }.getType();
        this.jsonHelper.unsupportedParameters(typeOfMap, json, RoleConstants.SUPPORTED_PARAMETER);

        final var jsonElement = this.jsonHelper.parse(json);

        final var name = this.jsonHelper.extractString(RoleConstants.NAME, jsonElement);
        validator.parameter(RoleConstants.NAME, name)
                .isString()
                .notEmpty()
                .maxLength(2);
    }

    public void update(String json) {

        final var typeOfMap = new TypeToken<Map<String, Object>>() {
        }.getType();
        this.jsonHelper.unsupportedParameters(typeOfMap, json, RoleConstants.SUPPORTED_PARAMETER);

        final var jsonElement = this.jsonHelper.parse(json);

        if (this.jsonHelper.parameterExists(RoleConstants.NAME, jsonElement)) {
            final var name = this.jsonHelper.extractString(RoleConstants.NAME, jsonElement);
            validator.parameter(RoleConstants.NAME, name)
                    .isString()
                    .notEmpty();
        }
    }

}
