package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.domain.dto.request.AssignRoleRequest;
import com.pcs8.orientasi.domain.dto.request.CreateRoleRequest;
import com.pcs8.orientasi.domain.dto.response.RoleResponse;
import com.pcs8.orientasi.domain.dto.response.UserWithRolesResponse;
import com.pcs8.orientasi.domain.entity.MstRole;
import com.pcs8.orientasi.domain.entity.MstUser;
import com.pcs8.orientasi.domain.entity.MstUserRole;
import com.pcs8.orientasi.exception.BadRequestException;
import com.pcs8.orientasi.exception.ResourceNotFoundException;
import com.pcs8.orientasi.repository.MstRoleRepository;
import com.pcs8.orientasi.repository.MstUserRepository;
import com.pcs8.orientasi.repository.UserRoleRepository;
import com.pcs8.orientasi.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private static final Logger log = LoggerFactory.getLogger(RoleServiceImpl.class);

    private final MstRoleRepository roleRepository;
    private final MstUserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        log.info("Creating new role: {}", request.getRoleName());

        if (roleRepository.existsByRoleName(request.getRoleName())) {
            throw new BadRequestException("Role with name '" + request.getRoleName() + "' already exists");
        }

        MstRole role = MstRole.builder()
                .roleName(request.getRoleName())
                .description(request.getDescription())
                .build();

        MstRole savedRole = roleRepository.save(role);
        log.info("Created role: {} with ID: {}", savedRole.getRoleName(), savedRole.getId());

        return mapToResponse(savedRole);
    }

    @Override
    @Transactional
    public RoleResponse updateRole(UUID roleId, CreateRoleRequest request) {
        log.info("Updating role with ID: {}", roleId);

        MstRole role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));

        // Check if the new name already exists (if name is being changed)
        if (!role.getRoleName().equals(request.getRoleName()) &&
                roleRepository.existsByRoleName(request.getRoleName())) {
            throw new BadRequestException("Role with name '" + request.getRoleName() + "' already exists");
        }

        role.setRoleName(request.getRoleName());
        role.setDescription(request.getDescription());

        MstRole updatedRole = roleRepository.save(role);
        log.info("Updated role: {}", updatedRole.getRoleName());

        return mapToResponse(updatedRole);
    }

    @Override
    public List<RoleResponse> getAllRoles() {
        log.info("Getting all roles");
        List<MstRole> roles = roleRepository.findAll();
        return roles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RoleResponse getRoleById(UUID roleId) {
        log.info("Getting role with ID: {}", roleId);
        MstRole role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));
        return mapToResponse(role);
    }

    @Override
    public MstRole getRoleByName(String roleName) {
        return roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + roleName));
    }

    @Override
    @Transactional
    public void deleteRole(UUID roleId) {
        log.info("Deleting role with ID: {}", roleId);

        MstRole role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));

        // Check if role is assigned to any users
        if (!role.getUserRoles().isEmpty()) {
            throw new BadRequestException("Cannot delete role that is assigned to users. Remove from users first.");
        }

        roleRepository.delete(role);
        log.info("Deleted role: {}", role.getRoleName());
    }

    @Override
    @Transactional
    public UserWithRolesResponse assignRolesToUser(AssignRoleRequest request, UUID assignedByUuid) {
        log.info("Assigning roles to user: {}", request.getUserUuid());

        MstUser user = userRepository.findById(request.getUserUuid())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with UUID: " + request.getUserUuid()));

        // Validate all role IDs exist
        Set<MstRole> roles = new HashSet<>();
        for (UUID roleId : request.getRoleIds()) {
            MstRole role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));
            roles.add(role);
        }

        // Remove existing roles
        userRoleRepository.deleteByUser_Uuid(user.getUuid());

        // Assign new roles
        for (MstRole role : roles) {
            MstUserRole userRole = MstUserRole.builder()
                    .user(user)
                    .role(role)
                    .assignedBy(assignedByUuid)
                    .build();
            userRoleRepository.save(userRole);
        }

        log.info("Assigned {} roles to user: {}", roles.size(), user.getUsername());

        // Fetch updated user with roles
        return getUserWithRoles(user.getUuid());
    }

    @Override
    @Transactional
    public void removeRoleFromUser(UUID userUuid, UUID roleId) {
        log.info("Removing role {} from user {}", roleId, userUuid);

        MstUser user = userRepository.findById(userUuid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with UUID: " + userUuid));

        MstRole role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));

        userRoleRepository.deleteByUser_UuidAndRole_Id(userUuid, roleId);

        log.info("Removed role {} from user {}", role.getRoleName(), user.getUsername());
    }

    @Override
    public List<UserWithRolesResponse> getAllUsersWithRoles() {
        log.info("Getting all users with their roles");
        List<MstUser> users = userRepository.findAll();
        return users.stream()
                .map(this::mapToUserWithRolesResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserWithRolesResponse getUserWithRoles(UUID userUuid) {
        log.info("Getting user with roles for UUID: {}", userUuid);
        MstUser user = userRepository.findById(userUuid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with UUID: " + userUuid));
        return mapToUserWithRolesResponse(user);
    }

    private RoleResponse mapToResponse(MstRole role) {
        return RoleResponse.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .description(role.getDescription())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }

    private UserWithRolesResponse mapToUserWithRolesResponse(MstUser user) {
        Set<RoleResponse> roleResponses = user.getUserRoles().stream()
                .map(userRole -> mapToResponse(userRole.getRole()))
                .collect(Collectors.toSet());

        return UserWithRolesResponse.builder()
                .uuid(user.getUuid())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .department(user.getDepartment())
                .title(user.getTitle())
                .lastLoginAt(user.getLastLoginAt())
                .roles(roleResponses)
                .hasRole(user.hasRole())
                .build();
    }
}
