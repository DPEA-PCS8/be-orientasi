package com.pcs8.orientasi.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("role_id")
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
        @JsonProperty("menu_id")
        @NotNull(message = "Menu ID is required")
        private UUID menuId;

        @JsonProperty("can_view")
        @Builder.Default
        private Boolean canView = false;

        @JsonProperty("can_create")
        @Builder.Default
        private Boolean canCreate = false;

        @JsonProperty("can_update")
        @Builder.Default
        private Boolean canUpdate = false;

        @JsonProperty("can_delete")
        @Builder.Default
        private Boolean canDelete = false;
    }
}