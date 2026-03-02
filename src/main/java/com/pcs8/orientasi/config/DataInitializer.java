package com.pcs8.orientasi.config;

import com.pcs8.orientasi.domain.entity.MstMenu;
import com.pcs8.orientasi.domain.entity.MstRole;
import com.pcs8.orientasi.repository.MstMenuRepository;
import com.pcs8.orientasi.repository.MstRoleRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Data initializer to create default roles and menus on application startup.
 * This will run once when the application starts.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final MstRoleRepository roleRepository;
    private final MstMenuRepository menuRepository;

    @Override
    @Transactional
    public void run(String... args) {
        createRolesIfNotExist();
        createMenusIfNotExist();
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
        if (!roleRepository.existsByRoleName("Admin")) {
            MstRole adminRole = MstRole.builder()
                    .roleName("Admin")
                    .description("Full access - Same as Pengembang but can also assign roles and manage system configuration")
                    .build();

            roleRepository.save(adminRole);
            log.info("Created role: Admin");
        }
    }

    private void createMenusIfNotExist() {
        // Parent menus
        MstMenu pksiMenu = createMenuIfNotExist("PKSI", "PKSI", "Menu untuk manajemen PKSI", null, 1);
        MstMenu rbsiMenu = createMenuIfNotExist("RBSI", "RBSI", "Menu untuk manajemen RBSI", null, 2);
        MstMenu monitoringMenu = createMenuIfNotExist("MONITORING", "Monitoring", "Menu untuk monitoring", null, 3);
        MstMenu userRolesMenu = createMenuIfNotExist("USER_ROLES", "User & Roles", "Menu untuk manajemen user dan role", null, 4);

        // PKSI sub-menus
        createMenuIfNotExist("PKSI_ALL", "Semua PKSI", "Daftar semua PKSI", pksiMenu, 1);
        createMenuIfNotExist("PKSI_APPROVED", "PKSI Disetujui", "Daftar PKSI yang sudah disetujui", pksiMenu, 2);
        createMenuIfNotExist("PKSI_ADD", "Tambah PKSI", "Form untuk menambah PKSI baru", pksiMenu, 3);

        // RBSI sub-menus
        createMenuIfNotExist("RBSI_LIST", "Daftar RBSI", "Daftar semua RBSI", rbsiMenu, 1);
        createMenuIfNotExist("RBSI_PROGRAM", "Program RBSI", "Daftar program dalam RBSI", rbsiMenu, 2);
        createMenuIfNotExist("RBSI_INISIATIF", "Inisiatif RBSI", "Daftar inisiatif dalam program RBSI", rbsiMenu, 3);

        // Monitoring sub-menus
        createMenuIfNotExist("MONITORING_DASHBOARD", "Dashboard", "Dashboard monitoring", monitoringMenu, 1);
        createMenuIfNotExist("MONITORING_REPORT", "Laporan", "Laporan monitoring", monitoringMenu, 2);

        // User & Roles sub-menus
        createMenuIfNotExist("USER_MANAGEMENT", "User Management", "Manajemen user", userRolesMenu, 1);
        createMenuIfNotExist("ROLE_PERMISSIONS", "Role Permissions", "Manajemen permission per role", userRolesMenu, 2);
    }

    private MstMenu createMenuIfNotExist(String menuCode, String menuName, String description, MstMenu parent, int displayOrder) {
        if (!menuRepository.existsByMenuCode(menuCode)) {
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
        return menuRepository.findByMenuCode(menuCode).orElse(null);
    }
}
