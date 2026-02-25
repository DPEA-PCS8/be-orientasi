package com.pcs8.orientasi.domain.dto.response;

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

    private UUID roleId;
    private String roleName;
    private String roleDescription;
    private List<MenuPermissionItem> menuPermissions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuPermissionItem {
        private UUID menuId;
        private String menuCode;
        private String menuName;
        private UUID parentId;
        private String parentName;
        private Integer displayOrder;
        private Boolean canView;
        private Boolean canCreate;
        private Boolean canUpdate;
        private Boolean canDelete;
    }
}