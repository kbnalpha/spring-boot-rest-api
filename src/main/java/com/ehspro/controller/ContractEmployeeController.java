package com.ehspro.controller;
import com.ehspro.dto.*;
import com.ehspro.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/ContractEmployee")
@PreAuthorize("hasAuthority('ManageContractEmployees') or hasRole('SUPER_ADMIN')")
public class ContractEmployeeController {
    private final EmployeeService service;
    @PutMapping("/{id}") public ApiResponse<?> update(@PathVariable Long id,@Valid @RequestBody EmployeeDto dto) { dto.userType=2;return ApiResponse.success(service.update(id,dto)); }
    public ContractEmployeeController(EmployeeService service) { this.service=service; }
    @PostMapping("/Create") public ApiResponse<?> create(@Valid @RequestBody EmployeeDto dto) { dto.userType=2;return ApiResponse.success(service.create(dto)); }
    @PostMapping("/GetList") public ApiResponse<?> list(@Valid @RequestBody ListRequest request) { request.workerType=2;return ApiResponse.success(service.list(request)); }
}
