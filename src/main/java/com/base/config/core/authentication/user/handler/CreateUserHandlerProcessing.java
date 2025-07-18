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
package com.base.config.core.authentication.user.handler;


import com.base.config.core.annotation.CommandType;
import com.base.config.core.authentication.user.controller.UserConstants;
import com.base.config.core.authentication.user.service.UserService;
import com.base.config.core.command.data.LogData;
import com.base.config.core.command.data.JsonCommand;
import com.base.config.core.command.service.CommandHandlerProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author YISivlay
 */
@Service
@Transactional
@CommandType(action = "UPDATE", entity = UserConstants.PERMISSION)
public class CreateUserHandlerProcessing implements CommandHandlerProcessing {

    private final UserService service;

    @Autowired
    public CreateUserHandlerProcessing(UserService service) {
        this.service = service;
    }

    @Override
    public LogData process(JsonCommand command) {
        return this.service.updateUser(command.getId(), command);
    }
}
