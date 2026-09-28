package com.ehspro.dto;
import jakarta.validation.constraints.*;
public class ContractorDto extends BaseDto {
    @NotBlank @Size(max=255) public String name;
    @NotNull @Positive public Long businessUnitId;
    @NotNull @Min(1) @Max(2) public Integer status;
}
