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
    private final ActivationEmailService mail;
    private final int temporaryPasswordHours;
    private final java.security.SecureRandom random=new java.security.SecureRandom();
    public AccountService(UserAccountRepository accounts,EmployeeRepository employees,RoleRepository roles,OrganizationUnitRepository organizations,
            PasswordEncoder encoder,AccountDetailsService identities,AccessService access,ActivationEmailService mail,
            @org.springframework.beans.factory.annotation.Value("${ehs.onboarding.temporary-password-hours:24}") int temporaryPasswordHours) {
        this.accounts=accounts;this.employees=employees;this.roles=roles;this.organizations=organizations;this.encoder=encoder;this.identities=identities;this.access=access;
        this.mail=mail;this.temporaryPasswordHours=temporaryPasswordHours;
        if(temporaryPasswordHours<1||temporaryPasswordHours>168) throw new IllegalArgumentException("Temporary password expiry must be between 1 and 168 hours");
    }
    @Transactional public Map<String,Object> activate(Long employeeId,AccountRequests.Activate dto) {
        access.administrator();
        Employee employee=employees.findById(employeeId).orElseThrow(() -> ApiException.notFound("Employee not found"));
        access.organization(employee.organizationUnitId);
        if(accounts.findByEmployeeId(employeeId).isPresent()) throw ApiException.badRequest("Employee already has a system account; use account management");
        OrganizationUnit unit=organizations.findById(employee.organizationUnitId).orElseThrow(() -> ApiException.badRequest("Employee organization is missing"));
        UserAccount account=new UserAccount();account.employeeId=employeeId;account.tenantId=unit.tenantId;
        access.tenant(account.tenantId);
        if(account.tenantId==null) throw ApiException.badRequest("Set the organization tenant before activating a user");
        account.username=emailUsername(employee,null);account.enabled=true;
        assignRoles(account,dto.basicRoleId(),dto.additionalRoleIds());assignScopes(account,dto.scopes());
        if(!Integer.valueOf(1).equals(employee.status)) throw ApiException.badRequest("Only active employees can be activated as system users");
        employee.hasAccess=true;employee.userRoleIds=new ArrayList<>();employee.userRoleIds.add(account.basicRoleId);employee.userRoleIds.addAll(account.additionalRoleIds);
        String temporaryPassword=temporaryPassword(account);
        employees.save(employee);accounts.saveAndFlush(account);
        mail.send(account.username,temporaryPassword,account.temporaryPasswordExpiresAt);
        return response(account);
    }
    @Transactional public Map<String,Object> resendActivation(Long id) {
        access.administrator();
        UserAccount account=accounts.findForUpdate(id).orElseThrow(() -> ApiException.notFound("System account not found"));manageable(account);
        Employee employee=employees.findById(account.employeeId).orElseThrow();
        if(!account.enabled||!Boolean.TRUE.equals(employee.hasAccess)||!Integer.valueOf(1).equals(employee.status)) throw ApiException.badRequest("Enable the active employee's account before resending activation");
        account.username=emailUsername(employee,account.id);
        String temporaryPassword=temporaryPassword(account);accounts.saveAndFlush(account);
        mail.send(account.username,temporaryPassword,account.temporaryPasswordExpiresAt);
        return response(account);
    }
    private String emailUsername(Employee employee,Long accountId) {
        String email=employee.emailAddress==null?"":employee.emailAddress.trim().toLowerCase(Locale.ROOT);
        if(email.isBlank()) throw ApiException.badRequest("An employee email is required for account activation");
        if(email.length()>254) throw ApiException.badRequest("Employee email exceeds the 254-character system login limit");
        if(identities.reservedUsername(email)) throw ApiException.badRequest("Reserved system username");
        accounts.findByUsernameIgnoreCase(email).ifPresent(existing -> {
            if(!Objects.equals(existing.id,accountId)) throw new ApiException(org.springframework.http.HttpStatus.CONFLICT,"An account already uses this email address");
        });
        return email;
    }
    private String temporaryPassword(UserAccount account) {
        byte[] bytes=new byte[24];random.nextBytes(bytes);
        String value=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        account.passwordHash=encoder.encode(value);account.mustChangePassword=true;
        account.temporaryPasswordExpiresAt=java.time.LocalDateTime.now(java.time.Clock.systemUTC()).plusHours(temporaryPasswordHours);
        return value;
    }
    @Transactional public Map<String,Object> firstLoginReset(AccountRequests.FirstLoginReset dto) {
        if(access.isSuperAdmin()) throw ApiException.badRequest("Configured Super Admin does not use employee onboarding");
        UserAccount account=accounts.findForUpdate(access.principal().accountId).orElseThrow(() -> ApiException.notFound("System account not found"));
        var now=java.time.LocalDateTime.now(java.time.Clock.systemUTC());
        if(!account.mustChangePassword) throw ApiException.badRequest("First-login password setup is already complete");
        if(account.temporaryPasswordExpiresAt==null||!now.isBefore(account.temporaryPasswordExpiresAt)||!encoder.matches(dto.currentPassword(),account.passwordHash))
            throw new ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED,"Temporary password is invalid or expired");
        var employee=employees.findById(account.employeeId).orElseThrow();
        if(!account.enabled||!Boolean.TRUE.equals(employee.hasAccess)||!Integer.valueOf(1).equals(employee.status)) throw new ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED,"Account is disabled");
        if(!dto.newPassword().equals(dto.confirmPassword())) throw ApiException.badRequest("New password and confirmation must match");
        if(dto.newPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72) throw ApiException.badRequest("Password exceeds BCrypt's 72-byte limit");
        if(encoder.matches(dto.newPassword(),account.passwordHash)) throw ApiException.badRequest("Choose a different password from the temporary password");
        account.passwordHash=encoder.encode(dto.newPassword());account.mustChangePassword=false;
        account.temporaryPasswordExpiresAt=null;account.passwordChangedAt=now;
        return Map.of("email",account.username,"mustChangePassword",false,"nextAction","LOGIN","message","Password set. Log in with your new password.");
    }
    @Transactional public Map<String,Object> roles(Long id,AccountRequests.Roles dto) {
        access.require("ManageRoleUsers");UserAccount account=account(id);
        manageable(account);
        assignRoles(account,dto.basicRoleId(),dto.additionalRoleIds());
        Employee employee=employees.findById(account.employeeId).orElseThrow();employee.userRoleIds=new ArrayList<>();employee.userRoleIds.add(account.basicRoleId);employee.userRoleIds.addAll(account.additionalRoleIds);
        return response(account);
    }
    @Transactional public Map<String,Object> scopes(Long id,AccountRequests.Scopes dto) {
        access.administrator();UserAccount account=account(id);manageable(account);assignScopes(account,dto.scopes());return response(account);
    }
    @Transactional public Map<String,Object> enabled(Long id,boolean enabled) {
        access.administrator();UserAccount account=account(id);manageable(account);account.enabled=enabled;
        employees.findById(account.employeeId).orElseThrow().hasAccess=enabled;return response(account);
    }
    public Map<String,Object> me() {
        Map<String,Object> result=new LinkedHashMap<>();result.put("superAdmin",access.isSuperAdmin());
        result.put("admin",access.isAdmin());
        result.put("accountType",access.isSuperAdmin()?"SUPER_ADMIN":access.isAdmin()?"ADMIN":"USER");
        var authentication=org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        result.put("email",authentication.getName());result.put("tenantId",access.isSuperAdmin()?null:access.principal().tenantId);
        result.put("organizationUnitIds",access.organizationIds());
        result.put("permissions",authentication.getAuthorities().stream().map(a -> a.getAuthority()).toList());
        if(!access.isSuperAdmin()) result.put("account",response(account(access.principal().accountId)));
        return result;
    }
    public Map<String,Object> response(UserAccount account) {
        Map<String,Object> result=new LinkedHashMap<>();result.put("id",account.id);result.put("employeeId",account.employeeId);
        result.put("username",account.username);result.put("tenantId",account.tenantId);result.put("enabled",account.enabled);
        result.put("mustChangePassword",account.mustChangePassword);result.put("temporaryPasswordExpiresAt",account.temporaryPasswordExpiresAt);
        result.put("basicRoleId",account.basicRoleId);result.put("additionalRoleIds",new HashSet<>(account.additionalRoleIds));
        result.put("scopes",new HashSet<>(account.scopes));return result;
    }
    private UserAccount account(Long id) { return accounts.findById(id).orElseThrow(() -> ApiException.notFound("System account not found")); }
    private void manageable(UserAccount account) {
        access.tenant(account.tenantId);
        access.organization(employees.findById(account.employeeId).orElseThrow().organizationUnitId);
        if(access.isSuperAdmin()) return;
        Set<Long> ids=new HashSet<>(account.additionalRoleIds);ids.add(account.basicRoleId);
        boolean targetAdmin=roles.findAllById(ids).stream().anyMatch(r -> r.builtInAdmin);
        if(targetAdmin) access.administrator();
        for(var scope:account.scopes) access.requireScopeWithinAccess(scope.organizationUnitId,scope.includeDescendants||targetAdmin);
    }
    private void assignRoles(UserAccount account,Long basic,Set<Long> additional) {
        Set<Long> all=new HashSet<>(additional==null?Set.of():additional);all.add(basic);
        for(Long id:all) {
            Role role=roles.findById(id).orElseThrow(() -> ApiException.badRequest("Unknown role: "+id));
            if(role.builtInAdmin) access.administrator();
            if((role.systemRole&&!role.builtInAdmin)||(!role.builtInAdmin&&!Objects.equals(role.tenantId,account.tenantId))||!Integer.valueOf(1).equals(role.status)) throw ApiException.badRequest("Role must be active and assignable in the user's instance");
        }
        account.basicRoleId=basic;account.additionalRoleIds.clear();all.remove(basic);account.additionalRoleIds.addAll(all);
    }
    private void assignScopes(UserAccount account,List<AccountRequests.Scope> scopes) {
        Set<OrganizationScope> normalized=new HashSet<>();
        for(var scope:scopes) {
            OrganizationUnit unit=organizations.findById(scope.organizationUnitId()).orElseThrow(() -> ApiException.badRequest("Unknown organization scope"));
            if(!Objects.equals(unit.tenantId,account.tenantId)) throw ApiException.badRequest("Organization scope belongs to another instance");
            access.requireScopeWithinAccess(unit.id,scope.includeDescendants());
            if(!normalized.add(new OrganizationScope(unit.id,scope.includeDescendants()))) throw ApiException.badRequest("Duplicate organization scope");
        }
        account.scopes.clear();account.scopes.addAll(normalized);
    }
}
