package com.ehspro.dto;
import java.time.*;
import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.JsonNode;
import com.ehspro.model.*;
public class EquipmentDto extends BaseDto {
    @NotNull @Positive public Long equipmentCategoryId;
    @NotNull @Positive public Long equipmentTypeId;
    @NotNull @Positive public Long organizationUnitId;
    @NotBlank @Size(max = 100) public String uid;
    @Size(max = 2000) public String manufacturer;
    @Size(max = 2000) public String modelNumber;
    @Size(max = 2000) public String serialNumber;
    @Size(max = 2000) public String yearofManufacture;
    public Integer status;
    @Size(max = 2000) public String equipmentCategoryName;
    @Size(max = 2000) public String equipmentTypeName;
    @Size(max = 2000) public String organizationUnitNames;
    @Size(max = 2000) public String active;
}
