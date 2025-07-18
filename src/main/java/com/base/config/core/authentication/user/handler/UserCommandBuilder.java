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


import com.base.config.core.authentication.user.controller.UserConstants;
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
