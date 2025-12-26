-- V7__create_comments_table.sql
-- Create comments table for task comments and notes

CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    is_edited BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comments_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT comments_content_not_empty CHECK (LENGTH(TRIM(content)) > 0)
);

CREATE INDEX idx_comments_task_id ON comments(task_id);
CREATE INDEX idx_comments_user_id ON comments(user_id);
CREATE INDEX idx_comments_created_at ON comments(created_at);
CREATE INDEX idx_comments_task_created ON comments(task_id, created_at);

COMMENT ON TABLE comments IS 'Comments and notes on tasks';
COMMENT ON COLUMN comments.id IS 'Primary key';
COMMENT ON COLUMN comments.task_id IS 'Foreign key to tasks table';
COMMENT ON COLUMN comments.user_id IS 'Foreign key to users table (comment author)';
COMMENT ON COLUMN comments.content IS 'Comment content (required, non-empty)';
COMMENT ON COLUMN comments.is_edited IS 'Flag indicating if comment has been edited';
COMMENT ON COLUMN comments.created_at IS 'Comment creation timestamp';
COMMENT ON COLUMN comments.updated_at IS 'Last update timestamp';
