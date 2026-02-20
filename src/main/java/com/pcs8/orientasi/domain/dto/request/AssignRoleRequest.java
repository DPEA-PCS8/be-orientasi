package com.pcs8.orientasi.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignRoleRequest {

    @NotNull(message = "User UUID is required")
    private UUID userUuid;

    @NotNull(message = "Role IDs are required")
    private Set<UUID> roleIds;
}
