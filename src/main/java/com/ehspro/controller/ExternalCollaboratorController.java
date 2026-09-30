package com.ehspro.controller;
import com.ehspro.dto.*;
import com.ehspro.service.ExternalCollaboratorService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/User")
@PreAuthorize("hasAuthority('ManageExternalCollaborators') or hasAnyRole('SUPER_ADMIN','ADMIN')")
public class ExternalCollaboratorController {
    private final ExternalCollaboratorService service;
    public ExternalCollaboratorController(ExternalCollaboratorService service) {this.service=service;}
    @PutMapping("/ExternalCollaborator/{id}")
    public ApiResponse<?> update(@PathVariable Long id,@Valid @RequestBody ExternalCollaboratorDto dto) {
        if(id<=0) throw com.ehspro.exception.ApiException.badRequest("ID must be positive");
        dto.id=id;return ApiResponse.success(service.save(dto));
    }
    // Preserve the spelling in the supplied contract.
    @PostMapping("/GetAllExternalCollabarator")
    public ApiResponse<?> list(@Valid @RequestBody ListRequest request) {return ApiResponse.success(service.list(request));}
    @PostMapping("/CreateOrUpdateExternalCollabarator")
    public ApiResponse<?> save(@Valid @RequestBody ExternalCollaboratorDto dto) {return ApiResponse.success(service.save(dto));}
}
