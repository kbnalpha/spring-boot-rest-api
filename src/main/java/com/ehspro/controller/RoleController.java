package com.ehspro.controller;
import com.ehspro.dto.*;
import com.ehspro.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/Role")
public class RoleController {
    private final RoleService service;
    public RoleController(RoleService service) { this.service = service; }
    @GetMapping("/GetAllRoles")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ViewRoles') or hasAuthority('CreateRole') or hasRole('SUPER_ADMIN')")
    public ApiResponse<?> list() {
        return ApiResponse.success(service.list());
    }
    @PostMapping("/CreateRole")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('CreateRole') or hasRole('SUPER_ADMIN')")
    public ApiResponse<?> create(@Valid @RequestBody RoleDto request) {
        var id = service.create(request);
        return ApiResponse.success(java.util.Map.of("id", id, "message", "Role created successfully."));
    }
    @PutMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<?> update(@PathVariable Long id, @Valid @RequestBody RoleDto request) {
        return ApiResponse.success(java.util.Map.of("id", service.update(id, request)));
    }
}
