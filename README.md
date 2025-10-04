# Project Manager API

A modern Spring Boot REST API for project management, similar to Jira, with features like user authentication, project tracking, task management with steps, and email notifications.

## Getting Started

### Prerequisites
- **Java 21** or higher
- **Gradle 8.x**
- **PostgreSQL 15+** (running locally on default port 5432)

### Setup

1. **Clone the Repository**
   ```bash
   git clone <repository-url>
   cd projectmanager
   ```

2. **Database Setup**
   - Create a PostgreSQL database:
     ```sql
     CREATE DATABASE projectmanagerdb;
     ```
   - Copy the example configuration:
     ```bash
     cp src/main/resources/application-example.properties src/main/resources/application.properties
     ```
   - Update database credentials in `application.properties` if needed

3. **Email Configuration**
   - Configure SMTP settings in `application.properties`
   - Example uses Mailtrap for development
   - For production, use Gmail, SendGrid, or other SMTP services

4. **JWT Secret**
   - Generate a secure secret key (minimum 256 bits recommended)
   - Update `jwt.secret` in `application.properties`
   - Example: `openssl rand -base64 64`

### Running the Application
1. Build the project:
   ```bash
   ./gradlew build
   ```
2. Run the application:
   ```bash
   ./gradlew bootRun
   ```

3. Access the application at `http://localhost:8080`

### API Documentation
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

### Default Admin User
On first startup, an admin user is automatically created if no users exist:
- **Email**: `admin@example.com` (configurable via `app.admin.email`)
- **Name**: `Admin User` (configurable via `app.admin.name`)
- **Password**: `admin123` (configurable via `app.admin.password`)
- **Important**: Change these credentials in production by setting the properties in `application.properties`

## ✨ Key Features

### Authentication & Authorization
- JWT-based authentication with access and refresh tokens
- Role-based access control (ADMIN, USER)
- Email verification for new accounts
- Secure password hashing with BCrypt

### User Management
- User registration with email verification
- Profile management (update name, email)
- Admin can view and manage all users
- Soft delete (users are marked as deleted, not removed)

### Project Management
- Create and manage projects
- Add/remove users to/from projects
- Project members can view and manage project tasks
- Automatic timestamp tracking (createdAt, updatedAt)

### Task Management with Steps
- Organize tasks into steps (similar to Jira columns)
- Assign tasks to project members
- Task status workflow: TODO → IN_PROGRESS → DONE
- Email notifications on task assignment
- Filter tasks by title, description, assignee, step, and status

### Advanced Pagination & Filtering
All list endpoints support:
- **Pagination**: `?page=0&size=10`
- **Dynamic Filters**: Optional query parameters for all fields
- **Optimized Queries**: JPA Specifications for efficient database queries

### Email Notifications
- Account verification emails
- Task assignment notifications
- Configurable SMTP settings

## API Endpoints

### Authentication
```
POST   /api/auth/register          - Register new user
POST   /api/auth/login             - Login and get tokens
POST   /api/auth/refresh           - Refresh access token
POST   /api/auth/logout            - Logout and revoke tokens
GET    /api/auth/verify-email      - Verify email with token
```

### Users (Authentication Required)
```
GET    /api/users                  - List all users (Admin only)
GET    /api/users/{id}             - Get user by ID
PUT    /api/users/{id}             - Update user
DELETE /api/users/{id}             - Delete user
```

**Filters**: `name`, `email`, `role`, `emailVerified`

### Projects (Authentication Required)
```
GET    /api/projects               - List user's projects
POST   /api/projects               - Create project
GET    /api/projects/{id}          - Get project details
PUT    /api/projects/{id}          - Update project
DELETE /api/projects/{id}          - Delete project
POST   /api/projects/{id}/users/{userId}   - Add user to project
DELETE /api/projects/{id}/users/{userId}   - Remove user from project
```

**Filters**: `name`, `description`, `userId`

### Steps (Authentication Required)
```
GET    /api/steps/project/{projectId}  - List project steps
POST   /api/steps/project/{projectId}  - Create step
GET    /api/steps/{id}                 - Get step details
PUT    /api/steps/{id}                 - Update step
DELETE /api/steps/{id}                 - Delete step
```

**Filters**: `name`

### Tasks (Authentication Required)
```
GET    /api/tasks/project/{projectId}  - List project tasks
POST   /api/tasks                      - Create task
GET    /api/tasks/{id}                 - Get task details
PUT    /api/tasks/{id}                 - Update task
DELETE /api/tasks/{id}                 - Delete task
PUT    /api/tasks/{id}/status          - Update task status (workflow)
```

**Filters**: `title`, `description`, `assignedToId`, `stepId`, `status`

## Security
- JWT tokens are used for authentication. Access tokens expire after 15 minutes; refresh tokens can be used to obtain new access tokens.
- Endpoints are secured with `@PreAuthorize` annotations to ensure role-based access control.

## Testing
- Unit tests for controllers are provided in the `src/test/java` directory using JUnit 5 and Mockito.
- Run tests with: `./gradlew test`

## Troubleshooting
- **Database Connection Issues**: Ensure PostgreSQL is running and the credentials in `application.properties` match your setup.
- **Email Not Sending**: Verify SMTP settings and credentials. Check logs for errors.
- **Token Issues**: Ensure `jwt.secret` is set to a long, secure string and tokens are included in the `Authorization` header as `Bearer <token>`.