package com.ehspro.entity;
import jakarta.persistence.*;
@Entity
public class ObservationSubType {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    public String subTypeDescription;
    public Integer status;
}

