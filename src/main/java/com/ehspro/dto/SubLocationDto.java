package com.ehspro.dto;
import jakarta.validation.constraints.*;
public class SubLocationDto {
    public Long id;
    @NotBlank @Size(max = 255) public String name;
    @Size(max = 255) public String description;
    public Integer status;
}
