package com.ehspro.entity;
import jakarta.persistence.*;
import java.time.*;
import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.ehspro.model.*;
import com.ehspro.persistence.JsonConverters;
@Entity
@Table(name = "observation_type")
public class ObservationType extends BaseEntity {
    public Long observationCategoryId;
    @Column(length = 2000) public String typeDescription;
    public Boolean enableSvt;
    public Integer status;
    public Long businessUnitId;
    public Boolean isDefault;
    @Convert(converter = JsonConverters.ObservationTranslationListConverter.class)
    @Lob @Column(length = Integer.MAX_VALUE) public List<ObservationTranslation> translations = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "observation_type_id", nullable = false)
    public List<ObservationSubType> observationSubTypes = new ArrayList<>();
}

