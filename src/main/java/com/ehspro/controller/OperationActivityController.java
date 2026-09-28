package com.ehspro.controller;
import com.ehspro.dto.*;
import com.ehspro.service.OperationActivityService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
@RestController
@org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ManageOperationalActivities') or hasRole('SUPER_ADMIN')")
@RequestMapping("/api/OperationActivity")
public class OperationActivityController {
    private final OperationActivityService service;
    @PutMapping("/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @Valid @RequestBody OperationActivityDto request) {
        return ApiResponse.success(java.util.Map.of("id", service.update(id, request)));
    }
    public OperationActivityController(OperationActivityService service) { this.service = service; }
    @PostMapping("/list")
    public ApiResponse<?> list(@Valid @RequestBody ListRequest request) {
        return ApiResponse.success(service.list(request));
    }
    @PostMapping
    public ApiResponse<?> create(@Valid @RequestBody OperationActivityDto request) {
        var id = service.create(request);
        return ApiResponse.success(java.util.Map.of("id", id));
    }
}
