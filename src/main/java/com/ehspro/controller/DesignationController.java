package com.ehspro.controller;
import com.ehspro.dto.*;
import com.ehspro.service.DesignationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
@RestController
@org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ManageDesignation') or hasAnyRole('SUPER_ADMIN','ADMIN')")
@RequestMapping("/api/Designation")
public class DesignationController {
    private final DesignationService service;
    @PutMapping("/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @Valid @RequestBody DesignationDto request) {
        return ApiResponse.success(java.util.Map.of("id", service.update(id, request)));
    }
    public DesignationController(DesignationService service) { this.service = service; }
    @PostMapping("/GetList")
    public ApiResponse<?> list(@Valid @RequestBody ListRequest request) {
        return ApiResponse.success(service.list(request));
    }
    @PostMapping("/Create")
    public ApiResponse<?> create(@Valid @RequestBody DesignationDto request) {
        var id = service.create(request);
        return ApiResponse.success(java.util.Map.of("id", id));
    }
}
