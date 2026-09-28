package com.ehspro.entity;
import jakarta.persistence.*;
@Entity
public class PermissionDefinition {
    @Id public Long id;
    @Column(nullable=false, unique=true, length=100) public String code;
    @Column(nullable=false, length=150) public String displayName;
    @Column(nullable=false, length=100) public String moduleName;
    public Long parentId;
    public boolean businessAction;
    public boolean superAdminOnly;
}
