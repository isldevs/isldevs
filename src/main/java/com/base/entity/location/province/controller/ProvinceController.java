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
package com.base.entity.location.province.controller;


import com.base.core.command.service.LogService;
import com.base.core.serializer.JsonSerializerImpl;
import com.base.entity.location.province.dto.ProvinceDTO;
import com.base.entity.location.province.handler.ProvinceCommandHandler;
import com.base.entity.location.province.service.ProvinceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.*;

/**
 * @author YISivlay
 */
@RestController
@RequestMapping(ProvinceConstants.API_PATH)
public class ProvinceController {

    private final JsonSerializerImpl<ProvinceDTO> serializer;
    private final ProvinceService service;
    private final LogService logService;

    @Autowired
    public ProvinceController(final JsonSerializerImpl<ProvinceDTO> serializer,
                              final ProvinceService service,
                              final LogService logService) {
        this.serializer = serializer;
        this.service = service;
        this.logService = logService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String createProvince(@RequestBody String json) {
        final var command = new ProvinceCommandHandler()
                .create()
                .json(json)
                .build();
        final var data = this.logService.log(command);
        return this.serializer.serialize(data);
    }

    @PutMapping(value = "{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @CacheEvict(value = "provinces", key = "#id")
    public String updateProvince(@RequestBody String json, @PathVariable Long id) {
        final var command = new ProvinceCommandHandler()
                .update(id)
                .json(json)
                .build();
        final var data = this.logService.log(command);
        return this.serializer.serialize(data);
    }

    @DeleteMapping(value = "{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @CacheEvict(value = "provinces", key = "#id")
    public String deleteProvince(@PathVariable Long id) {
        final var command = new ProvinceCommandHandler()
                .delete(id)
                .build();
        final var data = this.logService.log(command);
        return this.serializer.serialize(data);
    }

    @GetMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Cacheable(value = "provinces", key = "#id")
    public String getProvince(@PathVariable Long id) {
        var data = this.service.getProvinceById(id);
        return this.serializer.serialize(data);
    }

    @GetMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listOffices(@RequestParam(required = false) Integer page,
                                         @RequestParam(required = false) Integer size,
                                         @RequestParam(required = false) String search,
                                         PagedResourcesAssembler<ProvinceDTO> pagination) {
        if (page == null || size == null) {
            var provinces = this.service.listProvinces(null, null, search).getContent();
            return ResponseEntity.ok(provinces);
        }
        var provinces = this.service.listProvinces(page, size, search);
        return ResponseEntity.ok(pagination.toModel(provinces));
    }

}
