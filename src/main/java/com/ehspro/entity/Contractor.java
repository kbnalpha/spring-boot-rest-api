package com.ehspro.entity;
import jakarta.persistence.*;
@Entity
public class Contractor extends BaseEntity {
    @Column(nullable=false,length=255) public String name;
    @Column(nullable=false) public Long businessUnitId;
    @Column(nullable=false) public Integer status;
}
