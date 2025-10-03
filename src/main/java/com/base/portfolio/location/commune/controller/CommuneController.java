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
package com.base.portfolio.location.commune.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.base.core.command.service.LogService;
import com.base.core.pageable.PageableHateoasAssembler;
import com.base.core.serializer.JsonSerializerImpl;
import com.base.portfolio.location.commune.dto.CommuneDTO;
import com.base.portfolio.location.commune.handler.CommuneCommandHandler;
import com.base.portfolio.location.commune.service.CommuneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author YISivlay
 */
@RestController
@RequestMapping(CommuneConstants.API_PATH)
public class CommuneController {

	private final PageableHateoasAssembler pageable;

	private final JsonSerializerImpl<CommuneDTO> serializer;

	private final CommuneService service;

	private final LogService logService;

	@Autowired
	public CommuneController(final PageableHateoasAssembler pageable, final JsonSerializerImpl<CommuneDTO> serializer,
			final CommuneService service, final LogService logService) {
		this.pageable = pageable;
		this.serializer = serializer;
		this.service = service;
		this.logService = logService;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public String createCommune(@RequestBody String json) {
		final var command = new CommuneCommandHandler().create().json(json).build();
		final var data = this.logService.log(command);
		return this.serializer.serialize(data);
	}

	@PutMapping(value = "{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public String updateCommune(@RequestBody String json, @PathVariable Long id) {
		final var command = new CommuneCommandHandler().update(id).json(json).build();
		final var data = this.logService.log(command);
		return this.serializer.serialize(data);
	}

	@DeleteMapping(value = "{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public String deleteCommune(@PathVariable Long id) {
		final var command = new CommuneCommandHandler().delete(id).build();
		final var data = this.logService.log(command);
		return this.serializer.serialize(data);
	}

	@GetMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public String getCommune(@PathVariable Long id) {
		var data = this.service.getCommuneById(id);
		return this.serializer.serialize(data);
	}

	@GetMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> listCommunes(@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size, @RequestParam(required = false) String search) {
		var communes = this.service.listCommunes(page, size, search);
		var response = (page == null || size == null) ? pageable.unpaged(communes) : pageable.toModel(communes);
		return ResponseEntity.ok(response);
	}

}
