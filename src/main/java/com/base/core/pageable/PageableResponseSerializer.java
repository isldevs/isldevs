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
package com.base.core.pageable;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.List;

/**
 * @author YISivlay
 */
public class PageableResponseSerializer extends JsonSerializer<PageableResponse<?>> {

    @Override
    public void serialize(PageableResponse<?> response, JsonGenerator generator, SerializerProvider serializerProvider) throws IOException {
        generator.writeStartObject();

        generator.writeObjectFieldStart("embedded");
        if (response.getEmbedded() != null && response.getEmbedded().getContent() != null) {
            List<?> content = response.getEmbedded().getContent();
            if (!content.isEmpty()) {
                generator.writeObjectField("contents", content);
            }
        }
        generator.writeEndObject();

        if (response.getLinks() != null) {
            generator.writeObjectField("links", response.getLinks());
        }

        if (response.getPage() != null) {
            generator.writeObjectField("page", response.getPage());
        }

        generator.writeEndObject();
    }
}
