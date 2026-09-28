package com.ehspro.security;
import com.ehspro.entity.*;
import com.ehspro.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class AccountDetailsService implements UserDetailsService {
    private final UserAccountRepository accounts;
    private final EmployeeRepository employees;
    private final RoleRepository roles;
    private final PermissionDefinitionRepository permissions;
    private final String superUsername;
    private final String superPasswordHash;
    public AccountDetailsService(UserAccountRepository accounts, EmployeeRepository employees, RoleRepository roles,
            PermissionDefinitionRepository permissions, PasswordEncoder encoder,
            @Value("${spring.security.user.name:ehs-api}") String username,
            @Value("${spring.security.user.password:ehs-api-local}") String password) {
        this.accounts=accounts; this.employees=employees; this.roles=roles; this.permissions=permissions;
        superUsername=username; superPasswordHash=encoder.encode(password);
    }
    public boolean reservedUsername(String name) { return superUsername.equalsIgnoreCase(name); }
    @Override @Transactional(readOnly=true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (superUsername.equals(username)) return new EhsPrincipal(username,superPasswordHash,true,null,0L,
            List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")));
        UserAccount account=accounts.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
        Employee employee=employees.findById(account.employeeId).orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
        Set<Long> roleIds=new HashSet<>(account.additionalRoleIds); roleIds.add(account.basicRoleId);
        Set<Long> permissionIds=new HashSet<>();
        for (Role role:roles.findAllById(roleIds)) {
            if (!role.systemRole && Objects.equals(role.tenantId,account.tenantId) && Integer.valueOf(1).equals(role.status)) permissionIds.addAll(role.permissionIds);
        }
        var authorities=permissions.findAllById(permissionIds).stream().filter(p -> p.businessAction && !p.superAdminOnly)
            .map(p -> new SimpleGrantedAuthority(p.code)).toList();
        boolean active=account.enabled && Integer.valueOf(1).equals(employee.status) && Boolean.TRUE.equals(employee.hasAccess);
        return new EhsPrincipal(username,account.passwordHash,active,account.id,account.tenantId,authorities);
    }
}
