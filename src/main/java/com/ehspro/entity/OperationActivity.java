package com.ehspro.entity;
import jakarta.persistence.*;
import java.time.*;
import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.ehspro.model.*;
import com.ehspro.persistence.JsonConverters;
@Entity
@Table(name = "operation_activity")
public class OperationActivity extends BaseEntity {
    @Column(length = 2000) public String activityName;
    public Long businessUnitId;
    public Long categoryId;
    @Column(length = 2000) public String description;
    public Integer status;
    @Convert(converter = JsonConverters.ActivityTranslationListConverter.class)
    @Lob @Column(length = Integer.MAX_VALUE) public List<ActivityTranslation> translations = new ArrayList<>();
}

