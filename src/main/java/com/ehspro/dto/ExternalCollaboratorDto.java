package com.ehspro.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.*;

public class ExternalCollaboratorDto extends BaseDto {
    @NotNull public Long roleId;
    @NotNull public Long organizationUnitId;
    @NotBlank @Size(max=255) public String firstName;
    @Size(max=255) public String middleName;
    @NotBlank @Size(max=255) public String lastName;
    public String emailAddress;
    @Size(max=50) public String phoneNumber;
    @NotNull @Min(1) public Integer gender;
    @NotNull @Min(1) @Max(2) public Integer status;
    @Size(max=255) public String alias;
    public Long country;
    @Size(max=500) public String companyName;
    @Size(max=255) public String designation;
    public boolean hasAccess;
    @Min(0) @Max(150) public Integer age;
    @PastOrPresent public LocalDate dateOfBirth;
    public LocalDate dateOfJoining;
    @Valid public List<@NotNull Long> organizationUnitListIds=new ArrayList<>();
    @Valid public ExternalDetails externalDetails;
    public static class ExternalDetails {
        @Size(max=500) public String companyName;
        @Size(max=255) public String designation;
        @Min(1) @Max(2) public Integer status;
    }
    @com.fasterxml.jackson.annotation.JsonProperty(access=com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY) public String password;
    @com.fasterxml.jackson.annotation.JsonProperty(access=com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY) public String organizationUnitName;
    @com.fasterxml.jackson.annotation.JsonProperty(access=com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY) public String userRoleName;
    @com.fasterxml.jackson.annotation.JsonProperty(access=com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY) public String workerTypeName;
    @com.fasterxml.jackson.annotation.JsonProperty(access=com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY) public String fullName;
    @com.fasterxml.jackson.annotation.JsonProperty(access=com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY) public String designationName;
    @com.fasterxml.jackson.annotation.JsonProperty(access=com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY) public Integer userType;
    @com.fasterxml.jackson.annotation.JsonProperty(access=com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY) public List<Long> userRoleIds;
}
