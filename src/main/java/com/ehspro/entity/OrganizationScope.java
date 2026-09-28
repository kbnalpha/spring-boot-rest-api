package com.ehspro.entity;
import jakarta.persistence.*;
import java.util.Objects;
@Embeddable
public class OrganizationScope {
    public Long organizationUnitId;
    public boolean includeDescendants;
    public OrganizationScope() {}
    public OrganizationScope(Long id, boolean descendants) { organizationUnitId=id; includeDescendants=descendants; }
    @Override public boolean equals(Object o) { return o instanceof OrganizationScope s && Objects.equals(organizationUnitId,s.organizationUnitId); }
    @Override public int hashCode() { return Objects.hashCode(organizationUnitId); }
}
