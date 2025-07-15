# Teams, Comments & Attachments

## Teams Management

### 1. Create Team
```bash
curl -X POST http://localhost:8080/api/teams \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Engineering Team",
    "description": "Product development team"
  }'
```

### 2. Invite Team Member
```bash
curl -X POST http://localhost:8080/api/teams/{teamId}/invites \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "jane.smith@example.com",
    "role": "MEMBER"
  }'
```

### 3. Accept Team Invite
```bash
curl -X POST http://localhost:8080/api/teams/invites/accept \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -H 'Content-Type: application/json' \
  -d '{
    "token": "invite_token_123"
  }'
```

## Comments

### 1. Add Comment
```bash
curl -X POST http://localhost:8080/api/tasks/{taskId}/comments \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -H 'Content-Type: application/json' \
  -d '{
    "text": "Updated the design as requested",
    "mentions": ["@john.doe"]
  }'
```

### 2. Get Task Comments
```bash
curl -X GET http://localhost:8080/api/tasks/{taskId}/comments \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN'
```

### 3. Delete Comment
```bash
curl -X DELETE http://localhost:8080/api/tasks/{taskId}/comments/{commentId} \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN'
```

## Attachments

### 1. Upload Attachment
```bash
curl -X POST http://localhost:8080/api/tasks/{taskId}/attachments \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -H 'Content-Type: multipart/form-data' \
  -F 'file=@/path/to/document.pdf'
```

### 2. Download Attachment
```bash
curl -X GET http://localhost:8080/api/tasks/{taskId}/attachments/{attachmentId} \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  --output document.pdf
```

### 3. Delete Attachment
```bash
curl -X DELETE http://localhost:8080/api/tasks/{taskId}/attachments/{attachmentId} \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN'
```

## Business Logic & Edge Cases

### 1. Team Management
- Team names must be unique
- Maximum 50 members per team
- Member roles: ADMIN, MEMBER
- Only ADMIN can send invites
- Invites expire after 7 days
- Email verification required
- One active invite per email

### 2. Comments
- Maximum length: 1000 characters
- Supports @mentions
- HTML tags not allowed
- Only author can edit/delete
- Preserved after task completion
- Notification on @mention
- Rich text formatting supported

### 3. Attachments
- Maximum file size: 10MB
- Allowed types: pdf, doc, docx, xls, xlsx, jpg, png
- Virus scanning
- Automatic thumbnail generation
- Version control for same filename
- Storage optimization
- Metadata extraction

## Error Handling

### 1. Team Operations
```json
{
  "status": 400,
  "errorCode": "TEAM_ERROR",
  "message": "Failed to create team",
  "validationErrors": {
    "name": "Team name already exists",
    "size": "Maximum team size exceeded"
  }
}
```

### 2. Comment Operations
```json
{
  "status": 400,
  "errorCode": "COMMENT_ERROR",
  "message": "Failed to add comment",
  "validationErrors": {
    "text": "Comment text exceeds maximum length",
    "mentions": "Invalid user mention"
  }
}
```

### 3. Attachment Operations
```json
{
  "status": 400,
  "errorCode": "ATTACHMENT_ERROR",
  "message": "Failed to upload file",
  "validationErrors": {
    "size": "File size exceeds 10MB limit",
    "type": "File type not supported"
  }
}
```

## Best Practices

### 1. Team Organization
- Use descriptive team names
- Set clear team roles
- Regular member audits
- Document team purpose
- Use team templates

### 2. Comments
- Keep comments task-focused
- Use @mentions sparingly
- Follow thread structure
- Update status in comments
- Link related tasks

### 3. Attachments
- Use descriptive filenames
- Compress large files
- Remove obsolete files
- Tag attachments
- Version important files

## Security Considerations

### 1. Team Access
- Role-based permissions
- Audit logging
- Member activity tracking
- Secure invite process
- Data isolation

### 2. Comment Security
- Content sanitization
- XSS prevention
- Mention validation
- Edit history
- Access control

### 3. Attachment Security
- Virus scanning
- File type validation
- Access logging
- Encryption at rest
- Secure download links

## Performance Optimization

### 1. Team Operations
- Member list pagination
- Activity log caching
- Invite batch processing
- Team hierarchy caching
- Role permission caching

### 2. Comments
- Lazy loading
- Pagination
- Mention caching
- Thread optimization
- Search indexing

### 3. Attachments
- Chunked upload
- CDN integration
- Thumbnail caching
- Streaming download
- Background processing
