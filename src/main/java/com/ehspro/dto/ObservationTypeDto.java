package com.ehspro.dto;
import java.time.*;
import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.JsonNode;
import com.ehspro.model.*;
public class ObservationTypeDto extends BaseDto {
    @NotNull @Positive public Long observationCategoryId;
    @NotBlank @Size(max = 2000) public String typeDescription;
    public Boolean enableSvt = false;
    public Integer status;
    @NotNull @Positive public Long businessUnitId;
    public Boolean isDefault;
    @Valid public List<@NotNull ObservationTranslation> translations = new ArrayList<>();
    @Size(max = 2000) public String observationCategoryName;
    @Size(max = 2000) public String createdByName;
    @Size(max = 2000) public String observationSubTypeDescriptions;
    @Size(max = 2000) public String enableSvtDisplay;
    @Size(max = 2000) public String statusDisplay;
    public Boolean isEditDelete;
    @Valid public List<@NotNull ObservationSubTypeDto> observationSubTypes = new ArrayList<>();
}

