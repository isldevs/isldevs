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
package com.base.core.authentication.role.controller;

import com.base.core.authentication.role.dto.RoleDTO;
import com.base.core.authentication.role.handler.RoleCommandHandler;
import com.base.core.authentication.role.service.RoleService;
import com.base.core.command.service.LogService;
import com.base.core.pageable.PageableHateoasAssembler;
import com.base.core.serializer.JsonSerializerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author YISivlay
 */
@RestController
@RequestMapping(RoleConstants.API_PATH)
public class RoleController {

  private final PageableHateoasAssembler pageable;
  private final JsonSerializerImpl<RoleDTO> serializer;
  private final RoleService roleService;
  private final LogService logService;

  @Autowired
  public RoleController(
      final PageableHateoasAssembler pageable,
      final JsonSerializerImpl<RoleDTO> serializer,
      final RoleService roleService,
      final LogService logService) {
    this.pageable = pageable;
    this.serializer = serializer;
    this.roleService = roleService;
    this.logService = logService;
  }

  @PostMapping
  public String createRole(@RequestBody String json) {

    final var command = new RoleCommandHandler().create().json(json).build();

    final var data = this.logService.log(command);
    return this.serializer.serialize(data);
  }

  @PutMapping("/{id}")
  public String updateRole(@PathVariable Long id, @RequestBody String json) {

    final var command = new RoleCommandHandler().update(id).json(json).build();

    final var data = this.logService.log(command);
    return this.serializer.serialize(data);
  }

  @DeleteMapping("/{id}")
  public String deleteRole(@PathVariable Long id) {
    final var command = new RoleCommandHandler().delete(id).build();

    final var data = this.logService.log(command);
    return this.serializer.serialize(data);
  }

  @GetMapping("/{id}")
  public ResponseEntity<RoleDTO> getRole(@PathVariable Long id) {
    return ResponseEntity.ok(this.roleService.getRoleById(id));
  }

  @GetMapping
  public ResponseEntity<?> listRoles(
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size,
      @RequestParam(required = false) String search) {
    var roles = this.roleService.listRoles(page, size, search);
    var response =
        (page == null || size == null) ? pageable.unpaged(roles) : pageable.toModel(roles);
    return ResponseEntity.ok(response);
  }
}
