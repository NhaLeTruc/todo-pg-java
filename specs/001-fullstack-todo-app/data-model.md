# Data Model: Full-Stack TODO Application

**Date**: 2025-12-26
**Phase**: 1 (Design & Contracts)
**Database**: PostgreSQL 16
**ORM**: Spring Data JPA / Hibernate

## Overview

The data model supports all 15 user stories (P1-P15) with 14 core entities. The design follows:
- **Normalization**: 3NF to eliminate redundancy
- **Referential integrity**: Foreign keys with appropriate cascades
- **Indexing**: Optimized for common queries
- **Audit fields**: created_at, updated_at on all entities
- **Soft deletes**: Optional for entities requiring history

## Entity Relationship Diagram

```
┌─────────────┐
│    User     │
└──────┬──────┘
       │ 1
       │
       │ *
┌──────┴───────┐      *        ┌─────────────┐
│    Task      │───────────────│   TaskTag   │
└──────┬───────┘               └──────┬──────┘
       │                              │
       │ 1                            │ *
       │                              │
       │ *                            │
┌──────┴──────┐                ┌─────┴─────┐
│  Comment    │                │    Tag    │
└─────────────┘                └───────────┘

       ┌──────────────┐
       │  TaskShare   │───────┐
       └──────────────┘       │
                              │
       ┌──────────────┐       │
       │  TimeEntry   │       │
       └──────────────┘       │
                              │
       ┌──────────────┐       │
       │FileAttachment│       │
       └──────────────┘       │
                              │
       ┌──────────────┐       │
       │ Notification │       │
       └──────────────┘       │
                              │
       ┌──────────────┐       │
       │  Category    │───────┤ All relate
       └──────────────┘       │ to Task/User
                              │
       ┌──────────────┐       │
       │ Recurrence   │───────┤
       │   Pattern    │       │
       └──────────────┘       │
                              │
       ┌──────────────┐       │
       │ Notification │───────┘
       │ Preference   │
       └──────────────┘
```

## Entities

### 1. User

**Purpose**: Represents a registered user account

**Fields**:
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(60) NOT NULL,  -- BCrypt hash
    name VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT true,
    CONSTRAINT email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$')
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_active ON users(is_active);
```

**Validation Rules**:
- Email: Valid email format, unique, max 255 chars
- Password: Minimum 8 characters, hashed with BCrypt (stored as 60 chars)
- Name: Optional, max 100 chars
- is_active: Soft delete flag

**Java Entity**:
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash;

    @Column(length = 100)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Getters, setters, equals, hashCode
}
```

---

### 2. Task

**Purpose**: Represents a TODO item

**Fields**:
```sql
CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    parent_task_id BIGINT REFERENCES tasks(id) ON DELETE CASCADE,  -- For subtasks
    category_id BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    recurrence_pattern_id BIGINT REFERENCES recurrence_patterns(id) ON DELETE SET NULL,
    description TEXT NOT NULL CHECK (char_length(description) <= 10000),
    is_completed BOOLEAN NOT NULL DEFAULT false,
    priority VARCHAR(10) CHECK (priority IN ('HIGH', 'MEDIUM', 'LOW')),
    due_date TIMESTAMPTZ,  -- With timezone support
    estimated_duration_minutes INTEGER CHECK (estimated_duration_minutes > 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    CONSTRAINT description_not_empty CHECK (char_length(TRIM(description)) > 0)
);

CREATE INDEX idx_tasks_user_id ON tasks(user_id);
CREATE INDEX idx_tasks_parent_task_id ON tasks(parent_task_id);
CREATE INDEX idx_tasks_category_id ON tasks(category_id);
CREATE INDEX idx_tasks_due_date ON tasks(due_date);
CREATE INDEX idx_tasks_priority ON tasks(priority);
CREATE INDEX idx_tasks_is_completed ON tasks(is_completed);
CREATE INDEX idx_tasks_user_completed ON tasks(user_id, is_completed);
CREATE INDEX idx_tasks_fulltext ON tasks USING GIN(to_tsvector('english', description));
```

