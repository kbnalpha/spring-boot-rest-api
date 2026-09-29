package com.ehspro.controller;
import com.ehspro.dto.*;
import com.ehspro.service.*;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api")
public class AccountController {
    private final AccountService service;
    private final RoleService roles;
    public AccountController(AccountService service,RoleService roles) { this.service=service;this.roles=roles; }
    @GetMapping("/Auth/Me") public ApiResponse<?> me() { return ApiResponse.success(service.me()); }
    @GetMapping("/Permission/GetAll") public ApiResponse<?> permissions() { return ApiResponse.success(roles.catalog()); }
    @PostMapping("/User/{employeeId}/ActivateSystemUser") @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<?> activate(@PathVariable Long employeeId,@Valid @RequestBody AccountRequests.Activate dto) { return ApiResponse.success(service.activate(employeeId,dto)); }
    @PutMapping("/SystemUser/{id}/Roles") @PreAuthorize("hasAuthority('ManageRoleUsers') or hasRole('SUPER_ADMIN')")
    public ApiResponse<?> roles(@PathVariable Long id,@Valid @RequestBody AccountRequests.Roles dto) { return ApiResponse.success(service.roles(id,dto)); }
    @PutMapping("/SystemUser/{id}/Scope") @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<?> scope(@PathVariable Long id,@Valid @RequestBody AccountRequests.Scopes dto) { return ApiResponse.success(service.scopes(id,dto)); }
    @PutMapping("/SystemUser/{id}/Enabled") @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<?> enabled(@PathVariable Long id,@Valid @RequestBody AccountRequests.Enabled dto) { return ApiResponse.success(service.enabled(id,dto.enabled())); }
}
