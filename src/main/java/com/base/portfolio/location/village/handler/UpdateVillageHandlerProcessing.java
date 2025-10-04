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
package com.base.portfolio.location.village.handler;

import com.base.core.annotation.CommandType;
import com.base.core.command.data.JsonCommand;
import com.base.core.command.service.CommandHandlerProcessing;
import com.base.portfolio.location.village.controller.VillageConstants;
import com.base.portfolio.location.village.service.VillageService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author YISivlay
 */
@Service
@Transactional
@CommandType(action = "UPDATE", entity = VillageConstants.PERMISSION)
public class UpdateVillageHandlerProcessing implements CommandHandlerProcessing {

    private final VillageService service;

    @Autowired
    public UpdateVillageHandlerProcessing(VillageService service) {
        this.service = service;
    }

    @Override
    public Map<String, Object> process(JsonCommand command) {
        return this.service.updateVillage(command.getId(),
                                          command);
    }

}
