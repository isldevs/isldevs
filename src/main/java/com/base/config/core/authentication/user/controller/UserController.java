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
package com.base.config.core.authentication.user.controller;

import com.base.config.core.authentication.user.data.UserDTO;
import com.base.config.core.authentication.user.handler.UserCommandBuilder;
import com.base.config.core.authentication.user.service.UserService;
import com.base.config.core.command.service.LogService;
import com.base.config.core.serializer.JsonSerializerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author YISivlay
 */
@RestController
@RequestMapping(UserConstants.API_PATH)
public class UserController {

    private final JsonSerializerImpl<UserDTO> serializer;
    private final UserService userService;
    private final LogService logService;

    @Autowired
    public UserController(final JsonSerializerImpl<UserDTO> serializer,
                          final UserService userService,
                          final LogService logService) {
        this.serializer = serializer;
        this.userService = userService;
        this.logService = logService;
    }

    @PostMapping
    public String createUser(@RequestBody String json) {

        final var command = new UserCommandBuilder()
                .create()
                .json(json)
                .build();

        final var data = this.logService.log(command);
        return this.serializer.serialize(data);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping
    public ResponseEntity<?> listUsers(@RequestParam(required = false) Integer page,
                                       @RequestParam(required = false) Integer size,
                                       @RequestParam(required = false) String search,
                                       PagedResourcesAssembler<UserDTO> pagination) {
        if (page == null || size == null) {
            var allUsers = userService.listUsers(null, null, search).getContent();
            return ResponseEntity.ok(allUsers);
        }
        var users = userService.listUsers(page, size, search);
        return ResponseEntity.ok(pagination.toModel(users));
    }

    @PutMapping("/{id}")
    public String updateUser(@PathVariable Long id, @RequestBody String json) {
        final var command = new UserCommandBuilder()
                .update(id)
                .json(json)
                .build();

        final var data = this.logService.log(command);
        return this.serializer.serialize(data);
    }

    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {
        final var command = new UserCommandBuilder()
                .delete(id)
                .build();

        final var data = this.logService.log(command);
        return this.serializer.serialize(data);
    }

}
