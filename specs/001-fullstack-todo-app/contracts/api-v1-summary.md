# API Contract Summary: TODO Application v1

**Base URL**: `/api/v1`
**Protocol**: HTTPS (production), HTTP (local development)
**Authentication**: JWT Bearer token (after login)
**Content-Type**: `application/json`
**API Standard**: REST + WebSocket for real-time features

## Authentication Endpoints

### POST /api/v1/auth/register
Register a new user account.

**Request**:
```json
{
  "email": "user@example.com",
  "password": "SecureP@ss123",
  "name": "John Doe"
}
```

**Response** (201 Created):
```json
{
  "id": 1,
  "email": "user@example.com",
  "name": "John Doe",
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "createdAt": "2025-12-26T10:00:00Z"
}
```

**Errors**:
- 400: Invalid email format, weak password
- 409: Email already registered

---

### POST /api/v1/auth/login
Authenticate user and receive JWT token.

**Request**:
```json
{
  "email": "user@example.com",
  "password": "SecureP@ss123"
}
```

**Response** (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "name": "John Doe"
  }
}
```

**Errors**:
- 401: Invalid credentials

---

### POST /api/v1/auth/logout
Invalidate current session.

**Headers**: `Authorization: Bearer <token>`

**Response** (204 No Content)

---

### POST /api/v1/auth/password-reset-request
Request password reset email.

**Request**:
```json
{
  "email": "user@example.com"
}
```

**Response** (202 Accepted): Email sent if account exists

---

## Task Endpoints

### GET /api/v1/tasks
Get user's task list with filtering and pagination.

**Headers**: `Authorization: Bearer <token>`

**Query Parameters**:
- `completed`: boolean (filter by completion status)
- `priority`: HIGH|MEDIUM|LOW
- `categoryId`: number
- `tagIds`: comma-separated numbers
- `search`: text (full-text search)
- `dueBefore`: ISO date
- `dueAfter`: ISO date
- `page`: number (default: 0)
- `size`: number (default: 20, max: 100)
- `sort`: field,direction (e.g., dueDate,asc)

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": 1,
      "description": "Complete project proposal",
      "isCompleted": false,
      "priority": "HIGH",
      "dueDate": "2025-12-31T23:59:59Z",
      "categoryId": 2,
      "category": {
        "id": 2,
        "name": "Work",
        "colorHex": "#FF5733"
      },
      "tags": [
        {"id": 1, "name": "urgent"},
        {"id": 3, "name": "important"}
      ],
      "parentTaskId": null,
      "subtaskCount": 0,
      "completedSubtaskCount": 0,
      "estimatedDurationMinutes": 120,
      "totalTrackedMinutes": 45,
      "commentCount": 3,
      "attachmentCount": 1,
      "isShared": false,
      "createdAt": "2025-12-20T10:00:00Z",
      "updatedAt": "2025-12-25T15:30:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 45,
  "totalPages": 3,
  "last": false
}
```

---

### POST /api/v1/tasks
Create a new task.

**Headers**: `Authorization: Bearer <token>`

**Request**:
```json
{
  "description": "Complete project proposal",
  "priority": "HIGH",
  "dueDate": "2025-12-31T23:59:59Z",
  "categoryId": 2,
  "tagIds": [1, 3],
  "parentTaskId": null,
  "estimatedDurationMinutes": 120,
  "recurrencePattern": {
    "frequency": "WEEKLY",
    "intervalCount": 1,
    "daysOfWeek": ["MON", "WED", "FRI"],
    "timezone": "America/New_York"
  }
}
```

**Response** (201 Created):
```json
{
  "id": 1,
  "description": "Complete project proposal",
  "isCompleted": false,
  "priority": "HIGH",
  "dueDate": "2025-12-31T23:59:59Z",
  "categoryId": 2,
  "tags": [...],
  "createdAt": "2025-12-26T10:00:00Z"
}
```

**Errors**:
- 400: Invalid data (empty description, invalid priority, future due date parsing)
- 404: Category or tag not found

