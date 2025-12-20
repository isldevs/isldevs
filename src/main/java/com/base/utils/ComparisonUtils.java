package com.base.utils;


/**
 * @author YISivlay
 */
public class ComparisonUtils {

    /**
     * Checks if a value is within the inclusive range [start, end].
     *
     * @param value the value to check
     * @param start the lower bound
     * @param end the upper bound
     * @param <T> type must implement Comparable
     * @return true if value >= start and value <= end
     */
    public static <T extends Comparable<T>> boolean isBetween(T value, T start, T end) {
        return value.compareTo(start) >= 0 && value.compareTo(end) <= 0;
    }

}
