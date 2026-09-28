package com.ehspro.model;
import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
public class LocationTranslation {
    @NotNull @Positive public Long languageId;
    public String name;
    public String description;
    @Valid public List<@NotNull SubLocationTranslation> subLocations = new ArrayList<>();
}
