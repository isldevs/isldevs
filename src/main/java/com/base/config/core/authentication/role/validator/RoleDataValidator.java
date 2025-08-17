package com.base.config.core.authentication.role.validator;

import com.base.config.core.authentication.role.api.RoleConstants;
import com.base.config.core.exception.ApiDataValidator;
import com.base.config.serialization.JsonHelper;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.Map;

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

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {
        }.getType();
        this.jsonHelper.unsupportedParameters(typeOfMap, json, RoleConstants.SUPPORTED_PARAMETER);

        final JsonElement jsonElement = this.jsonHelper.parse(json);

        final String name = this.jsonHelper.extractString(RoleConstants.NAME, jsonElement);
        validator.parameter(RoleConstants.NAME, name).isString().notEmpty().maxLength(2);

    }

    public void update(String json) {

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {
        }.getType();
        this.jsonHelper.unsupportedParameters(typeOfMap, json, RoleConstants.SUPPORTED_PARAMETER);

        final JsonElement jsonElement = this.jsonHelper.parse(json);

        if (this.jsonHelper.parameterExists(RoleConstants.NAME, jsonElement)) {
            final String name = this.jsonHelper.extractString(RoleConstants.NAME, jsonElement);
            validator.parameter(RoleConstants.NAME, name).isString().notEmpty();
        }

    }

}
