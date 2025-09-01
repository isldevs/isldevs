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
package com.base.entity.file.service;

import com.base.core.command.data.JsonCommand;
import com.base.core.command.data.LogData;
import com.base.core.exception.ErrorException;
import com.base.core.exception.NotFoundException;
import com.base.entity.file.controller.FileConstants;
import com.base.entity.file.model.File;
import com.base.entity.file.repository.FileRepository;
import com.base.entity.file.repository.FileUtils;
import com.base.entity.file.repository.Storage;
import com.base.entity.file.repository.StorageUtils;
import com.base.entity.file.validation.FileDataValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * @author YISivlay
 */
@Service
public class FileServiceImpl implements FileService {

    private final Storage storage;
    private final FileDataValidation validator;
    private final MessageSource messageSource;
    private final FileRepository repository;

    @Autowired
    public FileServiceImpl(final Storage storage,
                           final FileDataValidation validator,
                           final MessageSource messageSource,
                           final FileRepository repository) {
        this.storage = storage;
        this.validator = validator;
        this.messageSource = messageSource;
        this.repository = repository;
    }

    @Override
    public Map<String, Object> uploadFile(JsonCommand command) {
        this.validator.upload(command.getEntityType(), command.getEntityId());
        var storage = this.storage.repository();
        try {
            var file = command.getFile();

            String oldFileName = null;
            File entityFile = this.repository.findByEntityAndEntityId(command.getEntityType(), command.getEntityId());
            if (entityFile == null) {
                entityFile = File.builder()
                        .entity(command.getEntityType())
                        .entityId(command.getEntityId())
                        .build();
            } else {
                oldFileName = entityFile.getName();
            }

            var directory = storage.writeFile(
                    file.getInputStream(),
                    command.getEntityId(),
                    command.getEntityType(),
                    file.getOriginalFilename(),
                    oldFileName,
                    storage
            );
            entityFile.setName(file.getOriginalFilename());
            entityFile.setSize(file.getSize());
            entityFile.setMimeType(file.getContentType());
            entityFile.setLocation(directory);
            entityFile.setStorageType(storage.getStorageType().getValue());

            this.repository.save(entityFile);

            return LogData.builder()
                    .id(entityFile.getId())
                    .success("msg.success", messageSource)
                    .build()
                    .claims();

        } catch (IOException e) {
            throw new ErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "msg.internal.error", "Error while uploading file");
        }
    }

    @Override
    public Map<String, Object> delete(final JsonCommand command) {
        var storage = this.storage.repository();
        var file = this.repository.findByEntityAndEntityId(command.getEntityType(), command.getEntityId());
        if (file == null)
            throw new NotFoundException("msg.not.found", command.getEntityType(), command.getEntityId());

        var dir = Paths.get(FileConstants.DIR, command.getEntityType(), String.valueOf(command.getEntityId()), file.getName());
        storage.deleteFile(dir);
        this.repository.delete(file);
        this.repository.flush();
        return LogData.builder()
                .success("msg.success", messageSource)
                .build()
                .claims();

    }

    @Override
    public Map<String, Object> fileURL(String entity, Long entityId) {
        var file = this.repository.findByEntityAndEntityId(entity, entityId);
        if (file == null)
            throw new NotFoundException("msg.not.found", entity, entityId);

        var dir = Paths.get(FileConstants.DIR, entity, String.valueOf(entityId), file.getName());
        var storageUtils = this.storage.repository(file.getStorageType());

        var url = storageUtils.readUrl(dir);
        return LogData.builder()
                .file(url)
                .success("msg.success", messageSource)
                .build()
                .claims();
    }

    @Override
    public Map<String, Object> fileBase64(String entity, Long entityId) {
        var file = this.repository.findByEntityAndEntityId(entity, entityId);
        if (file == null)
            throw new NotFoundException("msg.not.found", entity, entityId);

        var dir = Paths.get(FileConstants.DIR, entity, String.valueOf(entityId), file.getName());
        var storageUtils = this.storage.repository(file.getStorageType());

        var base64 = storageUtils.readBase64(dir);
        base64 = FileUtils.suffix(file.getName()) + base64;
        return LogData.builder()
                .file(base64)
                .success("msg.success", messageSource)
                .build()
                .claims();
    }

    @Override
    public Map<String, Object> fileByte(String entity, Long entityId) {
        var file = this.repository.findByEntityAndEntityId(entity, entityId);
        if (file == null)
            throw new NotFoundException("msg.not.found", entity, entityId);

        var dir = Paths.get(FileConstants.DIR, entity, String.valueOf(entityId), file.getName());
        var storageUtils = this.storage.repository(file.getStorageType());

        var bytes = storageUtils.readByte(dir);
        return LogData.builder()
                .file(bytes)
                .success("msg.success", messageSource)
                .build()
                .claims();
    }

    @Override
    public Map<String, Object> fileInputStream(String entity, Long entityId) {
        var file = this.repository.findByEntityAndEntityId(entity, entityId);
        if (file == null)
            throw new NotFoundException("msg.not.found", entity, entityId);

        var dir = Paths.get(FileConstants.DIR, entity, String.valueOf(entityId), file.getName());
        var storageUtils = this.storage.repository(file.getStorageType());

        var inputStream = storageUtils.readInputStream(dir);
        return LogData.builder()
                .file(inputStream)
                .success("msg.success", messageSource)
                .build()
                .claims();
    }
}
