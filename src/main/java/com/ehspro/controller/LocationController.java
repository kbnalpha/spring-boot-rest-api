package com.ehspro.controller;
import com.ehspro.dto.*;
import com.ehspro.service.LocationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
@RestController
@org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ManageLocations') or hasRole('SUPER_ADMIN')")
@RequestMapping("/api/Location")
public class LocationController {
    private final LocationService service;
    @PutMapping("/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @Valid @RequestBody LocationDto request) { return ApiResponse.success(service.update(id, request)); }
    public LocationController(LocationService service) { this.service = service; }
    @PostMapping("/GetAllLocations")
    public ApiResponse<?> list(@Valid @RequestBody ListRequest request) {
        return ApiResponse.success(service.list(request));
    }
    @PostMapping("/add")
    public ApiResponse<?> create(@Valid @RequestBody LocationDto request) {
        var id = service.create(request);
        return ApiResponse.success(id);
    }
}
