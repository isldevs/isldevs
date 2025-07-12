package com.base.config.core.serializer.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author YISivlay
 */
@Component
public class JsonSerializerImpl<T> implements JsonSerializer{

    private final JsonSerializer serializer;

    @Autowired
    public JsonSerializerImpl(JsonSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public String serialize(Object object) {
        return this.serializer.serialize(object);
    }
}
