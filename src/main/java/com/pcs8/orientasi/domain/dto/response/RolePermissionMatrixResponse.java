package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class RolePermissionMatrixResponse {

    @JsonProperty("role_id")
    private UUID roleId;

    @JsonProperty("role_name")
    private String roleName;

    @JsonProperty("role_description")
    private String roleDescription;

    @JsonProperty("menu_permissions")
    private List<MenuPermissionItem> menuPermissions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuPermissionItem {
        @JsonProperty("menu_id")
        private UUID menuId;

        @JsonProperty("menu_code")
        private String menuCode;

        @JsonProperty("menu_name")
        private String menuName;

        @JsonProperty("parent_id")
        private UUID parentId;

        @JsonProperty("parent_name")
        private String parentName;

        @JsonProperty("display_order")
        private Integer displayOrder;

        @JsonProperty("can_view")
        private Boolean canView;

        @JsonProperty("can_create")
        private Boolean canCreate;

        @JsonProperty("can_update")
        private Boolean canUpdate;

        @JsonProperty("can_delete")
        private Boolean canDelete;
    }
}