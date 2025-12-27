# Implementation Plan: Full-Stack TODO List Application

**Branch**: `001-fullstack-todo-app` | **Date**: 2025-12-26 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/001-fullstack-todo-app/spec.md`

## Summary

Building a comprehensive, enterprise-grade TODO list application with 15 user stories (P1-P15) covering basic task management, multi-user collaboration, and advanced productivity features. The application will use battle-proven Java technologies in a clean architecture following 15-Factor App principles, TDD for core logic, and hybrid testing strategy.

**Technical Approach**: Spring Boot microservices architecture with PostgreSQL database, React frontend, MinIO for file storage, Redis for caching, and RabbitMQ for async operations (recurring tasks, notifications). All services containerized with Docker Compose for local development and testing.

## Technical Context

**Language/Version**: Java 21 LTS (latest long-term support with virtual threads, pattern matching, records)
**Primary Dependencies**:
- **Backend**: Spring Boot 3.2.x, Spring Data JPA, Spring Security, Spring Web MVC
- **Frontend**: React 18.x, TypeScript 5.x, Vite 5.x, TanStack Query, Tailwind CSS
- **Database**: PostgreSQL 16
- **File Storage**: MinIO (S3-compatible object storage)
- **Message Queue**: RabbitMQ 3.12
- **Cache**: Redis 7.x
- **Testing**: JUnit 5, Mockito, TestContainers, REST Assured, Jest, React Testing Library

**Storage**:
- PostgreSQL 16 for relational data (tasks, users, comments, etc.)
- MinIO for file attachments (S3-compatible, open-source)
- Redis for session management and caching

**Testing**:
- JUnit 5 + Mockito for unit tests (TDD for domain logic)
- TestContainers for integration tests (real DB, real services)
- REST Assured for API contract tests
- Jest + React Testing Library for frontend unit tests
- Playwright for E2E tests

**Target Platform**: Linux containers (Docker), deployable to any container orchestration platform (Kubernetes, Docker Swarm, AWS ECS, etc.)

**Project Type**: Web application (full-stack with separate frontend/backend)

**Performance Goals**:
- < 1s for CRUD operations under normal load
- < 2s for search/filter on 10K+ tasks
- < 5s for file uploads up to 10MB
- 500 concurrent users without degradation

**Constraints**:
- < 500ms database query p95
- < 2GB RAM total across services
- < 60s startup time (`docker compose up`)
- 15-Factor App compliance (stateless, config in env vars, logs to stdout)

**Scale/Scope**:
- 15 user stories (104 functional requirements)
- 14 entity types
- 500 concurrent users
- 10,000+ tasks per user
- 1GB file storage per user

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### I. Test-Driven Development (TDD) for Core Logic ✅

- **Compliance**: JUnit 5 configured for TDD workflow
- **Core logic modules**: Domain entities, business validators, service layer
- **Test-first workflow**: Tests written before implementation for all business logic
- **Tools**: JUnit 5, Mockito, AssertJ for fluent assertions

### II. Hybrid Testing Strategy ✅

**Tier 1 - TDD Unit Tests** (written BEFORE implementation):
- Domain models (Task, User, Category, Tag, etc.)
- Business validators (TaskValidator, RecurrencePatternValidator)
- Service layer (TaskService, UserService, NotificationService)
- Utility classes (DateUtils, ValidationUtils)

**Tier 2 - Integration Tests** (written AFTER implementation):
- API endpoints (REST Assured with TestContainers)
- Database operations (Spring Data JPA with test database)
- File storage operations (MinIO with TestContainers)
- Message queue operations (RabbitMQ with TestContainers)
- Frontend components (Jest + React Testing Library)
- E2E workflows (Playwright)

**Contract Tests**:
- OpenAPI spec validation for all REST endpoints
- Schema validation for request/response payloads
- WebSocket contract tests for real-time features

### III. Clean Code & Maintainability ✅

- **Self-documenting**: Java records for DTOs, clear naming conventions
- **Single Responsibility**: Service layer methods < 30 lines, single purpose
- **DRY**: Shared validators, common utilities extracted
- **YAGNI**: No speculative generality, build for current requirements only
- **Consistent Style**: Google Java Style Guide, enforced by Checkstyle + Spotless

### IV. 15-Factor App Methodology ✅

1. **Codebase**: Single Git repo, multiple deployment environments via config
2. **Dependencies**: Maven for dependency management, all deps explicit in pom.xml
3. **Config**: Spring Boot profiles + environment variables (no hardcoded values)
4. **Backing Services**: PostgreSQL, MinIO, Redis, RabbitMQ all swappable via config
5. **Build, Release, Run**: Maven build → Docker image → container runtime
6. **Processes**: Stateless Spring Boot apps, all state in PostgreSQL/Redis
7. **Port Binding**: Embedded Tomcat, configurable ports via env vars
8. **Concurrency**: Horizontal scaling via multiple container instances
9. **Disposability**: Graceful shutdown hooks, fast startup with Spring Boot
10. **Dev/Prod Parity**: Docker Compose matches production container setup
11. **Logs**: Logback to stdout/stderr, structured JSON in production
12. **Admin Processes**: Flyway migrations, Spring Boot Actuator for ops tasks
13. **API First**: OpenAPI 3.0 spec generated from code annotations
14. **Telemetry**: Spring Boot Actuator, Micrometer for metrics, distributed tracing ready
15. **Security**: Spring Security, BCrypt password hashing, CORS config, input validation

### V. Separation of Concerns ✅

**4-Layer Architecture** (Hexagonal/Ports & Adapters):
- **Domain Layer** (`domain/`): Entities, value objects, domain services (framework-free)
- **Application Layer** (`application/`): Use cases, DTOs, service interfaces
- **Infrastructure Layer** (`infrastructure/`): JPA repositories, external APIs, file storage
- **Presentation Layer** (`presentation/`): REST controllers, WebSocket handlers, DTOs

**Dependency Flow**: Presentation → Application → Domain ← Infrastructure (ports/adapters pattern)

### VI. Progressive Enhancement ✅

**Phased Implementation Plan**:
- **Phase 1 (MVP)**: P1-P5 (basic task management) - independently deployable
- **Phase 2 (Multi-user)**: P6-P9 (auth, collaboration) - builds on Phase 1
- **Phase 3 (Advanced)**: P10-P15 (power features) - incremental additions

Each phase delivers value, can be deployed independently.

### VII. Observability & Debugging ✅

- **Structured Logging**: SLF4J + Logback with JSON encoder
- **Log Levels**: ERROR, WARN, INFO, DEBUG properly used
- **Metrics**: Micrometer + Spring Boot Actuator (request counts, durations, errors)
- **Health Checks**: `/actuator/health` endpoint for liveness/readiness
- **Correlation IDs**: MDC context for request tracing across services

### Complexity Constraints ✅

**Layers**: 4 layers (Domain, Application, Infrastructure, Presentation) - within limit
**Projects/Modules**:
- backend (Spring Boot)
- frontend (React)
- docker compose orchestration
Total: 2 main projects + 1 orchestration = 3 ✅

**Design Patterns**:
- Repository (JPA/Spring Data)
- Service Layer
- DTO Mapper
- Factory (for entities with complex creation)
All justified and commonly accepted patterns ✅

**External Dependencies**: All open-source, battle-tested:
- Spring ecosystem (10+ years production proven)
- PostgreSQL (25+ years proven)
- React (10+ years proven)
- MinIO (S3-compatible standard)
- RabbitMQ (15+ years proven)
All dependencies justified by feature requirements ✅

**VERDICT**: No constitution violations. All principles satisfied.

## Project Structure

### Documentation (this feature)

```text
specs/001-fullstack-todo-app/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (technology decisions)
├── data-model.md        # Phase 1 output (entity designs, DB schema)
├── quickstart.md        # Phase 1 output (local dev setup guide)
├── contracts/           # Phase 1 output (OpenAPI specs, WebSocket contracts)
│   ├── api-v1.yaml      # REST API OpenAPI 3.0 spec
│   └── websocket.md     # WebSocket event contracts
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created yet)
```

### Source Code (repository root)

```text
backend/
├── src/
│   ├── main/
│   │   ├── java/com/todoapp/
│   │   │   ├── domain/              # Domain layer (framework-free)
│   │   │   │   ├── model/           # Entities, Value Objects
│   │   │   │   ├── repository/      # Repository interfaces (ports)
│   │   │   │   ├── service/         # Domain services
│   │   │   │   └── validator/       # Business validators
│   │   │   ├── application/         # Application layer
│   │   │   │   ├── dto/             # Data Transfer Objects
│   │   │   │   ├── service/         # Application services (use cases)
│   │   │   │   └── mapper/          # DTO ↔ Entity mappers
│   │   │   ├── infrastructure/      # Infrastructure layer
│   │   │   │   ├── persistence/     # JPA implementations
│   │   │   │   ├── storage/         # MinIO file storage
│   │   │   │   ├── messaging/       # RabbitMQ producers/consumers
│   │   │   │   ├── cache/           # Redis cache implementations
│   │   │   │   └── config/          # Spring configuration
│   │   │   └── presentation/        # Presentation layer
│   │   │       ├── rest/            # REST controllers
│   │   │       ├── websocket/       # WebSocket handlers
│   │   │       └── exception/       # Global exception handlers
│   │   └── resources/
│   │       ├── db/migration/        # Flyway migrations
│   │       ├── application.yml      # Default config (no secrets)
│   │       └── application-dev.yml  # Dev profile
│   └── test/
│       ├── java/com/todoapp/
│       │   ├── unit/                # TDD unit tests
│       │   │   ├── domain/          # Domain logic tests
│       │   │   ├── application/     # Service tests
│       │   │   └── validator/       # Validator tests
│       │   ├── integration/         # Integration tests
│       │   │   ├── api/             # REST API tests (TestContainers)
│       │   │   ├── repository/      # JPA repository tests
│       │   │   └── storage/         # File storage tests
│       │   └── contract/            # Contract tests
│       │       └── api/             # OpenAPI validation tests
│       └── resources/
│           └── application-test.yml # Test profile
├── pom.xml                          # Maven dependencies
└── Dockerfile                       # Multi-stage build