---

### GET /api/v1/tasks/{id}
Get single task by ID.

**Headers**: `Authorization: Bearer <token>`

**Response** (200 OK): Single task object (same structure as GET /tasks)

**Errors**:
- 404: Task not found or not accessible

---

### PUT /api/v1/tasks/{id}
Update an existing task.

**Headers**: `Authorization: Bearer <token>`

**Request**: Same as POST (all fields optional except description)

**Response** (200 OK): Updated task object

**Errors**:
- 400: Invalid data
- 403: User doesn't have edit permission
- 404: Task not found

---

### DELETE /api/v1/tasks/{id}
Delete a task (with confirmation for subtasks).

**Headers**: `Authorization: Bearer <token>`

**Query Parameters**:
- `deleteSubtasks`: boolean (default: false)

**Response** (204 No Content)

**Errors**:
- 400: Task has subtasks and deleteSubtasks=false
- 403: User doesn't own the task
- 404: Task not found

---

### PATCH /api/v1/tasks/{id}/complete
Mark task as complete.

**Headers**: `Authorization: Bearer <token>`

**Response** (200 OK): Updated task with isCompleted=true

---

### PATCH /api/v1/tasks/{id}/uncomplete
Mark task as incomplete.

**Headers**: `Authorization: Bearer <token>`

**Response** (200 OK): Updated task with isCompleted=false

---

### POST /api/v1/tasks/batch
Batch operations on multiple tasks.

**Headers**: `Authorization: Bearer <token>`

**Request**:
```json
{
  "taskIds": [1, 2, 3, 5],
  "operation": "COMPLETE",  // or "UNCOMPLETE", "DELETE", "ADD_TAG", "REMOVE_TAG", "SET_CATEGORY"
  "params": {
    "tagId": 5,  // for ADD_TAG/REMOVE_TAG
    "categoryId": 2  // for SET_CATEGORY
  }
}
```

**Response** (200 OK):
```json
{
  "successCount": 4,
  "failureCount": 0,
  "failures": []
}
```

---

## Subtask Endpoints

### GET /api/v1/tasks/{id}/subtasks
Get all subtasks of a parent task.

**Headers**: `Authorization: Bearer <token>`

**Response** (200 OK): Array of task objects

---

### POST /api/v1/tasks/{id}/subtasks
Create a subtask under a parent task.

**Headers**: `Authorization: Bearer <token>`

**Request**: Same as POST /api/v1/tasks (parentTaskId auto-set)

**Response** (201 Created): Created subtask

**Errors**:
- 400: Maximum nesting depth exceeded (5 levels)

---

## Comment Endpoints

### GET /api/v1/tasks/{taskId}/comments
Get all comments for a task.

**Headers**: `Authorization: Bearer <token>`

**Response** (200 OK):
```json
[
  {
    "id": 1,
    "content": "Updated the proposal draft",
    "author": {
      "id": 1,
      "name": "John Doe",
      "email": "john@example.com"
    },
    "isEdited": false,
    "createdAt": "2025-12-26T10:00:00Z",
    "updatedAt": "2025-12-26T10:00:00Z"
  }
]
```

---

### POST /api/v1/tasks/{taskId}/comments
Add a comment to a task.

**Headers**: `Authorization: Bearer <token>`

**Request**:
```json
{
  "content": "Updated the proposal draft"
}
```

**Response** (201 Created): Created comment object

---

### PUT /api/v1/comments/{id}
Update own comment.

**Headers**: `Authorization: Bearer <token>`

**Request**:
```json
{
  "content": "Updated content"
}
```

**Response** (200 OK): Updated comment with isEdited=true

**Errors**:
- 403: Not the comment author

---

### DELETE /api/v1/comments/{id}
Delete own comment.

**Headers**: `Authorization: Bearer <token>`

**Response** (204 No Content)

**Errors**:
- 403: Not the comment author

---

## Category & Tag Endpoints

### GET /api/v1/categories
Get user's categories.

