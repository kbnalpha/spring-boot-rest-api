package com.ehspro.entity;
import jakarta.persistence.*;
import java.time.*;
import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.ehspro.model.*;
import com.ehspro.persistence.JsonConverters;
@Entity
@Table(name = "department")
public class Department extends BaseEntity {
    @Column(length = 2000) public String name;
    @Column(length = 2000) public String description;
    public Integer status;
    public Long businessUnitId;
    @Convert(converter = JsonConverters.NameTranslationListConverter.class)
    @Lob @Column(length = Integer.MAX_VALUE) public List<NameTranslation> translations = new ArrayList<>();
}

