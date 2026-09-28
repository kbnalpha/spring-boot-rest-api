package com.ehspro.entity;
import jakarta.persistence.*;
import java.time.*;
import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.ehspro.model.*;
import com.ehspro.persistence.JsonConverters;
@Entity
@Table(name = "employee", uniqueConstraints = @UniqueConstraint(columnNames = "userNumber"))
public class Employee extends BaseEntity {
    @Column(length = 20) public String countryCode;
    @Column(length = 2000) public String firstName;
    @Column(length = 2000) public String middleName;
    @Column(length = 2000) public String lastName;
    @Column(length = 2000) public String emailAddress;
    @Column(length = 2000) public String phoneNumber;
    public Long profilePictureId;
    public Integer gender;
    public Long department;
    public LocalDate dateOfJoining;
    public Long contractorId;
    public Integer accessFailedCount;
    public Long designation;
    public Integer userType;
    public Integer status;
    @Column(length = 100, nullable = false) public String userNumber;
    @Column(name = "tenant_id") public Long tenantID;
    @Column(name = "language_id") public Long languageID;
    public Long organizationUnitId;
    public Boolean hasAccess;
    public Boolean isMobileUser;
    @Column(length = 2000) public String alias;
    public LocalDate dateOfBirth;
    public Integer age;
    public Long country;
    public Boolean isSubscribed;
    @Convert(converter = JsonConverters.LongListConverter.class)
    @Lob @Column(length = Integer.MAX_VALUE) public List<Long> userRoleIds = new ArrayList<>();
    @ElementCollection
    @CollectionTable(name = "employee_organization", joinColumns = @JoinColumn(name = "employee_id"))
    @Column(name = "organization_id", nullable = false)
    public List<Long> organizationUnitIds = new ArrayList<>();
    @Convert(converter = JsonConverters.LongListConverter.class)
    @Lob @Column(length = Integer.MAX_VALUE) public List<Long> organizationUnitIdsMapped = new ArrayList<>();
    @Convert(converter = JsonConverters.LongListConverter.class)
    @Lob @Column(length = Integer.MAX_VALUE) public List<Long> organizationUnitListIds = new ArrayList<>();
    @Convert(converter = JsonConverters.TreeConverter.class)
    @Lob @Column(length = Integer.MAX_VALUE) public JsonNode uploadedFiles;
}