**Headers**: `Authorization: Bearer <token>`

**Response** (200 OK): Array of category objects

---

### POST /api/v1/categories
Create a new category.

**Headers**: `Authorization: Bearer <token>`

**Request**:
```json
{
  "name": "Work",
  "colorHex": "#FF5733"
}
```

**Response** (201 Created): Created category

**Errors**:
- 409: Category name already exists for user

---

### GET /api/v1/tags
Get user's tags.

**Headers**: `Authorization: Bearer <token>`

**Response** (200 OK): Array of tag objects

---

### POST /api/v1/tags
Create a new tag.

**Headers**: `Authorization: Bearer <token>`

**Request**:
```json
{
  "name": "urgent"
}
```

**Response** (201 Created): Created tag

---

## Task Sharing Endpoints

### POST /api/v1/tasks/{taskId}/share
Share a task with another user.

**Headers**: `Authorization: Bearer <token>`

**Request**:
```json
{
  "email": "colleague@example.com",
  "permissionLevel": "EDIT"  // or "VIEW"
}
```

**Response** (201 Created): TaskShare object

**Errors**:
- 403: User doesn't own the task
- 404: Target user not found

---

### DELETE /api/v1/tasks/{taskId}/share/{userId}
Revoke task sharing.

**Headers**: `Authorization: Bearer <token>`

**Response** (204 No Content)

---

### GET /api/v1/tasks/shared-with-me
Get tasks shared with current user.

**Headers**: `Authorization: Bearer <token>`

**Response** (200 OK): Paginated task list

---

## File Attachment Endpoints

### POST /api/v1/tasks/{taskId}/attachments
Upload a file attachment.

**Headers**:
- `Authorization: Bearer <token>`
- `Content-Type: multipart/form-data`

**Request**: Multipart file upload

**Response** (201 Created):
```json
{
  "id": 1,
  "fileName": "proposal.pdf",
  "fileType": "application/pdf",
  "fileSizeBytes": 1048576,
  "virusScanStatus": "PENDING",
  "uploadedAt": "2025-12-26T10:00:00Z",
  "downloadUrl": "/api/v1/attachments/1/download"
}
```

**Errors**:
- 400: File too large (>25MB), invalid file type
- 413: Payload too large

---

### GET /api/v1/attachments/{id}/download
Download a file attachment.

**Headers**: `Authorization: Bearer <token>`

**Response** (200 OK): Binary file stream with appropriate Content-Type

**Errors**:
- 403: User doesn't have access to parent task
- 404: Attachment not found
- 423: Virus scan in progress or file infected

---

### DELETE /api/v1/attachments/{id}
Delete a file attachment.

**Headers**: `Authorization: Bearer <token>`

**Response** (204 No Content)

---

## Time Tracking Endpoints

### POST /api/v1/tasks/{taskId}/time-entries/start
Start a timer on a task.

**Headers**: `Authorization: Bearer <token>`

**Response** (201 Created):
```json
{
  "id": 1,
  "startTime": "2025-12-26T10:00:00Z",
  "endTime": null,
  "entryType": "TIMER"
}
```

---

### POST /api/v1/time-entries/{id}/stop
Stop a running timer.

**Headers**: `Authorization: Bearer <token>`

**Response** (200 OK):
```json
{
  "id": 1,
  "startTime": "2025-12-26T10:00:00Z",
  "endTime": "2025-12-26T11:30:00Z",
  "durationMinutes": 90,
  "entryType": "TIMER"
}
```

---

### POST /api/v1/tasks/{taskId}/time-entries
Manually log time.

**Headers**: `Authorization: Bearer <token>`

**Request**:
```json
{
  "startTime": "2025-12-26T10:00:00Z",
  "endTime": "2025-12-26T11:30:00Z",
  "entryType": "MANUAL"
}
```

**Response** (201 Created): Created time entry

---

### GET /api/v1/time-entries/report
Get time tracking report.

**Headers**: `Authorization: Bearer <token>`

