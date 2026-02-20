package com.pcs8.orientasi.controller;

import com.pcs8.orientasi.config.annotation.RequiresRole;
import com.pcs8.orientasi.domain.dto.request.AssignRoleRequest;
import com.pcs8.orientasi.domain.dto.request.CreateRoleRequest;
import com.pcs8.orientasi.domain.dto.response.BaseResponse;
import com.pcs8.orientasi.domain.dto.response.RoleResponse;
import com.pcs8.orientasi.domain.dto.response.UserWithRolesResponse;
import com.pcs8.orientasi.service.RoleService;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private static final Logger log = LoggerFactory.getLogger(RoleController.class);

    private final RoleService roleService;

    // ========== ROLE MANAGEMENT ENDPOINTS ==========

    /**
     * Create a new role
     * Admin only
     */
    @RequiresRole("Admin")
    @PostMapping
    public ResponseEntity<BaseResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        log.info("Creating role: {}", request.getRoleName());
        RoleResponse roleResponse = roleService.createRole(request);
        BaseResponse response = new BaseResponse(
                HttpStatus.CREATED.value(),
                "Role created successfully",
                roleResponse
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing role
     * Admin only
     */
    @RequiresRole("Admin")
    @PutMapping("/{roleId}")
    public ResponseEntity<BaseResponse> updateRole(
            @PathVariable UUID roleId,
            @Valid @RequestBody CreateRoleRequest request) {
        log.info("Updating role: {}", roleId);
        RoleResponse roleResponse = roleService.updateRole(roleId, request);
        BaseResponse response = new BaseResponse(
                HttpStatus.OK.value(),
                "Role updated successfully",
                roleResponse
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get all roles
     * All authenticated users can access
     */
    @GetMapping
    public ResponseEntity<BaseResponse> getAllRoles() {
        log.info("Getting all roles");
        List<RoleResponse> roles = roleService.getAllRoles();
        BaseResponse response = new BaseResponse(
                HttpStatus.OK.value(),
                "Roles retrieved successfully",
                roles
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get role by ID
     * All authenticated users can access
     */
    @GetMapping("/{roleId}")
    public ResponseEntity<BaseResponse> getRoleById(@PathVariable UUID roleId) {
        log.info("Getting role: {}", roleId);
        RoleResponse roleResponse = roleService.getRoleById(roleId);
        BaseResponse response = new BaseResponse(
                HttpStatus.OK.value(),
                "Role retrieved successfully",
                roleResponse
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a role
     * Admin only
     */
    @RequiresRole("Admin")
    @DeleteMapping("/{roleId}")
    public ResponseEntity<BaseResponse> deleteRole(@PathVariable UUID roleId) {
        log.info("Deleting role: {}", roleId);
        roleService.deleteRole(roleId);
        BaseResponse response = new BaseResponse(
                HttpStatus.OK.value(),
                "Role deleted successfully",
                null
        );
        return ResponseEntity.ok(response);
    }

    // ========== USER-ROLE ASSIGNMENT ENDPOINTS ==========

    /**
     * Assign roles to a user
     * Admin only
     */
    @RequiresRole("Admin")
    @PostMapping("/assign")
    public ResponseEntity<BaseResponse> assignRolesToUser(
            @Valid @RequestBody AssignRoleRequest request,
            HttpServletRequest httpRequest) {
        log.info("Assigning roles to user: {}", request.getUserUuid());

        // Extract UUID of admin who is assigning the role from request attributes
        String userUuidStr = (String) httpRequest.getAttribute("user_uuid");

        log.info("UserUuidStr", userUuidStr);
        UUID assignedByUuid = null;
        if (userUuidStr != null && !userUuidStr.isEmpty()) {
            assignedByUuid = UUID.fromString(userUuidStr);
        }

        UserWithRolesResponse userResponse = roleService.assignRolesToUser(request, assignedByUuid);
        BaseResponse response = new BaseResponse(
                HttpStatus.OK.value(),
                "Roles assigned to user successfully",
                userResponse
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Remove a role from a user
     * Admin only
     */
    @RequiresRole("Admin")
    @DeleteMapping("/users/{userUuid}/roles/{roleId}")
    public ResponseEntity<BaseResponse> removeRoleFromUser(
            @PathVariable UUID userUuid,
            @PathVariable UUID roleId) {
        log.info("Removing role {} from user {}", roleId, userUuid);
        roleService.removeRoleFromUser(userUuid, roleId);
        BaseResponse response = new BaseResponse(
                HttpStatus.OK.value(),
                "Role removed from user successfully",
                null
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get all users with their roles
     * Admin only
     */
    @RequiresRole("Admin")
    @GetMapping("/users")
    public ResponseEntity<BaseResponse> getAllUsersWithRoles() {
        log.info("Getting all users with roles");
        List<UserWithRolesResponse> users = roleService.getAllUsersWithRoles();
        BaseResponse response = new BaseResponse(
                HttpStatus.OK.value(),
                "Users with roles retrieved successfully",
                users
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific user with their roles
     * Users can view their own roles, admins can view any user
     */
    @GetMapping("/users/{userUuid}")
    public ResponseEntity<BaseResponse> getUserWithRoles(@PathVariable UUID userUuid) {
        log.info("Getting user with roles: {}", userUuid);
        UserWithRolesResponse userResponse = roleService.getUserWithRoles(userUuid);
        BaseResponse response = new BaseResponse(
                HttpStatus.OK.value(),
                "User with roles retrieved successfully",
                userResponse
        );
        return ResponseEntity.ok(response);
    }
}
