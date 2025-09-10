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
package com.base.entity.location.district.controller;


import com.base.core.command.service.LogService;
import com.base.core.pageable.PageableHateoasAssembler;
import com.base.core.serializer.JsonSerializerImpl;
import com.base.entity.location.district.dto.DistrictDTO;
import com.base.entity.location.district.handler.DistrictCommandHandler;
import com.base.entity.location.district.service.DistrictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author YISivlay
 */
@RestController
@RequestMapping(DistrictConstants.API_PATH)
public class DistrictController {

    private final PageableHateoasAssembler pageable;
    private final JsonSerializerImpl<DistrictDTO> serializer;
    private final DistrictService service;
    private final LogService logService;

    @Autowired
    public DistrictController(final PageableHateoasAssembler pageable,
                              final JsonSerializerImpl<DistrictDTO> serializer,
                              final DistrictService service,
                              final LogService logService) {
        this.pageable = pageable;
        this.serializer = serializer;
        this.service = service;
        this.logService = logService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String createDistrict(@RequestBody String json) {
        final var command = new DistrictCommandHandler()
                .create()
                .json(json)
                .build();
        final var data = this.logService.log(command);
        return this.serializer.serialize(data);
    }

    @PutMapping(value = "{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateDistrict(@RequestBody String json, @PathVariable Long id) {
        final var command = new DistrictCommandHandler()
                .update(id)
                .json(json)
                .build();
        final var data = this.logService.log(command);
        return this.serializer.serialize(data);
    }

    @DeleteMapping(value = "{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public String deleteDistrict(@PathVariable Long id) {
        final var command = new DistrictCommandHandler()
                .delete(id)
                .build();
        final var data = this.logService.log(command);
        return this.serializer.serialize(data);
    }

    @GetMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String getDistrict(@PathVariable Long id) {
        var data = this.service.getDistrictById(id);
        return this.serializer.serialize(data);
    }

    @GetMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listDistricts(@RequestParam(required = false) Integer page,
                                         @RequestParam(required = false) Integer size,
                                         @RequestParam(required = false) String search) {
        var districts = this.service.listDistricts(page, size, search);
        var response = (page == null || size == null) ? pageable.unpaged(districts) : pageable.toModel(districts);
        return ResponseEntity.ok(response);
    }

}
