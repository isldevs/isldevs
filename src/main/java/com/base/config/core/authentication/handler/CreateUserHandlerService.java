package com.base.config.core.authentication.handler;


import com.base.config.core.annotation.CommandType;
import com.base.config.core.authentication.controller.UserConstants;
import com.base.config.core.authentication.service.UserService;
import com.base.config.core.command.data.CommandHandlerData;
import com.base.config.core.command.data.JsonCommand;
import com.base.config.core.command.service.CommandHandlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author YISivlay
 */
@Service
@CommandType(action = "CREATE", entity = UserConstants.PERMISSION)
public class CreateUserHandlerService implements CommandHandlerService {

    private final UserService service;

    @Autowired
    public CreateUserHandlerService(UserService service) {
        this.service = service;
    }

    @Override
    public CommandHandlerData process(JsonCommand command) {
        return this.service.createUser(command.getJson());
    }
}
