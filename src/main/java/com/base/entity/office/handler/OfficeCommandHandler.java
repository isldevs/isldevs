package com.base.entity.office.handler;


import com.base.core.command.data.CommandBuilder;
import com.base.entity.office.controller.OfficeConstants;

/**
 * @author YISivlay
 */
public class OfficeCommandHandler extends CommandBuilder {

    public CommandBuilder create() {
        return this.action("CREATE")
                .entity(OfficeConstants.PERMISSION)
                .href(OfficeConstants.API_PATH);
    }

    public CommandBuilder update(final Long id) {
        return this.action("UPDATE")
                .entity(OfficeConstants.PERMISSION)
                .id(id)
                .href(OfficeConstants.API_PATH + "/" + id);
    }

    public CommandBuilder delete(final Long id) {
        return this.action("DELETE")
                .entity(OfficeConstants.PERMISSION)
                .id(id)
                .href(OfficeConstants.API_PATH + "/" + id);
    }
}
