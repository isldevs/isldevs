package com.base.entity.file.controller;


import com.base.core.command.service.LogService;
import com.base.core.serializer.JsonSerializerImpl;
import com.base.entity.file.dto.FileDTO;
import com.base.entity.file.handler.FileCommandHandler;
import com.base.entity.file.repository.FileUtils;
import com.base.entity.file.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author YISivlay
 */
@RestController
@RequestMapping("{entity}/{entityId}/files")
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadFile(@PathVariable("entity") final String entity,
                             @PathVariable("entityId") final Long entityId,
                             @RequestParam("file") final MultipartFile file) {
        FileUtils.isValidEntityName(entity);
        FileUtils.isValidateMimeType(file.getContentType());
        final var command = new FileCommandHandler()
                .upload(entity, entityId)
                .file(file)
                .build();

        final var data = this.logService.log(command);
        return this.serializer.serialize(data);
    }
}
