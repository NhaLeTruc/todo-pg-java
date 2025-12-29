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
- **Make** (for simplified commands)
- **OR** Manual setup:
  - Java 21 JDK
  - Node.js 20+
  - Maven 3.9+
  - PostgreSQL 16
  - Redis 7
  - RabbitMQ 3.12
  - MinIO (optional)

### Using Makefile (Recommended)

The project includes a Makefile with convenient commands for common development tasks.

```bash
# View all available commands
make help

# Quick start - install dependencies and start all services
make install
make start

# For local development (infrastructure + manual backend/frontend)
make dev
# Then in separate terminals:
make dev-backend
make dev-frontend

# Run tests
make test

# Run linting and formatting
make lint
make format

# Check application status
make status

# View logs
make logs

# Stop services
make stop
```

### Running with Docker Compose

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
   make start
   # OR
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
   make stop
   # OR
   docker compose down
   ```

### Running Locally (Manual Setup)

#### Backend

1. **Start infrastructure services**
   ```bash
   make infra
   # OR
   docker compose up -d postgres redis rabbitmq minio
   ```

2. **Run database migrations**
   ```bash
   make db-migrate
   # OR
   cd backend && mvn flyway:migrate
   ```

3. **Start the backend**
   ```bash
   make dev-backend
   # OR
   cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

   Backend will be available at http://localhost:8080

#### Frontend

1. **Install dependencies**
   ```bash
   make install-frontend
   # OR
   cd frontend && npm install
   ```

2. **Start development server**
   ```bash
   make dev-frontend
   # OR
   cd frontend && npm run dev
   ```

   Frontend will be available at http://localhost:3000

## Development

### Makefile Commands Reference

For a complete list of available commands, run:
```bash
make help
```

Key commands for development workflow:

**Quick Start:**

- `make install` - Install all dependencies
- `make start` - Start all services with Docker
- `make dev` - Start infrastructure for local development

**Development:**

- `make dev-backend` - Run backend locally
- `make dev-frontend` - Run frontend locally
- `make logs` - View all service logs
- `make status` - Check service health

**Testing:**

- `make test` - Run all tests
- `make test-backend` - Backend tests only
- `make test-frontend` - Frontend tests only
- `make test-integration` - Integration tests
- `make test-coverage` - Generate coverage reports

**Code Quality:**

- `make lint` - Lint all code
- `make format` - Format all code
- `make check` - Run all quality checks

**Database:**

- `make db-migrate` - Run migrations
- `make db-info` - Show migration status
- `make db-rollback` - Rollback last migration

**Build:**

- `make build` - Build all artifacts
- `make docker-build` - Build Docker images

**Cleanup:**

- `make clean` - Clean build artifacts
- `make stop` - Stop all services
- `make clean-all` - Remove all data (WARNING: destructive)

### Running Tests (Manual)

#### Backend Tests
```bash
make test-backend
# OR
cd backend && mvn test

# Integration tests
make test-integration
# OR
cd backend && mvn verify

# All tests with coverage
cd backend && mvn clean verify
```

#### Frontend Tests
```bash
make test-frontend
# OR
cd frontend && npm test

# Coverage report
make test-coverage
# OR
cd frontend && npm run test:coverage

# E2E tests
make test-e2e
# OR
cd frontend && npm run test:e2e
```

### Code Quality (Manual)

#### Backend

```bash
make format-backend
# OR
cd backend && mvn spotless:apply

# Check code style
make lint-backend
# OR
cd backend && mvn checkstyle:check

# Check dependencies for vulnerabilities
cd backend && mvn dependency-check:check
```

#### Frontend

```bash
make format-frontend
# OR
cd frontend && npm run format

# Lint code
make lint-frontend
# OR
cd frontend && npm run lint

# Type check
cd frontend && npm run type-check
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

## Project Progress

**Overall Status:** 🎉 **99.4% Complete** (348/350 tasks)

**Last Updated:** 2025-12-29

### Implementation Status

**Completed Phases (18/18):**

- ✅ Phase 1: Project Setup & Configuration
- ✅ Phase 2: Foundational Infrastructure (DB, Auth, Core Services)
- ✅ Phase 3-7: Core Task Management Features (CRUD, filtering, priority, due dates)
- ✅ Phase 8: User Accounts & JWT Authentication
- ✅ Phase 9: Categories & Tags
- ✅ Phase 10: Comments & Notes
- ✅ Phase 11: Task Sharing & Real-time Collaboration (backend complete)
- ✅ Phase 12: Subtasks & Task Hierarchies
- ✅ Phase 13: Recurring Tasks
- ✅ Phase 14: Time Tracking & Duration
- ✅ Phase 15: Batch Operations
- ✅ Phase 16: File Attachments & Rich Text
- ✅ Phase 17: Notifications & Reminders
- ✅ Phase 18: Production Readiness & Observability

**Optional Remaining Work (2 tasks):**

- TasksPage WebSocket integration (real-time updates hook available)
- "Shared with me" view UI (backend API ready)

### Key Achievements

**Core Features:**

- ✅ Full CRUD operations on tasks with validation
- ✅ Advanced filtering and search capabilities
- ✅ Priority levels and due date management
- ✅ Overdue task indicators

**Security & Authentication:**

- ✅ JWT-based authentication with refresh tokens
- ✅ Role-based access control (RBAC)
- ✅ Secure password hashing with BCrypt
- ✅ Data isolation between users

**Advanced Features:**

- ✅ Hierarchical subtasks (up to 5 levels deep)
- ✅ Recurring tasks (daily, weekly, monthly patterns)
- ✅ Time tracking with start/stop timer
- ✅ Manual time logging
- ✅ File attachments with virus scanning
- ✅ Rich text formatting (Markdown support)
- ✅ Task sharing with VIEW/EDIT permissions
- ✅ Real-time WebSocket updates
- ✅ Comprehensive notification system (in-app + email)

**Production Ready:**

- ✅ Redis caching for performance
- ✅ Database connection pooling
- ✅ Async messaging with RabbitMQ
- ✅ Health checks and monitoring
- ✅ Structured logging
- ✅ OpenAPI/Swagger documentation
- ✅ Docker Compose deployment
- ✅ Comprehensive test coverage

### Testing Status

**Backend Tests:**

- Unit tests for domain logic
- Integration tests with TestContainers
- Repository tests with H2 database
- Service layer tests
- API endpoint tests

**Recent Test Fixes:**

- Fixed 20+ tests with improved mocking strategies
- Resolved H2 compatibility issues
- Fixed date calculation logic for recurring tasks
- Enhanced test data builders

For detailed task breakdown and implementation plan, see:

- [specs/001-fullstack-todo-app/tasks.md](specs/001-fullstack-todo-app/tasks.md) - Full task list
- [specs/001-fullstack-todo-app/plan.md](specs/001-fullstack-todo-app/plan.md) - Implementation plan
- [specs/001-fullstack-todo-app/spec.md](specs/001-fullstack-todo-app/spec.md) - Feature specification

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
