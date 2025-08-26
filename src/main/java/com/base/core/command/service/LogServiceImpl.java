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
package com.base.core.command.service;


import com.base.core.annotation.CommandTypeProvider;
import com.base.core.authentication.user.model.User;
import com.base.core.command.data.Command;
import com.base.core.command.data.JsonCommand;
import com.base.core.command.data.LogData;
import com.base.core.command.model.Logs;
import com.base.core.command.repository.LogRepository;
import com.base.core.serializer.JsonDelegator;
import com.base.config.security.service.SecurityContext;
import com.google.gson.JsonElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;

/**
 * @author YISivlay
 */
@Service
@Transactional
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
    public Map<String, Object> log(Command command) {
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
        Map<String, Object> logData = handler.process(jsonCommand);
        User createdBy = this.securityContext.authenticatedUser();
        Logs logs = new Logs(
                (Long) logData.get("id"),
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
