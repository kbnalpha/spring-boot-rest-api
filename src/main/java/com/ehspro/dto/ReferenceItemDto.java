package com.ehspro.dto;
import jakarta.validation.constraints.*;
public class ReferenceItemDto {
    @NotBlank @Size(max=255) public String name;
    @Positive public Long countryId;
    @Positive public Long stateId;
    @Size(max=100) public String code;
    @Size(max=10) public String currency;
    @Size(max=10) public String symbol;
    @Size(max=100) public String zoneId;
}
