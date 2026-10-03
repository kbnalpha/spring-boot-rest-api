package com.ehspro.dto;
import jakarta.validation.constraints.*;
public class ContractorDto extends BaseDto {
    @Size(max=255) public String name;
    @Size(max=255) public String contractorName;
    @NotNull @Positive public Long businessUnitId;
    @NotNull @Min(1) @Max(2) public Integer status;
    @Size(max=100) public String contractorCode;
    @Size(max=2000) public String servicesOffered;
    @Size(max=500) public String addressLine1;
    @Size(max=500) public String addressLine2;
    @Size(max=30) public String postalCode;
    @Size(max=255) public String primaryContactPersonName;
    @Size(max=255) public String primaryContactDesignation;
    @Size(max=50) public String phoneNumber;
    @Size(max=255) public String email;
    @Size(max=500) public String website;
    @Size(max=500) public String linkedIn;
    @Positive public Long countryId;
    @Positive public Long stateId;
    @Positive public Long cityId;
    @com.fasterxml.jackson.annotation.JsonProperty(access=com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY) public String country;
    @com.fasterxml.jackson.annotation.JsonProperty(access=com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY) public String state;
    @com.fasterxml.jackson.annotation.JsonProperty(access=com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY) public String city;
    @com.fasterxml.jackson.annotation.JsonProperty(access=com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY) public String businessUnitName;
    @com.fasterxml.jackson.annotation.JsonProperty(access=com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY) public String createdByName;
}
