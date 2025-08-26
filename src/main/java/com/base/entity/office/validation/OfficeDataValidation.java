package com.base.entity.office.validation;


import com.base.config.serialization.JsonHelper;
import com.base.core.authentication.role.controller.RoleConstants;
import com.base.core.exception.ApiDataValidator;
import com.base.entity.office.controller.OfficeConstants;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author YISivlay
 */
@Component
public class OfficeDataValidation {

    private final JsonHelper jsonHelper;
    private final ApiDataValidator validator;

    @Autowired
    public OfficeDataValidation(final JsonHelper jsonHelper,
                                final ApiDataValidator validator) {
        this.jsonHelper = jsonHelper;
        this.validator = validator;
    }

    public void create(String json) {

        final var typeOfMap = new TypeToken<Map<String, Object>>() {
        }.getType();
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

        final var typeOfMap = new TypeToken<Map<String, Object>>() {
        }.getType();
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
