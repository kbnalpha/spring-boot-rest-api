package com.ehspro.entity;
import jakarta.persistence.*;
@Entity @IdClass(ReferenceItemId.class)
public class ReferenceItem {
    @Id @Column(length=30) public String kind;
    @Id public Long id;
    @Column(nullable=false,length=255) public String name;
    public Long countryId;
    public Long stateId;
    @Column(length=100) public String code;
    @Column(length=10) public String currency;
    @Column(length=10) public String symbol;
    @Column(length=100) public String zoneId;
}
