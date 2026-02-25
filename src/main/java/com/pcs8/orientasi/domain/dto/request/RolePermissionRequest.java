package com.pcs8.orientasi.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionRequest {

    @NotNull(message = "Role ID is required")
    private UUID roleId;

    @NotNull(message = "Menu ID is required")
    private UUID menuId;

    @Builder.Default
    private Boolean canView = false;

    @Builder.Default
    private Boolean canCreate = false;

    @Builder.Default
    private Boolean canUpdate = false;

    @Builder.Default
    private Boolean canDelete = false;
}