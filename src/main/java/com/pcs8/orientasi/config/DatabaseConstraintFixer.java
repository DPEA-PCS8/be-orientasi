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
    
    // Known constraint name - hardcoded constant, safe from SQL injection
    private static final String KNOWN_CONSTRAINT = "CK__trn_pksi___statu__7F2BE32F";
    
    // Pre-built SQL with hardcoded constraint name - no dynamic input
    private static final String DROP_KNOWN_CONSTRAINT_SQL = """
        IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK__trn_pksi___statu__7F2BE32F')
        BEGIN
            ALTER TABLE trn_pksi_document DROP CONSTRAINT [CK__trn_pksi___statu__7F2BE32F]
        END
        """;
    
    // Static SQL to drop all status constraints - no external input
    private static final String DROP_ALL_STATUS_CONSTRAINTS_SQL = """
        DECLARE @sql NVARCHAR(MAX) = '';
        SELECT @sql = @sql + 'ALTER TABLE trn_pksi_document DROP CONSTRAINT [' + name + ']; '
        FROM sys.check_constraints 
        WHERE parent_object_id = OBJECT_ID('trn_pksi_document')
        AND name LIKE '%statu%';
        IF LEN(@sql) > 0 EXEC sp_executesql @sql;
        """;
    
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
            jdbcTemplate.execute(DROP_KNOWN_CONSTRAINT_SQL);
            log.info("Attempted to drop constraint: {}", KNOWN_CONSTRAINT);
        } catch (Exception e) {
            log.warn("Could not drop constraint {}: {}", KNOWN_CONSTRAINT, e.getMessage());
        }
        
        try {
            jdbcTemplate.execute(DROP_ALL_STATUS_CONSTRAINTS_SQL);
            log.info("Dropped all status-related constraints");
        } catch (Exception e) {
            log.warn("Could not drop status constraints: {}", e.getMessage());
        }
        
        log.info("Database constraint check completed");
    }
}
