-- V9__create_time_entries_table.sql
-- Create time_entries table for time tracking

CREATE TYPE entry_type AS ENUM ('MANUAL', 'TIMER');

CREATE TABLE time_entries (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    entry_type entry_type NOT NULL DEFAULT 'MANUAL',
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    duration_minutes INT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_time_entries_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_time_entries_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT time_entries_end_after_start CHECK (end_time IS NULL OR end_time > start_time),
    CONSTRAINT time_entries_duration_check CHECK (duration_minutes IS NULL OR duration_minutes > 0)
);

CREATE INDEX idx_time_entries_task_id ON time_entries(task_id);
CREATE INDEX idx_time_entries_user_id ON time_entries(user_id);
CREATE INDEX idx_time_entries_start_time ON time_entries(start_time);
CREATE INDEX idx_time_entries_entry_type ON time_entries(entry_type);
CREATE INDEX idx_time_entries_task_start ON time_entries(task_id, start_time);

COMMENT ON TABLE time_entries IS 'Time tracking entries for tasks';
COMMENT ON COLUMN time_entries.id IS 'Primary key';
COMMENT ON COLUMN time_entries.task_id IS 'Foreign key to tasks table';
COMMENT ON COLUMN time_entries.user_id IS 'Foreign key to users table (who tracked the time)';
COMMENT ON COLUMN time_entries.entry_type IS 'Entry type (MANUAL or TIMER)';
COMMENT ON COLUMN time_entries.start_time IS 'Time entry start timestamp';
COMMENT ON COLUMN time_entries.end_time IS 'Time entry end timestamp (NULL for running timers)';
COMMENT ON COLUMN time_entries.duration_minutes IS 'Calculated duration in minutes';
COMMENT ON COLUMN time_entries.notes IS 'Optional notes for the time entry';
COMMENT ON COLUMN time_entries.created_at IS 'Time entry creation timestamp';
COMMENT ON COLUMN time_entries.updated_at IS 'Last update timestamp';
