# TODO Application - System Architecture

## Overview

The TODO Application is a production-ready, full-stack task management system built with modern technologies and best practices for scalability, reliability, and maintainability.

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Client Layer                                 │
│  ┌──────────────────┐    ┌──────────────────┐    ┌──────────────┐  │
│  │  Web Browser     │    │  Mobile App      │    │   Desktop    │  │
│  │  (React SPA)     │    │  (Future)        │    │   (Future)   │  │
│  └────────┬─────────┘    └────────┬─────────┘    └──────┬───────┘  │
└───────────┼──────────────────────┼─────────────────────┼───────────┘
            │                      │                     │
            │  HTTPS/WSS          │                     │
            └──────────────────────┴─────────────────────┘
                                   │
┌──────────────────────────────────┼───────────────────────────────────┐
│                         API Gateway / Load Balancer                  │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │  Nginx / AWS ALB / GCP Load Balancer                           │ │
│  │  - SSL/TLS Termination                                         │ │
│  │  - Rate Limiting                                                │ │
│  │  - Request Routing                                              │ │
│  └────────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────┼───────────────────────────────────┘
                                   │
        ┌──────────────────────────┴──────────────────────────┐
        │                                                      │
        ▼                                                      ▼
┌──────────────────────┐                            ┌─────────────────┐
│  Frontend Service    │                            │  Backend API    │
│  ┌────────────────┐  │                            │  (Spring Boot)  │
│  │  React App     │  │                            │                 │
│  │  (Nginx)       │  │                            │  ┌───────────┐  │
│  │                │  │                            │  │Controllers│  │
│  │  - Static      │  │                            │  └─────┬─────┘  │
│  │    Assets      │  │                            │        │        │
│  │  - SPA Router  │  │                            │  ┌─────▼─────┐  │
│  │  - PWA         │  │                            │  │  Services │  │
│  └────────────────┘  │                            │  └─────┬─────┘  │
└──────────────────────┘                            │        │        │
                                                    │  ┌─────▼─────┐  │
                                                    │  │Repositories│  │
                                                    │  └─────┬─────┘  │
                                                    └────────┼────────┘
                                                             │
        ┌────────────────────────────┬───────────────────────┼────────────┐
        │                            │                       │            │
        ▼                            ▼                       ▼            ▼
┌──────────────┐            ┌──────────────┐        ┌─────────────┐  ┌──────────┐
│  PostgreSQL  │            │    Redis     │        │  RabbitMQ   │  │  MinIO   │
│              │            │              │        │             │  │  (S3)    │
│  - Tasks     │            │  - Cache     │        │  - Async    │  │          │
│  - Users     │            │  - Sessions  │        │    Tasks    │  │  - Files │
│  - Comments  │            │  - Rate      │        │  - Emails   │  │  - Attach│
│  - Tags      │            │    Limiting  │        │  - Notifs   │  │    ments │
│  - Files     │            │              │        │             │  │          │
└──────────────┘            └──────────────┘        └─────────────┘  └──────────┘
```

## Component Architecture

### Frontend (React + TypeScript)

```
frontend/
├── components/           # Reusable UI components
│   ├── ErrorBoundary     # Error handling
│   ├── Skeleton          # Loading states
│   └── common/           # Shared components
├── pages/                # Route-level components
│   ├── LoginPage
│   ├── TasksPage
│   └── NotificationPreferencesPage
├── hooks/                # Custom React hooks
│   ├── useTasks          # Task operations with optimistic updates
│   ├── useComments       # Comment operations
│   └── useNotifications  # Notification management
├── services/             # API clients
│   ├── api.ts            # Axios configuration
│   ├── taskService       # Task API
│   ├── authService       # Authentication
│   └── notificationService
├── context/              # React Context providers
│   └── AuthContext       # Authentication state
└── types/                # TypeScript definitions
```

**Key Features:**
- **Optimistic Updates**: Instant UI feedback for mutations
- **React Query**: Server state management with automatic caching
- **Error Boundaries**: Graceful error handling
- **Loading Skeletons**: Better UX during data fetching
- **WebSocket Integration**: Real-time notifications

### Backend (Spring Boot + Java 21)

```
backend/
├── domain/                      # Domain model (entities)
│   ├── task/
│   ├── user/
│   ├── comment/
│   └── notification/
├── application/                 # Use cases / business logic
│   ├── service/
│   └── dto/
├── presentation/                # Controllers / API layer
│   └── rest/
├── infrastructure/              # Technical implementations
│   ├── config/                  # Configuration
│   │   ├── SecurityConfig       # JWT + Spring Security
│   │   ├── WebSocketConfig      # Real-time updates
│   │   ├── RedisCacheConfig     # Caching
│   │   ├── RateLimitConfig      # Rate limiting (Bucket4j)
│   │   ├── CorrelationIdFilter  # Request tracing
│   │   ├── MetricsConfig        # Micrometer metrics
│   │   └── GracefulShutdownConfig
│   ├── persistence/             # JPA repositories
│   ├── cache/                   # Redis caching
│   ├── messaging/               # RabbitMQ producers/consumers
│   ├── storage/                 # MinIO file storage
│   └── monitoring/              # Metrics and observability
└── resources/
    ├── db/migration/            # Flyway migrations
    ├── application.yml          # Configuration
    └── logback-spring.xml       # Logging configuration
