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
package com.base.config.core.command.service;


import com.base.config.core.annotation.CommandTypeProvider;
import com.base.config.core.authentication.user.model.User;
import com.base.config.core.command.data.Command;
import com.base.config.core.command.data.JsonCommand;
import com.base.config.core.command.data.LogData;
import com.base.config.core.command.model.Logs;
import com.base.config.core.command.repository.LogRepository;
import com.base.config.core.serializer.JsonDelegator;
import com.base.config.security.service.SecurityContext;
import com.google.gson.JsonElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author YISivlay
 */
@Service
public class LogServiceImpl implements LogService {

    private final LogRepository logRepository;
    private final CommandTypeProvider commandTypeProvider;
    private final JsonDelegator jsonDelegator;
    private final SecurityContext securityContext;

    @Autowired
    public LogServiceImpl(final LogRepository logRepository,
                          final CommandTypeProvider commandTypeProvider,
                          final JsonDelegator jsonDelegator,
                          final SecurityContext securityContext) {
        this.logRepository = logRepository;
        this.commandTypeProvider = commandTypeProvider;
        this.jsonDelegator = jsonDelegator;
        this.securityContext = securityContext;
    }

    @Override
    public LogData log(Command command) {

        CommandHandlerProcessing handler = getHandler(command.getAction(), command.getEntity());
        JsonElement jsonElement = jsonDelegator.parseString(command.getJson());
        JsonCommand jsonCommand = JsonCommand.builder()
                .id(command.getId())
                .action(command.getAction())
                .entity(command.getEntity())
                .permission(command.getAction() + "_" + command.getEntity())
                .json(command.getJson())
                .href(command.getHref())
                .jsonDelegator(jsonDelegator)
                .jsonElement(jsonElement)
                .build();
        LogData logData = handler.process(jsonCommand);
        User createdBy = this.securityContext.authenticatedUser();
        Logs logs = new Logs(
                logData.getId(),
                command.getAction(),
                command.getEntity(),
                command.getHref(),
                command.getJson(),
                createdBy.getName(),
                new Date()
        );
        this.logRepository.save(logs);

        return logData;
    }

    private CommandHandlerProcessing getHandler(String action, String entity) {
        return this.commandTypeProvider.allHandler(action, entity);
    }
}
