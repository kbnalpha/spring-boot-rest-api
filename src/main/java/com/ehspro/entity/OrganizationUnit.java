package com.ehspro.entity;
import jakarta.persistence.*;
import java.time.*;
import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.ehspro.model.*;
import com.ehspro.persistence.JsonConverters;
@Entity
@Table(name = "organization_unit")
public class OrganizationUnit extends BaseEntity {
    @Column(length = 50) public String name;
    @Column(length = 2048, columnDefinition = "text") public String layoutImageUrl;
    @Column(precision = 10, scale = 7) public java.math.BigDecimal latitude;
    @Column(precision = 10, scale = 7) public java.math.BigDecimal longitude;
    @Column(length = 2000, columnDefinition = "text") public String description;
    public Long tenantId;
    public Long parentId;
    public Integer status;
    @Column(length = 2000) public String currency;
    @Column(length = 2000, columnDefinition = "text") public String line1;
    @Column(length = 2000, columnDefinition = "text") public String line2;
    public Long city;
    public Long state;
    public Long country;
    @Column(length = 2000) public String countryCode;
    @Column(length = 2000) public String symbol;
    @Column(length = 2000) public String timeZone;
    public Long timeZoneId;
    public Long languageId;
    @Column(length = 2000) public String keyContactName;
    @Column(length = 2000) public String phoneNumber;
    @Column(length = 2000) public String emailAddress;
    public Boolean isAnonymous;
    public Boolean isObservationProofRequired;
    @Convert(converter = JsonConverters.TreeConverter.class)
    @Lob @Column(length = Integer.MAX_VALUE) public JsonNode attachments;
    @Convert(converter = JsonConverters.TreeConverter.class)
    @Lob @Column(length = Integer.MAX_VALUE) public JsonNode shifts;
    @Convert(converter = JsonConverters.TreeConverter.class)
    @Lob @Column(length = Integer.MAX_VALUE) public JsonNode buImage;
}