```

**Key Features:**
- **Hexagonal Architecture**: Clear separation of concerns
- **Domain-Driven Design**: Rich domain model
- **CQRS Pattern**: Command/Query separation for complex operations
- **Event-Driven**: Async processing via RabbitMQ
- **Comprehensive Caching**: Redis for frequently accessed data
- **API Documentation**: OpenAPI/Swagger
- **Observability**: Micrometer metrics, correlation IDs, structured logging

## Data Flow

### Synchronous Request Flow

```
1. User Action (Frontend)
   ↓
2. HTTP Request → API Gateway
   ↓
3. Spring Security (JWT Validation)
   ↓
4. Rate Limiting Check (Bucket4j)
   ↓
5. Correlation ID Assignment (MDC)
   ↓
6. Controller → Service Layer
   ↓
7. Cache Check (Redis)
   ├─ Cache Hit → Return cached data
   └─ Cache Miss ↓
8. Repository → Database Query
   ↓
9. Cache Update (Redis)
   ↓
10. Metrics Recording (Micrometer)
    ↓
11. Response → Frontend
    ↓
12. Optimistic UI Update
```

### Asynchronous Event Flow

```
1. Event Trigger (e.g., Task Completion)
   ↓
2. Event Publisher → RabbitMQ
   ↓
3. Message Queue
   ↓
4. Event Consumer/Listener
   ├─ Email Notification Service
   ├─ In-App Notification Service
   └─ Analytics/Audit Service
   ↓
5. WebSocket Broadcast
   ↓
6. Real-time UI Update
```

## Data Model

### Core Entities

- **User**: Authentication and profile information
- **Task**: Main task entity with hierarchical support (subtasks)
- **Category**: Task categorization
- **Tag**: Flexible task labeling
- **Comment**: Task discussions with mention support
- **FileAttachment**: File metadata and storage references
- **Notification**: In-app and email notifications
- **NotificationPreference**: User notification settings
- **Recurrence**: Recurring task patterns
- **TimeEntry**: Time tracking for tasks

### Relationships

```
User (1) ──────── (M) Task
  │                    │
  │                    ├── (M) Comment
  │                    ├── (M) FileAttachment
  │                    ├── (M) Tag (M:M)
  │                    ├── (1) Category
  │                    ├── (1) Recurrence
  │                    └── (M) TimeEntry
  │
  └── (M) Notification
      └── (M) NotificationPreference
```

## Security Architecture

### Authentication & Authorization

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ 1. Login (username/password)
       ▼
┌─────────────────────┐
│  AuthController     │
│  /api/v1/auth/login │
└──────┬──────────────┘
       │ 2. Validate credentials
       ▼
┌─────────────────┐
│  UserService    │
└──────┬──────────┘
       │ 3. Generate JWT
       ▼
┌─────────────────┐
│   JWTService    │
└──────┬──────────┘
       │ 4. Return token
       ▼
┌─────────────┐
│   Client    │ Stores JWT in memory/localStorage
└──────┬──────┘
       │ 5. Subsequent requests with Authorization header
       ▼
┌──────────────────┐
│  JWTAuthFilter   │ Validates token, sets SecurityContext
└──────┬───────────┘
       │
       ▼
┌──────────────────┐
│   API Endpoint   │ Access granted
└──────────────────┘
```

