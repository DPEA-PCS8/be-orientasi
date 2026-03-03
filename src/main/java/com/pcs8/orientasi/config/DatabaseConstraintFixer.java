package com.pcs8.orientasi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseConstraintFixer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConstraintFixer.class);
    
    private final JdbcTemplate jdbcTemplate;

    public DatabaseConstraintFixer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        dropStatusCheckConstraints();
    }

    private void dropStatusCheckConstraints() {
        try {
            // Find and drop all check constraints on status column
            String findConstraintsSql = """
                SELECT name FROM sys.check_constraints 
                WHERE parent_object_id = OBJECT_ID('trn_pksi_document')
                """;
            
            jdbcTemplate.queryForList(findConstraintsSql, String.class)
                .forEach(constraintName -> {
                    try {
                        String dropSql = "ALTER TABLE trn_pksi_document DROP CONSTRAINT " + constraintName;
                        jdbcTemplate.execute(dropSql);
                        log.info("Dropped constraint: {}", constraintName);
                    } catch (Exception e) {
                        log.warn("Could not drop constraint {}: {}", constraintName, e.getMessage());
                    }
                });
            
            log.info("Database constraint check completed");
        } catch (Exception e) {
            log.warn("Could not check/drop constraints: {}", e.getMessage());
        }
    }
}
