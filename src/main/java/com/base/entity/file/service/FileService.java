package com.base.entity.file.service;


import com.base.core.command.data.JsonCommand;

import java.util.Map;

/**
 * @author YISivlay
 */
public interface FileService {
    Map<String, Object> uploadFile(JsonCommand command);
}
