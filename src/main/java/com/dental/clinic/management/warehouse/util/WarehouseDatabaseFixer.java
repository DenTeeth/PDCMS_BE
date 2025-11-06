package com.dental.clinic.management.warehouse.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * One-time database fix for warehouse tables.
 * Run once then delete this class.
 */
@Component
@Slf4j
public class WarehouseDatabaseFixer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            log.info("Fixing warehouse database schema...");

            // Drop old suppliers table (bigint -> uuid migration)
            jdbcTemplate.execute("DROP TABLE IF EXISTS item_suppliers CASCADE");
            log.info("Dropped item_suppliers table");

            jdbcTemplate.execute("DROP TABLE IF EXISTS suppliers CASCADE");
            log.info("Dropped suppliers table");

            log.info("✅ Database fix completed! Tables will be recreated by Hibernate.");
            log.info("⚠️ Please DELETE this WarehouseDatabaseFixer.java file after successful run!");

        } catch (Exception e) {
            log.error("Failed to fix database: {}", e.getMessage());
        }
    }
}
