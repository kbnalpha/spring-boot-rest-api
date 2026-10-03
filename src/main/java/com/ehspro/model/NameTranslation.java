package com.ehspro.model;
import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
public class NameTranslation {
    @NotNull public Long languageId;
    public String name;
    public String description;
}
