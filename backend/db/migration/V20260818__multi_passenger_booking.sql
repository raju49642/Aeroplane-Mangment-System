-- Run once against an existing MySQL AMS database before deploying this version.
-- New installations are created by the application's current Hibernate update strategy.
-- Existing ticket rows intentionally retain a NULL passenger_id as historical records.

CREATE TABLE IF NOT EXISTS passengers (
    passenger_id INT NOT NULL AUTO_INCREMENT,
    booking_id INT NOT NULL,
    passenger_name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    PRIMARY KEY (passenger_id),
    INDEX idx_passenger_booking (booking_id),
    CONSTRAINT fk_passenger_booking FOREIGN KEY (booking_id) REFERENCES bookings (booking_id)
);

ALTER TABLE tickets ADD COLUMN IF NOT EXISTS passenger_id INT NULL;
ALTER TABLE tickets ADD CONSTRAINT fk_ticket_passenger
    FOREIGN KEY (passenger_id) REFERENCES passengers (passenger_id);

-- The old one-ticket-per-booking schema created a unique index on booking_id.
-- Discover its generated name and remove only that non-primary unique index.
SET @ticket_booking_unique := (
    SELECT index_name FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'tickets'
      AND column_name = 'booking_id' AND non_unique = 0 AND index_name <> 'PRIMARY'
    LIMIT 1
);
SET @drop_ticket_booking_unique := IF(@ticket_booking_unique IS NULL, 'SELECT 1',
    CONCAT('ALTER TABLE tickets DROP INDEX `', @ticket_booking_unique, '`'));
PREPARE migration_statement FROM @drop_ticket_booking_unique;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

CREATE INDEX IF NOT EXISTS idx_ticket_booking ON tickets (booking_id);
