package com.pcs8.orientasi.config;

import com.pcs8.orientasi.domain.entity.MstMenu;
import com.pcs8.orientasi.domain.entity.MstRole;
import com.pcs8.orientasi.domain.entity.MstRolePermission;
import com.pcs8.orientasi.repository.MstMenuRepository;
import com.pcs8.orientasi.repository.MstRolePermissionRepository;
import com.pcs8.orientasi.repository.MstRoleRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Data initializer to create default roles and menus on application startup.
 * This will run once when the application starts.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    // Role constants
    private static final String ROLE_ADMIN = "Admin";

    private final MstRoleRepository roleRepository;
    private final MstMenuRepository menuRepository;
    private final MstRolePermissionRepository rolePermissionRepository;

    // List of obsolete menu codes to deactivate
    private static final Set<String> OBSOLETE_MENU_CODES = new HashSet<>(Arrays.asList(
        "RBSI_LIST", "RBSI_PROGRAM", "RBSI_INISIATIF",
        "MONITORING", "MONITORING_DASHBOARD", "MONITORING_REPORT",
        "PKSI_ADD"
    ));

    @Override
    @Transactional
    public void run(String... args) {
        createRolesIfNotExist();
        deactivateObsoleteMenus();
        createOrUpdateMenus();
        assignAdminPermissionsToAllMenus();
        deactivateObsoleteMenus();
        createOrUpdateMenus();
        assignAdminPermissionsToAllMenus();
    }

    private void createRolesIfNotExist() {
        // 1. SKPA Role - Can submit PKSI proposals and view monitoring (read-only)
        if (!roleRepository.existsByRoleName("SKPA")) {
            MstRole skpaRole = MstRole.builder()
                    .roleName("SKPA")
                    .description("Can submit PKSI proposals (upload and edit) and view monitoring (read-only) for specific fields")
                    .build();

            roleRepository.save(skpaRole);
            log.info("Created role: SKPA");
        }

        // 2. Pengembang Role - Same as SKPA but can also edit monitoring
        if (!roleRepository.existsByRoleName("Pengembang")) {
            MstRole pengembangRole = MstRole.builder()
                    .roleName("Pengembang")
                    .description("Same as SKPA but can also edit monitoring data")
                    .build();

            roleRepository.save(pengembangRole);
            log.info("Created role: Pengembang");
        }

        // 3. Admin Role - Same as Pengembang but can also assign roles and have special admin privileges
        if (!roleRepository.existsByRoleName(ROLE_ADMIN)) {
            MstRole adminRole = MstRole.builder()
                    .roleName(ROLE_ADMIN)
                    .description("Full access - Same as Pengembang but can also assign roles and manage system configuration")
                    .build();

            roleRepository.save(adminRole);
            log.info("Created role: {}", ROLE_ADMIN);
        }
    }

    private void createMenusIfNotExist() {       
        // ========== FEATURES SECTION ==========
        
        // RBSI Parent Menu (Manajemen RBSI)
        MstMenu rbsiMenu = createOrUpdateMenu("RBSI", "Manajemen RBSI", "Menu untuk manajemen RBSI", null, 1);
        
        // RBSI sub-menus - Updated to match current sidebar structure
        createOrUpdateMenu("RBSI_MONITORING", "RBSI Monitoring", "Monitoring RBSI", rbsiMenu, 1);
        createOrUpdateMenu("RBSI_ARCHITECTURE", "RBSI Arsitektur", "Arsitektur RBSI", rbsiMenu, 2);
        
        // PKSI Parent Menu
        MstMenu pksiMenu = createOrUpdateMenu("PKSI", "PKSI", "Menu untuk manajemen PKSI", null, 2);
        
        // PKSI sub-menus
        createOrUpdateMenu("PKSI_ALL", "Semua PKSI", "Daftar semua PKSI", pksiMenu, 1);
        createOrUpdateMenu("PKSI_APPROVED", "PKSI Disetujui", "Daftar PKSI yang sudah disetujui", pksiMenu, 2);
        createOrUpdateMenu("PKSI_ALL", "Semua PKSI", "Daftar semua PKSI", pksiMenu, 1);
        createOrUpdateMenu("PKSI_APPROVED", "PKSI Disetujui", "Daftar PKSI yang sudah disetujui", pksiMenu, 2);

        // ========== ADMIN SECTION ==========
        
        // User & Roles Parent Menu
        MstMenu userRolesMenu = createOrUpdateMenu("USER_ROLES", "User & Roles", "Menu untuk manajemen user dan role", null, 3);
        
        // User & Roles sub-menus
        createOrUpdateMenu("USER_MANAGEMENT", "User Management", "Manajemen user", userRolesMenu, 1);
        createOrUpdateMenu("ROLE_PERMISSIONS", "Role Permissions", "Manajemen permission per role", userRolesMenu, 2);
        
        // Other Admin menus (standalone)
        createOrUpdateMenu("SETTINGS", "Settings", "Pengaturan sistem", null, 4);
        createOrUpdateMenu("AUDIT_LOG", "Audit Log", "Log audit aktivitas sistem", null, 5);
        createOrUpdateMenu("NOTIFICATIONS", "Notifications", "Notifikasi sistem", null, 6);
    }

    /**
     * Alias for createOrUpdateMenu - called by createMenusIfNotExist
     */
    private void createOrUpdateMenus() {
        createMenusIfNotExist();
    }

    /**
     * Deactivate obsolete menus that are no longer used
     */
    private void deactivateObsoleteMenus() {
        for (String menuCode : OBSOLETE_MENU_CODES) {
            Optional<MstMenu> menuOpt = menuRepository.findByMenuCode(menuCode);
            if (menuOpt.isPresent()) {
                MstMenu menu = menuOpt.get();
                if (menu.getIsActive()) {
                    menu.setIsActive(false);
                    menuRepository.save(menu);
                    log.info("Deactivated obsolete menu: {}", menuCode);
                }
            }
        }
    }

    /**
     * Create or update a menu
     */
    private MstMenu createOrUpdateMenu(String menuCode, String menuName, String description, MstMenu parent, int displayOrder) {
        Optional<MstMenu> existingMenuOpt = menuRepository.findByMenuCode(menuCode);
        
        if (existingMenuOpt.isEmpty()) {
            return createNewMenu(menuCode, menuName, description, parent, displayOrder);
        }
        
        return updateExistingMenu(existingMenuOpt.get(), menuCode, menuName, description, parent, displayOrder);
    }

    /**
     * Create a new menu
     */
    private MstMenu createNewMenu(String menuCode, String menuName, String description, MstMenu parent, int displayOrder) {
        MstMenu menu = MstMenu.builder()
                .menuCode(menuCode)
                .menuName(menuName)
                .description(description)
                .parent(parent)
                .displayOrder(displayOrder)
                .isActive(true)
                .build();

        MstMenu savedMenu = menuRepository.save(menu);
        log.info("Created menu: {}", menuCode);
        return savedMenu;
    }

    /**
     * Update an existing menu if changes are detected
     */
    private MstMenu updateExistingMenu(MstMenu existingMenu, String menuCode, String menuName, String description, MstMenu parent, int displayOrder) {
        boolean updated = false;
        
        updated |= updateMenuName(existingMenu, menuName);
        updated |= updateMenuDescription(existingMenu, description);
        updated |= updateMenuDisplayOrder(existingMenu, displayOrder);
        updated |= updateMenuActiveStatus(existingMenu);
        updated |= updateMenuParent(existingMenu, parent);
        
        if (updated) {
            MstMenu savedMenu = menuRepository.save(existingMenu);
            log.info("Updated menu: {}", menuCode);
            return savedMenu;
        }
        return existingMenu;
    }

    private boolean updateMenuName(MstMenu menu, String menuName) {
        if (!menu.getMenuName().equals(menuName)) {
            menu.setMenuName(menuName);
            return true;
        }
        return false;
    }

    private boolean updateMenuDescription(MstMenu menu, String description) {
        if (description != null && !description.equals(menu.getDescription())) {
            menu.setDescription(description);
            return true;
        }
        return false;
    }

    private boolean updateMenuDisplayOrder(MstMenu menu, int displayOrder) {
        if (menu.getDisplayOrder() != displayOrder) {
            menu.setDisplayOrder(displayOrder);
            return true;
        }
        return false;
    }

    private boolean updateMenuActiveStatus(MstMenu menu) {
        if (!menu.getIsActive()) {
            menu.setIsActive(true);
            return true;
        }
        return false;
    }

    private boolean updateMenuParent(MstMenu menu, MstMenu parent) {
        UUID currentParentId = menu.getParent() != null ? menu.getParent().getId() : null;
        UUID newParentId = parent != null ? parent.getId() : null;
        
        if (!Objects.equals(currentParentId, newParentId)) {
            menu.setParent(parent);
            return true;
        }
        return false;
    }

    /**
     * Ensure Admin role has full permissions on all active menus
     */
    private void assignAdminPermissionsToAllMenus() {
        Optional<MstRole> adminRoleOpt = roleRepository.findByRoleName(ROLE_ADMIN);
        if (adminRoleOpt.isEmpty()) {
            log.warn("Admin role not found - skipping permission assignment");
            return;
        }

        MstRole adminRole = adminRoleOpt.get();
        List<MstMenu> allActiveMenus = menuRepository.findAllActiveMenus();

        int[] counts = {0, 0}; // [created, updated]

        for (MstMenu menu : allActiveMenus) {
            processAdminPermissionForMenu(adminRole, menu, counts);
        }

        logPermissionStats(counts[0], counts[1]);
    }

    /**
     * Process admin permission for a single menu
     */
    private void processAdminPermissionForMenu(MstRole adminRole, MstMenu menu, int[] counts) {
        Optional<MstRolePermission> existingPermOpt = rolePermissionRepository
                .findByRoleIdAndMenuId(adminRole.getId(), menu.getId());

        if (existingPermOpt.isPresent()) {
            if (updatePermissionToFullAccess(existingPermOpt.get())) {
                counts[1]++;
            }
        } else {
            createFullAccessPermission(adminRole, menu);
            counts[0]++;
        }
    }

    /**
     * Update permission to full access if needed
     */
    private boolean updatePermissionToFullAccess(MstRolePermission perm) {
        boolean needsUpdate = !perm.getCanView() || !perm.getCanCreate() 
                || !perm.getCanUpdate() || !perm.getCanDelete();
        
        if (needsUpdate) {
            perm.setCanView(true);
            perm.setCanCreate(true);
            perm.setCanUpdate(true);
            perm.setCanDelete(true);
            rolePermissionRepository.save(perm);
        }
        return needsUpdate;
    }

    /**
     * Create new permission with full access
     */
    private void createFullAccessPermission(MstRole role, MstMenu menu) {
        MstRolePermission newPerm = MstRolePermission.builder()
                .role(role)
                .menu(menu)
                .canView(true)
                .canCreate(true)
                .canUpdate(true)
                .canDelete(true)
                .build();
        rolePermissionRepository.save(newPerm);
    }

    /**
     * Log permission statistics if any changes were made
     */
    private void logPermissionStats(int created, int updated) {
        if (created > 0 || updated > 0) {
            log.info("Admin permissions - Created: {}, Updated: {}", created, updated);
        }
    }
}