**Validation Rules**:
- Description: Required, 1-10,000 characters, no whitespace-only
- Priority: Optional, must be HIGH/MEDIUM/LOW
- Due date: Optional, timezone-aware
- Estimated duration: Optional, positive integer (minutes)
- Parent task: Creates subtask hierarchy, max 5 levels (enforced in application)

**Java Entity**:
```java
@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id")
    private Task parentTask;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurrence_pattern_id")
    private RecurrencePattern recurrencePattern;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Size(min = 1, max = 10000)
    private String description;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Priority priority;

    @Column(name = "due_date")
    private ZonedDateTime dueDate;

    @Column(name = "estimated_duration_minutes")
    @Min(1)
    private Integer estimatedDurationMinutes;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskTag> taskTags = new ArrayList<>();

    // Audit fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}

public enum Priority {
    HIGH, MEDIUM, LOW
}
```

---

### 3. Category

**Purpose**: User-defined task organization categories

**Fields**:
```sql
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(50) NOT NULL,
    color_hex VARCHAR(7),  -- e.g., #FF5733
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, name),
    CONSTRAINT color_format CHECK (color_hex IS NULL OR color_hex ~* '^#[0-9A-F]{6}$')
);

CREATE INDEX idx_categories_user_id ON categories(user_id);
```

**Validation Rules**:
- Name: Required, max 50 chars, unique per user
- Color: Optional, hex format (#RRGGBB)

**Java Entity**:
```java
@Entity
@Table(name = "categories", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "name"})
})
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "color_hex", length = 7)
    @Pattern(regexp = "^#[0-9A-F]{6}$")
    private String colorHex;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

---

### 4. Tag

**Purpose**: Flexible task labeling/tagging

**Fields**:
```sql
CREATE TABLE tags (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, name)
);

CREATE INDEX idx_tags_user_id ON tags(user_id);
```

**Validation Rules**:
- Name: Required, max 30 chars, unique per user

**Java Entity**:
```java
@Entity
@Table(name = "tags", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "name"})
})
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

---

### 5. TaskTag (Join Table)

**Purpose**: Many-to-many relationship between tasks and tags

**Fields**:
```sql
CREATE TABLE task_tags (
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (task_id, tag_id)
);

CREATE INDEX idx_task_tags_task_id ON task_tags(task_id);
CREATE INDEX idx_task_tags_tag_id ON task_tags(tag_id);
```

**Java Entity**:
```java
@Entity
@Table(name = "task_tags")
public class TaskTag {
    @EmbeddedId
    private TaskTagId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("taskId")
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id")
    private Tag tag;
}

@Embeddable
public class TaskTagId implements Serializable {
    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "tag_id")
    private Long tagId;

    // equals, hashCode
}
```

---

### 6. Comment

**Purpose**: User comments on tasks

**Fields**:
```sql
CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    author_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content TEXT NOT NULL CHECK (char_length(content) <= 5000),
    is_edited BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT content_not_empty CHECK (char_length(TRIM(content)) > 0)
);

CREATE INDEX idx_comments_task_id ON comments(task_id);
CREATE INDEX idx_comments_author_id ON comments(author_id);
CREATE INDEX idx_comments_created_at ON comments(created_at);
```

**Validation Rules**:
- Content: Required, 1-5,000 characters, no whitespace-only
- is_edited: Set to true when comment is modified after creation

**Java Entity**:
```java
@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Size(min = 1, max = 5000)
    private String content;

    @Column(name = "is_edited", nullable = false)
    private Boolean isEdited = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

---

### 7. TaskShare

**Purpose**: Task sharing/collaboration permissions

**Fields**:
```sql
CREATE TABLE task_shares (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    shared_with_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    shared_by_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    permission_level VARCHAR(10) NOT NULL CHECK (permission_level IN ('VIEW', 'EDIT')),
    shared_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(task_id, shared_with_user_id)
);

