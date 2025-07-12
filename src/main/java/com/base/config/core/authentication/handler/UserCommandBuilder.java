package com.base.config.core.authentication.handler;


import com.base.config.core.authentication.controller.UserConstants;
import com.base.config.core.command.data.CommandBuilder;

/**
 * @author YISivlay
 */
public class UserCommandBuilder extends CommandBuilder {

    public CommandBuilder create() {
        return this.action("CREATE")
                .entity("USER")
                .href(UserConstants.API_PATH);
    }

    public CommandBuilder update(final Long id) {
        return this.action("UPDATE")
                .entity("USER")
                .id(id)
                .href(UserConstants.API_PATH + "/" + id);
    }

    public CommandBuilder delete(final Long id) {
        return this.action("DELETE")
                .entity("USER")
                .id(id)
                .href(UserConstants.API_PATH + "/" + id);
    }
}
