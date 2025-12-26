-- V12__create_notification_preferences_table.sql
-- Create notification_preferences table for user notification settings

CREATE TABLE notification_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    notification_type notification_type NOT NULL,
    email_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    in_app_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_preferences_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT notification_preferences_unique UNIQUE (user_id, notification_type)
);

CREATE INDEX idx_notification_preferences_user_id ON notification_preferences(user_id);
CREATE INDEX idx_notification_preferences_type ON notification_preferences(notification_type);

COMMENT ON TABLE notification_preferences IS 'User preferences for notification delivery channels';
COMMENT ON COLUMN notification_preferences.id IS 'Primary key';
COMMENT ON COLUMN notification_preferences.user_id IS 'Foreign key to users table';
COMMENT ON COLUMN notification_preferences.notification_type IS 'Type of notification';
COMMENT ON COLUMN notification_preferences.email_enabled IS 'Email notification enabled flag';
COMMENT ON COLUMN notification_preferences.in_app_enabled IS 'In-app notification enabled flag';
COMMENT ON COLUMN notification_preferences.created_at IS 'Preference creation timestamp';
COMMENT ON COLUMN notification_preferences.updated_at IS 'Last update timestamp';
