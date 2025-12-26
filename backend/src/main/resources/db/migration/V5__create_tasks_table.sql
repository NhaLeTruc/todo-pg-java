-- V5__create_tasks_table.sql
-- Create tasks table (core entity)

CREATE TYPE priority_level AS ENUM ('LOW', 'MEDIUM', 'HIGH');

CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    parent_task_id BIGINT,
    category_id BIGINT,
    recurrence_pattern_id BIGINT,
    description TEXT NOT NULL,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    priority priority_level NOT NULL DEFAULT 'MEDIUM',
    due_date TIMESTAMP,
    completed_at TIMESTAMP,
    position INT NOT NULL DEFAULT 0,
    estimated_duration_minutes INT,
    actual_duration_minutes INT,
    depth INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tasks_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_tasks_parent FOREIGN KEY (parent_task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_tasks_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    CONSTRAINT fk_tasks_recurrence FOREIGN KEY (recurrence_pattern_id) REFERENCES recurrence_patterns(id) ON DELETE SET NULL,
    CONSTRAINT tasks_description_not_empty CHECK (LENGTH(TRIM(description)) > 0),
    CONSTRAINT tasks_depth_check CHECK (depth >= 0 AND depth <= 5),
    CONSTRAINT tasks_estimated_duration_check CHECK (estimated_duration_minutes IS NULL OR estimated_duration_minutes > 0),
    CONSTRAINT tasks_actual_duration_check CHECK (actual_duration_minutes IS NULL OR actual_duration_minutes > 0)
);

CREATE INDEX idx_tasks_user_id ON tasks(user_id);
CREATE INDEX idx_tasks_parent_task_id ON tasks(parent_task_id);
CREATE INDEX idx_tasks_category_id ON tasks(category_id);
CREATE INDEX idx_tasks_recurrence_pattern_id ON tasks(recurrence_pattern_id);
CREATE INDEX idx_tasks_is_completed ON tasks(is_completed);
CREATE INDEX idx_tasks_priority ON tasks(priority);
CREATE INDEX idx_tasks_due_date ON tasks(due_date);
CREATE INDEX idx_tasks_created_at ON tasks(created_at);
CREATE INDEX idx_tasks_user_completed ON tasks(user_id, is_completed);
CREATE INDEX idx_tasks_user_due_date ON tasks(user_id, due_date) WHERE due_date IS NOT NULL;

COMMENT ON TABLE tasks IS 'Core tasks table with all task attributes';
COMMENT ON COLUMN tasks.id IS 'Primary key';
COMMENT ON COLUMN tasks.user_id IS 'Foreign key to users table (task owner)';
COMMENT ON COLUMN tasks.parent_task_id IS 'Foreign key to parent task for subtasks (nullable)';
COMMENT ON COLUMN tasks.category_id IS 'Foreign key to categories table (nullable)';
COMMENT ON COLUMN tasks.recurrence_pattern_id IS 'Foreign key to recurrence_patterns table (nullable)';
COMMENT ON COLUMN tasks.description IS 'Task description (required, non-empty)';
COMMENT ON COLUMN tasks.is_completed IS 'Task completion status';
COMMENT ON COLUMN tasks.priority IS 'Task priority level (LOW, MEDIUM, HIGH)';
COMMENT ON COLUMN tasks.due_date IS 'Task due date and time (nullable)';
COMMENT ON COLUMN tasks.completed_at IS 'Timestamp when task was completed';
COMMENT ON COLUMN tasks.position IS 'Display order position';
COMMENT ON COLUMN tasks.estimated_duration_minutes IS 'Estimated task duration in minutes';
COMMENT ON COLUMN tasks.actual_duration_minutes IS 'Actual task duration in minutes (from time tracking)';
COMMENT ON COLUMN tasks.depth IS 'Nesting depth for subtasks (0-5)';
COMMENT ON COLUMN tasks.created_at IS 'Task creation timestamp';
COMMENT ON COLUMN tasks.updated_at IS 'Last update timestamp';
