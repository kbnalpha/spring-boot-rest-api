package com.ehspro.controller;
import com.ehspro.dto.*;
import com.ehspro.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/User")
public class UserController {
    private final EmployeeService service;
    @PutMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ManageEmployees') or hasRole('SUPER_ADMIN')")
    public ApiResponse<?> update(@PathVariable Long id, @Valid @RequestBody EmployeeDto request) {
        return ApiResponse.success(service.update(id, request));
    }
    public UserController(EmployeeService service) { this.service = service; }
    @PostMapping("/GetAllEmployees")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ManageEmployees') or hasRole('SUPER_ADMIN')")
    public ApiResponse<?> list(@Valid @RequestBody ListRequest request) {
        return ApiResponse.success(service.list(request));
    }
    @PostMapping("/CreateEmployee")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ManageEmployees') or hasRole('SUPER_ADMIN')")
    public ApiResponse<?> create(@Valid @RequestBody EmployeeDto request) {
        var id = service.create(request);
        return ApiResponse.success(id);
    }
    @PostMapping("/GetUsers")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ViewSystemUsers') or hasAuthority('ManageEmployees') or hasRole('SUPER_ADMIN')")
    public ApiResponse<?> users(@Valid @RequestBody ListRequest request) {
        return ApiResponse.success(service.listUsers(request));
    }
}
