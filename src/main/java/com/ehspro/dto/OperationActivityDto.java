package com.ehspro.dto;
import java.time.*;
import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.JsonNode;
import com.ehspro.model.*;
public class OperationActivityDto extends BaseDto {
    @NotBlank @Size(max = 2000) public String activityName;
    @NotNull @Positive public Long businessUnitId;
    @NotNull @Positive public Long categoryId;
    @Size(max = 2000) public String description;
    public Integer status;
    @Valid public List<@NotNull ActivityTranslation> translations = new ArrayList<>();
    @Size(max = 2000) public String category;
    @Size(max = 2000) public String businessUnitName;
}

