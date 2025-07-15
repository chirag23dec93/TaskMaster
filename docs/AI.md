# AI Task Generation

## Overview
TaskMaster integrates with the Groq API to provide AI-powered task generation. This feature helps users create well-structured tasks from natural language descriptions.

## Features
- Natural language task generation
- Smart task property inference
- Priority and due date suggestions
- Task template generation
- Bulk task creation

## API Endpoints

### 1. Generate Task from Description
```bash
curl -X POST http://localhost:8080/api/ai/tasks/generate \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -H 'Content-Type: application/json' \
  -d '{
    "description": "I need to prepare a quarterly report for the finance team by next month",
    "options": {
      "inferPriority": true,
      "inferDueDate": true,
      "suggestSubtasks": true
    }
  }'
```

**Response**:
```json
{
  "task": {
    "title": "Prepare Q3 Finance Report",
    "description": "Create comprehensive quarterly report for finance team including financial analysis and key metrics",
    "priority": "HIGH",
    "dueDate": "2025-08-15T00:00:00Z",
    "suggestedSubtasks": [
      "Gather financial data",
      "Analyze quarterly metrics",
      "Create visualizations",
      "Draft report",
      "Review with stakeholders"
    ],
    "estimatedDuration": "40 hours"
  },
  "confidence": 0.92
}
```

### 2. Generate Task Template
```bash
curl -X POST http://localhost:8080/api/ai/tasks/template \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -H 'Content-Type: application/json' \
  -d '{
    "type": "PROJECT_KICKOFF",
    "context": {
      "projectName": "Mobile App Launch",
      "teamSize": 5,
      "duration": "3 months"
    }
  }'
```

### 3. Bulk Task Generation
```bash
curl -X POST http://localhost:8080/api/ai/tasks/bulk \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -H 'Content-Type: application/json' \
  -d '{
    "description": "Plan and execute company holiday party",
    "generateSubtasks": true,
    "maxTasks": 10
  }'
```

## Model Details

### 1. Model Configuration
```json
{
  "model": "meta-llama/llama-4-scout-17b-16e-instruct",
  "temperature": 0.7,
  "maxCompletionTokens": 1024,
  "topP": 1.0,
  "responseFormat": {
    "type": "json_object"
  }
}
```

### 2. Prompt Structure
```json
{
  "messages": [
    {
      "role": "system",
      "content": "You are a task planning assistant that helps create well-structured tasks from natural language descriptions."
    },
    {
      "role": "user",
      "content": "Generate a task for: {user_input}"
    }
  ]
}
```

## Testing the AI Integration

### 1. Using the Test UI
1. Open `http://localhost:8080/ai-task-generator.html`
2. Enter your task description
3. Select generation options
4. Click "Generate Task"
5. Review and edit the generated task
6. Click "Create Task" to save

### 2. Sample Test Cases
```bash
# Test Case 1: Simple Task
curl -X POST http://localhost:8080/api/ai/tasks/generate \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -d '{"description": "Schedule team meeting for next week"}'

# Test Case 2: Complex Project
curl -X POST http://localhost:8080/api/ai/tasks/generate \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -d '{"description": "Launch new product website", "options": {"suggestSubtasks": true}}'

# Test Case 3: Recurring Task
curl -X POST http://localhost:8080/api/ai/tasks/generate \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -d '{"description": "Monthly team performance review", "options": {"makeRecurring": true}}'
```

## Error Handling

### 1. AI Service Error
```json
{
  "status": 503,
  "errorCode": "AI_SERVICE_ERROR",
  "message": "Failed to communicate with Groq API",
  "details": {
    "retryAfter": 30
  }
}
```

### 2. Invalid Input Error
```json
{
  "status": 400,
  "errorCode": "INVALID_INPUT",
  "message": "Task description is too vague",
  "suggestions": [
    "Add more context",
    "Specify timeframe",
    "Include key requirements"
  ]
}
```

### 3. Generation Limit Error
```json
{
  "status": 429,
  "errorCode": "GENERATION_LIMIT_EXCEEDED",
  "message": "Daily task generation limit exceeded",
  "limits": {
    "daily": 50,
    "remaining": 0,
    "resetAt": "2025-07-17T00:00:00Z"
  }
}
```

## Best Practices

### 1. Input Guidelines
- Be specific and clear in descriptions
- Include timeframes when relevant
- Specify dependencies if any
- Mention team/project context

### 2. Generation Options
- Use `inferPriority` for automatic priority setting
- Enable `suggestSubtasks` for complex tasks
- Set `maxTokens` based on task complexity
- Adjust `temperature` for creativity vs precision

### 3. Post-Processing
- Review generated tasks before saving
- Adjust titles for clarity
- Verify due dates are realistic
- Customize subtasks if needed
- Add team-specific context

### 4. Rate Limiting
- Maximum 50 generations per user per day
- Bulk generation limited to 10 tasks per request
- 5 requests per minute per user
- Exponential backoff for retries
