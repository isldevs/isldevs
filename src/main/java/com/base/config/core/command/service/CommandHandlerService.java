package com.base.config.core.command.service;


import com.base.config.core.command.data.CommandHandlerData;
import com.base.config.core.command.data.JsonCommand;

/**
 * @author YISivlay
 */
public interface CommandHandlerService {

    CommandHandlerData process(JsonCommand command);

}
