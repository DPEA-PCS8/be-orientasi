package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionResponse {

    private UUID id;

    @JsonProperty("role_id")
    private UUID roleId;

    @JsonProperty("role_name")
    private String roleName;

    @JsonProperty("menu_id")
    private UUID menuId;

    @JsonProperty("menu_code")
    private String menuCode;

    @JsonProperty("menu_name")
    private String menuName;

    @JsonProperty("can_view")
    private Boolean canView;

    @JsonProperty("can_create")
    private Boolean canCreate;

    @JsonProperty("can_update")
    private Boolean canUpdate;

    @JsonProperty("can_delete")
    private Boolean canDelete;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}