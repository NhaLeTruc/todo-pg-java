-- V8__create_task_shares_table.sql
-- Create task_shares table for task collaboration

CREATE TYPE permission_level AS ENUM ('VIEW', 'EDIT');

CREATE TABLE task_shares (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    owner_user_id BIGINT NOT NULL,
    shared_with_user_id BIGINT NOT NULL,
    permission permission_level NOT NULL DEFAULT 'VIEW',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_task_shares_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_shares_owner FOREIGN KEY (owner_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_shares_shared_with FOREIGN KEY (shared_with_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT task_shares_unique UNIQUE (task_id, shared_with_user_id),
    CONSTRAINT task_shares_not_self CHECK (owner_user_id != shared_with_user_id)
);

CREATE INDEX idx_task_shares_task_id ON task_shares(task_id);
CREATE INDEX idx_task_shares_owner_user_id ON task_shares(owner_user_id);
CREATE INDEX idx_task_shares_shared_with_user_id ON task_shares(shared_with_user_id);
CREATE INDEX idx_task_shares_permission ON task_shares(permission);

COMMENT ON TABLE task_shares IS 'Task sharing and collaboration permissions';
COMMENT ON COLUMN task_shares.id IS 'Primary key';
COMMENT ON COLUMN task_shares.task_id IS 'Foreign key to tasks table';
COMMENT ON COLUMN task_shares.owner_user_id IS 'Foreign key to users table (task owner)';
COMMENT ON COLUMN task_shares.shared_with_user_id IS 'Foreign key to users table (collaborator)';
COMMENT ON COLUMN task_shares.permission IS 'Permission level (VIEW or EDIT)';
COMMENT ON COLUMN task_shares.created_at IS 'Share creation timestamp';
COMMENT ON COLUMN task_shares.updated_at IS 'Last update timestamp';
