package com.ehspro.security;
import com.ehspro.entity.*;
import com.ehspro.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class AccessService {
    private final UserAccountRepository accounts;
    private final OrganizationUnitRepository organizations;
    public AccessService(UserAccountRepository accounts, OrganizationUnitRepository organizations) { this.accounts=accounts; this.organizations=organizations; }
    public boolean isSuperAdmin() {
        var auth=SecurityContextHolder.getContext().getAuthentication();
        return auth!=null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }
    public void superAdmin() { if (!isSuperAdmin()) throw new AccessDeniedException("Only Super Admin can perform this action"); }
    public boolean isAdmin() {
        var auth=SecurityContextHolder.getContext().getAuthentication();
        return auth!=null&&auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
    public void administrator() { if(!isSuperAdmin()&&!isAdmin()) throw new AccessDeniedException("Admin or Super Admin is required"); }
    public EhsPrincipal principal() {
        var auth=SecurityContextHolder.getContext().getAuthentication();
        if (auth==null || !(auth.getPrincipal() instanceof EhsPrincipal p)) throw new AccessDeniedException("Authenticated system account required");
        return p;
    }
    public Long tenant(Long requested) {
        if (isSuperAdmin()) return requested==null ? 1L : requested;
        Long actual=principal().tenantId;
        if (requested!=null && !requested.equals(actual)) throw new AccessDeniedException("Another instance is outside your access");
        return actual;
    }
    public boolean hasPermission(String code) {
        var auth=SecurityContextHolder.getContext().getAuthentication();
        return isSuperAdmin() || isAdmin() || (auth!=null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(code)));
    }
    public void require(String code) { if (!hasPermission(code)) throw new AccessDeniedException("Permission required: "+code); }
    @Transactional(readOnly=true)
    public Set<Long> organizationIds() {
        if (isSuperAdmin()) return organizations.findAll().stream().map(o -> o.id).collect(java.util.stream.Collectors.toSet());
        EhsPrincipal principal=principal();
        UserAccount account=accounts.findById(principal.accountId).orElseThrow(() -> new AccessDeniedException("System account unavailable"));
        return organizationIds(account,isAdmin());
    }
    @Transactional(readOnly=true)
    public Set<Long> organizationIds(UserAccount account,boolean targetAdmin) {
        Map<Long,OrganizationUnit> eligible=new HashMap<>();
        organizations.findAll().stream().filter(o -> Objects.equals(o.tenantId,account.tenantId) && Integer.valueOf(1).equals(o.status)).forEach(o -> eligible.put(o.id,o));
        Set<Long> result=new HashSet<>();
        for (OrganizationScope scope:account.scopes) {
            if (!eligible.containsKey(scope.organizationUnitId)) continue;
            result.add(scope.organizationUnitId);
            if (scope.includeDescendants || targetAdmin) {
                Set<Long> descendants=new HashSet<>(Set.of(scope.organizationUnitId));
                boolean changed;
                do { changed=false; for (OrganizationUnit unit:eligible.values()) {
                    if (descendants.contains(unit.parentId) && descendants.add(unit.id)) changed=true;
                }} while(changed);
                result.addAll(descendants);
            }
        }
        return result;
    }
    public void organization(Long id) {
        if (!isSuperAdmin() && !organizationIds().contains(id)) throw new AccessDeniedException("Organization is outside your assigned scope");
    }
    @Transactional(readOnly=true)
    public void requireScopeWithinAccess(Long id,boolean descendants) {
        if(isSuperAdmin()) return;
        Set<Long> requested=new HashSet<>(Set.of(id));
        if(descendants) {
            var units=organizations.findAll();boolean changed;
            do {changed=false;for(var unit:units) {
                if(Objects.equals(unit.tenantId,principal().tenantId)&&Integer.valueOf(1).equals(unit.status)
                        &&requested.contains(unit.parentId)&&requested.add(unit.id)) changed=true;
            }} while(changed);
        }
        if(!organizationIds().containsAll(requested)) throw new AccessDeniedException("Cannot grant scope outside your organization tree");
    }
}
