package com.ehspro.entity;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class ExternalCollaborator extends BaseEntity {
    @Column(nullable=false) public Long roleId;
    @Column(nullable=false) public Long organizationUnitId;
    @Column(nullable=false) public String firstName;
    public String middleName;
    @Column(nullable=false) public String lastName;
    public String emailAddress;
    @Column(length=50) public String phoneNumber;
    @Column(nullable=false) public Integer gender;
    @Column(nullable=false) public Integer status;
    public String alias;
    public Long country;
    @Column(length=500) public String companyName;
    public String designation;
    @Column(nullable=false) public boolean hasAccess;
    public Integer age;
    public LocalDate dateOfBirth;
    public LocalDate dateOfJoining;
    @ElementCollection
    @CollectionTable(name="external_collaborator_organization",joinColumns=@JoinColumn(name="external_collaborator_id"))
    @Column(name="organization_unit_id",nullable=false)
    public java.util.Set<Long> organizationUnitListIds=new java.util.LinkedHashSet<>();
}
