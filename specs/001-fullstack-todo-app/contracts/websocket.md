# WebSocket Contract: Real-Time Updates

**Protocol**: STOMP over WebSocket
**Library**: Spring WebSocket + SockJS
**Endpoint**: `/ws`
**Authentication**: JWT token in STOMP headers

## Connection Flow

1. **Client connects** to `ws://localhost:8080/ws` (or `wss://` in production)
2. **Client sends CONNECT frame** with JWT token:
```
CONNECT
Authorization:Bearer eyJhbGciOiJIUzI1NiIs...
accept-version:1.1,1.0
heart-beat:10000,10000

^@
```

3. **Server responds** with CONNECTED frame:
```
CONNECTED
version:1.1
heart-beat:10000,10000

^@
```

4. **Client subscribes** to topics
5. **Server sends messages** to subscribed topics
6. **Client sends** updates via SEND frames
7. **Client disconnects** with DISCONNECT frame

---

## Topics

### /user/queue/tasks
**Purpose**: Real-time task updates for current user

**Subscription**:
```
SUBSCRIBE
id:sub-0
destination:/user/queue/tasks

^@
```

**Message Types**:

#### TASK_CREATED
```json
{
  "type": "TASK_CREATED",
  "taskId": 1,
  "task": {
    "id": 1,
    "description": "New task",
    "isCompleted": false,
    "priority": "HIGH",
    "dueDate": "2025-12-31T23:59:59Z",
    "createdAt": "2025-12-26T10:00:00Z"
  },
  "timestamp": "2025-12-26T10:00:00Z"
}
```

#### TASK_UPDATED
```json
{
  "type": "TASK_UPDATED",
  "taskId": 1,
  "task": { ... },  // Full updated task
  "changes": ["description", "dueDate"],  // Changed fields
  "updatedBy": {
    "id": 2,
    "name": "Collaborator Name"
  },
  "timestamp": "2025-12-26T10:00:00Z"
}
```

#### TASK_DELETED
```json
{
  "type": "TASK_DELETED",
  "taskId": 1,
  "deletedBy": {
    "id": 1,
    "name": "Task Owner"
  },
  "timestamp": "2025-12-26T10:00:00Z"
}
```

#### TASK_COMPLETED
```json
{
  "type": "TASK_COMPLETED",
  "taskId": 1,
  "completedBy": {
    "id": 1,
    "name": "John Doe"
  },
  "timestamp": "2025-12-26T10:00:00Z"
}
```

#### TASK_SHARED
```json
{
  "type": "TASK_SHARED",
  "taskId": 1,
  "task": { ... },
  "sharedBy": {
    "id": 1,
    "name": "Task Owner"
  },
  "permissionLevel": "EDIT",
  "timestamp": "2025-12-26T10:00:00Z"
}
```

---

### /user/queue/notifications
**Purpose**: In-app notifications

**Subscription**:
```
SUBSCRIBE
id:sub-1
destination:/user/queue/notifications

^@
```

**Message Format**:
```json
{
  "type": "NOTIFICATION",
  "notification": {
    "id": 1,
    "notificationType": "DUE_DATE",
    "message": "Task 'Complete proposal' is due tomorrow",
    "relatedTaskId": 1,
    "isRead": false,
    "createdAt": "2025-12-26T10:00:00Z"
  },
  "timestamp": "2025-12-26T10:00:00Z"
}
```

---

### /user/queue/comments
**Purpose**: Real-time comment updates on user's tasks

**Subscription**:
```
SUBSCRIBE
id:sub-2
destination:/user/queue/comments

^@
```

**Message Types**:

#### COMMENT_ADDED
```json
{
  "type": "COMMENT_ADDED",
  "taskId": 1,
  "comment": {
    "id": 1,
    "content": "Updated the draft",
    "author": {
      "id": 2,
      "name": "Collaborator"
    },
    "createdAt": "2025-12-26T10:00:00Z"
  },
  "timestamp": "2025-12-26T10:00:00Z"
}
```

#### COMMENT_UPDATED
```json
{
  "type": "COMMENT_UPDATED",
  "taskId": 1,
  "commentId": 1,
  "comment": { ... },
  "timestamp": "2025-12-26T10:00:00Z"
}
```

#### COMMENT_DELETED
```json
{
  "type": "COMMENT_DELETED",
  "taskId": 1,
  "commentId": 1,
  "timestamp": "2025-12-26T10:00:00Z"
}
```