**Query Parameters**:
- `startDate`: ISO date
- `endDate`: ISO date
- `categoryId`: number
- `tagIds`: comma-separated numbers

**Response** (200 OK):
```json
{
  "totalMinutes": 1200,
  "byTask": [
    {
      "taskId": 1,
      "taskDescription": "Complete proposal",
      "totalMinutes": 120
    }
  ],
  "byCategory": [...],
  "byTag": [...]
}
```

---

## Notification Endpoints

### GET /api/v1/notifications
Get user's notifications.

**Headers**: `Authorization: Bearer <token>`

**Query Parameters**:
- `unreadOnly`: boolean
- `page`, `size`, `sort`

**Response** (200 OK): Paginated notifications

---

### PATCH /api/v1/notifications/{id}/read
Mark notification as read.

**Headers**: `Authorization: Bearer <token>`

**Response** (200 OK): Updated notification

---

### PATCH /api/v1/notifications/mark-all-read
Mark all notifications as read.

**Headers**: `Authorization: Bearer <token>`

**Response** (200 OK): Count of updated notifications

---

### GET /api/v1/notification-preferences
Get notification preferences.

**Headers**: `Authorization: Bearer <token>`

**Response** (200 OK): Array of preferences

---

### PUT /api/v1/notification-preferences/{type}
Update notification preference for a type.

**Headers**: `Authorization: Bearer <token>`

**Request**:
```json
{
  "emailEnabled": true,
  "inAppEnabled": true,
  "pushEnabled": false
}
```

**Response** (200 OK): Updated preference

---

## WebSocket Real-Time Updates

### Connection
**Endpoint**: `ws://localhost:8080/ws`
**Protocol**: STOMP over WebSocket
**Authentication**: JWT token in connection headers

### Subscribe to Task Updates
**Topic**: `/user/queue/tasks`

Receives real-time updates when:
- Task is created/updated/deleted
- Task is shared with user
- Comment added to user's task
- Collaborator updates shared task

**Message Format**:
```json
{
  "type": "TASK_UPDATED",
  "taskId": 1,
  "task": { ... },
  "timestamp": "2025-12-26T10:00:00Z"
}
```

### Subscribe to Notifications
**Topic**: `/user/queue/notifications`

Receives real-time in-app notifications.

---

## Error Response Format

All errors follow consistent format:

```json
{
  "timestamp": "2025-12-26T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Task description cannot be empty",
  "path": "/api/v1/tasks",
  "errors": [
    {
      "field": "description",
      "rejectedValue": "",
      "message": "must not be blank"
    }
  ]
}
```

---

## Rate Limiting

- **Anonymous**: 60 requests/minute
- **Authenticated**: 1000 requests/minute
- **File uploads**: 10 uploads/minute

Headers:
- `X-RateLimit-Limit`: Total requests allowed
- `X-RateLimit-Remaining`: Requests remaining
- `X-RateLimit-Reset`: Unix timestamp when limit resets

---

## API Versioning

- Current version: v1
- Version in URL: `/api/v1/...`
- Version in Accept header: `application/vnd.todoapp.v1+json`

Future versions will maintain backward compatibility or provide migration path.

---

## Security Headers

All responses include:
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `X-XSS-Protection: 1; mode=block`
- `Strict-Transport-Security: max-age=31536000` (production)

---

## Health & Monitoring

### GET /actuator/health
Public health check endpoint.

**Response** (200 OK):
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "redis": {"status": "UP"},
    "rabbitmq": {"status": "UP"},
    "minio": {"status": "UP"}
  }
}
```

### GET /actuator/info
Application information.

**Response** (200 OK):
```json
{
  "app": {
    "name": "TODO Application",
    "version": "1.0.0"
  }
}
```

### GET /actuator/metrics
Prometheus-compatible metrics (protected).

---

This API contract covers all 104 functional requirements from the specification. Full OpenAPI 3.0 YAML spec can be auto-generated from Spring Boot annotations using SpringDoc.
