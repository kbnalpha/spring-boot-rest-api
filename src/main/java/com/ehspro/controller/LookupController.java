package com.ehspro.controller;
import com.ehspro.dto.*;
import com.ehspro.service.ReferenceDataService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/Lookup")
public class LookupController {
    private final ReferenceDataService service;
    public LookupController(ReferenceDataService service) { this.service=service; }
    @GetMapping("/{kind}")
    public ApiResponse<?> list(@PathVariable String kind,@RequestParam(required=false) Long countryId,@RequestParam(required=false) Long stateId) {
        return ApiResponse.success(service.list(kind.toUpperCase(),countryId,stateId));
    }
    @PutMapping("/{kind}/{id}") @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<?> save(@PathVariable String kind,@PathVariable Long id,@Valid @RequestBody ReferenceItemDto dto) {
        return ApiResponse.success(service.save(kind.toUpperCase(),id,dto));
    }
}