**Security Features:**
- JWT-based stateless authentication
- BCrypt password hashing
- CORS configuration
- Rate limiting per IP address
- Input validation
- SQL injection prevention (Prepared statements)
- XSS protection
- CSRF protection for state-changing operations

## Caching Strategy

### Multi-Level Cache

```
Request
  ↓
Browser Cache (Service Worker)
  ↓
CDN Cache (Static Assets)
  ↓
Application Cache (React Query)
  ↓
API Gateway Cache
  ↓
Backend Cache (Redis)
  ↓
Database Query Cache (Hibernate L2)
  ↓
Database
```

### Cache Invalidation

- **Write-through**: Update cache on write operations
- **TTL-based**: 15-minute expiration for task data
- **Event-based**: Clear cache on entity updates
- **Manual**: Admin endpoints for cache clearing

## Deployment Architecture

### Production Deployment (Cloud-Ready)

```
┌────────────────────────────────────────────────────────┐
│                    Cloud Provider                       │
│  ┌──────────────────────────────────────────────────┐  │
│  │  Load Balancer / Ingress                         │  │
│  │  - SSL/TLS                                        │  │
│  │  - DDoS Protection                                │  │
│  └─────────────────┬────────────────────────────────┘  │
│                    │                                    │
│  ┌─────────────────┴────────────────────────────────┐  │
│  │  Kubernetes Cluster / ECS / App Service          │  │
│  │                                                   │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐       │  │
│  │  │ Frontend │  │ Frontend │  │ Frontend │       │  │
│  │  │  Pod 1   │  │  Pod 2   │  │  Pod 3   │       │  │
│  │  └──────────┘  └──────────┘  └──────────┘       │  │
│  │                                                   │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐       │  │
│  │  │ Backend  │  │ Backend  │  │ Backend  │       │  │
│  │  │  Pod 1   │  │  Pod 2   │  │  Pod 3   │       │  │
│  │  └────┬─────┘  └────┬─────┘  └────┬─────┘       │  │
│  └───────┼─────────────┼─────────────┼─────────────┘  │
│          │             │             │                 │
│  ┌───────┴─────────────┴─────────────┴─────────────┐  │
│  │  Managed Services                                │  │
│  │  ┌────────────┐  ┌──────────┐  ┌──────────┐    │  │
│  │  │ PostgreSQL │  │  Redis   │  │ RabbitMQ │    │  │
│  │  │   (RDS)    │  │(ElastiC.)│  │(AmazonMQ)│    │  │
│  │  └────────────┘  └──────────┘  └──────────┘    │  │
│  │                                                  │  │
│  │  ┌────────────────────────────────────────────┐ │  │
│  │  │  Object Storage (S3 / Cloud Storage)      │ │  │
│  │  └────────────────────────────────────────────┘ │  │
│  └──────────────────────────────────────────────────┘ │
│                                                        │
│  ┌──────────────────────────────────────────────────┐ │
│  │  Monitoring & Observability                      │ │
│  │  - CloudWatch / Stackdriver / Datadog            │ │
│  │  - Prometheus + Grafana                          │ │
│  │  - ELK Stack / Splunk                            │ │
│  └──────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────┘
```

## Observability

### Metrics (Micrometer + Prometheus)

- **Application Metrics**:
  - Request rate, latency, error rate
  - Task operations (create, update, delete, complete)
  - User authentication events
  - File upload metrics
  - Cache hit/miss ratios

- **System Metrics**:
  - JVM memory and GC
  - Thread pools
  - Database connection pools
  - HTTP connection metrics

### Logging (Logback + JSON)

- **Structured Logging**: JSON format for easy parsing
- **Correlation IDs**: Track requests across services
- **Log Levels**: DEBUG, INFO, WARN, ERROR
- **Log Aggregation**: Centralized via ELK, CloudWatch, etc.

