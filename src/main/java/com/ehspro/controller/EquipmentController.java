package com.ehspro.controller;
import com.ehspro.dto.*;
import com.ehspro.service.EquipmentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
@RestController
@org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ManageEquipment') or hasAnyRole('SUPER_ADMIN','ADMIN')")
@RequestMapping("/api/Equipment")
public class EquipmentController {
    private final EquipmentService service;
    @PutMapping("/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @Valid @RequestBody EquipmentDto request) {
        return ApiResponse.success(java.util.Map.of("id", service.update(id, request)));
    }
    public EquipmentController(EquipmentService service) { this.service = service; }
    @PostMapping("/List")
    public ApiResponse<?> list(@Valid @RequestBody ListRequest request) {
        return ApiResponse.success(service.list(request));
    }
    @PostMapping("/Add")
    public ApiResponse<?> create(@Valid @RequestBody EquipmentDto request) {
        var id = service.create(request);
        return ApiResponse.success("Equipment added successfully.");
    }
}
