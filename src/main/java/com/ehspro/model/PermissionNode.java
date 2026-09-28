package com.ehspro.model;
import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
public class PermissionNode {
    public Long id;
    public String name;
    public String displayName;
    public Boolean isGranted;
    public Integer status;
    public Long parentId;
    @Valid public List<@NotNull PermissionNode> children = new ArrayList<>();
}
