# Authentication Guide

## Overview
TaskMaster uses JWT (JSON Web Token) based authentication. This document explains how to register, login, and manage user sessions.

## User Management

### 1. User Registration
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{
    "username": "john.doe",
    "email": "john.doe@example.com",
    "password": "securePassword123",
    "fullName": "John Doe"
  }'
```

**Response**:
```json
{
  "id": "user123",
  "username": "john.doe",
  "email": "john.doe@example.com",
  "fullName": "John Doe",
  "createdAt": "2025-07-16T02:51:03Z"
}
```

### 2. User Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{
    "username": "john.doe",
    "password": "securePassword123"
  }'
```

**Response**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "expiresIn": 86400000
}
```

### 3. User Logout
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN'
```

## JWT Token Details

### Token Structure
The JWT token contains three parts:
1. **Header**:
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

2. **Payload**:
```json
{
  "sub": "john.doe",          // Username
  "roles": ["ROLE_USER"],     // User roles
  "iat": 1689459063,         // Issued at timestamp
  "exp": 1689545463          // Expiration timestamp
}
```

3. **Signature**: HMAC-SHA256 signature

### Token Properties
- **Expiration**: 24 hours from issuance
- **Algorithm**: HS256 (HMAC with SHA-256)
- **Token Type**: Bearer

## Security Features

### Password Requirements
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one number
- At least one special character

### Account Protection
1. **Rate Limiting**:
   - 5 failed login attempts within 15 minutes triggers a temporary lockout
   - Account is locked for 30 minutes after consecutive failed attempts

2. **Session Management**:
   - Single active session per user
   - Previous sessions are invalidated on new login
   - Automatic logout on token expiration

### Error Responses

1. **Invalid Credentials**:
```json
{
  "status": 401,
  "errorCode": "INVALID_CREDENTIALS",
  "message": "Invalid username or password",
  "timestamp": "2025-07-16T02:51:03Z"
}
```

2. **Account Locked**:
```json
{
  "status": 423,
  "errorCode": "ACCOUNT_LOCKED",
  "message": "Account temporarily locked. Try again in 30 minutes",
  "timestamp": "2025-07-16T02:51:03Z"
}
```

3. **Registration Error**:
```json
{
  "status": 400,
  "errorCode": "VALIDATION_ERROR",
  "message": "Invalid registration data",
  "timestamp": "2025-07-16T02:51:03Z",
  "validationErrors": {
    "email": "Invalid email format",
    "password": "Password does not meet requirements"
  }
}
```

## Best Practices

1. **Token Storage**:
   - Store tokens securely (e.g., HttpOnly cookies)
   - Never store in localStorage or sessionStorage
   - Clear tokens on logout

2. **Security Headers**:
   - Always use HTTPS in production
   - Include appropriate security headers
   - Set secure and SameSite cookie attributes

3. **Error Handling**:
   - Never expose sensitive information in error messages
   - Use generic messages for authentication failures
   - Log detailed errors server-side only
