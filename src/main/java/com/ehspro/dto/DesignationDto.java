package com.ehspro.dto;
import java.time.*;
import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.JsonNode;
import com.ehspro.model.*;
public class DesignationDto extends BaseDto {
    @NotBlank @Size(max = 2000) public String name;
    @Size(max = 2000) public String description;
    public Integer status;
    @NotNull @Positive public Long businessUnitId;
    @Valid public List<@NotNull NameTranslation> translations = new ArrayList<>();
    @Size(max = 2000) public String statusDisplay;
}

