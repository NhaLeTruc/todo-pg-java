-- V10__create_file_attachments_table.sql
-- Create file_attachments table for file upload management

CREATE TYPE virus_scan_status AS ENUM ('PENDING', 'CLEAN', 'INFECTED', 'FAILED');

CREATE TABLE file_attachments (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    filename VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    virus_scan_status virus_scan_status NOT NULL DEFAULT 'PENDING',
    virus_scan_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_file_attachments_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_file_attachments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT file_attachments_size_check CHECK (file_size_bytes > 0 AND file_size_bytes <= 26214400),
    CONSTRAINT file_attachments_filename_not_empty CHECK (LENGTH(TRIM(filename)) > 0),
    CONSTRAINT file_attachments_storage_path_not_empty CHECK (LENGTH(TRIM(storage_path)) > 0)
);

CREATE INDEX idx_file_attachments_task_id ON file_attachments(task_id);
CREATE INDEX idx_file_attachments_user_id ON file_attachments(user_id);
CREATE INDEX idx_file_attachments_virus_scan_status ON file_attachments(virus_scan_status);
CREATE INDEX idx_file_attachments_created_at ON file_attachments(created_at);

COMMENT ON TABLE file_attachments IS 'File attachments for tasks';
COMMENT ON COLUMN file_attachments.id IS 'Primary key';
COMMENT ON COLUMN file_attachments.task_id IS 'Foreign key to tasks table';
COMMENT ON COLUMN file_attachments.user_id IS 'Foreign key to users table (uploader)';
COMMENT ON COLUMN file_attachments.filename IS 'Unique filename in storage';
COMMENT ON COLUMN file_attachments.original_filename IS 'Original filename from user';
COMMENT ON COLUMN file_attachments.file_size_bytes IS 'File size in bytes (max 25MB)';
COMMENT ON COLUMN file_attachments.mime_type IS 'MIME type of the file';
COMMENT ON COLUMN file_attachments.storage_path IS 'Path in MinIO storage';
COMMENT ON COLUMN file_attachments.virus_scan_status IS 'Virus scan status (PENDING, CLEAN, INFECTED, FAILED)';
COMMENT ON COLUMN file_attachments.virus_scan_at IS 'Timestamp of virus scan completion';
COMMENT ON COLUMN file_attachments.created_at IS 'File upload timestamp';
COMMENT ON COLUMN file_attachments.updated_at IS 'Last update timestamp';
