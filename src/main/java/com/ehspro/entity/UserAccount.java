package com.ehspro.entity;
import jakarta.persistence.*;
import java.util.*;
@Entity
public class UserAccount {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false, unique=true) public Long employeeId;
    @Column(nullable=false) public Long tenantId;
    @Column(nullable=false, unique=true, length=100) public String username;
    @Column(nullable=false, length=100) public String passwordHash;
    public boolean enabled;
    @Column(nullable=false) public Long basicRoleId;
    @ElementCollection
    @CollectionTable(name="account_role",joinColumns=@JoinColumn(name="account_id"))
    @Column(name="role_id") public Set<Long> additionalRoleIds = new HashSet<>();
    @ElementCollection
    @CollectionTable(name="account_organization_scope",joinColumns=@JoinColumn(name="account_id"))
    public Set<OrganizationScope> scopes = new HashSet<>();
}