### Tracing

- **Correlation IDs**: UUID per request in X-Correlation-ID header
- **MDC Context**: Available in all log statements
- **Distributed Tracing**: Ready for OpenTelemetry integration

## Performance Optimizations

### Backend

- **Connection Pooling**: HikariCP for database, Lettuce for Redis
- **Query Optimization**: Indexes, pagination, eager/lazy loading
- **Caching**: Redis for frequently accessed data
- **Async Processing**: RabbitMQ for non-blocking operations
- **Database Indexing**: Optimized for common queries
- **JPA Batch Operations**: Batch inserts/updates

### Frontend

- **Code Splitting**: Lazy loading of routes and components
- **React Query**: Smart caching and background refetching
- **Optimistic Updates**: Instant UI feedback
- **Memoization**: React.memo, useMemo, useCallback
- **Image Optimization**: Lazy loading, proper sizing
- **Service Worker**: Offline support and caching

## Scalability Considerations

### Horizontal Scaling

- **Stateless Backend**: Multiple instances behind load balancer
- **Sticky Sessions**: Not required (JWT-based auth)
- **Shared Cache**: Redis for distributed caching
- **Message Queue**: RabbitMQ for async task distribution

### Database Scaling

- **Read Replicas**: For read-heavy workloads
- **Connection Pooling**: Efficient connection management
- **Partitioning**: Table partitioning for large datasets
- **Archiving**: Move old data to archive tables

### Caching Strategy

- **Application-Level**: React Query, Redis
- **Database-Level**: Hibernate L2 cache
- **CDN**: Static assets and public content

## High Availability

- **Multi-AZ Deployment**: Database, cache, message broker
- **Health Checks**: Liveness and readiness probes
- **Graceful Shutdown**: 30-second timeout for in-flight requests
- **Circuit Breakers**: Prevent cascade failures
- **Retry Logic**: Exponential backoff for transient failures
- **Database Failover**: Automatic with managed services

## Disaster Recovery

- **Database Backups**: Automated daily backups
- **Point-in-Time Recovery**: Available for last 7-30 days
- **File Storage Replication**: S3/MinIO cross-region replication
- **Configuration Backup**: Infrastructure as Code (IaC)
- **Monitoring & Alerts**: Proactive issue detection

## Technology Decisions

### Why Java 21 + Spring Boot?

- **Enterprise-Ready**: Battle-tested for production workloads
- **Virtual Threads**: Improved concurrency (Project Loom)
- **Strong Ecosystem**: Extensive libraries and community support
- **Security**: Regular updates and CVE patches
- **Performance**: JVM optimizations, JIT compilation

### Why React + TypeScript?

- **Type Safety**: Catch errors at compile time
- **Developer Experience**: Excellent tooling and IDE support
- **Community**: Large ecosystem of libraries and resources
- **Performance**: Virtual DOM, code splitting, lazy loading

### Why PostgreSQL?

- **ACID Compliance**: Data integrity and consistency
- **Advanced Features**: JSON support, full-text search, arrays
- **Performance**: Query optimization, indexing, parallel queries
- **Reliability**: Proven track record in production

### Why Redis?

- **Performance**: In-memory data structure store
- **Versatility**: Cache, session store, rate limiting
- **Persistence**: Optional AOF/RDB for durability
- **Scalability**: Clustering and replication support

### Why RabbitMQ?

- **Reliability**: Message persistence and acknowledgments
- **Flexibility**: Multiple exchange types and routing
- **Management**: Excellent admin UI and monitoring
- **Community**: Well-documented and widely adopted

## Future Enhancements

- **Microservices Migration**: Split into separate services
- **GraphQL API**: Alternative to REST for flexible queries
- **Mobile Apps**: Native iOS and Android applications
- **Advanced Analytics**: Task completion trends, productivity insights
- **AI/ML Features**: Smart task suggestions, priority recommendations
- **Multi-tenancy**: Support for multiple organizations
- **Advanced Collaboration**: Real-time collaborative editing
- **Integrations**: Calendar, email, third-party tools

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on contributing to this project.

## License

[MIT License](LICENSE)
