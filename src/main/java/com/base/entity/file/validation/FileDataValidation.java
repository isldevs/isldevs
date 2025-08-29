package com.base.entity.file.validation;


import com.base.config.serialization.JsonHelper;
import com.base.core.exception.ApiDataValidator;
import com.base.entity.file.controller.FileConstants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

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

    public void upload(String type, Long id) {

        final var typeOfMap = new TypeToken<Map<String, Object>>() {
        }.getType();

        Map<String, Object> map = Map.of(FileConstants.ENTITY, type, FileConstants.ENTITY_ID, id);
        String json = new Gson().toJson(map);

        this.jsonHelper.unsupportedParameters(typeOfMap, json, FileConstants.SUPPORTED_PARAMETER);

        final var jsonElement = this.jsonHelper.parse(json);

        final var entity = this.jsonHelper.extractString(FileConstants.ENTITY, jsonElement);
        validator.parameter(FileConstants.ENTITY, entity).isString().notEmpty().maxLength(100);

        final var entityId = this.jsonHelper.extractLong(FileConstants.ENTITY_ID, jsonElement);
        validator.parameter(FileConstants.ENTITY_ID, entityId).isNumber().notNullAndNotEmpty().maxLength(20);

    }
}
