package com.base.utils;


import com.google.gson.Gson;

import java.util.List;

/**
 * @author YISivlay
 */
public class JsonUtils {

    private static final Gson gson = new Gson();

    /**
     * Parses a JSON array string into a list of elements of the given type.
     *
     * @param json the JSON array string (e.g. [{"id":1},{"id":2}])
     * @param elementArrayType the array class of the target type (e.g. MyDto[].class)
     * @param <T> the element type
     * @return a list of parsed elements, or an empty list if the input is null or blank
     * @throws com.google.gson.JsonSyntaxException if the JSON is invalid
     */
    public static <T> List<T> parseJsonArray(String json, Class<T[]> className) {
        if (json == null || json.isBlank()) {
            return List.of();
        }

        T[] array = gson.fromJson(json, className);
        return array == null ? List.of() : List.of(array);
    }

    /**
     * Parses a JSON string into an object of the specified type.
     *
     * @param json the JSON string representation
     * @param targetType the target class to deserialize into
     * @param <T> the target type
     * @return the parsed object, or {@code null} if the input is null or blank
     * @throws com.google.gson.JsonSyntaxException if the JSON is invalid
     */
    public static <T> T parseJsonObject(String json, Class<T> targetType) {
        if (json == null || json.isBlank()) {
            return null;
        }
        return gson.fromJson(json, targetType);
    }

}
