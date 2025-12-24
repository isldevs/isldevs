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
package com.base.utils;

import java.util.Objects;

/**
 * @author YISivlay
 */
public class ComparisonUtils {

    /**
     * Checks if a value is within the inclusive range [start, end].
     *
     * @param value
     *     the value to check
     * @param start
     *     the lower bound
     * @param end
     *     the upper bound
     * @param <T>
     *     type must implement Comparable
     * @return true if value >= start and value <= end
     */
    public static <T extends Comparable<T>> boolean isBetween(T value,
                                                              T start,
                                                              T end) {

        Objects.requireNonNull(value, "value must not be null");
        Objects.requireNonNull(value, "start must not be null");
        Objects.requireNonNull(value, "end must not be null");

        return value.compareTo(start) >= 0 && value.compareTo(end) <= 0;
    }

}
