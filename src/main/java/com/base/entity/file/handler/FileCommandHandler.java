package com.base.entity.file.handler;


import com.base.core.command.data.CommandBuilder;
import com.base.entity.file.controller.FileConstants;

/**
 * @author YISivlay
 */
public class FileCommandHandler extends CommandBuilder {

    public CommandBuilder upload(String entity, Long entityId) {
        return this.action("UPLOAD")
                .entity(FileConstants.PERMISSION)
                .entityType(entity)
                .entityId(entityId)
                .href("/" + entity + "/" + entityId + "/files")
                .id(entityId);
    }

    public CommandBuilder delete(final String entity, final Long entityId, final Long id) {
        return this.action("DELETE")
                .entity(FileConstants.PERMISSION)
                .entityType(entity)
                .entityId(entityId)
                .href("/" + entity + "/" + entityId + "/files/" + id)
                .id(id);
    }
}
