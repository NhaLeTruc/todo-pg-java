-- V6__create_task_tags_table.sql
-- Create task_tags join table for many-to-many relationship between tasks and tags

CREATE TABLE task_tags (
    task_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (task_id, tag_id),
    CONSTRAINT fk_task_tags_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_tags_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

CREATE INDEX idx_task_tags_task_id ON task_tags(task_id);
CREATE INDEX idx_task_tags_tag_id ON task_tags(tag_id);

COMMENT ON TABLE task_tags IS 'Join table for many-to-many relationship between tasks and tags';
COMMENT ON COLUMN task_tags.task_id IS 'Foreign key to tasks table';
COMMENT ON COLUMN task_tags.tag_id IS 'Foreign key to tags table';
COMMENT ON COLUMN task_tags.created_at IS 'Timestamp when tag was assigned to task';
