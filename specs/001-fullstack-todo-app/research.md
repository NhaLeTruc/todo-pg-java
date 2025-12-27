# Technology Research: Full-Stack TODO Application

**Date**: 2025-12-26
**Phase**: 0 (Research & Technology Selection)
**Purpose**: Document technology decisions, rationale, and alternatives considered for the TODO application

## Executive Summary

Selected a battle-proven Java stack centered on **Spring Boot 3.2**, **PostgreSQL 16**, **React 18**, and supporting infrastructure services. All choices prioritize:
- Open-source, community-supported software
- Production-proven stability (5+ years in production use)
- 15-Factor App compliance
- TDD/testing capabilities
- Docker Compose local development
- Minimal custom code (leverage frameworks and libraries)

## Core Technology Stack

### Backend Framework

**Decision**: Spring Boot 3.2.x with Java 21 LTS

**Rationale**:
- **Battle-tested**: 10+ years in production, millions of deployments worldwide
- **15-Factor compliance**: Built-in support for externalized config, health checks, metrics, graceful shutdown
- **TDD-friendly**: Excellent testing support (Spring Test, MockMvc, TestContainers integration)
- **Ecosystem**: Comprehensive Spring ecosystem (Security, Data, Cloud, Batch) covers all requirements
- **Java 21 features**: Virtual threads (Project Loom) for high concurrency, pattern matching, records for DTOs
- **Clean architecture**: Spring's dependency injection enables proper layering and testability
- **Community**: Massive community, extensive documentation, proven patterns

