package com.ams.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * Hibernate's update mode creates the Passenger table and passenger_id column, but it
 * does not reliably remove the legacy unique index on tickets.booking_id. That old
 * index would silently preserve the one-ticket-per-booking limitation on upgraded
 * MySQL installations. This runs once per startup and only changes that old index.
 */
@Component
public class TicketSchemaMigration implements ApplicationRunner {
    private final DataSource dataSource;
    private final JdbcTemplate jdbc;

    public TicketSchemaMigration(DataSource dataSource, JdbcTemplate jdbc) {
        this.dataSource = dataSource;
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            if (!connection.getMetaData().getDatabaseProductName().toLowerCase().contains("mysql")) {
                return;
            }
        }

        List<Map<String, Object>> indexes = jdbc.queryForList("""
                SELECT INDEX_NAME FROM information_schema.statistics
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tickets'
                  AND COLUMN_NAME = 'booking_id' AND NON_UNIQUE = 0 AND INDEX_NAME <> 'PRIMARY'
                """);
        if (indexes.isEmpty()) return;

        // Preserve the booking foreign-key lookup before removing its unique index.
        Integer ordinaryIndexCount = jdbc.queryForObject("""
                SELECT COUNT(*) FROM information_schema.statistics
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tickets'
                  AND COLUMN_NAME = 'booking_id' AND NON_UNIQUE = 1
                """, Integer.class);
        if (ordinaryIndexCount == null || ordinaryIndexCount == 0) {
            jdbc.execute("CREATE INDEX idx_tickets_booking_multi ON tickets (booking_id)");
        }

        for (Map<String, Object> index : indexes) {
            String name = String.valueOf(index.get("INDEX_NAME"));
            if (name.matches("[A-Za-z0-9_]+")) {
                jdbc.execute("ALTER TABLE tickets DROP INDEX `" + name + "`");
            }
        }
    }
}
