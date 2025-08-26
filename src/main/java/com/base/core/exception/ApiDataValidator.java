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

import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.Collection;

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

        /**
         * Check if value is a String
         */
        public Validator isString() {
            if (value != null && !(value instanceof String)) {
                throw new ErrorException("validation.string", param);
            }
            return this;
        }

        /**
         * Check if value is Boolean
         */
        public Validator isBoolean() {
            if (value != null && !(value instanceof Boolean)) {
                throw new ErrorException("validation.boolean", param);
            }
            return this;
        }

        /**
         * Check if String is not empty
         */
        public Validator notEmpty() {
            if (value == null || value.toString().trim().isEmpty()) {
                throw new ErrorException("validation.not.empty", param);
            }
            return this;
        }

        /**
         * Check max length for String
         */
        public Validator maxLength(int max) {
            if (value != null && value.toString().length() > max) {
                throw new ErrorException("validation.max.length", param, max);
            }
            return this;
        }

        /**
         * Check if value is a Number
         */
        public Validator isNumber() {
            if (value != null && !(value instanceof Number)) {
                throw new ErrorException("validation.number", param);
            }
            return this;
        }

        /**
         * Check numeric range
         */
        public Validator between(Number min, Number max) {
            if (value instanceof Number num) {
                double v = num.doubleValue();
                if (v < min.doubleValue() || v > max.doubleValue()) {
                    throw new ErrorException("validation.range", param,
                            NumberFormat.getNumberInstance().format(min),
                            NumberFormat.getNumberInstance().format(max));
                }
            }
            return this;
        }

        /**
         * Check if collection is not empty
         */
        public Validator notEmptyCollection() {
            if (value == null || (value instanceof Collection<?> col && col.isEmpty())) {
                throw new ErrorException("validation.not.empty.collection", param);
            }
            return this;
        }
    }
}