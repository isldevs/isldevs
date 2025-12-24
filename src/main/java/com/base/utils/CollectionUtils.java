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

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author YISivlay
 */
public class CollectionUtils {

    private CollectionUtils() {
    }

    /**
     * Returns all duplicate elements from the given collection.
     *
     * @param values
     *     the input collection
     * @param <T>
     *     the element type
     * @return a set containing elements that appear more than once
     */
    public static <T> Set<T> findDuplicates(Collection<T> values) {

        Objects.requireNonNull(values, "values must not be null");

        return values.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .filter(e -> e.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Creates a map keyed by an extracted String value (upper-cased),
     * ignoring null keys and keeping the first occurrence on duplicates.
     *
     * @param values
     *     collection of elements
     * @param keyExtractor
     *     function to extract the key
     */
    public static <T> Map<String, T> toMapByCode(Collection<T> values,
                                                 Function<T, String> keyExtractor) {

        Objects.requireNonNull(values, "values must not be null");
        Objects.requireNonNull(keyExtractor, "keyExtractor must not be null");

        return values.stream()
                .filter(v -> keyExtractor.apply(v) != null)
                .collect(Collectors.toMap(v -> keyExtractor.apply(v)
                        .toUpperCase(), Function.identity(), (a,
                                                              _) -> a));
    }
}
