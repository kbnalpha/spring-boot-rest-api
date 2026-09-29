package com.ehspro.entity;
import jakarta.persistence.*;
@Entity
public class PermissionSourceAlias {
    @Id public Long sourceId;
    @Column(nullable=false) public Long permissionId;
    @Column(nullable=false,length=100) public String sourceCode;
}
