package com.ehspro.entity;
import jakarta.persistence.*;
@Entity
public class SubLocation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    public String name;
    public String description;
    public Integer status;
}

