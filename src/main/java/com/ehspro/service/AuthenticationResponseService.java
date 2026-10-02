package com.ehspro.service;

import com.ehspro.entity.*;
import com.ehspro.repository.*;
import com.ehspro.security.EhsPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@Transactional(readOnly=true)
public class AuthenticationResponseService {
    @org.springframework.beans.factory.annotation.Value("${ehs.super-admin.email:kbnalpha@gmail.com}") private String superEmail;
    private final com.ehspro.security.JwtService jwt;
    private final UserAccountRepository accounts;
    private final EmployeeRepository employees;
    private final OrganizationUnitRepository organizations;
    private final RoleRepository roles;
    private final ReferenceItemRepository lookups;
    private final PermissionDefinitionRepository permissions;
    private final PermissionSourceAliasRepository aliases;
    public AuthenticationResponseService(UserAccountRepository accounts,EmployeeRepository employees,
            OrganizationUnitRepository organizations,RoleRepository roles,ReferenceItemRepository lookups,
            PermissionDefinitionRepository permissions,PermissionSourceAliasRepository aliases,com.ehspro.security.JwtService jwt) {
        this.jwt=jwt;
        this.accounts=accounts;this.employees=employees;this.organizations=organizations;this.roles=roles;
        this.lookups=lookups;this.permissions=permissions;this.aliases=aliases;
    }
    public Map<String,Object> response(EhsPrincipal p) {
        boolean superAdmin="SUPER_ADMIN".equals(p.accountType);
        var account=superAdmin?null:accounts.findById(p.accountId).orElseThrow();
        var employee=account==null?null:employees.findById(account.employeeId).orElseThrow();
        var unit=employee==null?null:organizations.findById(employee.organizationUnitId).orElseThrow();
        Set<Long> roleIds=new HashSet<>();
        if(account!=null) {roleIds.add(account.basicRoleId);roleIds.addAll(account.additionalRoleIds);}
        var activeRoles=roles.findAllById(roleIds).stream().filter(r -> Integer.valueOf(1).equals(r.status))
            .filter(r -> r.builtInAdmin||(!r.systemRole&&Objects.equals(r.tenantId,p.tenantId))).toList();
        Set<String> roleNames=new TreeSet<>();roleNames.add("User");
        if(superAdmin) roleNames.add("SuperAdmin");
        for(var role:activeRoles) roleNames.add(role.builtInAdmin?"Admin":role.name);
        Long landingId=account==null?Long.valueOf(1):activeRoles.stream().filter(r -> r.id.equals(account.basicRoleId)).map(r -> r.landingPageId).filter(Objects::nonNull).findFirst().orElse(null);
        var result=new LinkedHashMap<String,Object>();
        result.put("id",superAdmin?Long.valueOf(-1):p.accountId);
        result.put("organizationUnitId",employee==null?null:employee.organizationUnitId);
        result.put("userName",employee==null?p.getUsername():java.util.stream.Stream.of(employee.firstName,employee.middleName,employee.lastName).filter(s -> s!=null&&!s.isBlank()).collect(java.util.stream.Collectors.joining(" ")));
        result.put("email",account==null?superEmail:account.username);
        result.put("token",jwt.issue(p));
        result.put("expiresIn",jwt.expiresIn());
        result.put("roles",roleNames);
        result.put("permissions",permissionNames(p));
        result.put("landingPage",lookup("LANDING_PAGE",landingId,false));
        result.put("resetPassword",p.mustChangePassword);
        result.put("userType",employee==null?1:employee.userType);
        result.put("contractorCompanyId",employee==null||employee.contractorId==null?0L:employee.contractorId);
        result.put("clientId",p.tenantId);
        result.put("languageCode","en-US");
        result.put("buLanguageCode","en-US");
        result.put("accountType",p.accountType);
        result.put("mustChangePassword",p.mustChangePassword);
        result.put("nextAction",p.mustChangePassword?"RESET_PASSWORD":"LOGIN_SUCCESS");
        result.put("authenticationType","BEARER");
        if(p.mustChangePassword) result.put("resetPasswordEndpoint","/api/Auth/FirstLoginPasswordReset");
        return result;
    }
    private String lookup(String kind,Long id,boolean code) {
        if(id==null) return null;
        return lookups.findById(new ReferenceItemId(kind,id)).map(r -> code?r.code:r.name).orElse(null);
    }
    private Set<String> permissionNames(EhsPrincipal p) {
        Set<String> result=new TreeSet<>();
        if(p.mustChangePassword) return result;
        boolean elevated=Set.of("SUPER_ADMIN","ADMIN").contains(p.accountType);
        Set<String> granted=p.getAuthorities().stream().map(a -> a.getAuthority()).collect(java.util.stream.Collectors.toSet());
        Map<Long,PermissionDefinition> catalog=new HashMap<>();permissions.findAll().forEach(d -> catalog.put(d.id,d));
        Map<Long,String> names=new HashMap<>();
        aliases.findAll().forEach(a -> names.put(a.permissionId,a.sourceCode));
        for(var d:catalog.values()) {
            if(!elevated&&!granted.contains(d.code)) continue;
            String name=names.getOrDefault(d.id,d.code);
            // Source contract places EditBusinessUnit under BusinessUnit(BU), while the canonical permission is administrative.
            Long parent=Long.valueOf(3301).equals(d.id)?Long.valueOf(3330):d.parentId;
            if(parent!=null&&catalog.containsKey(parent)) {
                var group=catalog.get(parent);String groupName=names.getOrDefault(parent,group.code);
                result.add(groupName);name=groupName+"."+name;
            }
            result.add(name);
        }
        return result;
    }
}
