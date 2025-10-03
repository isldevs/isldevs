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
package com.base.portfolio.location.village.validation;

import com.base.config.serialization.JsonHelper;
import com.base.core.exception.ApiDataValidator;
import com.base.portfolio.location.village.controller.VillageConstants;
import com.google.gson.reflect.TypeToken;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author YISivlay
 */
@Component
public class VillageDataValidation {

	private final JsonHelper jsonHelper;

	private final ApiDataValidator validator;

	@Autowired
	public VillageDataValidation(final JsonHelper jsonHelper, final ApiDataValidator validator) {
		this.jsonHelper = jsonHelper;
		this.validator = validator;
	}

	public void create(String json) {

		final var typeOfMap = new TypeToken<Map<String, String>>() {
		}.getType();
		this.jsonHelper.unsupportedParameters(typeOfMap, json, VillageConstants.SUPPORTED_PARAMETERS);

		final var jsonElement = this.jsonHelper.parse(json);

		final var commune = this.jsonHelper.extractLong(VillageConstants.COMMUNE, jsonElement);
		validator.parameter(VillageConstants.COMMUNE, commune).isNumber().notNull().maxLength(20);

		final var type = this.jsonHelper.extractString(VillageConstants.TYPE, jsonElement);
		validator.parameter(VillageConstants.TYPE, type).isString().notEmpty().maxLength(50);

		final var nameEn = this.jsonHelper.extractString(VillageConstants.NAME_EN, jsonElement);
		validator.parameter(VillageConstants.NAME_EN, nameEn).isString().notEmpty().maxLength(100);

		final var nameKm = this.jsonHelper.extractString(VillageConstants.NAME_KM, jsonElement);
		validator.parameter(VillageConstants.NAME_KM, nameKm).isString().notEmpty().maxLength(100);

		final var nameZh = this.jsonHelper.extractString(VillageConstants.NAME_ZH, jsonElement);
		validator.parameter(VillageConstants.NAME_ZH, nameZh).isString().notEmpty().maxLength(100);

		final var postalCode = this.jsonHelper.extractString(VillageConstants.POSTAL_CODE, jsonElement);
		validator.parameter(VillageConstants.POSTAL_CODE, postalCode).isString().notEmpty().maxLength(50);
	}

	public void update(String json) {
		final var typeOfMap = new TypeToken<Map<String, String>>() {
		}.getType();
		this.jsonHelper.unsupportedParameters(typeOfMap, json, VillageConstants.SUPPORTED_PARAMETERS);

		final var jsonElement = this.jsonHelper.parse(json);

		if (this.jsonHelper.parameterExists(VillageConstants.COMMUNE, jsonElement)) {
			final var commune = this.jsonHelper.extractLong(VillageConstants.COMMUNE, jsonElement);
			validator.parameter(VillageConstants.COMMUNE, commune).isNumber().notNull().maxLength(20);
		}
		if (this.jsonHelper.parameterExists(VillageConstants.TYPE, jsonElement)) {
			final var type = this.jsonHelper.extractString(VillageConstants.TYPE, jsonElement);
			validator.parameter(VillageConstants.TYPE, type).isString().notEmpty().maxLength(50);
		}
		if (this.jsonHelper.parameterExists(VillageConstants.NAME_EN, jsonElement)) {
			final var nameEn = this.jsonHelper.extractString(VillageConstants.NAME_EN, jsonElement);
			validator.parameter(VillageConstants.NAME_EN, nameEn).isString().notEmpty().maxLength(100);
		}
		if (this.jsonHelper.parameterExists(VillageConstants.NAME_KM, jsonElement)) {
			final var nameKm = this.jsonHelper.extractString(VillageConstants.NAME_KM, jsonElement);
			validator.parameter(VillageConstants.NAME_KM, nameKm).isString().notEmpty().maxLength(100);
		}
		if (this.jsonHelper.parameterExists(VillageConstants.NAME_ZH, jsonElement)) {
			final var nameZh = this.jsonHelper.extractString(VillageConstants.NAME_ZH, jsonElement);
			validator.parameter(VillageConstants.NAME_ZH, nameZh).isString().notEmpty().maxLength(100);
		}
		if (this.jsonHelper.parameterExists(VillageConstants.POSTAL_CODE, jsonElement)) {
			final var postalCode = this.jsonHelper.extractString(VillageConstants.POSTAL_CODE, jsonElement);
			validator.parameter(VillageConstants.POSTAL_CODE, postalCode).isString().notEmpty().maxLength(50);
		}
	}

}