CREATE INDEX idx_task_shares_task_id ON task_shares(task_id);
CREATE INDEX idx_task_shares_shared_with_user_id ON task_shares(shared_with_user_id);
```

**Validation Rules**:
- Permission level: Must be VIEW or EDIT
- Unique constraint prevents duplicate shares

**Java Entity**:
```java
@Entity
@Table(name = "task_shares", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"task_id", "shared_with_user_id"})
})
public class TaskShare {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_with_user_id", nullable = false)
    private User sharedWithUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_by_user_id", nullable = false)
    private User sharedByUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission_level", nullable = false, length = 10)
    private PermissionLevel permissionLevel;

    @Column(name = "shared_at", nullable = false, updatable = false)
    private LocalDateTime sharedAt;
}

public enum PermissionLevel {
    VIEW, EDIT
}
```

---

### 8. RecurrencePattern

**Purpose**: Defines recurring task schedules

**Fields**:
```sql
CREATE TABLE recurrence_patterns (
    id BIGSERIAL PRIMARY KEY,
    frequency VARCHAR(10) NOT NULL CHECK (frequency IN ('DAILY', 'WEEKLY', 'MONTHLY')),
    interval_count INTEGER NOT NULL DEFAULT 1 CHECK (interval_count > 0),
    days_of_week VARCHAR(50),  -- Comma-separated: MON,WED,FRI
    end_date DATE,
    timezone VARCHAR(50) NOT NULL DEFAULT 'UTC',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

**Validation Rules**:
- Frequency: DAILY, WEEKLY, MONTHLY
- Interval: Positive integer (every N days/weeks/months)
- Days of week: For WEEKLY, comma-separated (MON, TUE, WED, THU, FRI, SAT, SUN)
- Timezone: IANA timezone name

**Java Entity**:
```java
@Entity
@Table(name = "recurrence_patterns")
public class RecurrencePattern {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Frequency frequency;

    @Column(name = "interval_count", nullable = false)
    @Min(1)
    private Integer intervalCount = 1;

    @Column(name = "days_of_week", length = 50)
    private String daysOfWeek;  // Comma-separated

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(nullable = false, length = 50)
    private String timezone = "UTC";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

public enum Frequency {
    DAILY, WEEKLY, MONTHLY
}
```

---

### 9. TimeEntry

**Purpose**: Time tracking for tasks

**Fields**:
```sql
CREATE TABLE time_entries (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,  -- NULL for running timers
    duration_minutes INTEGER,  -- Calculated when end_time set
    entry_type VARCHAR(10) NOT NULL CHECK (entry_type IN ('MANUAL', 'TIMER')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT end_after_start CHECK (end_time IS NULL OR end_time > start_time)
);

CREATE INDEX idx_time_entries_task_id ON time_entries(task_id);
CREATE INDEX idx_time_entries_user_id ON time_entries(user_id);
CREATE INDEX idx_time_entries_start_time ON time_entries(start_time);
```

**Validation Rules**:
- End time: Must be after start time
- Duration: Auto-calculated from start/end times
- Entry type: MANUAL (user logged) or TIMER (started/stopped)

**Java Entity**:
```java
@Entity
@Table(name = "time_entries")
public class TimeEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 10)
    private EntryType entryType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PreUpdate
    @PrePersist
    public void calculateDuration() {
        if (endTime != null) {
            durationMinutes = (int) ChronoUnit.MINUTES.between(startTime, endTime);
        }
    }
}

public enum EntryType {
    MANUAL, TIMER
}
```

---

### 10. FileAttachment

**Purpose**: File attachments for tasks

**Fields**:
```sql
CREATE TABLE file_attachments (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    uploaded_by_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(100) NOT NULL,  -- MIME type
    file_size_bytes BIGINT NOT NULL CHECK (file_size_bytes > 0),
    storage_path VARCHAR(500) NOT NULL,  -- MinIO object key
    virus_scan_status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (virus_scan_status IN ('PENDING', 'CLEAN', 'INFECTED', 'ERROR')),
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT file_size_limit CHECK (file_size_bytes <= 26214400)  -- 25MB
);

CREATE INDEX idx_file_attachments_task_id ON file_attachments(task_id);
CREATE INDEX idx_file_attachments_uploaded_by_user_id ON file_attachments(uploaded_by_user_id);
```

**Validation Rules**:
- File size: Max 25MB (26,214,400 bytes)
- File type: MIME type validation in application
- Virus scan status: PENDING → CLEAN/INFECTED/ERROR

**Java Entity**:
```java
@Entity
@Table(name = "file_attachments")
public class FileAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_user_id", nullable = false)
    private User uploadedBy;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_type", nullable = false, length = 100)
    private String fileType;

    @Column(name = "file_size_bytes", nullable = false)
    @Max(26214400)
    private Long fileSizeBytes;

    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "virus_scan_status", nullable = false, length = 20)
    private VirusScanStatus virusScanStatus = VirusScanStatus.PENDING;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;
}

public enum VirusScanStatus {
    PENDING, CLEAN, INFECTED, ERROR
}
```

---

### 11. Notification

**Purpose**: User notifications

**Fields**:
```sql
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    recipient_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    notification_type VARCHAR(30) NOT NULL CHECK (notification_type IN
        ('DUE_DATE', 'MENTION', 'TASK_SHARED', 'COMMENT_ADDED', 'TASK_COMPLETED')),
    related_task_id BIGINT REFERENCES tasks(id) ON DELETE CASCADE,
    related_comment_id BIGINT REFERENCES comments(id) ON DELETE CASCADE,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP
);

CREATE INDEX idx_notifications_recipient_user_id ON notifications(recipient_user_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);
```

**Validation Rules**:
- Type: One of predefined notification types
- Message: Required, notification content
- is_read: Tracks read status

**Java Entity**:
```java
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_user_id", nullable = false)
    private User recipientUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 30)
    private NotificationType notificationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_task_id")
    private Task relatedTask;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_comment_id")
    private Comment relatedComment;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;
}

public enum NotificationType {
    DUE_DATE, MENTION, TASK_SHARED, COMMENT_ADDED, TASK_COMPLETED
}
```

---

### 12. NotificationPreference

**Purpose**: User notification channel preferences

**Fields**:
```sql
CREATE TABLE notification_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    notification_type VARCHAR(30) NOT NULL,
    email_enabled BOOLEAN NOT NULL DEFAULT true,
    in_app_enabled BOOLEAN NOT NULL DEFAULT true,
    push_enabled BOOLEAN NOT NULL DEFAULT false,
    UNIQUE(user_id, notification_type)
);

CREATE INDEX idx_notification_preferences_user_id ON notification_preferences(user_id);
```

**Validation Rules**:
- Unique per user + notification type
- Channels: email, in-app, push (each boolean)

**Java Entity**:
```java
@Entity
@Table(name = "notification_preferences", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "notification_type"})
})
public class NotificationPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 30)
    private NotificationType notificationType;

    @Column(name = "email_enabled", nullable = false)
    private Boolean emailEnabled = true;

    @Column(name = "in_app_enabled", nullable = false)
    private Boolean inAppEnabled = true;

    @Column(name = "push_enabled", nullable = false)
    private Boolean pushEnabled = false;
}
```

---

## Database Migrations (Flyway)

### Migration Structure

```
src/main/resources/db/migration/
├── V1__create_users_table.sql
├── V2__create_categories_table.sql
├── V3__create_tags_table.sql
├── V4__create_recurrence_patterns_table.sql
├── V5__create_tasks_table.sql
├── V6__create_task_tags_table.sql
├── V7__create_comments_table.sql
├── V8__create_task_shares_table.sql
├── V9__create_time_entries_table.sql
├── V10__create_file_attachments_table.sql
├── V11__create_notifications_table.sql
├── V12__create_notification_preferences_table.sql
├── V13__create_indexes.sql
└── V14__insert_test_data.sql (dev only)
```

### Migration Best Practices

1. **Sequential versioning**: V1, V2, V3...
2. **Descriptive names**: `V5__create_tasks_table.sql`
3. **One change per migration**: Easier rollback/debugging
4. **Idempotent**: Can run multiple times safely
5. **Testing**: Test migrations on copy of production data

---

## Query Optimization

### Common Queries & Indexes

1. **Get user's tasks**:
```sql
SELECT * FROM tasks WHERE user_id = ? AND is_completed = false;
-- Index: idx_tasks_user_completed
```

2. **Search tasks**:
```sql
SELECT * FROM tasks
WHERE user_id = ?
  AND to_tsvector('english', description) @@ plainto_tsquery('english', ?);
-- Index: idx_tasks_fulltext (GIN)
```

3. **Get overdue tasks**:
```sql
SELECT * FROM tasks
WHERE user_id = ?
  AND is_completed = false
  AND due_date < NOW();
-- Index: idx_tasks_due_date, idx_tasks_is_completed
```

4. **Get tasks by tag**:
```sql
SELECT t.* FROM tasks t
JOIN task_tags tt ON t.id = tt.task_id
JOIN tags tg ON tt.tag_id = tg.id
WHERE t.user_id = ? AND tg.name = ?;
-- Indexes: idx_task_tags_tag_id, idx_tasks_user_id
```

5. **Get shared tasks**:
```sql
SELECT t.* FROM tasks t
JOIN task_shares ts ON t.id = ts.task_id
WHERE ts.shared_with_user_id = ?;
-- Index: idx_task_shares_shared_with_user_id
```

---

## Data Integrity Rules

### Cascade Delete Behavior

- **User deleted** → All tasks, comments, categories, tags CASCADE deleted
- **Task deleted** → Comments, attachments, time entries CASCADE deleted
- **Task deleted with subtasks** → All subtasks CASCADE deleted (warn user first)
- **Category deleted** → Tasks set to NULL (SET NULL)
- **Tag deleted** → TaskTag entries CASCADE deleted

### Orphan Prevention

- Comments cannot exist without a task
- TaskTag entries cannot exist without both task and tag
- Time entries cannot exist without a task

### Constraints

- Email uniqueness enforced at database level
- Category/Tag names unique per user
- Task shares unique per task+user combination
- File size limits enforced at database level

---

## Performance Considerations

1. **Pagination**: Use `LIMIT` and `OFFSET` for large result sets
2. **Lazy loading**: JPA `FetchType.LAZY` for associations
3. **N+1 prevention**: Use `JOIN FETCH` for eager loading when needed
4. **Connection pooling**: HikariCP optimized pool size
5. **Query caching**: Redis for frequently accessed tasks
6. **Batch operations**: Use `@BatchSize` for collection fetching

---

## Audit & History

All tables include:
- `created_at`: Timestamp of creation (immutable)
- `updated_at`: Timestamp of last update (auto-updated)

For sensitive operations (user deletion, task sharing), consider:
- Audit log table (separate from main entities)
- Soft deletes with `deleted_at` timestamp

---

## Schema Summary

- **14 tables**: 12 main entities + 2 join tables (TaskTag, TaskShare has its own fields)
- **PostgreSQL 16**: JSON support, full-text search, timezone handling
- **Spring Data JPA**: ORM with Hibernate
- **Flyway**: Version-controlled migrations
- **Indexes**: 30+ indexes for query optimization
- **Constraints**: Foreign keys, unique constraints, check constraints

The data model supports all 104 functional requirements from the specification while maintaining normalization, integrity, and performance.
