package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.domain.dto.request.BulkRolePermissionRequest;
import com.pcs8.orientasi.domain.dto.request.CreateMenuRequest;
import com.pcs8.orientasi.domain.dto.request.RolePermissionRequest;
import com.pcs8.orientasi.domain.dto.response.MenuResponse;
import com.pcs8.orientasi.domain.dto.response.RolePermissionMatrixResponse;
import com.pcs8.orientasi.domain.dto.response.RolePermissionResponse;
import com.pcs8.orientasi.domain.entity.MstMenu;
import com.pcs8.orientasi.domain.entity.MstRole;
import com.pcs8.orientasi.domain.entity.MstRolePermission;
import com.pcs8.orientasi.exception.BadRequestException;
import com.pcs8.orientasi.exception.ResourceNotFoundException;
import com.pcs8.orientasi.repository.MstMenuRepository;
import com.pcs8.orientasi.repository.MstRolePermissionRepository;
import com.pcs8.orientasi.repository.MstRoleRepository;
import com.pcs8.orientasi.service.RolePermissionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RolePermissionServiceImpl implements RolePermissionService {

    private static final Logger log = LoggerFactory.getLogger(RolePermissionServiceImpl.class);

    private final MstMenuRepository menuRepository;
    private final MstRoleRepository roleRepository;
    private final MstRolePermissionRepository rolePermissionRepository;

    // ========== MENU MANAGEMENT ==========

    @Override
    @Transactional
    public MenuResponse createMenu(CreateMenuRequest request) {
        log.info("Creating menu: {}", request.getMenuCode());

        if (menuRepository.existsByMenuCode(request.getMenuCode())) {
            throw new BadRequestException("Menu with code '" + request.getMenuCode() + "' already exists");
        }

        MstMenu parent = null;
        if (request.getParentId() != null) {
            parent = menuRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent menu not found with ID: " + request.getParentId()));
        }

        MstMenu menu = MstMenu.builder()
                .menuCode(request.getMenuCode())
                .menuName(request.getMenuName())
                .description(request.getDescription())
                .parent(parent)
                .displayOrder(request.getDisplayOrder())
                .isActive(request.getIsActive())
                .build();

        MstMenu savedMenu = menuRepository.save(menu);
        log.info("Created menu: {} with ID: {}", savedMenu.getMenuCode(), savedMenu.getId());

        // Auto-assign full permissions to Admin role for new menu
        autoAssignAdminPermissions(savedMenu);

        return mapToMenuResponse(savedMenu, false);
    }

    /**
     * Auto-assign full permissions (view, create, update, delete) to Admin role for a menu
     */
    private void autoAssignAdminPermissions(MstMenu menu) {
        Optional<MstRole> adminRole = roleRepository.findByRoleName("Admin");
        if (adminRole.isPresent()) {
            MstRole admin = adminRole.get();
            
            // Check if permission already exists
            Optional<MstRolePermission> existingPermission = rolePermissionRepository
                    .findByRoleIdAndMenuId(admin.getId(), menu.getId());
            
            if (existingPermission.isEmpty()) {
                MstRolePermission permission = MstRolePermission.builder()
                        .role(admin)
                        .menu(menu)
                        .canView(true)
                        .canCreate(true)
                        .canUpdate(true)
                        .canDelete(true)
                        .build();
                rolePermissionRepository.save(permission);
                log.info("Auto-assigned full permissions to Admin role for menu: {}", menu.getMenuCode());
            }
        } else {
            log.warn("Admin role not found - skipping auto-assign permissions for menu: {}", menu.getMenuCode());
        }
    }

    @Override
    @Transactional
    public MenuResponse updateMenu(UUID menuId, CreateMenuRequest request) {
        log.info("Updating menu: {}", menuId);

        MstMenu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found with ID: " + menuId));

        // Check if new menu code already exists (if changed)
        if (!menu.getMenuCode().equals(request.getMenuCode()) &&
                menuRepository.existsByMenuCode(request.getMenuCode())) {
            throw new BadRequestException("Menu with code '" + request.getMenuCode() + "' already exists");
        }

        MstMenu parent = null;
        if (request.getParentId() != null) {
            if (request.getParentId().equals(menuId)) {
                throw new BadRequestException("Menu cannot be its own parent");
            }
            parent = menuRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent menu not found with ID: " + request.getParentId()));
        }

        menu.setMenuCode(request.getMenuCode());
        menu.setMenuName(request.getMenuName());
        menu.setDescription(request.getDescription());
        menu.setParent(parent);
        menu.setDisplayOrder(request.getDisplayOrder());
        menu.setIsActive(request.getIsActive());

        MstMenu updatedMenu = menuRepository.save(menu);
        log.info("Updated menu: {}", updatedMenu.getMenuCode());

        return mapToMenuResponse(updatedMenu, false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuResponse> getAllMenus() {
        log.info("Getting all menus");
        List<MstMenu> menus = menuRepository.findAllActiveMenus();
        return menus.stream()
                .map(m -> mapToMenuResponse(m, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuResponse> getMenuTree() {
        log.info("Getting menu tree");
        List<MstMenu> rootMenus = menuRepository.findAllRootMenus();
        return rootMenus.stream()
                .map(m -> mapToMenuResponse(m, true))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MenuResponse getMenuById(UUID menuId) {
        log.info("Getting menu: {}", menuId);
        MstMenu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found with ID: " + menuId));
        return mapToMenuResponse(menu, true);
    }

    @Override
    @Transactional
    public void deleteMenu(UUID menuId) {
        log.info("Deleting menu: {}", menuId);
        MstMenu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found with ID: " + menuId));

        // Check if menu has children
        if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
            throw new BadRequestException("Cannot delete menu with children. Delete children first.");
        }

        menuRepository.delete(menu);
        log.info("Deleted menu: {}", menu.getMenuCode());
    }

    // ========== ROLE PERMISSION MANAGEMENT ==========

    @Override
    @Transactional
    public RolePermissionResponse createOrUpdatePermission(RolePermissionRequest request) {
        log.info("Creating/updating permission for role: {} on menu: {}", request.getRoleId(), request.getMenuId());

        MstRole role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + request.getRoleId()));

        MstMenu menu = menuRepository.findById(request.getMenuId())
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found with ID: " + request.getMenuId()));

        Optional<MstRolePermission> existingPermission = rolePermissionRepository
                .findByRoleIdAndMenuId(request.getRoleId(), request.getMenuId());

        MstRolePermission permission;
        if (existingPermission.isPresent()) {
            permission = existingPermission.get();
            permission.setCanView(request.getCanView());
            permission.setCanCreate(request.getCanCreate());
            permission.setCanUpdate(request.getCanUpdate());
            permission.setCanDelete(request.getCanDelete());
        } else {
            permission = MstRolePermission.builder()
                    .role(role)
                    .menu(menu)
                    .canView(request.getCanView())
                    .canCreate(request.getCanCreate())
                    .canUpdate(request.getCanUpdate())
                    .canDelete(request.getCanDelete())
                    .build();
        }

        MstRolePermission savedPermission = rolePermissionRepository.save(permission);
        log.info("Saved permission for role: {} on menu: {}", role.getRoleName(), menu.getMenuCode());

        return mapToRolePermissionResponse(savedPermission);
    }

    @Override
    @Transactional
    public List<RolePermissionResponse> bulkUpdatePermissions(BulkRolePermissionRequest request) {
        log.info("Bulk updating permissions for role: {}", request.getRoleId());

        MstRole role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + request.getRoleId()));

        List<RolePermissionResponse> results = new ArrayList<>();

        for (BulkRolePermissionRequest.MenuPermission menuPerm : request.getPermissions()) {
            MstMenu menu = menuRepository.findById(menuPerm.getMenuId())
                    .orElseThrow(() -> new ResourceNotFoundException("Menu not found with ID: " + menuPerm.getMenuId()));

            Optional<MstRolePermission> existingPermission = rolePermissionRepository
                    .findByRoleIdAndMenuId(request.getRoleId(), menuPerm.getMenuId());

            MstRolePermission permission;
            if (existingPermission.isPresent()) {
                permission = existingPermission.get();
                permission.setCanView(menuPerm.getCanView());
                permission.setCanCreate(menuPerm.getCanCreate());
                permission.setCanUpdate(menuPerm.getCanUpdate());
                permission.setCanDelete(menuPerm.getCanDelete());
            } else {
                permission = MstRolePermission.builder()
                        .role(role)
                        .menu(menu)
                        .canView(menuPerm.getCanView())
                        .canCreate(menuPerm.getCanCreate())
                        .canUpdate(menuPerm.getCanUpdate())
                        .canDelete(menuPerm.getCanDelete())
                        .build();
            }

            MstRolePermission savedPermission = rolePermissionRepository.save(permission);
            results.add(mapToRolePermissionResponse(savedPermission));
        }

        log.info("Bulk updated {} permissions for role: {}", results.size(), role.getRoleName());
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolePermissionResponse> getPermissionsByRole(UUID roleId) {
        log.info("Getting permissions for role: {}", roleId);

        if (!roleRepository.existsById(roleId)) {
            throw new ResourceNotFoundException("Role not found with ID: " + roleId);
        }

        List<MstRolePermission> permissions = rolePermissionRepository.findByRoleId(roleId);
        return permissions.stream()
                .map(this::mapToRolePermissionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RolePermissionMatrixResponse getPermissionMatrix(UUID roleId) {
        log.info("Getting permission matrix for role: {}", roleId);

        MstRole role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));

        List<MstMenu> allMenus = menuRepository.findAllActiveMenus();
        List<MstRolePermission> rolePermissions = rolePermissionRepository.findByRoleId(roleId);

        List<RolePermissionMatrixResponse.MenuPermissionItem> menuPermissions = new ArrayList<>();

        for (MstMenu menu : allMenus) {
            Optional<MstRolePermission> permission = rolePermissions.stream()
                    .filter(rp -> rp.getMenu().getId().equals(menu.getId()))
                    .findFirst();

            RolePermissionMatrixResponse.MenuPermissionItem item = RolePermissionMatrixResponse.MenuPermissionItem.builder()
                    .menuId(menu.getId())
                    .menuCode(menu.getMenuCode())
                    .menuName(menu.getMenuName())
                    .parentId(menu.getParent() != null ? menu.getParent().getId() : null)
                    .parentName(menu.getParent() != null ? menu.getParent().getMenuName() : null)
                    .displayOrder(menu.getDisplayOrder())
                    .canView(permission.map(MstRolePermission::getCanView).orElse(false))
                    .canCreate(permission.map(MstRolePermission::getCanCreate).orElse(false))
                    .canUpdate(permission.map(MstRolePermission::getCanUpdate).orElse(false))
                    .canDelete(permission.map(MstRolePermission::getCanDelete).orElse(false))
                    .build();

            menuPermissions.add(item);
        }

        // Sort by parent first, then by display order
        menuPermissions.sort(Comparator
                .comparing((RolePermissionMatrixResponse.MenuPermissionItem item) -> item.getParentId() != null ? 1 : 0)
                .thenComparing(RolePermissionMatrixResponse.MenuPermissionItem::getDisplayOrder));

        return RolePermissionMatrixResponse.builder()
                .roleId(role.getId())
                .roleName(role.getRoleName())
                .roleDescription(role.getDescription())
                .menuPermissions(menuPermissions)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolePermissionMatrixResponse> getAllPermissionMatrices() {
        log.info("Getting all permission matrices");

        List<MstRole> allRoles = roleRepository.findAll();
        return allRoles.stream()
                .map(role -> getPermissionMatrix(role.getId()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deletePermission(UUID roleId, UUID menuId) {
        log.info("Deleting permission for role: {} on menu: {}", roleId, menuId);

        if (!roleRepository.existsById(roleId)) {
            throw new ResourceNotFoundException("Role not found with ID: " + roleId);
        }

        if (!menuRepository.existsById(menuId)) {
            throw new ResourceNotFoundException("Menu not found with ID: " + menuId);
        }

        rolePermissionRepository.deleteByRoleIdAndMenuId(roleId, menuId);
        log.info("Deleted permission for role: {} on menu: {}", roleId, menuId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(String roleName, String menuCode, String permissionType) {
        log.debug("Checking permission: role={}, menu={}, type={}", roleName, menuCode, permissionType);

        Optional<MstRolePermission> permission = rolePermissionRepository
                .findByRoleNameAndMenuCode(roleName, menuCode);

        if (permission.isEmpty()) {
            return false;
        }

        MstRolePermission rp = permission.get();
        return switch (permissionType.toLowerCase()) {
            case "view" -> rp.getCanView();
            case "create" -> rp.getCanCreate();
            case "update" -> rp.getCanUpdate();
            case "delete" -> rp.getCanDelete();
            default -> false;
        };
    }

    @Override
    @Transactional(readOnly = true)
    public RolePermissionMatrixResponse getCombinedPermissionsForRoles(List<String> roleNames) {
        log.info("Getting combined permissions for roles: {}", roleNames);

        // Check if any role is Admin - Admin has full access
        boolean isAdmin = roleNames.stream()
                .anyMatch(roleName -> roleName.equalsIgnoreCase("Admin"));

        List<MstMenu> allMenus = menuRepository.findAllActiveMenus();
        List<RolePermissionMatrixResponse.MenuPermissionItem> menuPermissions = new ArrayList<>();

        if (isAdmin) {
            // Admin has full access to all menus
            for (MstMenu menu : allMenus) {
                RolePermissionMatrixResponse.MenuPermissionItem item = RolePermissionMatrixResponse.MenuPermissionItem.builder()
                        .menuId(menu.getId())
                        .menuCode(menu.getMenuCode())
                        .menuName(menu.getMenuName())
                        .parentId(menu.getParent() != null ? menu.getParent().getId() : null)
                        .parentName(menu.getParent() != null ? menu.getParent().getMenuName() : null)
                        .displayOrder(menu.getDisplayOrder())
                        .canView(true)
                        .canCreate(true)
                        .canUpdate(true)
                        .canDelete(true)
                        .build();
                menuPermissions.add(item);
            }
        } else {
            // Get combined permissions from all user roles
            List<MstRole> roles = roleNames.stream()
                    .map(roleRepository::findByRoleName)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();

            for (MstMenu menu : allMenus) {
                boolean canView = false;
                boolean canCreate = false;
                boolean canUpdate = false;
                boolean canDelete = false;

                // Combine permissions from all roles (OR logic)
                for (MstRole role : roles) {
                    Optional<MstRolePermission> permission = rolePermissionRepository
                            .findByRoleIdAndMenuId(role.getId(), menu.getId());
                    
                    if (permission.isPresent()) {
                        MstRolePermission rp = permission.get();
                        canView = canView || rp.getCanView();
                        canCreate = canCreate || rp.getCanCreate();
                        canUpdate = canUpdate || rp.getCanUpdate();
                        canDelete = canDelete || rp.getCanDelete();
                    }
                }

                RolePermissionMatrixResponse.MenuPermissionItem item = RolePermissionMatrixResponse.MenuPermissionItem.builder()
                        .menuId(menu.getId())
                        .menuCode(menu.getMenuCode())
                        .menuName(menu.getMenuName())
                        .parentId(menu.getParent() != null ? menu.getParent().getId() : null)
                        .parentName(menu.getParent() != null ? menu.getParent().getMenuName() : null)
                        .displayOrder(menu.getDisplayOrder())
                        .canView(canView)
                        .canCreate(canCreate)
                        .canUpdate(canUpdate)
                        .canDelete(canDelete)
                        .build();
                menuPermissions.add(item);
            }
        }

        // Sort by parent first, then by display order
        menuPermissions.sort(Comparator
                .comparing((RolePermissionMatrixResponse.MenuPermissionItem item) -> item.getParentId() != null ? 1 : 0)
                .thenComparing(RolePermissionMatrixResponse.MenuPermissionItem::getDisplayOrder));

        return RolePermissionMatrixResponse.builder()
                .roleId(null) // Combined roles, no single role ID
                .roleName(String.join(", ", roleNames))
                .roleDescription("Combined permissions for user roles")
                .menuPermissions(menuPermissions)
                .build();
    }

    // ========== MAPPING METHODS ==========

    private MenuResponse mapToMenuResponse(MstMenu menu, boolean includeChildren) {
        List<MenuResponse> children = null;
        if (includeChildren && menu.getChildren() != null && !menu.getChildren().isEmpty()) {
            children = menu.getChildren().stream()
                    .filter(MstMenu::getIsActive)
                    .sorted(Comparator.comparing(MstMenu::getDisplayOrder))
                    .map(child -> mapToMenuResponse(child, true))
                    .collect(Collectors.toList());
        }

        return MenuResponse.builder()
                .id(menu.getId())
                .menuCode(menu.getMenuCode())
                .menuName(menu.getMenuName())
                .description(menu.getDescription())
                .parentId(menu.getParent() != null ? menu.getParent().getId() : null)
                .parentName(menu.getParent() != null ? menu.getParent().getMenuName() : null)
                .displayOrder(menu.getDisplayOrder())
                .isActive(menu.getIsActive())
                .children(children)
                .createdAt(menu.getCreatedAt())
                .updatedAt(menu.getUpdatedAt())
                .build();
    }

    private RolePermissionResponse mapToRolePermissionResponse(MstRolePermission permission) {
        return RolePermissionResponse.builder()
                .id(permission.getId())
                .roleId(permission.getRole().getId())
                .roleName(permission.getRole().getRoleName())
                .menuId(permission.getMenu().getId())
                .menuCode(permission.getMenu().getMenuCode())
                .menuName(permission.getMenu().getMenuName())
                .canView(permission.getCanView())
                .canCreate(permission.getCanCreate())
                .canUpdate(permission.getCanUpdate())
                .canDelete(permission.getCanDelete())
                .createdAt(permission.getCreatedAt())
                .updatedAt(permission.getUpdatedAt())
                .build();
    }
}