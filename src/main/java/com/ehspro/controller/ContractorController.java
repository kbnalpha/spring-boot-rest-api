package com.ehspro.controller;
import com.ehspro.dto.*;
import com.ehspro.service.ContractorService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/Contractor")
@PreAuthorize("hasAuthority('ManageContractors') or hasAnyRole('SUPER_ADMIN','ADMIN')")
public class ContractorController {
    private final ContractorService service;
    public ContractorController(ContractorService service) { this.service=service; }
    @PostMapping("/Create") public ApiResponse<?> create(@Valid @RequestBody ContractorDto dto) { return ApiResponse.success(java.util.Map.of("id",service.create(dto))); }
    @PostMapping({"/GetList","/GetAllContractors"}) public ApiResponse<?> list(@Valid @RequestBody ListRequest request) { return ApiResponse.success(service.list(request)); }
    @PostMapping("/Add") public ApiResponse<?> add(@Valid @RequestBody ContractorDto dto) { service.create(dto);return ApiResponse.success("Contractor added successfully."); }
    @PutMapping("/{id}") public ApiResponse<?> update(@PathVariable Long id,@Valid @RequestBody ContractorDto dto) { return ApiResponse.success(java.util.Map.of("id",service.update(id,dto))); }
}
