package com.ehspro.controller;
import com.ehspro.dto.*;
import com.ehspro.service.ObservationTypeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
@RestController
@org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ObservationTypes') or hasRole('SUPER_ADMIN')")
@RequestMapping("/api/ObservationType")
public class ObservationTypeController {
    private final ObservationTypeService service;
    @PutMapping("/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @Valid @RequestBody ObservationTypeDto request) { return ApiResponse.success(service.update(id, request)); }
    public ObservationTypeController(ObservationTypeService service) { this.service = service; }
    @PostMapping("/GetAll")
    public ApiResponse<?> list(@Valid @RequestBody ListRequest request) {
        return ApiResponse.success(service.list(request));
    }
    @PostMapping("/Create")
    public ApiResponse<?> create(@Valid @RequestBody ObservationTypeDto request) {
        var id = service.create(request);
        return ApiResponse.success(id);
    }
}
