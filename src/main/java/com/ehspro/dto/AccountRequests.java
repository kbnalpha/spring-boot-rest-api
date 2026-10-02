package com.ehspro.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
public final class AccountRequests {
    private AccountRequests() {}
    public record Scope(@NotNull @Positive Long organizationUnitId, boolean includeDescendants) {}
    public record Activate(
        @NotNull @Positive Long basicRoleId,
        Set<@NotNull @Positive Long> additionalRoleIds,
        @NotEmpty List<@NotNull @Valid Scope> scopes) {}
    public record Roles(@NotNull @Positive Long basicRoleId, Set<@NotNull @Positive Long> additionalRoleIds) {}
    public record Scopes(@NotEmpty List<@NotNull @Valid Scope> scopes) {}
    public record Enabled(boolean enabled) {}
    public record Login(@NotBlank @Email @Size(max=254) String email, @NotBlank @Size(max=200) String password) {}
    public record FirstLoginReset(@NotBlank @Size(max=200) String currentPassword,
        @NotBlank @Size(min=12,max=72) String newPassword, @NotBlank @Size(max=72) String confirmPassword) {}
}
