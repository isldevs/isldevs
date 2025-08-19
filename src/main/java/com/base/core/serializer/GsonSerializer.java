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
package com.base.core.serializer;


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
        final var json = this.gson.toJson(object);
        if (!"null".equalsIgnoreCase(json)) {
            serialized = json;
        }
        return serialized;
    }
}
