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
package com.base.portfolio.file.controller;

import com.base.core.command.service.LogService;
import com.base.core.serializer.JsonSerializerImpl;
import com.base.portfolio.file.dto.FileDTO;
import com.base.portfolio.file.handler.FileCommandHandler;
import com.base.portfolio.file.repository.FileUtils;
import com.base.portfolio.file.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @author YISivlay
 */
@RestController
@RequestMapping(FileConstants.API_PATH)
@Scope("singleton")
public class FileController {

    private final JsonSerializerImpl<FileDTO> serializer;

    private final FileService service;

    private final LogService logService;

    @Autowired
    public FileController(final JsonSerializerImpl<FileDTO> serializer,
                          final FileService service,
                          final LogService logService) {
        this.serializer = serializer;
        this.service = service;
        this.logService = logService;
    }

    @PostMapping(value = "/{entity}/{entityId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadFile(@PathVariable("entity")
    final String entity,
                             @PathVariable("entityId")
                             final Long entityId,
                             @RequestParam("file")
                             final MultipartFile file) {

        FileUtils.isValidEntityName(entity);
        FileUtils.isValidateMimeType(file.getContentType());

        final var command = new FileCommandHandler().upload(entity,
                                                            entityId)
                                                    .file(file)
                                                    .build();

        final var data = this.logService.log(command);
        return this.serializer.serialize(data);
    }

    @DeleteMapping(value = "/{entity}/{entityId}")
    public String deleteFile(@PathVariable("entity")
    final String entity,
                             @PathVariable("entityId")
                             final Long entityId) {

        FileUtils.isValidEntityName(entity);
        final var command = new FileCommandHandler().delete(entity,
                                                            entityId)
                                                    .build();

        final var data = this.logService.log(command);
        return this.serializer.serialize(data);
    }

    @GetMapping("{entity}/{entityId}/url")
    public String fileURL(@PathVariable("entity")
    final String entity,
                          @PathVariable("entityId")
                          final Long entityId) {
        FileUtils.isValidEntityName(entity);
        Map<String, Object> url = this.service.fileURL(entity,
                                                       entityId);
        return this.serializer.serialize(url);
    }

    @GetMapping("{entity}/{entityId}/base64")
    public String fileBase64(@PathVariable("entity")
    final String entity,
                             @PathVariable("entityId")
                             final Long entityId) {
        FileUtils.isValidEntityName(entity);
        Map<String, Object> url = this.service.fileBase64(entity,
                                                          entityId);
        return this.serializer.serialize(url);
    }

    @GetMapping("{entity}/{entityId}/byte")
    public String fileByte(@PathVariable("entity")
    final String entity,
                           @PathVariable("entityId")
                           final Long entityId) {
        FileUtils.isValidEntityName(entity);
        Map<String, Object> url = this.service.fileByte(entity,
                                                        entityId);
        return this.serializer.serialize(url);
    }

    @GetMapping("{entity}/{entityId}/inputstream")
    public String fileInputStream(@PathVariable("entity")
    final String entity,
                                  @PathVariable("entityId")
                                  final Long entityId) {
        FileUtils.isValidEntityName(entity);
        Map<String, Object> url = this.service.fileInputStream(entity,
                                                               entityId);
        return this.serializer.serialize(url);
    }

}