**Alternatives Considered**:
- **Quarkus**: Faster startup, smaller memory footprint, but smaller ecosystem and less mature (5 years vs Spring's 20+)
- **Micronaut**: Good for microservices, but smaller community and fewer integrations than Spring
- **Plain Jakarta EE**: Too low-level, requires more custom code, less batteries-included than Spring Boot

**Why Spring Boot Won**: Most battle-proven, richest ecosystem, best alignment with 15-Factor App, excellent testing support for TDD workflow.

---

### Database

**Decision**: PostgreSQL 16

**Rationale**:
- **Battle-tested**: 25+ years in production, proven reliability and data integrity
- **Feature-rich**: JSON support (for flexible schemas), full-text search, CTEs, window functions
- **Open-source**: Truly free, BSD license, active community
- **Performance**: Handles 10,000+ tasks per user with proper indexing
- **ACID compliance**: Strong consistency guarantees for multi-user collaboration
- **Extensions**: pg_trgm for fuzzy search, pgcrypto for encryption if needed
- **Spring Data JPA**: Excellent integration with Spring Boot

**Alternatives Considered**:
- **MySQL/MariaDB**: Good, but PostgreSQL has better JSON support and advanced features
- **MongoDB**: NoSQL flexibility, but loses ACID guarantees needed for task sharing/collaboration
- **H2**: Good for testing, but not production-ready for concurrent users

**Why PostgreSQL Won**: Best balance of reliability, features, and open-source maturity. JSON support enables flexible data models while maintaining ACID guarantees.

---

### Frontend Framework

**Decision**: React 18.x with TypeScript 5.x

**Rationale**:
- **Battle-tested**: 10+ years, used by Facebook, Airbnb, Netflix, etc.
- **Component model**: Perfect for TODO UI (TaskList, TaskItem, TaskForm components)
- **TypeScript**: Type safety reduces bugs, excellent IDE support, better refactoring
- **Ecosystem**: Massive library ecosystem (TanStack Query, React Hook Form, Tailwind)
- **Testing**: Jest + React Testing Library are industry standards
- **Virtual DOM**: Efficient updates for real-time collaboration features

**Tooling**:
- **Vite**: Fast build tool, instant HMR, optimized production builds
- **TanStack Query (React Query)**: Server state management, caching, automatic refetching
- **Tailwind CSS**: Utility-first CSS, rapid UI development, consistent design
- **React Hook Form**: Performant form handling with validation

**Alternatives Considered**:
- **Vue.js**: Simpler learning curve, but smaller ecosystem than React
- **Angular**: Full framework, but more opinionated and heavier than needed
- **Svelte**: Excellent performance, but smaller community and fewer libraries

**Why React Won**: Largest ecosystem, most battle-tested, best TypeScript support, widest talent pool.

---

### File Storage

**Decision**: MinIO (S3-compatible object storage)

**Rationale**:
- **S3-compatible**: Standard API, easy migration to AWS S3, Azure Blob, or Google Cloud Storage
- **Open-source**: Apache License 2.0, no vendor lock-in
- **Docker-friendly**: Official Docker image, easy local development
- **Performance**: High-throughput object storage, handles 25MB file uploads efficiently
- **SDKs**: Java SDK available, integrates with Spring Boot
- **Scalable**: Can run distributed or standalone

**Alternatives Considered**:
- **Local filesystem**: Simple, but not scalable, doesn't work with horizontal scaling
- **AWS S3**: Cloud-only, not suitable for local Docker Compose development
- **GridFS (MongoDB)**: Tightly couples storage to MongoDB, less flexible

**Why MinIO Won**: S3-compatible standard, works locally and in production, open-source, proven reliability.

---

### Message Queue

**Decision**: RabbitMQ 3.12

**Rationale**:
- **Battle-tested**: 15+ years in production, proven reliability
- **AMQP standard**: Open protocol, multiple client libraries
- **Use cases**: Recurring task generation, notification dispatch, async file processing
- **Features**: Message persistence, acknowledgments, dead-letter queues for reliability
- **Spring Integration**: Spring AMQP provides excellent abstraction
- **Management UI**: Built-in web UI for monitoring queues
- **Docker-friendly**: Official Docker image

**Alternatives Considered**:
- **Apache Kafka**: Over-engineered for this use case, designed for event streaming not task queues
- **Redis Streams**: Simpler, but less mature than RabbitMQ for task queuing
- **ActiveMQ**: Older, less active community than RabbitMQ

**Why RabbitMQ Won**: Perfect fit for task queue patterns (recurring tasks, notifications), battle-proven, excellent Spring support.

---

### Caching Layer

**Decision**: Redis 7.x

**Rationale**:
- **Battle-tested**: 15+ years, used by Twitter, GitHub, Stack Overflow
- **Use cases**: Session management, cache frequently accessed tasks, rate limiting
- **Performance**: In-memory, sub-millisecond latency
- **Data structures**: Supports strings, hashes, sets, sorted sets (useful for task sorting)
- **Spring Integration**: Spring Data Redis, Spring Session Redis
- **Persistence**: Optional persistence (RDB/AOF) for session durability

**Alternatives Considered**:
- **Memcached**: Simpler, but lacks data structures and persistence options
- **Hazelcast**: Distributed cache, but over-engineered for single-node dev setup
- **Ehcache**: In-process, doesn't work well with horizontal scaling

**Why Redis Won**: Industry standard, rich data structures, excellent Spring support, works for both caching and session management.

---

### Testing Stack

**Decision**: Multi-layered testing approach

**Unit Testing (TDD)**:
- **JUnit 5**: Industry standard Java testing framework
- **Mockito**: Mocking framework for isolating dependencies
- **AssertJ**: Fluent assertions for readable tests

**Integration Testing**:
- **TestContainers**: Real Docker containers for PostgreSQL, Redis, RabbitMQ, MinIO
- **REST Assured**: Fluent API testing framework
- **Spring Test**: Spring Boot test slices (@WebMvcTest, @DataJpaTest)

**Frontend Testing**:
- **Jest**: JavaScript testing framework
- **React Testing Library**: Component testing following best practices
- **Playwright**: E2E testing across browsers

**Rationale**:
- Supports hybrid testing strategy (TDD for unit, integration after)
- TestContainers ensures tests run against real infrastructure
- All tools are battle-proven and community standards

**Alternatives Considered**:
- **Testcontainers alternatives**: None - TestContainers is the industry standard
- **E2E alternatives**: Cypress (good, but Playwright has better cross-browser support)
- **Mocking alternatives**: EasyMock (older, less active than Mockito)

**Why This Stack Won**: Perfect alignment with constitution's hybrid testing strategy, industry standards, excellent developer experience.

---

### Database Migration

**Decision**: Flyway

**Rationale**:
- **Version-controlled migrations**: SQL or Java-based migrations in Git
- **Repeatable migrations**: For views, functions, procedures
- **Spring Boot integration**: Auto-runs migrations on startup
- **Rollback support**: Can roll forward/backward
- **Team collaboration**: Prevents migration conflicts

**Alternatives Considered**:
- **Liquibase**: More features (XML, YAML), but more complex than needed
- **JPA schema generation**: Not suitable for production, loses control over DDL

**Why Flyway Won**: Simpler than Liquibase, SQL-first approach, excellent Spring Boot integration.

---

### API Documentation

**Decision**: SpringDoc OpenAPI 3.0 (Swagger)

**Rationale**:
- **Auto-generated**: Documentation from code annotations
- **Interactive UI**: Swagger UI for testing endpoints
- **OpenAPI standard**: Industry-standard spec format
- **Contract testing**: Can validate responses against schema
- **Spring Boot integration**: Seamless integration

**Alternatives Considered**:
- **Manual documentation**: Error-prone, out of sync with code
- **Postman collections**: Not a standard, requires separate maintenance

**Why SpringDoc Won**: Auto-generated, standard format, interactive UI, supports contract testing.

---

### Security

**Decision**: Spring Security 6.x

**Rationale**:
- **Comprehensive**: Authentication, authorization, CSRF, CORS, session management
- **BCrypt**: Industry-standard password hashing (built-in)
- **JWT support**: For stateless auth (if needed for scaling)
- **Method security**: Annotation-based (@PreAuthorize, @Secured)
- **Battle-tested**: Proven in enterprise applications

**Alternatives Considered**:
- **Custom auth**: Not recommended, security is hard to get right
- **Apache Shiro**: Less active than Spring Security
- **Keycloak**: Full identity provider, over-engineered for this use case

**Why Spring Security Won**: Industry standard, comprehensive features, excellent Spring Boot integration.

---

### Logging

**Decision**: SLF4J + Logback with JSON encoder

**Rationale**:
- **SLF4J**: Logging facade, allows switching implementations
- **Logback**: Native SLF4J implementation, efficient, configurable
- **JSON encoder**: Structured logging for production (logstash-logback-encoder)
- **MDC**: Correlation IDs for request tracing
- **12-Factor compliant**: Logs to stdout/stderr

**Alternatives Considered**:
- **Log4j2**: Good, but Logback is Spring Boot default and works well
- **JUL (java.util.logging)**: Too basic, poor performance

**Why Logback Won**: Spring Boot default, excellent performance, structured logging support.

---

### Monitoring

**Decision**: Spring Boot Actuator + Micrometer

**Rationale**:
- **Actuator**: Health checks, metrics, info endpoints
- **Micrometer**: Vendor-neutral metrics (can export to Prometheus, Grafana, etc.)
- **Built-in metrics**: JVM, HTTP requests, database connection pools
- **Custom metrics**: Easy to add application-specific metrics
- **Zero config**: Works out of the box with Spring Boot

**Alternatives Considered**:
- **Custom metrics**: Reinventing the wheel
- **Prometheus client directly**: Less flexible than Micrometer

**Why Actuator + Micrometer Won**: Built-in, vendor-neutral, comprehensive coverage.

---

## Docker Compose Services

### Local Development Stack

```yaml
services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: todoapp
      POSTGRES_USER: todoapp
      POSTGRES_PASSWORD: todoapp_dev
    volumes:
      - postgres-data:/var/lib/postgresql/data
    ports:
      - "5432:5432"

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  rabbitmq:
    image: rabbitmq:3.12-management-alpine
    environment:
      RABBITMQ_DEFAULT_USER: todoapp
      RABBITMQ_DEFAULT_PASS: todoapp_dev
    ports:
      - "5672:5672"  # AMQP
      - "15672:15672" # Management UI

  minio:
    image: minio/minio:latest
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: todoapp
      MINIO_ROOT_PASSWORD: todoapp_dev
    volumes:
      - minio-data:/data
    ports:
      - "9000:9000"  # API
      - "9001:9001"  # Console

  backend:
    build: ./backend
    environment:
      SPRING_PROFILES_ACTIVE: dev
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/todoapp
      SPRING_DATA_REDIS_HOST: redis
      SPRING_RABBITMQ_HOST: rabbitmq
      MINIO_URL: http://minio:9000
    depends_on:
      - postgres
      - redis
      - rabbitmq
      - minio
    ports:
      - "8080:8080"

  frontend:
    build: ./frontend
    environment:
      VITE_API_URL: http://localhost:8080
    ports:
      - "3000:3000"
    depends_on:
      - backend

volumes:
  postgres-data:
  minio-data:
```

**Total Services**: 6 (Postgres, Redis, RabbitMQ, MinIO, Backend, Frontend)
**Startup Time**: ~30-40 seconds on modern hardware
**RAM Usage**: ~1.5-2GB total (within 2GB budget)

---

## Architecture Patterns

### Hexagonal Architecture (Ports & Adapters)

**Domain Layer** (framework-free):
- Entities: Task, User, Category, Tag, etc.
- Value Objects: Priority, RecurrencePattern
- Repository Interfaces (ports)
- Domain Services

**Application Layer**:
- Use Cases (TaskService, UserService)
- DTOs
- Mappers

**Infrastructure Layer** (adapters):
- JPA Implementations (repository adapters)
- MinIO Client (file storage adapter)
- RabbitMQ Producers/Consumers (messaging adapter)
- Redis Cache (cache adapter)

**Presentation Layer**:
- REST Controllers
- WebSocket Handlers
- Exception Handlers

**Rationale**: Clean separation, testable, framework-agnostic domain, easy to swap implementations.

---

### Design Patterns

1. **Repository**: Spring Data JPA for data access
2. **Service Layer**: Business logic orchestration
3. **DTO Pattern**: Decouple API from domain entities
4. **Factory**: Complex entity creation (RecurringTask)
5. **Strategy**: Multiple notification channels (email, in-app, push)
6. **Observer**: Event-driven for task changes (publish/subscribe via RabbitMQ)
7. **Builder**: Complex object construction (filters, queries)

All patterns are standard, well-documented, and have Spring Boot support.

---

## Performance Optimizations

1. **Database Indexing**:
   - Tasks: user_id, due_date, priority, created_at
   - Full-text search index on task description
   - Composite indexes for common queries

2. **Caching Strategy**:
   - Redis cache for frequently accessed tasks
   - Cache invalidation on updates
   - Session storage in Redis (stateless backend)

3. **Pagination**:
   - Spring Data JPA Pageable for large result sets
   - Cursor-based pagination for infinite scroll

4. **Lazy Loading**:
   - JPA lazy loading for associations (comments, subtasks)
   - Frontend lazy loading for long lists

5. **Connection Pooling**:
   - HikariCP (Spring Boot default) for database connections
   - Optimized pool size based on load testing

6. **Async Processing**:
   - RabbitMQ for async operations (file scanning, notifications)
   - Spring @Async for non-blocking operations
   - Virtual threads (Java 21) for high concurrency

---

## Security Measures

1. **Authentication**: Spring Security with BCrypt password hashing
2. **Authorization**: Method-level security (@PreAuthorize)
3. **CORS**: Configured for frontend origin
4. **CSRF**: Token-based protection for forms
5. **Input Validation**: Bean Validation (JSR-380)
6. **SQL Injection**: JPA/Hibernate parameterized queries
7. **XSS**: React auto-escapes by default, DOMPurify for rich text
8. **File Upload**: Type validation, size limits, virus scanning (ClamAV via API)
9. **Rate Limiting**: Redis-based rate limiter (Bucket4j)
10. **HTTPS**: TLS 1.3 in production

---

## Development Workflow

1. **TDD for Domain Logic**:
   - Write test (JUnit 5)
   - Run test (should fail - red)
   - Implement minimal code
   - Run test (should pass - green)
   - Refactor
   - Repeat

2. **Integration Testing**:
   - Write feature implementation
   - Write integration test (TestContainers)
   - Verify against real database/services
   - Fix bugs found in integration

3. **API Contract Testing**:
   - Define OpenAPI spec
   - Implement endpoint
   - Write contract test (REST Assured)
   - Validate response against schema

4. **Local Development**:
   - `docker compose up -d` (start services)
   - `mvn spring-boot:run` (backend with hot reload)
   - `npm run dev` (frontend with HMR)
   - Make changes, tests auto-run

---

## Deployment Strategy

1. **Build**: Multi-stage Docker builds for minimal image size
2. **Release**: Tag Docker images with version
3. **Run**: Docker Compose (dev), Kubernetes (production)
4. **Config**: Environment variables injected at runtime
5. **Migrations**: Flyway auto-runs on startup
6. **Health Checks**: Actuator endpoints for orchestrator probes
7. **Logs**: Centralized to stdout, collected by Docker/K8s
8. **Metrics**: Exposed via Actuator, scraped by Prometheus

---

## Conclusion

The selected stack provides:
- ✅ Battle-proven reliability (all technologies 5+ years in production)
- ✅ Open-source, community-supported
- ✅ 15-Factor App compliant
- ✅ Excellent TDD/testing support
- ✅ Docker Compose local development
- ✅ Minimal custom code (leverage frameworks)
- ✅ Scalable, maintainable, production-ready

All technology choices are justified by requirements, proven in production, and well-documented. No experimental or bleeding-edge technologies selected.
