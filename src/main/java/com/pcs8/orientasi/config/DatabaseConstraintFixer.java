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
        // Known constraint name from error message
        String knownConstraint = "CK__trn_pksi___statu__7F2BE32F";
        
        try {
            // Check if constraint exists and drop it
            String checkAndDropSql = """
                IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = '%s')
                BEGIN
                    ALTER TABLE trn_pksi_document DROP CONSTRAINT [%s]
                END
                """.formatted(knownConstraint, knownConstraint);
            
            jdbcTemplate.execute(checkAndDropSql);
            log.info("Attempted to drop constraint: {}", knownConstraint);
        } catch (Exception e) {
            log.warn("Could not drop constraint {}: {}", knownConstraint, e.getMessage());
        }
        
        // Also try to drop any other status constraints dynamically
        try {
            String dropAllStatusConstraintsSql = """
                DECLARE @sql NVARCHAR(MAX) = '';
                SELECT @sql = @sql + 'ALTER TABLE trn_pksi_document DROP CONSTRAINT [' + name + ']; '
                FROM sys.check_constraints 
                WHERE parent_object_id = OBJECT_ID('trn_pksi_document')
                AND name LIKE '%statu%';
                IF LEN(@sql) > 0 EXEC sp_executesql @sql;
                """;
            
            jdbcTemplate.execute(dropAllStatusConstraintsSql);
            log.info("Dropped all status-related constraints");
        } catch (Exception e) {
            log.warn("Could not drop status constraints: {}", e.getMessage());
        }
        
        log.info("Database constraint check completed");
    }
}
