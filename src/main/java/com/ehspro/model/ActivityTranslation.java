package com.ehspro.model;
import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
public class ActivityTranslation {
    @NotNull @Positive public Long languageId;
    public String activityName;
    public String description;
}

