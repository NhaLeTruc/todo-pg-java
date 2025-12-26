-- V4__create_recurrence_patterns_table.sql
-- Create recurrence_patterns table for recurring tasks

CREATE TYPE frequency_type AS ENUM ('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY');

CREATE TABLE recurrence_patterns (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    frequency frequency_type NOT NULL,
    interval INT NOT NULL DEFAULT 1,
    days_of_week VARCHAR(20),
    day_of_month INT,
    month_of_year INT,
    end_date DATE,
    occurrences INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_recurrence_patterns_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT recurrence_interval_check CHECK (interval > 0),
    CONSTRAINT recurrence_day_of_month_check CHECK (day_of_month IS NULL OR (day_of_month >= 1 AND day_of_month <= 31)),
    CONSTRAINT recurrence_month_of_year_check CHECK (month_of_year IS NULL OR (month_of_year >= 1 AND month_of_year <= 12)),
    CONSTRAINT recurrence_occurrences_check CHECK (occurrences IS NULL OR occurrences > 0)
);

CREATE INDEX idx_recurrence_patterns_user_id ON recurrence_patterns(user_id);
CREATE INDEX idx_recurrence_patterns_frequency ON recurrence_patterns(frequency);

COMMENT ON TABLE recurrence_patterns IS 'Patterns for recurring task generation';
COMMENT ON COLUMN recurrence_patterns.id IS 'Primary key';
COMMENT ON COLUMN recurrence_patterns.user_id IS 'Foreign key to users table';
COMMENT ON COLUMN recurrence_patterns.frequency IS 'Recurrence frequency (DAILY, WEEKLY, MONTHLY, YEARLY)';
COMMENT ON COLUMN recurrence_patterns.interval IS 'Interval between occurrences (e.g., every 2 weeks)';
COMMENT ON COLUMN recurrence_patterns.days_of_week IS 'Comma-separated days (MON,TUE,WED,THU,FRI,SAT,SUN) for WEEKLY frequency';
COMMENT ON COLUMN recurrence_patterns.day_of_month IS 'Day of month (1-31) for MONTHLY frequency';
COMMENT ON COLUMN recurrence_patterns.month_of_year IS 'Month (1-12) for YEARLY frequency';
COMMENT ON COLUMN recurrence_patterns.end_date IS 'Date when recurrence ends (optional)';
COMMENT ON COLUMN recurrence_patterns.occurrences IS 'Maximum number of occurrences (optional)';
