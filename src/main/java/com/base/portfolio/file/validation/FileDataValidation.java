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
package com.base.portfolio.file.validation;

import com.base.config.serialization.JsonHelper;
import com.base.core.exception.ApiDataValidator;
import com.base.portfolio.file.controller.FileConstants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author YISivlay
 */
@Component
public class FileDataValidation {

    private final JsonHelper jsonHelper;

    private final ApiDataValidator validator;

    @Autowired
    public FileDataValidation(final JsonHelper jsonHelper,
                              final ApiDataValidator validator) {
        this.jsonHelper = jsonHelper;
        this.validator = validator;
    }

    public void upload(String type,
                       Long id) {

        final var typeOfMap = new TypeToken<Map<String, Object>>() {
        }.getType();

        Map<String, Object> map = Map.of(FileConstants.ENTITY,
                                         type,
                                         FileConstants.ENTITY_ID,
                                         id);
        String json = new Gson().toJson(map);

        this.jsonHelper.unsupportedParameters(typeOfMap,
                                              json,
                                              FileConstants.SUPPORTED_PARAMETER);

        final var jsonElement = this.jsonHelper.parse(json);

        final var entity = this.jsonHelper.extractString(FileConstants.ENTITY,
                                                         jsonElement);
        validator.parameter(FileConstants.ENTITY,
                            entity)
                 .isString()
                 .notEmpty()
                 .maxLength(100);

        final var entityId = this.jsonHelper.extractLong(FileConstants.ENTITY_ID,
                                                         jsonElement);
        validator.parameter(FileConstants.ENTITY_ID,
                            entityId)
                 .isNumber()
                 .notNullAndNotEmpty()
                 .maxLength(20);
    }

}
