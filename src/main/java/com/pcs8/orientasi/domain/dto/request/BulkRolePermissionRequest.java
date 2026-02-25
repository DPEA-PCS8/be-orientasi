package com.pcs8.orientasi.domain.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkRolePermissionRequest {

    @NotNull(message = "Role ID is required")
    private UUID roleId;

    @NotEmpty(message = "Permissions list cannot be empty")
    @Valid
    private List<MenuPermission> permissions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuPermission {
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
}