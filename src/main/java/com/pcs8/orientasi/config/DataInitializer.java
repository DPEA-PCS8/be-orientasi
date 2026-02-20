package com.pcs8.orientasi.config;

import com.pcs8.orientasi.domain.entity.MstRole;
import com.pcs8.orientasi.repository.MstRoleRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Data initializer to create default roles on application startup.
 * This will run once when the application starts.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final MstRoleRepository roleRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initializing default roles...");

        try {
            createRolesIfNotExist();
            log.info("Default roles initialized successfully");
        } catch (Exception e) {
            log.error("Error initializing default roles: {}", e.getMessage(), e);
        }
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
}
