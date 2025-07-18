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
package com.base.config.core.annotation;


import com.base.config.core.command.service.CommandHandlerProcessing;
import com.base.config.core.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author YISivlay
 */
@Component
public class CommandTypeProvider implements ApplicationContextAware {

    private final Logger logger = LoggerFactory.getLogger(CommandTypeProvider.class);

    @Autowired
    private Environment environment;
    private ApplicationContext applicationContext;
    private HashMap<String, String> registeredCommandTypes;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        List<String> commandTypes = new ArrayList<>();
        if (registeredCommandTypes == null) {
            this.registeredCommandTypes = new HashMap<>();
            this.applicationContext.getBeansWithAnnotation(CommandType.class)
                    .forEach((_, bean) -> {
                        CommandType commandType = AopUtils.getTargetClass(bean).getAnnotation(CommandType.class);
                        this.registeredCommandTypes.put(commandType.action() + " | " + commandType.entity(), AopUtils.getTargetClass(bean).getName());
                        commandTypes.add(commandType.action() + " | " + commandType.entity());
                    });
        }
        if (isDevProfileActive()) {
            logger.info("Registered @CommandType annotation {}", commandTypes);
        }
    }

    public CommandHandlerProcessing allHandler(String action, String entity) {
        final String permission = action + " | " + entity;
        if (!this.registeredCommandTypes.containsKey(permission)) {
            throw new BadRequestException("msg.bad.request.description", permission);
        }
        try {
            Class<?> clazz = Class.forName(this.registeredCommandTypes.get(permission));
            return (CommandHandlerProcessing) this.applicationContext.getBean(clazz);
        } catch (ClassNotFoundException e) {
            throw new BadRequestException("msg.bad.request.description", this.registeredCommandTypes.get(permission));
        }
    }

    private boolean isDevProfileActive() {
        for (String profile : environment.getActiveProfiles()) {
            if ("dev".equals(profile)) {
                return true;
            }
        }
        return false;
    }
}
