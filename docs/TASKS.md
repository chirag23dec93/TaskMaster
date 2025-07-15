# Task Management

## Overview
TaskMaster provides comprehensive task management with features like creation, assignment, sorting, filtering, and soft deletion.

## Task Operations

### 1. Create Task
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "Project Review",
    "description": "Review Q3 project deliverables",
    "priority": "HIGH",
    "dueDate": "2025-08-01T00:00:00Z",
    "status": "PENDING"
  }'
```

**Response**:
```json
{
  "id": "task123",
  "title": "Project Review",
  "description": "Review Q3 project deliverables",
  "priority": "HIGH",
  "status": "PENDING",
  "dueDate": "2025-08-01T00:00:00Z",
  "createdBy": "john.doe",
  "createdAt": "2025-07-16T02:51:03Z"
}
```

### 2. Assign Task
```bash
curl -X PUT http://localhost:8080/api/tasks/{taskId}/assign \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -H 'Content-Type: application/json' \
  -d '{
    "assigneeUsername": "jane.smith"
  }'
```

### 3. Update Task Status
```bash
curl -X PUT http://localhost:8080/api/tasks/{taskId}/status \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -H 'Content-Type: application/json' \
  -d '{
    "status": "IN_PROGRESS"
  }'
```

### 4. Search Tasks
```bash
# Search by title with sorting
curl -X GET 'http://localhost:8080/api/tasks/search?title=review&sortField=dueDate&sortDir=ASC' \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN'

# Search by description
curl -X GET 'http://localhost:8080/api/tasks/search?description=project&sortField=priority' \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN'

# Combined search
curl -X GET 'http://localhost:8080/api/tasks/search?title=review&description=project&searchType=both' \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN'
```

### 5. Delete Task
```bash
curl -X DELETE http://localhost:8080/api/tasks/{taskId} \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN'
```

## Business Logic & Edge Cases

### 1. Task Creation
- Title is required and must be 3-100 characters
- Description is optional but limited to 1000 characters
- Due date must be in the future
- Priority must be one of: LOW, MEDIUM, HIGH
- Status must be one of: PENDING, IN_PROGRESS, COMPLETED, BLOCKED

### 2. Task Assignment
- Only task creator can assign tasks
- Cannot assign to non-existent users
- Cannot assign already assigned tasks
- Cannot assign completed tasks
- Assignment triggers notification

### 3. Status Updates
- Only assignee can update status
- Status transitions must be valid:
  - PENDING → IN_PROGRESS
  - IN_PROGRESS → COMPLETED
  - IN_PROGRESS → BLOCKED
  - BLOCKED → IN_PROGRESS
- Status update triggers notification

### 4. Search & Filtering
- Case-insensitive search
- Multiple sort fields supported
- Sort directions: ASC, DESC
- Search types:
  - "any": Match title OR description
  - "both": Match title AND description
- Pagination support (default 20 items per page)

### 5. Soft Deletion
- Tasks are never physically deleted
- Deleted tasks are marked with:
  - deleted = true
  - deletedAt timestamp
  - deletedBy username
- Archives are preserved
- Associated records (comments, attachments) remain

## Error Handling

### 1. Invalid Task Data
```json
{
  "status": 400,
  "errorCode": "VALIDATION_ERROR",
  "message": "Invalid task data",
  "validationErrors": {
    "title": "Title is required",
    "dueDate": "Due date must be in the future"
  }
}
```

### 2. Assignment Error
```json
{
  "status": 400,
  "errorCode": "TASK_ALREADY_ASSIGNED",
  "message": "Task is already assigned to another user"
}
```

### 3. Status Update Error
```json
{
  "status": 400,
  "errorCode": "INVALID_STATUS_TRANSITION",
  "message": "Cannot transition from COMPLETED to IN_PROGRESS"
}
```

## Task States & Transitions

```
PENDING → IN_PROGRESS → COMPLETED
         ↕
         BLOCKED
```

### State Rules
1. **PENDING**:
   - Initial state for new tasks
   - Can be assigned
   - Can be deleted

2. **IN_PROGRESS**:
   - Task is being worked on
   - Can be blocked
   - Can be completed
   - Cannot be deleted

3. **BLOCKED**:
   - Work is temporarily stopped
   - Requires reason for blocking
   - Can return to IN_PROGRESS
   - Cannot be completed

4. **COMPLETED**:
   - Final state
   - Cannot be reassigned
   - Cannot be deleted
   - Preserved in archive

## Best Practices

1. **Task Creation**:
   - Use clear, descriptive titles
   - Set realistic due dates
   - Include necessary context in description
   - Assign appropriate priority

2. **Task Management**:
   - Regularly update task status
   - Document blocking issues
   - Use comments for updates
   - Attach relevant files

3. **Search & Organization**:
   - Use consistent naming conventions
   - Add proper descriptions
   - Use appropriate status updates
   - Archive completed tasks
