package com.base.entity.file.handler;


import com.base.core.annotation.CommandType;
import com.base.core.command.data.JsonCommand;
import com.base.core.command.service.CommandHandlerProcessing;
import com.base.entity.file.controller.FileConstants;
import com.base.entity.file.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * @author YISivlay
 */
@Service
@Transactional
@CommandType(action = "UPLOAD", entity = FileConstants.PERMISSION)
public class CreateFileHandlerProcessing implements CommandHandlerProcessing {

    private final FileService service;

    @Autowired
    public CreateFileHandlerProcessing(FileService service) {
        this.service = service;
    }

    @Override
    public Map<String, Object> process(JsonCommand command) {
        return this.service.uploadFile(command);
    }
}
