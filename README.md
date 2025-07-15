# TaskMaster

Author: Chirag Arora

## About
TaskMaster is a comprehensive task management system built with Spring Boot. It provides a robust platform for team collaboration, task tracking, and project management with features like real-time notifications, file attachments, and AI-powered task generation.

## Tech Stack
- **Backend Framework**: Spring Boot 3.5.3
- **Language**: Java 17
- **Build Tool**: Maven
- **Database**: H2 Database (Development), MySQL 8.0.33 (Production)
- **Security**: Spring Security with JWT Authentication
- **Documentation**: OpenAPI 3.0 (Swagger)
- **WebSocket**: STOMP for real-time notifications
- **AI Integration**: Groq API for task generation

## Prerequisites
- Java Development Kit (JDK) 17 or higher
- Maven 3.6 or higher
- MySQL 8.0.33 (for production)
- Git

## Getting Started

1. Clone the repository:
```bash
git clone https://github.com/chirag23dec93/TaskMaster.git
cd TaskMaster
```

2. Configure application properties:
```properties
# src/main/resources/application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/taskmaster
spring.datasource.username=your_username
spring.datasource.password=your_password
```

3. Build the application:
```bash
mvn clean install
```

4. Set up environment variables:
```bash
# Linux/macOS
export GROQ_API_KEY=your_api_key_here

# Windows
set GROQ_API_KEY=your_api_key_here
```

5. Run the application:
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## Security Implementation

1. **Authentication**:
   - JWT (JSON Web Token) based authentication
   - Token expiration: 24 hours
   - Secure password hashing using BCrypt

2. **Authorization**:
   - Role-based access control (RBAC)
   - Method-level security using @PreAuthorize
   - Endpoint security for sensitive operations

3. **API Security**:
   - CORS configuration
   - CSRF protection
   - Secure headers configuration

## Using the APIs

1. **API Documentation**:
   - Swagger UI: `http://localhost:8080/swagger-ui/index.html`
   - OpenAPI Spec: `http://localhost:8080/v3/api-docs`

2. **Authentication**:
```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username": "user", "password": "password"}'

# Use the returned JWT token in subsequent requests:
curl -X GET http://localhost:8080/api/tasks \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN'
```

3. **Main Features**:
   - Task Management (CRUD operations)
   - Team Collaboration
   - Real-time Notifications
   - File Attachments
   - Comments
   - AI Task Generation

## Database Schema

The application uses the following main entities:
- `User`: User management and authentication
- `Task`: Core task management
- `TaskAssignment`: Task assignment tracking
- `TaskArchive`: Historical task data
- `Team`: Team management
- `TeamInvite`: Team invitation system
- `Comment`: Task comments
- `Attachment`: File attachments
- `Notification`: System notifications

For detailed documentation on specific features, please refer to:
- [Authentication Guide](docs/AUTH.md)
- [Notification System](docs/NOTIFICATIONS.md)
- [Task Management](docs/TASKS.md)
- [Team & Collaboration](docs/TEAMS.md)
- [AI Integration](docs/AI.md)

## Contributing
Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
