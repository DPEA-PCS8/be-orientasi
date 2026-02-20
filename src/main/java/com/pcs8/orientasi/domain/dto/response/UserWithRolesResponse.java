package com.pcs8.orientasi.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserWithRolesResponse {

    private UUID uuid;
    private String username;
    private String fullName;
    private String email;
    private String department;
    private String title;
    private LocalDateTime lastLoginAt;
    private Set<RoleResponse> roles;
    private boolean hasRole;
}
