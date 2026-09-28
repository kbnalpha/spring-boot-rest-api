package com.ehspro.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
public final class AccountRequests {
    private AccountRequests() {}
    public record Scope(@NotNull @Positive Long organizationUnitId, boolean includeDescendants) {}
    public record Activate(
        @NotBlank @Pattern(regexp="[a-zA-Z0-9._@-]{3,100}") String username,
        @NotBlank @Size(min=12,max=72) String password,
        @NotNull @Positive Long basicRoleId,
        Set<@NotNull @Positive Long> additionalRoleIds,
        @NotEmpty List<@NotNull @Valid Scope> scopes) {}
    public record Roles(@NotNull @Positive Long basicRoleId, Set<@NotNull @Positive Long> additionalRoleIds) {}
    public record Scopes(@NotEmpty List<@NotNull @Valid Scope> scopes) {}
    public record Enabled(boolean enabled) {}
}
