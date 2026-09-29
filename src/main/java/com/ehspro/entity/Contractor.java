package com.ehspro.entity;
import jakarta.persistence.*;
@Entity
public class Contractor extends BaseEntity {
    @Column(nullable=false,length=255) public String name;
    @Column(nullable=false) public Long businessUnitId;
    @Column(nullable=false) public Integer status;
    @Column(length=100) public String contractorCode;
    @Column(length=2000) public String servicesOffered;
    @Column(length=500) public String addressLine1;
    @Column(length=500) public String addressLine2;
    @Column(length=30) public String postalCode;
    @Column(length=255) public String primaryContactPersonName;
    @Column(length=255) public String primaryContactDesignation;
    @Column(length=50) public String phoneNumber;
    @Column(length=255) public String email;
    @Column(length=500) public String website;
    @Column(length=500) public String linkedIn;
    public Long countryId;
    public Long stateId;
    public Long cityId;
}
