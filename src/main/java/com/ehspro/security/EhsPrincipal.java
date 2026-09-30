package com.ehspro.security;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import java.util.Collection;
public class EhsPrincipal extends User {
    public final Long accountId;
    public final Long tenantId;
    public boolean mustChangePassword;
    public String accountType="SUPER_ADMIN";
    public EhsPrincipal(String username, String password, boolean enabled, Long accountId, Long tenantId, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, true, true, true, authorities);
        this.accountId = accountId; this.tenantId = tenantId;
    }
}
