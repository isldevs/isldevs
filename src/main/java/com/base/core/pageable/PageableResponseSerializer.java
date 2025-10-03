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
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.List;

/**
 * @author YISivlay
 */
public class PageableResponseSerializer extends StdSerializer<PageableResponse> {

	public PageableResponseSerializer() {
		super(PageableResponse.class);
	}

	@Override
	public void serialize(PageableResponse pageableResponse, JsonGenerator jsonGenerator,
			SerializerProvider serializerProvider) throws IOException {
		jsonGenerator.writeStartObject();

		jsonGenerator.writeObjectFieldStart("embedded");
		if (pageableResponse.getEmbedded() != null && pageableResponse.getEmbedded().getContent() != null) {
			List<?> content = pageableResponse.getEmbedded().getContent();
			if (!content.isEmpty()) {
				jsonGenerator.writeObjectField("contents", content);
			}
		}
		jsonGenerator.writeEndObject();

		if (pageableResponse.getLinks() != null) {
			jsonGenerator.writeObjectField("links", pageableResponse.getLinks());
		}

		if (pageableResponse.getPage() != null) {
			jsonGenerator.writeObjectField("page", pageableResponse.getPage());
		}

		jsonGenerator.writeEndObject();
	}

}
