package com.ehspro.controller;
import com.ehspro.dto.*;
import com.ehspro.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
@RestController
@org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ManageDepartment') or hasRole('SUPER_ADMIN')")
@RequestMapping("/api/Department")
public class DepartmentController {
    private final DepartmentService service;
    @PutMapping("/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @Valid @RequestBody DepartmentDto request) {
        return ApiResponse.success(java.util.Map.of("id", service.update(id, request)));
    }
    public DepartmentController(DepartmentService service) { this.service = service; }
    @PostMapping("/GetList")
    public ApiResponse<?> list(@Valid @RequestBody ListRequest request) {
        return ApiResponse.success(service.list(request));
    }
    @PostMapping("/Create")
    public ApiResponse<?> create(@Valid @RequestBody DepartmentDto request) {
        var id = service.create(request);
        return ApiResponse.success(java.util.Map.of("id", id));
    }
}
