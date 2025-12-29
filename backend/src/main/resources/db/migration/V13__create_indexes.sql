-- V13__create_indexes.sql
-- Create additional composite indexes and full-text search indexes for performance

-- Full-text search index on tasks description
CREATE INDEX idx_tasks_description_fulltext ON tasks USING gin(to_tsvector('english', description));

-- Composite indexes for common query patterns
CREATE INDEX idx_tasks_user_priority_due ON tasks(user_id, priority, due_date) WHERE is_completed = FALSE;
CREATE INDEX idx_tasks_user_category_completed ON tasks(user_id, category_id, is_completed);
CREATE INDEX idx_tasks_user_created_completed ON tasks(user_id, created_at, is_completed);

-- Composite index for subtask queries
CREATE INDEX idx_tasks_parent_depth ON tasks(parent_task_id, depth) WHERE parent_task_id IS NOT NULL;

-- Composite index for recurrence queries
CREATE INDEX idx_tasks_recurrence_completed ON tasks(recurrence_pattern_id, is_completed) WHERE recurrence_pattern_id IS NOT NULL;

-- Composite index for shared tasks
CREATE INDEX idx_task_shares_shared_with_permission ON task_shares(shared_with_user_id, permission);

-- Composite index for tag filtering
CREATE INDEX idx_task_tags_tag_task ON task_tags(tag_id, task_id);

-- Partial indexes for performance on common filters
CREATE INDEX idx_tasks_incomplete_due_date ON tasks(user_id, due_date) WHERE is_completed = FALSE AND due_date IS NOT NULL;

-- Time tracking aggregation indexes
CREATE INDEX idx_time_entries_task_user_dates ON time_entries(task_id, user_id, start_time, end_time);

-- Comment ordering index
CREATE INDEX idx_comments_task_created_desc ON comments(task_id, created_at DESC);

-- File attachments by status
CREATE INDEX idx_file_attachments_pending_scan ON file_attachments(scan_status) WHERE scan_status = 'PENDING';

-- Covering index for notification list queries
CREATE INDEX idx_notifications_user_unread_created ON notifications(user_id, created_at DESC) WHERE is_read = FALSE;

COMMENT ON INDEX idx_tasks_description_fulltext IS 'Full-text search index for task descriptions';
COMMENT ON INDEX idx_tasks_user_priority_due IS 'Composite index for filtering incomplete tasks by user, priority, and due date';
COMMENT ON INDEX idx_tasks_user_category_completed IS 'Composite index for filtering tasks by user, category, and completion status';
COMMENT ON INDEX idx_tasks_incomplete_due_date IS 'Partial index for incomplete tasks with due dates';
COMMENT ON INDEX idx_notifications_user_unread_created IS 'Covering index for unread notification queries with frequently accessed columns';
