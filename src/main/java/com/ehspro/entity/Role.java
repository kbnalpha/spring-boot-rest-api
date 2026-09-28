package com.ehspro.entity;
import jakarta.persistence.*;
import java.time.*;
import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.ehspro.model.*;
import com.ehspro.persistence.JsonConverters;
@Entity
@Table(name = "role")
public class Role extends BaseEntity {
    public Long tenantId = 1L;
    public boolean systemRole;
    @ElementCollection
    @CollectionTable(name = "role_permission", joinColumns = @JoinColumn(name = "role_id"))
    @Column(name = "permission_id") public Set<Long> permissionIds = new HashSet<>();
    @Column(length = 2000) public String name;
    @Column(length = 2000) public String displayName;
    public Integer status;
    @Column(length = 2000) public String roleDescription;
    @Column(length = 2000) public String roleType;
    public Long landingPageId;
    @Convert(converter = JsonConverters.PermissionNodeListConverter.class)
    @Lob @Column(length = Integer.MAX_VALUE) public List<PermissionNode> permissions = new ArrayList<>();
    @Convert(converter = JsonConverters.LongListConverter.class)
    @Lob @Column(length = Integer.MAX_VALUE) public List<Long> roleOrganizationUnits = new ArrayList<>();
    @Convert(converter = JsonConverters.LongListConverter.class)
    @Lob @Column(length = Integer.MAX_VALUE) public List<Long> userRoles = new ArrayList<>();
}
