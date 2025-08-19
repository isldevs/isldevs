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
package com.base.core.authentication.user.service;


import com.base.core.authentication.user.data.UserDTO;
import com.base.core.command.data.JsonCommand;
import com.base.core.command.data.LogData;
import org.springframework.data.domain.Page;

/**
 * @author YISivlay
 */
public interface UserService {

    LogData createUser(JsonCommand command);

    UserDTO getUserById(Long id);

    Page<UserDTO> listUsers(Integer page, Integer size, String search);

    LogData updateUser(Long id, JsonCommand command);

    LogData deleteUser(Long id);

}
