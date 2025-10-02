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
package com.base.core.authentication.user.validation;

import com.base.config.serialization.JsonHelper;
import com.base.core.authentication.user.controller.UserConstants;
import com.base.core.exception.ApiDataValidator;
import com.google.gson.reflect.TypeToken;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author YISivlay
 */
@Component
public class UserDataValidation {

  private final JsonHelper jsonHelper;
  private final ApiDataValidator validator;

  @Autowired
  public UserDataValidation(final JsonHelper jsonHelper, final ApiDataValidator validator) {
    this.jsonHelper = jsonHelper;
    this.validator = validator;
  }

  public void create(String json) {
    final var typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
    this.jsonHelper.unsupportedParameters(typeOfMap, json, UserConstants.SUPPORTED_PARAMETER);

    final var jsonElement = this.jsonHelper.parse(json);

    final var username = this.jsonHelper.extractString(UserConstants.USERNAME, jsonElement);
    validator.parameter(UserConstants.USERNAME, username).isString().notEmpty().maxLength(255);

    final var name = this.jsonHelper.extractString(UserConstants.NAME, jsonElement);
    validator.parameter(UserConstants.NAME, name).isString().notEmpty().maxLength(255);

    final var email = this.jsonHelper.extractString(UserConstants.EMAIL, jsonElement);
    validator.parameter(UserConstants.EMAIL, email).isString().notEmpty();

    if (this.jsonHelper.parameterExists(UserConstants.ENABLED, jsonElement)) {
      final var enabled = this.jsonHelper.extractBoolean(UserConstants.ENABLED, jsonElement);
      validator.parameter(UserConstants.ENABLED, enabled).isBoolean();
    }
    if (this.jsonHelper.parameterExists(UserConstants.IS_ACCOUNT_NON_EXPIRED, jsonElement)) {
      final var isAccountNonExpired =
          this.jsonHelper.extractBoolean(UserConstants.IS_ACCOUNT_NON_EXPIRED, jsonElement);
      validator.parameter(UserConstants.IS_ACCOUNT_NON_EXPIRED, isAccountNonExpired).isBoolean();
    }
    if (this.jsonHelper.parameterExists(UserConstants.IS_ACCOUNT_NON_LOCKED, jsonElement)) {
      final var isAccountNonLocked =
          this.jsonHelper.extractBoolean(UserConstants.IS_ACCOUNT_NON_LOCKED, jsonElement);
      validator.parameter(UserConstants.IS_ACCOUNT_NON_LOCKED, isAccountNonLocked).isBoolean();
    }
    if (this.jsonHelper.parameterExists(UserConstants.IS_CREDENTIALS_NON_EXPIRED, jsonElement)) {
      final var isCredentialsNonExpired =
          this.jsonHelper.extractBoolean(UserConstants.IS_CREDENTIALS_NON_EXPIRED, jsonElement);
      validator
          .parameter(UserConstants.IS_CREDENTIALS_NON_EXPIRED, isCredentialsNonExpired)
          .isBoolean();
    }
  }

  public void update(String json) {
    final var typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
    this.jsonHelper.unsupportedParameters(typeOfMap, json, UserConstants.SUPPORTED_PARAMETER);

    final var jsonElement = this.jsonHelper.parse(json);

    if (this.jsonHelper.parameterExists(UserConstants.USERNAME, jsonElement)) {
      final var username = this.jsonHelper.extractString(UserConstants.USERNAME, jsonElement);
      validator.parameter(UserConstants.USERNAME, username).isString().notEmpty().maxLength(255);
    }
    if (this.jsonHelper.parameterExists(UserConstants.NAME, jsonElement)) {
      final var name = this.jsonHelper.extractString(UserConstants.NAME, jsonElement);
      validator.parameter(UserConstants.NAME, name).isString().notEmpty().maxLength(255);
    }
    if (this.jsonHelper.parameterExists(UserConstants.EMAIL, jsonElement)) {
      final var email = this.jsonHelper.extractString(UserConstants.EMAIL, jsonElement);
      validator.parameter(UserConstants.EMAIL, email).isString().notEmpty();
    }
    if (this.jsonHelper.parameterExists(UserConstants.ENABLED, jsonElement)) {
      final var enabled = this.jsonHelper.extractBoolean(UserConstants.ENABLED, jsonElement);
      validator.parameter(UserConstants.ENABLED, enabled).isBoolean();
    }
    if (this.jsonHelper.parameterExists(UserConstants.IS_ACCOUNT_NON_EXPIRED, jsonElement)) {
      final var isAccountNonExpired =
          this.jsonHelper.extractBoolean(UserConstants.IS_ACCOUNT_NON_EXPIRED, jsonElement);
      validator.parameter(UserConstants.IS_ACCOUNT_NON_EXPIRED, isAccountNonExpired).isBoolean();
    }
    if (this.jsonHelper.parameterExists(UserConstants.IS_ACCOUNT_NON_LOCKED, jsonElement)) {
      final var isAccountNonLocked =
          this.jsonHelper.extractBoolean(UserConstants.IS_ACCOUNT_NON_LOCKED, jsonElement);
      validator.parameter(UserConstants.IS_ACCOUNT_NON_LOCKED, isAccountNonLocked).isBoolean();
    }
    if (this.jsonHelper.parameterExists(UserConstants.IS_CREDENTIALS_NON_EXPIRED, jsonElement)) {
      final var isCredentialsNonExpired =
          this.jsonHelper.extractBoolean(UserConstants.IS_CREDENTIALS_NON_EXPIRED, jsonElement);
      validator
          .parameter(UserConstants.IS_CREDENTIALS_NON_EXPIRED, isCredentialsNonExpired)
          .isBoolean();
    }
  }
}
