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

import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author YISivlay
 */
@Component
public class PageableHateoasAssembler {

    @Autowired
    private HttpServletRequest request;

    public <T> PageableResponse<T> toModel(Page<T> page) {

        PageableResponse.EmbeddedContent<T> embedded = PageableResponse.EmbeddedContent.<T>builder()
                                                                                       .content(page.getContent())
                                                                                       .build();

        PageableResponse.PageMetadata pageMetadata = PageableResponse.PageMetadata.builder()
                                                                                  .size(page.getSize())
                                                                                  .totalElements(page.getTotalElements())
                                                                                  .totalPages(page.getTotalPages())
                                                                                  .number(page.getNumber())
                                                                                  .build();

        Map<String, Link> links = generateLinks(page,
                                                request.getRequestURI());

        return PageableResponse.<T>builder()
                               .embedded(embedded)
                               .links(links)
                               .page(pageMetadata)
                               .build();
    }

    private Map<String, Link> generateLinks(Page<?> page,
                                            String basePath) {
        Map<String, Link> links = new LinkedHashMap<>();

        int currentPage = page.getNumber();
        int pageSize = page.getSize();
        int totalPages = page.getTotalPages();

        links.put("self",
                  createLink(basePath,
                             currentPage,
                             pageSize));
        links.put("first",
                  createLink(basePath,
                             0,
                             pageSize));
        if (totalPages > 0) {
            links.put("last",
                      createLink(basePath,
                                 totalPages - 1,
                                 pageSize));
        }
        if (page.hasNext()) {
            links.put("next",
                      createLink(basePath,
                                 currentPage + 1,
                                 pageSize));
        }
        if (page.hasPrevious()) {
            links.put("prev",
                      createLink(basePath,
                                 currentPage - 1,
                                 pageSize));
        }

        return links;
    }

    private Link createLink(String basePath,
                            int page,
                            int size) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(basePath);
        builder.queryParam("page",
                           page);
        builder.queryParam("size",
                           size);
        return Link.of(builder.build()
                              .toUriString());
    }

    public <T> List<T> unpaged(Page<T> page) {
        return page.getContent();
    }

}
