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


import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.Link;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author YISivlay
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageableResponse<T> {

    @JsonProperty("embedded")
    private EmbeddedContent<T> embedded;

    @JsonProperty("links")
    private Map<String, Link> links;

    @JsonProperty("page")
    private PageMetadata page;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EmbeddedContent<T> {

        @JsonIgnore
        private List<T> content;

        @JsonAnyGetter
        public Map<String, List<T>> getDynamicPropertyName() {
            if (content == null || content.isEmpty()) {
                return Collections.emptyMap();
            }
            return Map.of("contents", content);
        }

        @JsonAnySetter
        public void setDynamicProperties(String key, List<T> value) {
            this.content = value;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PageMetadata {

        private Integer size;
        private Long totalElements;
        private Integer totalPages;
        private Integer number;

    }

}
