package com.ehspro.dto;
import java.time.*;
import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.JsonNode;
import com.ehspro.model.*;
public class RoleDto extends BaseDto {
    @Positive public Long tenantId;
    public boolean systemRole;
    public Set<@NotNull @Positive Long> permissionIds = new HashSet<>();
    @NotBlank @Size(max = 2000) public String name;
    @Size(max = 2000) public String displayName;
    public Integer status;
    @Size(max = 2000) public String roleDescription;
    @Size(max = 2000) public String roleType;
    @NotNull @Positive public Long landingPageId;
    @Valid public List<@NotNull PermissionNode> permissions = new ArrayList<>();
    @Valid public List<@NotNull @Positive Long> roleOrganizationUnits = new ArrayList<>();
    @Valid public List<@NotNull @Positive Long> userRoles = new ArrayList<>();
    public Long createBy;
    @Valid public List<@NotNull PermissionNode> permissionLookupHierarchyDto = new ArrayList<>();
}
