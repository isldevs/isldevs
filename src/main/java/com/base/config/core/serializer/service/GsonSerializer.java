package com.base.config.core.serializer.service;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Component;

/**
 * @author YISivlay
 */
@Component
public final class GsonSerializer {

    private final Gson gson;

    public GsonSerializer() {
        final GsonBuilder builder = new GsonBuilder();
        this.gson = builder.create();
    }

    public String serialize(Object object) {
        String serialized = null;
        final String json = this.gson.toJson(object);
        if (!"null".equalsIgnoreCase(json)) {
            serialized = json;
        }
        return serialized;
    }
}