frontend/
├── src/
│   ├── components/                  # React components
│   │   ├── tasks/                   # Task-related components
│   │   ├── auth/                    # Auth components
│   │   ├── shared/                  # Shared UI components
│   │   └── layout/                  # Layout components
│   ├── pages/                       # Page components (routes)
│   ├── services/                    # API client services
│   ├── hooks/                       # Custom React hooks
│   ├── utils/                       # Utility functions
│   ├── types/                       # TypeScript types
│   └── App.tsx                      # Root component
├── tests/
│   ├── unit/                        # Jest unit tests
│   └── e2e/                         # Playwright E2E tests
├── package.json                     # npm dependencies
├── vite.config.ts                   # Vite configuration
└── Dockerfile                       # Multi-stage build

docker-compose.yml                   # Local development stack
docker-compose.test.yml              # Testing environment
README.md                            # Project documentation
```

**Structure Decision**: Web application (Option 2) with backend/frontend separation. This aligns with:
- Full-stack requirement from spec
- Clean separation of concerns (backend REST API, frontend SPA)
- Independent scaling (frontend static files, backend API instances)
- 15-Factor compliance (stateless services, config externalized)

## Complexity Tracking

> **No violations - table not needed**

All complexity is within constitutional limits:
- 4 architectural layers (within best practices)
- 3 main components (backend, frontend, orchestration)
- All design patterns are standard Spring/JPA patterns
- All dependencies are battle-tested, open-source solutions
