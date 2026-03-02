package com.pcs8.orientasi.service;

import com.pcs8.orientasi.domain.dto.request.BulkRolePermissionRequest;
import com.pcs8.orientasi.domain.dto.request.CreateMenuRequest;
import com.pcs8.orientasi.domain.dto.request.RolePermissionRequest;
import com.pcs8.orientasi.domain.dto.response.MenuResponse;
import com.pcs8.orientasi.domain.dto.response.RolePermissionMatrixResponse;
import com.pcs8.orientasi.domain.dto.response.RolePermissionResponse;

import java.util.List;
import java.util.UUID;

public interface RolePermissionService {

    // ========== MENU MANAGEMENT ==========

    /**
     * Create a new menu
     */
    MenuResponse createMenu(CreateMenuRequest request);

    /**
     * Update an existing menu
     */
    MenuResponse updateMenu(UUID menuId, CreateMenuRequest request);

    /**
     * Get all menus (flat list)
     */
    List<MenuResponse> getAllMenus();

    /**
     * Get all menus in tree structure
     */
    List<MenuResponse> getMenuTree();

    /**
     * Get menu by ID
     */
    MenuResponse getMenuById(UUID menuId);

    /**
     * Delete a menu
     */
    void deleteMenu(UUID menuId);

    // ========== ROLE PERMISSION MANAGEMENT ==========

    /**
     * Create or update a single role permission
     */
    RolePermissionResponse createOrUpdatePermission(RolePermissionRequest request);

    /**
     * Bulk update permissions for a role
     */
    List<RolePermissionResponse> bulkUpdatePermissions(BulkRolePermissionRequest request);

    /**
     * Get all permissions for a role
     */
    List<RolePermissionResponse> getPermissionsByRole(UUID roleId);

    /**
     * Get permission matrix for a role (all menus with their permissions)
     */
    RolePermissionMatrixResponse getPermissionMatrix(UUID roleId);

    /**
     * Get permission matrices for all roles
     */
    List<RolePermissionMatrixResponse> getAllPermissionMatrices();

    /**
     * Delete a specific permission
     */
    void deletePermission(UUID roleId, UUID menuId);

    /**
     * Check if a role has specific permission on a menu
     */
    boolean hasPermission(String roleName, String menuCode, String permissionType);

    /**
     * Get combined permissions for multiple roles (used for current user's accessible menus)
     */
    RolePermissionMatrixResponse getCombinedPermissionsForRoles(List<String> roleNames);
}