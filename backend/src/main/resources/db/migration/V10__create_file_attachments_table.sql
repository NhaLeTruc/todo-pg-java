-- V10__create_file_attachments_table.sql
-- Create file_attachments table for file upload management

CREATE TABLE file_attachments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    storage_key VARCHAR(500) NOT NULL UNIQUE,
    scan_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    scanned_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_file_attachments_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_file_attachments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT file_attachments_size_check CHECK (file_size > 0 AND file_size <= 26214400),
    CONSTRAINT file_attachments_filename_not_empty CHECK (LENGTH(TRIM(file_name)) > 0),
    CONSTRAINT file_attachments_storage_key_not_empty CHECK (LENGTH(TRIM(storage_key)) > 0)
);

CREATE INDEX idx_file_attachments_task_id ON file_attachments(task_id);
CREATE INDEX idx_file_attachments_user_id ON file_attachments(user_id);
CREATE INDEX idx_file_attachments_scan_status ON file_attachments(scan_status);
CREATE INDEX idx_file_attachments_created_at ON file_attachments(created_at);

COMMENT ON TABLE file_attachments IS 'File attachments for tasks';
COMMENT ON COLUMN file_attachments.id IS 'Primary key (UUID)';
COMMENT ON COLUMN file_attachments.task_id IS 'Foreign key to tasks table';
COMMENT ON COLUMN file_attachments.user_id IS 'Foreign key to users table (uploader)';
COMMENT ON COLUMN file_attachments.file_name IS 'Original filename from user';
COMMENT ON COLUMN file_attachments.file_size IS 'File size in bytes (max 25MB)';
COMMENT ON COLUMN file_attachments.mime_type IS 'MIME type of the file';
COMMENT ON COLUMN file_attachments.storage_key IS 'Unique key in MinIO storage';
COMMENT ON COLUMN file_attachments.scan_status IS 'Virus scan status';
COMMENT ON COLUMN file_attachments.scanned_at IS 'Timestamp of virus scan completion';
COMMENT ON COLUMN file_attachments.created_at IS 'File upload timestamp';
