package com.ehspro.entity;
import jakarta.persistence.*;
import java.time.*;
import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.ehspro.model.*;
import com.ehspro.persistence.JsonConverters;
@Entity
@Table(name = "location")
public class Location extends BaseEntity {
    @Column(length = 2000) public String name;
    @Column(length = 2000) public String locationDescription;
    public Integer status;
    public Long organizationUnitId;
    @Convert(converter = JsonConverters.LongListConverter.class)
    @Lob @Column(length = Integer.MAX_VALUE) public List<Long> supervisorIds = new ArrayList<>();
    @Convert(converter = JsonConverters.LocationTranslationListConverter.class)
    @Lob @Column(length = Integer.MAX_VALUE) public List<LocationTranslation> translations = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "location_id", nullable = false)
    public List<SubLocation> subLocations = new ArrayList<>();
}

