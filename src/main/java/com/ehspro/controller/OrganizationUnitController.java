package com.ehspro.controller;
import com.ehspro.dto.*;
import com.ehspro.service.OrganizationUnitService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/OrganizationUnit")
public class OrganizationUnitController {
    private final OrganizationUnitService service;
    @PutMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ApiResponse<?> update(@PathVariable Long id, @Valid @RequestBody OrganizationUnitDto request) {
        return ApiResponse.success(java.util.Map.of("id", service.update(id, request)));
    }
    public OrganizationUnitController(OrganizationUnitService service) { this.service = service; }
    @GetMapping("/GetAllOrganizations")
    public ApiResponse<?> list() {
        return ApiResponse.success(service.list());
    }
    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ApiResponse<?> create(@Valid @RequestBody OrganizationUnitDto request) {
        var id = service.create(request);
        return ApiResponse.success("Organization Unit created successfully.");
    }
}
