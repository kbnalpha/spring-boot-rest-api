package com.ehspro.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

public class ListRequest {
    @com.fasterxml.jackson.annotation.JsonIgnore public boolean systemUsersOnly;
    @com.fasterxml.jackson.annotation.JsonIgnore public Integer workerType;
    public String sorting;
    @Pattern(regexp = "(?i)asc|desc|^$") public String sortingType;
    public String filter;
    @Valid public List<@NotNull ColumnFilter> filters = new ArrayList<>();
    @Min(1) @Max(1000) public int maxResultCount = 10;
    @Min(0) public int skipCount;
    @Valid public List<@NotNull SortField> multiSortMeta = new ArrayList<>();
    public String businessUnitIds;
    @PositiveOrZero public Long id;
    @PositiveOrZero public Long userId;
    public boolean isExportToExcel;

    public static class ColumnFilter {
        @NotBlank public String field;
        public JsonNode value;
        public String matchMode = "contains";
    }
    public static class SortField {
        @NotBlank public String field;
        @Min(-1) @Max(1) public int order = 1;
    }
}
