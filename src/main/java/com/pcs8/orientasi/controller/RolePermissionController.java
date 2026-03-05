package com.pcs8.orientasi.controller;

import com.pcs8.orientasi.config.annotation.RequiresRole;
import com.pcs8.orientasi.domain.dto.request.BulkRolePermissionRequest;
import com.pcs8.orientasi.domain.dto.request.CreateMenuRequest;
import com.pcs8.orientasi.domain.dto.request.RolePermissionRequest;
import com.pcs8.orientasi.domain.dto.response.BaseResponse;
import com.pcs8.orientasi.domain.dto.response.MenuResponse;
import com.pcs8.orientasi.domain.dto.response.RolePermissionMatrixResponse;
import com.pcs8.orientasi.domain.dto.response.RolePermissionResponse;
import com.pcs8.orientasi.service.RolePermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/role-permissions")
@RequiredArgsConstructor
public class RolePermissionController {

    private static final Logger log = LoggerFactory.getLogger(RolePermissionController.class);

    private final RolePermissionService rolePermissionService;

    // ========== MENU ENDPOINTS ==========

    /**
     * Create a new menu
     * Admin only
     */
    @RequiresRole("Admin")
    @PostMapping("/menus")
    public ResponseEntity<BaseResponse> createMenu(@Valid @RequestBody CreateMenuRequest request) {
        MenuResponse menuResponse = rolePermissionService.createMenu(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(HttpStatus.CREATED.value(), "Menu created successfully", menuResponse));
    }

    /**
     * Update an existing menu
     * Admin only
     */
    @RequiresRole("Admin")
    @PutMapping("/menus/{menuId}")
    public ResponseEntity<BaseResponse> updateMenu(
            @PathVariable UUID menuId,
            @Valid @RequestBody CreateMenuRequest request) {
        log.info("Updating menu");
        MenuResponse menuResponse = rolePermissionService.updateMenu(menuId, request);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Menu updated successfully", menuResponse));
    }

    /**
     * Get all menus (flat list)
     * All authenticated users can access
     */
    @GetMapping("/menus")
    public ResponseEntity<BaseResponse> getAllMenus() {
        log.info("Getting all menus");
        List<MenuResponse> menus = rolePermissionService.getAllMenus();
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Menus retrieved successfully", menus));
    }

    /**
     * Get menu tree (hierarchical structure)
     * All authenticated users can access
     */
    @GetMapping("/menus/tree")
    public ResponseEntity<BaseResponse> getMenuTree() {
        log.info("Getting menu tree");
        List<MenuResponse> menuTree = rolePermissionService.getMenuTree();
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Menu tree retrieved successfully", menuTree));
    }

    /**
     * Get menu by ID
     * All authenticated users can access
     */
    @GetMapping("/menus/{menuId}")
    public ResponseEntity<BaseResponse> getMenuById(@PathVariable UUID menuId) {
        log.info("Getting menu by ID");
        MenuResponse menu = rolePermissionService.getMenuById(menuId);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Menu retrieved successfully", menu));
    }

    /**
     * Delete a menu
     * Admin only
     */
    @RequiresRole("Admin")
    @DeleteMapping("/menus/{menuId}")
    public ResponseEntity<BaseResponse> deleteMenu(@PathVariable UUID menuId) {
        log.info("Deleting menu");
        rolePermissionService.deleteMenu(menuId);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Menu deleted successfully", null));
    }

    // ========== PERMISSION ENDPOINTS ==========

    /**
     * Create or update a single permission
     * Admin only
     */
    @RequiresRole("Admin")
    @PostMapping("/permissions")
    public ResponseEntity<BaseResponse> createOrUpdatePermission(@Valid @RequestBody RolePermissionRequest request) {
        RolePermissionResponse response = rolePermissionService.createOrUpdatePermission(request);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Permission saved successfully", response));
    }

    /**
     * Bulk update permissions for a role
     * Admin only
     */
    @RequiresRole("Admin")
    @PostMapping("/permissions/bulk")
    public ResponseEntity<BaseResponse> bulkUpdatePermissions(@Valid @RequestBody BulkRolePermissionRequest request) {
        List<RolePermissionResponse> responses = rolePermissionService.bulkUpdatePermissions(request);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Permissions updated successfully", responses));
    }

    /**
     * Get all permissions for a specific role
     * All authenticated users can access
     */
    @GetMapping("/permissions/role/{roleId}")
    public ResponseEntity<BaseResponse> getPermissionsByRole(@PathVariable UUID roleId) {
        log.info("Getting permissions for role");
        List<RolePermissionResponse> permissions = rolePermissionService.getPermissionsByRole(roleId);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Permissions retrieved successfully", permissions));
    }

    /**
     * Get permission matrix for a single role (all menus with permissions)
     * All authenticated users can access
     */
    @GetMapping("/matrix/{roleId}")
    public ResponseEntity<BaseResponse> getPermissionMatrix(@PathVariable UUID roleId) {
        log.info("Getting permission matrix for role");
        RolePermissionMatrixResponse matrix = rolePermissionService.getPermissionMatrix(roleId);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Permission matrix retrieved successfully", matrix));
    }

    /**
     * Get permission matrices for all roles
     * Admin only
     */
    @RequiresRole("Admin")
    @GetMapping("/matrix")
    public ResponseEntity<BaseResponse> getAllPermissionMatrices() {
        log.info("Getting all permission matrices");
        List<RolePermissionMatrixResponse> matrices = rolePermissionService.getAllPermissionMatrices();
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Permission matrices retrieved successfully", matrices));
    }

    /**
     * Delete a specific permission
     * Admin only
     */
    @RequiresRole("Admin")
    @DeleteMapping("/permissions/role/{roleId}/menu/{menuId}")
    public ResponseEntity<BaseResponse> deletePermission(
            @PathVariable UUID roleId,
            @PathVariable UUID menuId) {
        log.info("Deleting permission for role on menu");
        rolePermissionService.deletePermission(roleId, menuId);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Permission deleted successfully", null));
    }

    /**
     * Check if a role has specific permission on a menu
     * All authenticated users can access
     */
    @GetMapping("/check")
    public ResponseEntity<BaseResponse> checkPermission(
            @RequestParam String roleName,
            @RequestParam String menuCode,
            @RequestParam String permissionType) {
        boolean hasPermission = rolePermissionService.hasPermission(roleName, menuCode, permissionType);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Permission check completed", hasPermission));
    }

    /**
     * Get current user's combined permissions based on their roles
     * All authenticated users can access
     */
    @GetMapping("/my-permissions")
    public ResponseEntity<BaseResponse> getMyPermissions(@RequestParam(required = false) List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new BaseResponse(HttpStatus.BAD_REQUEST.value(), "Roles parameter is required", null));
        }
        try {
            RolePermissionMatrixResponse matrix = rolePermissionService.getCombinedPermissionsForRoles(roles);
            return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "User permissions retrieved successfully", matrix));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BaseResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                            "Failed to get permissions: " + e.getMessage(), null));
        }
    }
}