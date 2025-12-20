package com.base.utils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author YISivlay
 */
public class CollectionUtils {

    /**
     * Returns all duplicate elements from the given collection.
     *
     * @param values the input collection
     * @param <T> the element type
     * @return a set containing elements that appear more than once
     */
    public static <T> Set<T> findDuplicates(Collection<T> values) {
        if (values == null || values.isEmpty()) {
            return Set.of();
        }
        return values.stream()
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .filter(e -> e.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableSet());
    }
}
