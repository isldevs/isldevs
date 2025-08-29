package com.base.entity.file.service;


import com.base.core.command.data.JsonCommand;
import com.base.core.command.data.LogData;
import com.base.entity.file.model.File;
import com.base.entity.file.repository.FileRepository;
import com.base.entity.file.repository.Storage;
import com.base.entity.file.validation.FileDataValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

/**
 * @author YISivlay
 */
@Service
public class FileServiceImpl implements FileService {

    private final FileDataValidation validator;
    private final Storage storage;
    private final MessageSource messageSource;
    private final FileRepository repository;

    @Autowired
    public FileServiceImpl(final FileDataValidation validator,
                           final Storage storage,
                           final MessageSource messageSource,
                           final FileRepository repository) {
        this.validator = validator;
        this.storage = storage;
        this.messageSource = messageSource;
        this.repository = repository;
    }

    @Override
    public Map<String, Object> uploadFile(JsonCommand command) {

        this.validator.upload(command.getEntityType(), command.getEntityId());

        final var storage = this.storage.repository();

        try {
            var file = command.getFile();
            final var directory = storage.writeFile(
                    file.getInputStream(),
                    command.getEntityId(),
                    command.getEntityType(),
                    file.getOriginalFilename(),
                    storage);

            var existing = this.repository.findByEntityAndEntityId(command.getEntityType(), command.getEntityId());
            if (existing != null) {

                existing.setName(file.getOriginalFilename());
                existing.setSize(file.getSize());
                existing.setMimeType(file.getContentType());
                existing.setLocation(directory);
                existing.setStorageType(storage.getStorageType().getValue());

                this.repository.save(existing);

                return LogData.builder()
                        .id(existing.getId())
                        .success("msg.success", messageSource)
                        .build()
                        .claims();

            } else {
                File newFile = File.builder()
                        .entity(command.getEntityType())
                        .entityId(command.getEntityId())
                        .name(file.getOriginalFilename())
                        .size(file.getSize())
                        .mimeType(file.getContentType())
                        .storageType(storage.getStorageType().getValue())
                        .location(directory)
                        .build();
                this.repository.save(newFile);

                return LogData.builder()
                        .id(newFile.getId())
                        .success("msg.success", messageSource)
                        .build()
                        .claims();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
