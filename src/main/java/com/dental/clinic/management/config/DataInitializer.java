package com.dental.clinic.management.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.concurrent.TimeUnit;

/**
 * Data Initializer - Loads seed data AFTER Hibernate creates tables
 *
 * EXECUTION ORDER:
 * 1. spring.sql.init runs dental-clinic-seed-data.sql (creates ENUMs only, with
 * continue-on-error=true)
 * 2. Hibernate creates all tables from Entity classes (ddl-auto: update)
 * 3. This PostConstruct bean loads INSERT statements from same SQL file
 *
 * NOTE: This is part of the dental-clinic-seed-data.sql strategy.
 * No additional SQL files are created - we parse the same file and skip CREATE
 * TYPE statements.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    @Autowired
    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initData() {
        try {
            log.info("Starting seed data initialization...");

            // Wait briefly for Hibernate to create schema (some environments are fast, some
            // slower).
            // We check for the existence of a known table (roles) in information_schema and
            // wait up to
            // ~10 seconds before proceeding. This prevents the initializer from querying
            // tables that do
            // not yet exist and failing the whole startup.
            waitForTable("roles", 20, 500);
            // Check if data already exists in multiple tables (avoid duplicate inserts)
            Integer roleCount = safeQueryForInt("SELECT COUNT(*) FROM roles WHERE role_id = 'ROLE_ADMIN'");
            Integer itemCount = safeQueryForInt("SELECT COUNT(*) FROM item_masters");
            Integer serviceCount = safeQueryForInt("SELECT COUNT(*) FROM services");

            // API 6.17: Check service_consumables separately (may need reload)
            Integer consumablesCount = safeQueryForInt("SELECT COUNT(*) FROM service_consumables");

            // FIX: Check invoice_items to prevent duplicate invoice items bug
            Integer invoiceItemsCount = safeQueryForInt("SELECT COUNT(*) FROM invoice_items");

            // Log counts for debugging
            log.info("Data counts: roles={}, items={}, services={}, consumables={}, invoiceItems={}",
                    roleCount, itemCount, serviceCount, consumablesCount, invoiceItemsCount);

            // If ALL tables have data, still run initialization (idempotent with ON
            // CONFLICT DO NOTHING)
            // This ensures dashboard test data and any new seed data always gets loaded
            if (roleCount != null && roleCount > 0 &&
                    itemCount != null && itemCount > 0 &&
                    serviceCount != null && serviceCount > 0 &&
                    consumablesCount != null && consumablesCount > 0 &&
                    invoiceItemsCount != null && invoiceItemsCount > 0) {
                log.info(
                        "Seed data already exists (roles: {}, items: {}, services: {}, consumables: {}, invoiceItems: {}), running initialization to load any new test data",
                        roleCount, itemCount, serviceCount, consumablesCount, invoiceItemsCount);
                // Don't return - continue to load new data with ON CONFLICT DO NOTHING
            }

            // If ANY critical table is empty, reload ALL data
            if (serviceCount != null && serviceCount == 0) {
                log.warn("Services table is empty - will reload ALL seed data");
            }

            if (consumablesCount != null && consumablesCount == 0) {
                log.warn("Service consumables table is empty - will reload ALL seed data for API 6.17");
            }

            if (roleCount != null && roleCount > 0 && (serviceCount == 0 || itemCount == 0)) {
                log.info("Partial seed data detected - will attempt to load missing data...");
            }

            // Read seed data file
            ClassPathResource resource = new ClassPathResource("db/dental-clinic-seed-data.sql");
            String sqlContent;

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                sqlContent = reader.lines().collect(Collectors.joining("\n"));
            }

            // Filter out CREATE TYPE statements, keep only INSERT/UPDATE/DELETE
            // Remove all CREATE TYPE blocks (they're already executed by spring.sql.init)
            String insertOnlyContent = sqlContent.replaceAll("(?i)CREATE\\s+TYPE[^;]+;", "");

            // Remove comment blocks while preserving newlines to avoid breaking multi-line
            // statements
            insertOnlyContent = insertOnlyContent.replaceAll("--[^\n]*\n", "\n");

            // Execute the filtered SQL content as a single script
            int executedCount = 0;
            int skippedCount = 0;

            try {
                // Split by semicolon but preserve multi-line statements
                String[] statements = insertOnlyContent.split(";");

                for (String statement : statements) {
                    String trimmed = statement.trim();

                    // Skip empty statements
                    if (trimmed.isEmpty() || trimmed.length() < 10) {
                        continue;
                    }

                    // Execute DML statements (INSERT, UPDATE, DELETE, SELECT) and constraint fixes
                    // (ALTER TABLE)
                    String upperStatement = trimmed.toUpperCase();
                    if (upperStatement.startsWith("INSERT") ||
                            upperStatement.startsWith("UPDATE") ||
                            upperStatement.startsWith("DELETE") ||
                            upperStatement.startsWith("SELECT") ||
                            upperStatement.startsWith("ALTER SEQUENCE") ||
                            upperStatement.startsWith("ALTER TABLE")) {

                        try {
                            jdbcTemplate.execute(trimmed);
                            executedCount++;

                            // Log first few inserts and service_consumables for verification
                            if (executedCount <= 5 || trimmed.toUpperCase().contains("SERVICE_CONSUMABLES")) {
                                log.debug("Executed: {}", trimmed.substring(0, Math.min(150, trimmed.length())));
                            }
                        } catch (Exception e) {
                            // Log but continue (some statements might fail due to FK constraints - that's
                            // OK)
                            // Log service_consumables failures for debugging API 6.17
                            if (executedCount < 10 || trimmed.toUpperCase().contains("SERVICE_CONSUMABLES")) {
                                log.warn("Failed statement: {}", trimmed.substring(0, Math.min(150, trimmed.length())));
                                log.warn("Error: {}", e.getMessage());
                                log.warn("Root cause: {}",
                                        e.getCause() != null ? e.getCause().getMessage() : "No cause");
                            }
                            skippedCount++;
                        }
                    } else {
                        skippedCount++;
                    }
                }
            } catch (Exception e) {
                log.error("Error processing SQL content", e);
            }

            log.info("Seed data initialization completed: {} statements executed, {} skipped",
                    executedCount, skippedCount);

        } catch (Exception e) {
            log.error("Failed to initialize seed data", e);
            // Don't throw exception - allow server to start even if seed data fails
        }
    }

    /**
     * Safe helper: run a COUNT(*) query and return 0 if the table does not exist or
     * any SQL error occurs.
     */
    private Integer safeQueryForInt(String sql) {
        try {
            Integer v = jdbcTemplate.queryForObject(sql, Integer.class);
            return v == null ? 0 : v;
        } catch (Exception e) {
            // Most common reason: table does not exist yet. Log at debug and return 0 so
            // initialization
            // proceeds gracefully.
            log.debug("safeQueryForInt failed for SQL [{}]: {}", sql, e.getMessage());
            return 0;
        }
    }

    /**
     * Wait for a table to appear in the current schema by polling
     * information_schema.
     * Attempts up to maxAttempts sleeping sleepMillis between attempts.
     */
    private void waitForTable(String tableName, int maxAttempts, long sleepMillis) {
        try {
            int attempts = 0;
            while (attempts < maxAttempts) {
                try {
                    Integer found = jdbcTemplate.queryForObject(
                            "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = current_schema() AND table_name = ?",
                            new Object[] { tableName }, Integer.class);
                    if (found != null && found > 0) {
                        log.debug("Table '{}' found in schema", tableName);
                        return;
                    }
                } catch (Exception e) {
                    // ignore and retry
                    log.trace("Waiting for table '{}' - attempt {}: {}", tableName, attempts + 1, e.getMessage());
                }
                attempts++;
                try {
                    TimeUnit.MILLISECONDS.sleep(sleepMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            log.warn("Table '{}' not found after {} attempts - proceeding anyway (queries will be resilient)",
                    tableName, maxAttempts);
        } catch (Exception e) {
            log.debug("waitForTable encountered error: {}", e.getMessage());
        }
    }
}
