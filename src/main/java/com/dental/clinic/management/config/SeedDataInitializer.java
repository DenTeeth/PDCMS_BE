package com.dental.clinic.management.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Seed Data Initializer
 * 
 * Automatically loads seed data from dental-clinic-seed-data.sql on application startup.
 * This ensures production database always has the latest permissions, roles, and master data.
 * 
 * Features:
 * - Runs AFTER Hibernate creates tables
 * - Safe to run multiple times (seed SQL should use ON CONFLICT DO NOTHING)
 * - Can be disabled via environment variable: SEED_DATA_ENABLED=false
 * 
 * Execution Order:
 * 1. Spring SQL Init: enums.sql (creates ENUMs)
 * 2. Hibernate: Creates/updates tables (ddl-auto: update)
 * 3. This Component: Loads seed data (INSERT statements)
 */
@Slf4j
@Component
public class SeedDataInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    
    @Value("${app.seed-data.enabled:true}")
    private boolean seedDataEnabled;
    
    @Value("${app.seed-data.file:db/dental-clinic-seed-data.sql}")
    private String seedDataFile;

    public SeedDataInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!seedDataEnabled) {
            log.info("Seed data loading is DISABLED (app.seed-data.enabled=false)");
            return;
        }

        try {
            log.info("========================================");
            log.info("Starting seed data initialization...");
            log.info("========================================");

            // Check if seed data already exists
            Long permissionCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM permissions", Long.class);
            
            log.info("Current permissions count: {}", permissionCount);

            // Load seed data file
            ClassPathResource resource = new ClassPathResource(seedDataFile);
            if (!resource.exists()) {
                log.warn("Seed data file not found: {}", seedDataFile);
                return;
            }

            log.info("Loading seed data from: {}", seedDataFile);
            
            String sql;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                sql = reader.lines().collect(Collectors.joining("\n"));
            }

            // Remove comments and split by semicolon
            String[] statements = sql
                .replaceAll("--.*", "") // Remove single-line comments
                .replaceAll("/\\*[\\s\\S]*?\\*/", "") // Remove multi-line comments
                .split(";");

            int successCount = 0;
            int skipCount = 0;
            int errorCount = 0;

            for (String statement : statements) {
                String trimmed = statement.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }

                try {
                    jdbcTemplate.execute(trimmed);
                    successCount++;
                } catch (Exception e) {
                    // If it's a duplicate key error, it's expected (data already exists)
                    if (e.getMessage().contains("duplicate key") || 
                        e.getMessage().contains("already exists")) {
                        skipCount++;
                        log.debug("Skipped existing data: {}", e.getMessage());
                    } else {
                        errorCount++;
                        log.error("Error executing SQL: {}", trimmed.substring(0, Math.min(100, trimmed.length())));
                        log.error("Error details: {}", e.getMessage());
                    }
                }
            }

            // Show final stats
            Long finalPermissionCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM permissions", Long.class);
            Long roleCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM roles", Long.class);
            Long rolePermissionCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM role_permissions", Long.class);

            log.info("========================================");
            log.info("Seed data initialization completed!");
            log.info("========================================");
            log.info("Statements executed: {} success, {} skipped, {} errors", 
                successCount, skipCount, errorCount);
            log.info("Database state:");
            log.info("  - Permissions: {}", finalPermissionCount);
            log.info("  - Roles: {}", roleCount);
            log.info("  - Role-Permissions: {}", rolePermissionCount);
            log.info("========================================");

        } catch (Exception e) {
            log.error("========================================");
            log.error("Failed to load seed data!");
            log.error("========================================");
            log.error("Error: ", e);
            // Don't throw exception - let app start even if seed data fails
        }
    }
}
