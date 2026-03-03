package com.pcs8.orientasi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class DatabaseConstraintFixer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConstraintFixer.class);
    
    // Valid SQL Server identifier pattern (letters, digits, underscores only)
    private static final Pattern VALID_IDENTIFIER = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");
    
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
                .forEach(this::dropConstraintSafely);
            
            log.info("Database constraint check completed");
        } catch (Exception e) {
            log.warn("Could not check/drop constraints: {}", e.getMessage());
        }
    }
    
    private void dropConstraintSafely(String constraintName) {
        // Validate constraint name to prevent SQL injection
        if (constraintName == null || !VALID_IDENTIFIER.matcher(constraintName).matches()) {
            log.warn("Invalid constraint name, skipping: {}", constraintName);
            return;
        }
        
        try {
            // Use parameterized approach with sp_executesql for safety
            String dropSql = "EXEC sp_executesql N'ALTER TABLE trn_pksi_document DROP CONSTRAINT [' + ? + N']'";
            jdbcTemplate.update(dropSql, constraintName);
            log.info("Dropped constraint: {}", constraintName);
        } catch (Exception e) {
            log.warn("Could not drop constraint {}: {}", constraintName, e.getMessage());
        }
    }
}
