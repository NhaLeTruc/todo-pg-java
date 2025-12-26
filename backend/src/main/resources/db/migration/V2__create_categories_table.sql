-- V2__create_categories_table.sql
-- Create categories table for task organization

CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    color VARCHAR(7),
    icon VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_categories_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT categories_name_user_unique UNIQUE (user_id, name),
    CONSTRAINT categories_color_check CHECK (color IS NULL OR color ~* '^#[0-9A-Fa-f]{6}$')
);

CREATE INDEX idx_categories_user_id ON categories(user_id);
CREATE INDEX idx_categories_name ON categories(name);

COMMENT ON TABLE categories IS 'User-defined categories for organizing tasks';
COMMENT ON COLUMN categories.id IS 'Primary key';
COMMENT ON COLUMN categories.user_id IS 'Foreign key to users table';
COMMENT ON COLUMN categories.name IS 'Category name (unique per user)';
COMMENT ON COLUMN categories.color IS 'Hex color code for visual display';
COMMENT ON COLUMN categories.icon IS 'Icon identifier for visual display';
COMMENT ON COLUMN categories.created_at IS 'Category creation timestamp';
COMMENT ON COLUMN categories.updated_at IS 'Last update timestamp';
