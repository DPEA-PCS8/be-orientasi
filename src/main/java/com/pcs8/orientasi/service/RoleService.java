package com.pcs8.orientasi.service;

import com.pcs8.orientasi.domain.dto.request.AssignRoleRequest;
import com.pcs8.orientasi.domain.dto.request.CreateRoleRequest;
import com.pcs8.orientasi.domain.dto.response.RoleResponse;
import com.pcs8.orientasi.domain.dto.response.UserWithRolesResponse;
import com.pcs8.orientasi.domain.entity.MstRole;

import java.util.List;
import java.util.UUID;

public interface RoleService {

    /**
     * Create a new role
     */
    RoleResponse createRole(CreateRoleRequest request);

    /**
     * Update an existing role
     */
    RoleResponse updateRole(UUID roleId, CreateRoleRequest request);

    /**
     * Get all roles
     */
    List<RoleResponse> getAllRoles();

    /**
     * Get role by ID
     */
    RoleResponse getRoleById(UUID roleId);

    /**
     * Get role by name
     */
    MstRole getRoleByName(String roleName);

    /**
     * Delete a role
     */
    void deleteRole(UUID roleId);

    /**
     * Assign roles to a user
     */
    UserWithRolesResponse assignRolesToUser(AssignRoleRequest request, UUID assignedByUuid);

    /**
     * Remove a role from a user
     */
    void removeRoleFromUser(UUID userUuid, UUID roleId);

    /**
     * Get all users with their roles
     */
    List<UserWithRolesResponse> getAllUsersWithRoles();

    /**
     * Get user with roles by user UUID
     */
    UserWithRolesResponse getUserWithRoles(UUID userUuid);
}
