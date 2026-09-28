package com.ehspro.dto;
import jakarta.validation.constraints.*;
public class ObservationSubTypeDto {
    public Long id;
    public Long observationTypeId;
    @NotBlank @Size(max = 255) public String subTypeDescription;
    public Integer status;
}
