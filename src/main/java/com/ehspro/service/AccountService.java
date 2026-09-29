package com.ehspro.service;
import com.ehspro.dto.AccountRequests;
import com.ehspro.entity.*;
import com.ehspro.exception.ApiException;
import com.ehspro.repository.*;
import com.ehspro.security.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service @Transactional(readOnly=true)
public class AccountService {
    private final UserAccountRepository accounts;
    private final EmployeeRepository employees;
    private final RoleRepository roles;
    private final OrganizationUnitRepository organizations;
    private final PasswordEncoder encoder;
    private final AccountDetailsService identities;
    private final AccessService access;
    public AccountService(UserAccountRepository accounts,EmployeeRepository employees,RoleRepository roles,OrganizationUnitRepository organizations,
            PasswordEncoder encoder,AccountDetailsService identities,AccessService access) {
        this.accounts=accounts;this.employees=employees;this.roles=roles;this.organizations=organizations;this.encoder=encoder;this.identities=identities;this.access=access;
    }
    @Transactional public Map<String,Object> activate(Long employeeId,AccountRequests.Activate dto) {
        access.superAdmin();
        Employee employee=employees.findById(employeeId).orElseThrow(() -> ApiException.notFound("Employee not found"));
        if(accounts.findByEmployeeId(employeeId).isPresent()) throw ApiException.badRequest("Employee already has a system account; use account management");
        if(identities.reservedUsername(dto.username())) throw ApiException.badRequest("Reserved system username");
        if(dto.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72) throw ApiException.badRequest("Password exceeds BCrypt's 72-byte limit");
        OrganizationUnit unit=organizations.findById(employee.organizationUnitId).orElseThrow(() -> ApiException.badRequest("Employee organization is missing"));
        UserAccount account=new UserAccount();account.employeeId=employeeId;account.tenantId=unit.tenantId;
        if(account.tenantId==null) throw ApiException.badRequest("Set the organization tenant before activating a user");
        account.username=dto.username();account.passwordHash=encoder.encode(dto.password());account.enabled=true;
        assignRoles(account,dto.basicRoleId(),dto.additionalRoleIds());assignScopes(account,dto.scopes());
        if(!Integer.valueOf(1).equals(employee.status)) throw ApiException.badRequest("Only active employees can be activated as system users");
        employee.hasAccess=true;employee.userRoleIds=new ArrayList<>();employee.userRoleIds.add(account.basicRoleId);employee.userRoleIds.addAll(account.additionalRoleIds);
        employees.save(employee);accounts.saveAndFlush(account);return response(account);
    }
    @Transactional public Map<String,Object> roles(Long id,AccountRequests.Roles dto) {
        access.require("ManageRoleUsers");UserAccount account=account(id);
        access.tenant(account.tenantId);
        access.organization(employees.findById(account.employeeId).orElseThrow().organizationUnitId);
        assignRoles(account,dto.basicRoleId(),dto.additionalRoleIds());
        Employee employee=employees.findById(account.employeeId).orElseThrow();employee.userRoleIds=new ArrayList<>();employee.userRoleIds.add(account.basicRoleId);employee.userRoleIds.addAll(account.additionalRoleIds);
        return response(account);
    }
    @Transactional public Map<String,Object> scopes(Long id,AccountRequests.Scopes dto) {
        access.superAdmin();UserAccount account=account(id);assignScopes(account,dto.scopes());return response(account);
    }
    @Transactional public Map<String,Object> enabled(Long id,boolean enabled) {
        access.superAdmin();UserAccount account=account(id);account.enabled=enabled;
        employees.findById(account.employeeId).orElseThrow().hasAccess=enabled;return response(account);
    }
    public Map<String,Object> me() {
        Map<String,Object> result=new LinkedHashMap<>();result.put("superAdmin",access.isSuperAdmin());
        var authentication=org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        result.put("username",authentication.getName());result.put("tenantId",access.isSuperAdmin()?null:access.principal().tenantId);
        result.put("organizationUnitIds",access.organizationIds());
        result.put("permissions",authentication.getAuthorities().stream().map(a -> a.getAuthority()).toList());
        if(!access.isSuperAdmin()) result.put("account",response(account(access.principal().accountId)));
        return result;
    }
    public Map<String,Object> response(UserAccount account) {
        Map<String,Object> result=new LinkedHashMap<>();result.put("id",account.id);result.put("employeeId",account.employeeId);
        result.put("username",account.username);result.put("tenantId",account.tenantId);result.put("enabled",account.enabled);
        result.put("basicRoleId",account.basicRoleId);result.put("additionalRoleIds",new HashSet<>(account.additionalRoleIds));
        result.put("scopes",new HashSet<>(account.scopes));return result;
    }
    private UserAccount account(Long id) { return accounts.findById(id).orElseThrow(() -> ApiException.notFound("System account not found")); }
    private void assignRoles(UserAccount account,Long basic,Set<Long> additional) {
        Set<Long> all=new HashSet<>(additional==null?Set.of():additional);all.add(basic);
        for(Long id:all) {
            Role role=roles.findById(id).orElseThrow(() -> ApiException.badRequest("Unknown role: "+id));
            if(role.systemRole||!Objects.equals(role.tenantId,account.tenantId)||!Integer.valueOf(1).equals(role.status)) throw ApiException.badRequest("Role must be active and belong to the user's instance");
        }
        account.basicRoleId=basic;account.additionalRoleIds.clear();all.remove(basic);account.additionalRoleIds.addAll(all);
    }
    private void assignScopes(UserAccount account,List<AccountRequests.Scope> scopes) {
        Set<OrganizationScope> normalized=new HashSet<>();
        for(var scope:scopes) {
            OrganizationUnit unit=organizations.findById(scope.organizationUnitId()).orElseThrow(() -> ApiException.badRequest("Unknown organization scope"));
            if(!Objects.equals(unit.tenantId,account.tenantId)) throw ApiException.badRequest("Organization scope belongs to another instance");
            if(!normalized.add(new OrganizationScope(unit.id,scope.includeDescendants()))) throw ApiException.badRequest("Duplicate organization scope");
        }
        account.scopes.clear();account.scopes.addAll(normalized);
    }
}
