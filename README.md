# TODO Application

A full-stack TODO list application with advanced features including real-time collaboration, file attachments, time tracking, and notifications.

## Technology Stack

### Backend
- **Java 21 LTS** with Spring Boot 3.2.x
- **Spring Data JPA** with Hibernate
- **Spring Security** with JWT authentication
- **PostgreSQL 16** database
- **Redis** for caching and sessions
- **RabbitMQ** for async messaging
- **MinIO** for S3-compatible object storage
- **WebSockets** for real-time updates
- **Flyway** for database migrations

### Frontend
- **React 18.x** with TypeScript 5.x
- **Vite 5.x** for blazing-fast development
- **TanStack Query** for server state management
- **Tailwind CSS** for styling
- **React Router** for navigation
- **Axios** for HTTP client

### Infrastructure
- **Docker Compose** for local development
- **Nginx** for frontend serving
- **TestContainers** for integration testing
- **JaCoCo** for code coverage
- **OpenAPI/Swagger** for API documentation

## Quick Start

### Prerequisites

- **Docker** and **Docker Compose** (recommended)
- **OR** Manual setup:
  - Java 21 JDK
  - Node.js 20+
  - Maven 3.9+
  - PostgreSQL 16
  - Redis 7
  - RabbitMQ 3.12
  - MinIO (optional)

### Running with Docker Compose (Recommended)

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd todo-pg-java
   ```

2. **Copy environment file**
   ```bash
   cp .env.example .env
   # Edit .env with your configuration (optional for local dev)
   ```

3. **Start all services**
   ```bash
   docker compose up -d
   ```

4. **Access the application**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080
   - API Documentation: http://localhost:8080/swagger-ui.html
   - RabbitMQ Management: http://localhost:15672 (user/pass: todoapp/todoapp)
   - MinIO Console: http://localhost:9001 (user/pass: todoapp/todoapp123)

5. **Stop all services**
   ```bash
   docker compose down
   ```

6. **Stop and remove all data**
   ```bash
   docker compose down -v
   ```

### Running Locally (Manual Setup)

#### Backend

1. **Start infrastructure services**
   ```bash
   docker compose up -d postgres redis rabbitmq minio
   ```

2. **Run database migrations**
   ```bash
   cd backend
   mvn flyway:migrate
   ```

3. **Start the backend**
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

   Backend will be available at http://localhost:8080

#### Frontend

1. **Install dependencies**
   ```bash
   cd frontend
   npm install
   ```

2. **Start development server**
   ```bash
   npm run dev
   ```

   Frontend will be available at http://localhost:3000

## Development

### Running Tests

#### Backend Tests
```bash
cd backend

# Unit tests only
mvn test

# Integration tests
mvn verify

# All tests with coverage
mvn clean verify
```

#### Frontend Tests
```bash
cd frontend

# Unit tests
npm test

# Coverage report
npm run test:coverage

# E2E tests
npm run test:e2e
```

### Code Quality

#### Backend

```bash
cd backend

# Format code (Google Java Style)
mvn spotless:apply

# Check code style
mvn checkstyle:check

# Check dependencies for vulnerabilities
mvn dependency-check:check
```

#### Frontend

```bash
cd frontend

# Lint and auto-fix
npm run lint:fix

# Format code
npm run format

# Type check
npm run type-check
```

### Pre-Commit Hooks

This project uses Git pre-commit hooks to enforce:

1. **Markdown file organization** - All `.md` files must be in `docs/` (except `README.md`, `CLAUDE.md`, and `specs/**/*.md`)
2. **Credential leak prevention** - Scans for common credential patterns
3. **Code linting** - Ensures all code passes linting before commit

The hook runs automatically before each commit. See [docs/CONTRIBUTING.md](docs/CONTRIBUTING.md) for details.

## API Documentation

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **Health Check**: http://localhost:8080/actuator/health

## Project Structure

```
.
├── backend/                 # Spring Boot backend
│   ├── src/
│   │   ├── main/java/com/todoapp/
│   │   │   ├── domain/      # Domain entities, validators
│   │   │   ├── application/ # Use cases, DTOs, services
│   │   │   ├── infrastructure/ # JPA, Redis, MinIO, RabbitMQ
│   │   │   └── presentation/   # REST controllers, WebSocket
│   │   └── main/resources/
│   │       ├── db/migration/    # Flyway migrations
│   │       └── application.yml  # Spring Boot config
│   └── pom.xml
├── frontend/                # React + Vite frontend
│   ├── src/
│   │   ├── components/      # React components
│   │   ├── pages/           # Page components
│   │   ├── services/        # API services
│   │   ├── hooks/           # Custom React hooks
│   │   └── types/           # TypeScript types
│   └── package.json
├── docs/                    # Documentation
│   ├── CONTRIBUTING.md      # Contributing guide
│   ├── ENFORCEMENT.md       # Pre-commit hook details
│   └── QUICK-REFERENCE.md   # Quick reference guide
├── specs/                   # Feature specifications
│   └── 001-fullstack-todo-app/
│       ├── spec.md          # Feature specification
│       ├── plan.md          # Implementation plan
│       ├── tasks.md         # Task breakdown
│       ├── data-model.md    # Data model
│       └── contracts/       # API contracts
├── docker-compose.yml       # Docker Compose configuration
├── .env.example             # Environment variable template
└── README.md                # This file
```

## Features

See [specs/001-fullstack-todo-app/spec.md](specs/001-fullstack-todo-app/spec.md) for complete feature specification.

### Core Features (P1-P5)
- Create, view, edit, and delete tasks
- Mark tasks as complete/incomplete
- Filter and search tasks
- Task priority levels and due dates
- Visual indicators for overdue tasks

### Multi-User Features (P6)
- User registration and authentication
- JWT-based secure login/logout
- Data isolation between users

### Organization Features (P7)
- Task categories and tags
- Multi-select tag filtering
- Category-based organization

### Collaboration Features (P8-P9)
- Task comments and notes
- Task sharing with permissions (VIEW, EDIT)
- Real-time WebSocket updates

### Advanced Features (P10-P15)
- Subtasks with hierarchical nesting (max 5 levels)
- Recurring tasks (daily, weekly, monthly)
- Time tracking with start/stop timer
- Manual time logging
- Batch operations on multiple tasks
- File attachments with virus scanning
- Rich text formatting (Markdown)
- Notifications (due dates, mentions, shares, comments)
- Email and in-app notifications
- Customizable notification preferences

## Architecture

The application follows **Hexagonal Architecture** (Ports & Adapters) with clean separation of concerns:

- **Domain Layer**: Pure business logic, framework-free
- **Application Layer**: Use cases, DTOs, orchestration
- **Infrastructure Layer**: External integrations (DB, cache, storage, messaging)
- **Presentation Layer**: REST API, WebSocket, request/response handling

The architecture implements **15-Factor App** principles including:
- API First design
- Telemetry and observability
- Security by default
- Configuration externalization
- Graceful shutdown

📖 **For detailed system architecture, deployment diagrams, and technical decisions, see [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)**

## Contributing

See [docs/CONTRIBUTING.md](docs/CONTRIBUTING.md) for contribution guidelines.

## License

[Add your license here]

## Support

For questions, issues, or feature requests, please file an issue on the GitHub repository.

---

For detailed implementation plan and task breakdown, see [specs/001-fullstack-todo-app/tasks.md](specs/001-fullstack-todo-app/tasks.md).

For quick start testing scenarios, see [specs/001-fullstack-todo-app/quickstart.md](specs/001-fullstack-todo-app/quickstart.md).
