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
package com.base.core.exception;

import java.util.Collection;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * @author YISivlay
 */
@Component
public class ApiDataValidator {

  public Validator parameter(String param, Object value) {
    return new Validator(param, value);
  }

  public static class Validator {

    private final String param;
    private final Object value;

    private Validator(String param, Object value) {
      this.param = param;
      this.value = value;
    }

    /** Check if value is a String */
    public Validator isString() {
      if (value != null && !(value instanceof String)) {
        throw new ErrorException(
            HttpStatus.BAD_REQUEST, "msg.internal.error", "Value of param must be string", param);
      }
      return this;
    }

    /** Check if value is Boolean */
    public Validator isBoolean() {
      if (value != null && !(value instanceof Boolean)) {
        throw new ErrorException(
            HttpStatus.BAD_REQUEST, "msg.internal.error", "Value of param must be boolean", param);
      }
      return this;
    }

    /** Check if String is not empty */
    public Validator notEmpty() {
      if (value.toString().trim().isEmpty()) {
        throw new ErrorException(
            HttpStatus.BAD_REQUEST,
            "msg.internal.error",
            "Value of param must not be empty",
            param);
      }
      return this;
    }

    /** Check if String is not empty */
    public Validator notNull() {
      if (value == null) {
        throw new ErrorException(
            HttpStatus.BAD_REQUEST, "msg.internal.error", "Value of param must not be null", param);
      }
      return this;
    }

    /** Check if String is not empty and null */
    public Validator notNullAndNotEmpty() {
      if (value == null) {
        throw new ErrorException(
            HttpStatus.BAD_REQUEST, "msg.internal.error", "Value of param must not be null", param);
      }
      if (value.toString().trim().isEmpty()) {
        throw new ErrorException(
            HttpStatus.BAD_REQUEST,
            "msg.internal.error",
            "Value of param must not be empty",
            param);
      }
      return this;
    }

    /** Check max length for String */
    public Validator maxLength(int max) {
      if (value != null && value.toString().length() > max) {
        throw new ErrorException(
            HttpStatus.BAD_REQUEST,
            "msg.internal.error",
            "Value of param must not be exceed " + max + " characters",
            param);
      }
      return this;
    }

    /** Check if value is a Number */
    public Validator isNumber() {
      if (value != null && !(value instanceof Number)) {
        throw new ErrorException(
            HttpStatus.BAD_REQUEST, "msg.internal.error", "Value of param must be number", param);
      }
      return this;
    }

    /** Check if value is an Array of Numbers */
    public Validator isArrayOfNumber() {
      if (value != null) {
        if (value instanceof Collection<?>) {
          for (Object item : (Collection<?>) value) {
            if (!(item instanceof Number)) {
              throw new ErrorException(
                  HttpStatus.BAD_REQUEST,
                  "msg.internal.error",
                  "All elements of param must be numbers",
                  param);
            }
          }
        } else if (value.getClass().isArray()) {
          int length = java.lang.reflect.Array.getLength(value);
          for (int i = 0; i < length; i++) {
            Object item = java.lang.reflect.Array.get(value, i);
            if (!(item instanceof Number)) {
              throw new ErrorException(
                  HttpStatus.BAD_REQUEST,
                  "msg.internal.error",
                  "All elements of param must be numbers",
                  param);
            }
          }
        } else {
          throw new ErrorException(
              HttpStatus.BAD_REQUEST,
              "msg.internal.error",
              "Value of param must be an array of numbers",
              param);
        }
      }
      return this;
    }

    /** Check if value is a positive Number */
    public Validator isPositive() {
      if (value != null) {
        if (value instanceof Number num) {
          if (num.doubleValue() <= 0) {
            throw new ErrorException(
                HttpStatus.BAD_REQUEST,
                "msg.internal.error",
                "Value of param must be positive",
                param);
          }
        } else {
          throw new ErrorException(
              HttpStatus.BAD_REQUEST,
              "msg.internal.error",
              "Value of param must be positive",
              param);
        }
      }
      return this;
    }

    /** Check numeric range */
    public Validator between(Number min, Number max) {
      if (value instanceof Number num) {
        double v = num.doubleValue();
        if (v < min.doubleValue() || v > max.doubleValue()) {
          throw new ErrorException(
              HttpStatus.BAD_REQUEST,
              "msg.internal.error",
              "Value of param must be between " + min + " and " + max,
              param);
        }
      }
      return this;
    }

    /** Check if collection is not empty */
    public Validator notEmptyCollection() {
      if (value == null || (value instanceof Collection<?> col && col.isEmpty())) {
        throw new ErrorException(
            HttpStatus.BAD_REQUEST,
            "msg.internal.error",
            "Value of param must not be empty",
            param);
      }
      return this;
    }
  }
}
