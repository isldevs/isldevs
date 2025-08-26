package com.base.entity.office.controller;


import com.base.core.command.service.LogService;
import com.base.core.serializer.JsonSerializerImpl;
import com.base.entity.office.dto.OfficeDTO;
import com.base.entity.office.handler.OfficeCommandHandler;
import com.base.entity.office.service.OfficeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author YISivlay
 */
@RestController
@RequestMapping(OfficeConstants.API_PATH)
@Scope("singleton")
public class OfficeController {

    private final JsonSerializerImpl<OfficeDTO> serializer;
    private final OfficeService service;
    private final LogService logService;

    @Autowired
    public OfficeController(final JsonSerializerImpl<OfficeDTO> serializer,
                            final OfficeService service,
                            final LogService logService) {
        this.serializer = serializer;
        this.service = service;
        this.logService = logService;
    }

    @PostMapping
    public String createOffice(@RequestBody String json) {

        final var command = new OfficeCommandHandler()
                .create()
                .json(json)
                .build();

        final var data = this.logService.log(command);
        return this.serializer.serialize(data);
    }

    @PutMapping("/{id}")
    public String updateOffice(@PathVariable Long id, @RequestBody String json) {

        final var command = new OfficeCommandHandler()
                .update(id)
                .json(json)
                .build();

        final var data = this.logService.log(command);
        return this.serializer.serialize(data);
    }

    @DeleteMapping("/{id}")
    public String deleteOffice(@PathVariable Long id) {
        final var command = new OfficeCommandHandler()
                .delete(id)
                .build();

        final var data = this.logService.log(command);
        return this.serializer.serialize(data);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfficeDTO> getOffice(@PathVariable Long id) {
        return ResponseEntity.ok(this.service.getOfficeById(id));
    }

    @GetMapping
    public ResponseEntity<?> listOffices(@RequestParam(required = false) Integer page,
                                       @RequestParam(required = false) Integer size,
                                       @RequestParam(required = false) String search,
                                       PagedResourcesAssembler<OfficeDTO> pagination) {
        if (page == null || size == null) {
            var offices = this.service.listOffices(null, null, search);
            return ResponseEntity.ok(offices);
        }
        var offices = this.service.listOffices(page, size, search);
        return ResponseEntity.ok(pagination.toModel(offices));
    }
}
