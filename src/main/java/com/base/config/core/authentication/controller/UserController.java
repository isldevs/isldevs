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
package com.base.config.core.authentication.controller;

import com.base.config.core.authentication.data.UserCreateDTO;
import com.base.config.core.authentication.data.UserDTO;
import com.base.config.core.authentication.handler.UserCommandBuilder;
import com.base.config.core.authentication.service.UserService;
import com.base.config.core.command.data.Command;
import com.base.config.core.serializer.service.JsonSerializerImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author YISivlay
 */
@RestController
@RequestMapping(UserConstants.API_PATH)
public class UserController {

    private final JsonSerializerImpl<UserDTO> serializer;
    private final UserService userService;

    @Autowired
    public UserController(final JsonSerializerImpl<UserDTO> serializer,
                          final UserService userService) {
        this.serializer = serializer;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody String json) {

        final Command command = new UserCommandBuilder()
                .create()
                .json(json)
                .build();

        return this.serializer.serialize(json);
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
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id, @Valid @RequestBody UserCreateDTO dto) {
        return ResponseEntity.ok(userService.updateUser(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Accepted");
    }

}
