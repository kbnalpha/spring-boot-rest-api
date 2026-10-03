package com.ehspro.dto;
import java.time.*;
import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.JsonNode;
import com.ehspro.model.*;
public class LocationDto extends BaseDto {
    @NotBlank @Size(max = 2000) public String name;
    @Size(max = 2000) public String locationDescription;
    public Integer status;
    @NotNull public Long organizationUnitId;
    @Valid public List<@NotNull Long> supervisorIds = new ArrayList<>();
    @Valid public List<@NotNull LocationTranslation> translations = new ArrayList<>();
    @Size(max = 2000) public String statusName;
    @Size(max = 2000) public String supervisorNames;
    @Size(max = 2000) public String subLocationNames;
    @Size(max = 2000) public String organizationUnitName;
    @Size(max = 2000) public String createByName;
    @Size(max = 2000) public String locationSupervisor;
    @Valid public List<@NotNull Long> subLocationIds = new ArrayList<>();
    @Valid public List<@NotNull SubLocationDto> newSublocations = new ArrayList<>();
    @Valid public List<@NotNull SubLocationDto> subLocations = new ArrayList<>();
}
