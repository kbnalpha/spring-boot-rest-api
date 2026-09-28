package com.ehspro.entity;
import jakarta.persistence.*;
import java.time.*;
import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.ehspro.model.*;
import com.ehspro.persistence.JsonConverters;
@Entity
@Table(name = "equipment", uniqueConstraints = @UniqueConstraint(columnNames = {"organizationUnitId", "uid"}))
public class Equipment extends BaseEntity {
    public Long equipmentCategoryId;
    public Long equipmentTypeId;
    public Long organizationUnitId;
    @Column(length = 100, nullable = false) public String uid;
    @Column(length = 2000) public String manufacturer;
    @Column(length = 2000) public String modelNumber;
    @Column(length = 2000) public String serialNumber;
    @Column(length = 2000) public String yearofManufacture;
    public Integer status;

}
