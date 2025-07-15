# Real-time Notification System

## Overview
TaskMaster implements real-time notifications using WebSocket with STOMP protocol. This enables instant updates for task assignments, comments, and other important events.

## Features
- Real-time task assignment notifications
- Task update notifications
- Comment notifications
- Team invite notifications
- User-specific notification routing
- Persistent notification storage

## WebSocket Connection

### 1. Connect to WebSocket
```javascript
// Client-side JavaScript
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({
  'Authorization': 'Bearer YOUR_JWT_TOKEN'
}, function(frame) {
  console.log('Connected: ' + frame);
  // Subscribe to personal notifications
  stompClient.subscribe('/user/queue/notifications', function(notification) {
    console.log(JSON.parse(notification.body));
  });
});
```

### 2. Subscribe to Notifications
```bash
# STOMP subscription URL format
/user/queue/notifications  # Personal notifications
/topic/team/{teamId}      # Team-wide notifications
/topic/task/{taskId}      # Task-specific notifications
```

## API Endpoints

### 1. Get Unread Notifications
```bash
curl -X GET http://localhost:8080/api/notifications/unread \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN'
```

**Response**:
```json
{
  "notifications": [
    {
      "id": "notif123",
      "type": "TASK_ASSIGNED",
      "message": "You have been assigned to task: Project Review",
      "taskId": "task456",
      "createdAt": "2025-07-16T02:51:03Z",
      "read": false
    }
  ],
  "count": 1
}
```

### 2. Mark Notification as Read
```bash
curl -X PUT http://localhost:8080/api/notifications/{notificationId}/read \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN'
```

### 3. Get Notification Preferences
```bash
curl -X GET http://localhost:8080/api/notifications/preferences \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN'
```

## Business Logic & Edge Cases

### 1. Notification Types
- `TASK_ASSIGNED`: When user is assigned to a task
- `TASK_UPDATED`: When a task's important fields are modified
- `TASK_COMMENTED`: When someone comments on user's task
- `TASK_COMPLETED`: When a task is marked as complete
- `TEAM_INVITE`: When user is invited to a team
- `MENTION`: When user is mentioned in a comment

### 2. Delivery Rules
- Notifications are sent only to relevant users
- Task notifications go to assignee and creator
- Team notifications go to all team members
- Comment notifications go to task participants
- Mentions notify the mentioned user

### 3. Edge Cases Handled
1. **Connection Loss**:
   - Notifications are persisted in database
   - Delivered when user reconnects
   - Client implements reconnection strategy

2. **Multiple Sessions**:
   - Notifications delivered to all active sessions
   - Read status synced across sessions
   - Last-read timestamp maintained

3. **Rate Limiting**:
   - Maximum 10 notifications per second per user
   - Batching for high-frequency updates
   - Throttling for excessive activity

## Testing WebSocket Notifications

### 1. Using Test Client
```bash
# Start test client
npm install -g wscat
wscat -c ws://localhost:8080/ws

# Connect with authentication
{"type": "CONNECT", "token": "YOUR_JWT_TOKEN"}
```

### 2. Test Notification Flow
1. Open test page: `http://localhost:8080/notification-demo.html`
2. Login with test credentials
3. Subscribe to notifications
4. Trigger test notifications:
```bash
curl -X POST http://localhost:8080/api/test/notifications/send \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -H 'Content-Type: application/json' \
  -d '{
    "type": "TEST_NOTIFICATION",
    "message": "Test notification"
  }'
```

### 3. Monitor Multiple Notifications
1. Open multiple browser windows
2. Login with different users
3. Perform actions that trigger notifications:
   - Assign tasks between users
   - Add comments
   - Update task status
4. Verify real-time delivery
5. Check notification persistence

## Error Handling

### 1. Connection Errors
```json
{
  "type": "ERROR",
  "code": "CONNECTION_FAILED",
  "message": "Failed to establish WebSocket connection"
}
```

### 2. Subscription Errors
```json
{
  "type": "ERROR",
  "code": "SUBSCRIPTION_FAILED",
  "message": "Failed to subscribe to notification channel"
}
```

### 3. Delivery Errors
```json
{
  "type": "ERROR",
  "code": "DELIVERY_FAILED",
  "message": "Failed to deliver notification",
  "notificationId": "notif123"
}
```

## Best Practices

1. **Connection Management**:
   - Implement exponential backoff for reconnection
   - Handle connection timeouts gracefully
   - Monitor connection health

2. **Performance**:
   - Use message batching for bulk updates
   - Implement client-side caching
   - Clean up old notifications periodically

3. **Security**:
   - Validate all WebSocket messages
   - Implement proper authentication
   - Use secure WebSocket (wss://) in production