---

### /user/queue/time-tracking
**Purpose**: Real-time timer updates

**Subscription**:
```
SUBSCRIBE
id:sub-3
destination:/user/queue/time-tracking

^@
```

**Message Types**:

#### TIMER_STARTED
```json
{
  "type": "TIMER_STARTED",
  "timeEntryId": 1,
  "taskId": 1,
  "startTime": "2025-12-26T10:00:00Z",
  "timestamp": "2025-12-26T10:00:00Z"
}
```

#### TIMER_STOPPED
```json
{
  "type": "TIMER_STOPPED",
  "timeEntryId": 1,
  "taskId": 1,
  "startTime": "2025-12-26T10:00:00Z",
  "endTime": "2025-12-26T11:30:00Z",
  "durationMinutes": 90,
  "timestamp": "2025-12-26T11:30:00Z"
}
```

---

## Client-to-Server Messages

### Send Task Update
**Destination**: `/app/tasks/update`

**Payload**:
```json
{
  "taskId": 1,
  "updates": {
    "description": "Updated description",
    "priority": "HIGH"
  }
}
```

**Response**: Broadcast to all collaborators via `/user/queue/tasks`

---

### Send Comment
**Destination**: `/app/tasks/{taskId}/comments`

**Payload**:
```json
{
  "content": "New comment"
}
```

**Response**: Broadcast to task owner and collaborators via `/user/queue/comments`

---

### Send Typing Indicator (Optional)
**Destination**: `/app/tasks/{taskId}/typing`

**Payload**:
```json
{
  "isTyping": true
}
```

**Response**: Broadcast to collaborators (ephemeral, not stored)

---

## Heartbeat

**Client heartbeat**: Every 10 seconds
**Server heartbeat**: Every 10 seconds

If no heartbeat received for 30 seconds, connection is considered dead and will be closed.

---

## Error Handling

### Authentication Failure
```json
{
  "type": "ERROR",
  "code": "UNAUTHORIZED",
  "message": "Invalid or expired token",
  "timestamp": "2025-12-26T10:00:00Z"
}
```

### Subscription Denied
```json
{
  "type": "ERROR",
  "code": "FORBIDDEN",
  "message": "No access to task 123",
  "timestamp": "2025-12-26T10:00:00Z"
}
```

---

## Connection Management

### Reconnection Strategy
1. **Exponential backoff**: 1s, 2s, 4s, 8s, 16s, 30s (max)
2. **Automatic resubscription**: Client resubscribes to all previous topics
3. **State sync**: Client requests full state after reconnection

### Browser Tab Management
- **Multiple tabs**: Each tab maintains separate connection
- **Tab close**: Server cleans up subscription
- **Tab visibility**: Can pause subscriptions when tab hidden (optional optimization)

---

## Performance Considerations

1. **Message batching**: Server batches rapid updates (e.g., bulk operations)
2. **Throttling**: Max 100 messages/second per user
3. **Selective updates**: Only send changed fields in TASK_UPDATED
4. **Compression**: Enable WebSocket compression for large messages

---

## Security

1. **Token validation**: Every message validates JWT
2. **Authorization**: Check user permissions before broadcasting
3. **Rate limiting**: Prevent spam/DoS
4. **Input sanitization**: Sanitize all client-sent content

---

## Spring Boot Configuration

```yaml
spring:
  websocket:
    message-size-limit: 64KB
    send-time-limit: 20s
    send-buffer-size-limit: 512KB
```

---

## Client Implementation Example (JavaScript)

```javascript
import SockJS from 'sockjs-client';
import {Client} from '@stomp/stompjs';

const client = new Client({
  webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
  connectHeaders: {
    Authorization: `Bearer ${token}`
  },
  debug: (str) => console.log(str),
  reconnectDelay: 5000,
  heartbeatIncoming: 10000,
  heartbeatOutgoing: 10000,
});

client.onConnect = () => {
  // Subscribe to task updates
  client.subscribe('/user/queue/tasks', (message) => {
    const update = JSON.parse(message.body);
    handleTaskUpdate(update);
  });

  // Subscribe to notifications
  client.subscribe('/user/queue/notifications', (message) => {
    const notification = JSON.parse(message.body);
    showNotification(notification);
  });
};

client.activate();
```

---

This WebSocket contract enables real-time collaboration features (P9) and live updates for all task operations, ensuring users see changes immediately without polling.
