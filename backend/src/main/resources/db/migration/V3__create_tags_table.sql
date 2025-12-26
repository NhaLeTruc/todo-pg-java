-- V3__create_tags_table.sql
-- Create tags table for task labeling

CREATE TABLE tags (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    color VARCHAR(7),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tags_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT tags_name_user_unique UNIQUE (user_id, name),
    CONSTRAINT tags_color_check CHECK (color IS NULL OR color ~* '^#[0-9A-Fa-f]{6}$')
);

CREATE INDEX idx_tags_user_id ON tags(user_id);
CREATE INDEX idx_tags_name ON tags(name);

COMMENT ON TABLE tags IS 'User-defined tags for labeling tasks';
COMMENT ON COLUMN tags.id IS 'Primary key';
COMMENT ON COLUMN tags.user_id IS 'Foreign key to users table';
COMMENT ON COLUMN tags.name IS 'Tag name (unique per user)';
COMMENT ON COLUMN tags.color IS 'Hex color code for visual display';
COMMENT ON COLUMN tags.created_at IS 'Tag creation timestamp';
COMMENT ON COLUMN tags.updated_at IS 'Last update timestamp';
