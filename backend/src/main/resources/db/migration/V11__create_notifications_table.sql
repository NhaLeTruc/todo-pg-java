-- V11__create_notifications_table.sql
-- Create notifications table for user notifications

CREATE TYPE notification_type AS ENUM (
    'TASK_DUE_SOON',
    'TASK_OVERDUE',
    'TASK_SHARED',
    'COMMENT_MENTION',
    'COMMENT_ADDED',
    'TASK_ASSIGNED',
    'TASK_COMPLETED',
    'RECURRENCE_CREATED'
);

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    task_id BIGINT,
    notification_type notification_type NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_task_id ON notifications(task_id);
CREATE INDEX idx_notifications_type ON notifications(notification_type);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
CREATE INDEX idx_notifications_user_unread ON notifications(user_id, is_read) WHERE is_read = FALSE;

COMMENT ON TABLE notifications IS 'User notifications for various events';
COMMENT ON COLUMN notifications.id IS 'Primary key';
COMMENT ON COLUMN notifications.user_id IS 'Foreign key to users table (recipient)';
COMMENT ON COLUMN notifications.task_id IS 'Foreign key to tasks table (optional, related task)';
COMMENT ON COLUMN notifications.notification_type IS 'Type of notification';
COMMENT ON COLUMN notifications.title IS 'Notification title';
COMMENT ON COLUMN notifications.message IS 'Notification message';
COMMENT ON COLUMN notifications.is_read IS 'Read status flag';
COMMENT ON COLUMN notifications.read_at IS 'Timestamp when notification was read';
COMMENT ON COLUMN notifications.created_at IS 'Notification creation timestamp';
