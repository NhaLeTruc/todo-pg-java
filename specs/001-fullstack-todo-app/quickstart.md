# Quick Start Guide: TODO Application

**Purpose**: Get the full-stack TODO application running locally in under 5 minutes.
**Prerequisites**: Docker, Docker Compose, Java 21, Node.js 18+, Maven 3.9+

## TL;DR - One Command Start

```bash
# From project root
docker-compose up -d

# Wait 30-40 seconds for all services to start
# Open browser: http://localhost:3000
```

**Services Started**:
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- API Docs (Swagger): http://localhost:8080/swagger-ui.html
- PostgreSQL: localhost:5432
- Redis: localhost:6379
- RabbitMQ Management: http://localhost:15672 (user: todoapp, pass: todoapp_dev)
- MinIO Console: http://localhost:9001 (user: todoapp, pass: todoapp_dev)

---

## Prerequisites

### Required Software

1. **Docker Desktop** (or Docker + Docker Compose)
   - Version: 20.10+ with Compose V2
   - Download: https://www.docker.com/products/docker-desktop

2. **Java Development Kit 21** (for local backend development)
   - Download: https://adoptium.net/
   - Verify: `java -version` → should show Java 21

3. **Maven 3.9+** (for backend builds)
   - Download: https://maven.apache.org/download.cgi
   - Verify: `mvn -version` → should show 3.9+

4. **Node.js 18+** and **npm 9+** (for local frontend development)
   - Download: https://nodejs.org/
   - Verify: `node -v` → v18.x, `npm -v` → 9.x

### Optional Tools

- **Git**: For version control
- **IntelliJ IDEA** or **VS Code**: Recommended IDEs
- **Postman** or **Insomnia**: API testing
- **DBeaver** or **pgAdmin**: Database management

---

## Project Structure (After Setup)

```
todo-pg-java/
├── backend/                 # Spring Boot application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/todoapp/
│   │   │   └── resources/
│   │   └── test/
│   ├── pom.xml
│   ├── Dockerfile
│   └── README.md
├── frontend/                # React application
│   ├── src/
│   ├── public/
│   ├── package.json
│   ├── vite.config.ts
│   ├── Dockerfile
│   └── README.md
├── docker-compose.yml       # Local development stack
├── docker-compose.test.yml  # Testing environment
├── specs/                   # Documentation
│   └── 001-fullstack-todo-app/
│       ├── spec.md
│       ├── plan.md          # ← You are here
│       ├── research.md
│       ├── data-model.md
│       ├── quickstart.md
│       └── contracts/
└── README.md
```

---

## Quick Start (Docker Compose)

### Step 1: Clone Repository

```bash
git clone <repository-url>
cd todo-pg-java
```

### Step 2: Start All Services

```bash
# Start all services in background
docker-compose up -d

# View logs
docker-compose logs -f

# Check service health
docker-compose ps
```

**Expected Output**:
```
NAME                COMMAND                  SERVICE      STATUS          PORTS
todoapp-backend     "java -jar app.jar"      backend      Up 30 seconds   0.0.0.0:8080->8080/tcp
todoapp-frontend    "docker-entrypoint.s…"   frontend     Up 30 seconds   0.0.0.0:3000->3000/tcp
todoapp-postgres    "docker-entrypoint.s…"   postgres     Up 40 seconds   0.0.0.0:5432->5432/tcp
todoapp-redis       "docker-entrypoint.s…"   redis        Up 40 seconds   0.0.0.0:6379->6379/tcp
todoapp-rabbitmq    "docker-entrypoint.s…"   rabbitmq     Up 40 seconds   5672/tcp, 15672/tcp
todoapp-minio       "/usr/bin/docker-ent…"   minio        Up 40 seconds   9000-9001/tcp
```

### Step 3: Verify Services

1. **Frontend**: http://localhost:3000
   - Should show login/registration page

2. **Backend API**: http://localhost:8080/actuator/health
   - Should return: `{"status":"UP"}`

3. **API Documentation**: http://localhost:8080/swagger-ui.html
   - Interactive API explorer

4. **RabbitMQ Management**: http://localhost:15672
   - Login: todoapp / todoapp_dev

5. **MinIO Console**: http://localhost:9001
   - Login: todoapp / todoapp_dev

### Step 4: Create Test User

**Option A: Via Frontend**
1. Navigate to http://localhost:3000
2. Click "Register"
3. Enter email, password, name
4. Login automatically redirects

**Option B: Via API (curl)**
```bash
# Register user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "SecureP@ss123",
    "name": "Test User"
  }'

# Response includes JWT token
```

### Step 5: Create First Task

**Via Frontend**:
1. Login
2. Enter task description in input
3. Press Enter or click "Add Task"
4. Task appears in list

**Via API**:
```bash
# Login and save token
TOKEN=$(curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"SecureP@ss123"}' \
  | jq -r '.token')

# Create task
curl -X POST http://localhost:8080/api/v1/tasks \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "My first task",
    "priority": "HIGH"
  }'
```

---

## Local Development (Without Docker)

### Backend Development

#### 1. Start Infrastructure Services

```bash
# Start only backend dependencies
docker-compose up -d postgres redis rabbitmq minio

# Verify services
docker-compose ps postgres redis rabbitmq minio
```

#### 2. Configure Backend

Edit `backend/src/main/resources/application-dev.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/todoapp
    username: todoapp
    password: todoapp_dev
  data:
    redis:
      host: localhost
      port: 6379
  rabbitmq:
    host: localhost
    port: 5672
    username: todoapp
    password: todoapp_dev

minio:
  url: http://localhost:9000
  access-key: todoapp
  secret-key: todoapp_dev
  bucket-name: todoapp-files
```

#### 3. Run Backend

```bash
cd backend

# Install dependencies and compile
mvn clean install

# Run with dev profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Or run tests
mvn test

# Backend starts on http://localhost:8080
```

**Hot Reload**: Spring Boot DevTools enabled - code changes auto-reload.

---

### Frontend Development

#### 1. Install Dependencies

```bash
cd frontend
npm install
```

#### 2. Configure Frontend

Create `frontend/.env.local`:
```env
VITE_API_URL=http://localhost:8080
VITE_WS_URL=ws://localhost:8080/ws
```

#### 3. Run Frontend

```bash
# Development server with HMR
npm run dev

# Frontend starts on http://localhost:3000
# Changes auto-reload in browser
```

#### 4. Build for Production

```bash
npm run build

# Output in dist/ directory
npm run preview  # Preview production build
```

---

## Testing

### Run All Tests (Docker)

```bash
# Start test environment
docker-compose -f docker-compose.test.yml up --abort-on-container-exit

# This runs:
# - Backend unit tests (JUnit)
# - Backend integration tests (TestContainers)
# - Frontend unit tests (Jest)
# - E2E tests (Playwright)
```

### Backend Tests

```bash
cd backend

# Unit tests only (fast, no Docker)
mvn test -Dtest="**/unit/**"

# Integration tests (uses TestContainers)
mvn test -Dtest="**/integration/**"

# All tests
mvn verify

# Test coverage report
mvn jacoco:report
# Open: target/site/jacoco/index.html
```

### Frontend Tests

```bash
cd frontend

# Unit tests (Jest + React Testing Library)
npm test

# Watch mode
npm test -- --watch

# Coverage report
npm test -- --coverage

# E2E tests (Playwright)
npm run test:e2e

# E2E in UI mode
npm run test:e2e:ui
```

---

## Database Management

### Access PostgreSQL

**Via Docker**:
```bash
docker-compose exec postgres psql -U todoapp -d todoapp
```

**Via Local Client**:
```
Host: localhost
Port: 5432
Database: todoapp
Username: todoapp
Password: todoapp_dev
```

### Run Migrations Manually

```bash
# Flyway migrations run automatically on startup

# Check migration status
cd backend
mvn flyway:info

# Validate migrations
mvn flyway:validate

# Repair migrations (if needed)
mvn flyway:repair
```

### View Data

```sql
-- List all tables
\dt

-- View users
SELECT * FROM users;

-- View tasks
SELECT * FROM tasks;

-- Task count by user
SELECT u.email, COUNT(t.id) as task_count
FROM users u
LEFT JOIN tasks t ON u.id = t.user_id
GROUP BY u.email;
```

---

## File Storage (MinIO)

### Access MinIO Console

1. Open http://localhost:9001
2. Login: todoapp / todoapp_dev
3. Navigate to "Buckets" → `todoapp-files`
4. View uploaded files

### Upload Test File (API)

```bash
curl -X POST http://localhost:8080/api/v1/tasks/1/attachments \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/path/to/document.pdf"
```

---

## Message Queue (RabbitMQ)

### Access RabbitMQ Management

1. Open http://localhost:15672
2. Login: todoapp / todoapp_dev
3. Navigate to "Queues" tab
4. See queues:
   - `notifications-queue`: Pending notifications
   - `recurring-tasks-queue`: Scheduled recurring tasks
   - `file-scan-queue`: Files awaiting virus scan

### Monitor Queue

```bash
# View queue stats
docker-compose exec rabbitmq rabbitmqctl list_queues name messages

# View connections
docker-compose exec rabbitmq rabbitmqctl list_connections
```

---

## Monitoring & Debugging

### Application Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend

# Last 100 lines
docker-compose logs --tail=100 backend

# Follow with timestamps
docker-compose logs -f --timestamps backend
```

### Health Checks

```bash
# Backend health
curl http://localhost:8080/actuator/health

# Detailed health (includes DB, Redis, RabbitMQ)
curl http://localhost:8080/actuator/health | jq

# Metrics
curl http://localhost:8080/actuator/metrics

# Specific metric
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```

### Performance Profiling

```bash
# Enable JMX in backend
export JAVA_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9010"

# Connect with VisualVM or JConsole
# Host: localhost:9010
```

---

## Common Issues & Solutions

### Issue: Port Already in Use

**Error**: `Bind for 0.0.0.0:8080 failed: port is already allocated`

**Solution**:
```bash
# Find process using port
lsof -i :8080

# Kill process
kill -9 <PID>

# Or change port in docker-compose.yml
```

---

### Issue: Database Connection Failed

**Error**: `Connection refused: postgres:5432`

**Solution**:
```bash
# Check PostgreSQL is running
docker-compose ps postgres

# Restart PostgreSQL
docker-compose restart postgres

# View logs
docker-compose logs postgres
```

---

### Issue: Frontend Can't Connect to Backend

**Error**: `Network Error` or `CORS Error`

**Solution**:
1. Check backend is running: `curl http://localhost:8080/actuator/health`
2. Check `.env.local` has correct API URL
3. Clear browser cache and hard refresh (Ctrl+Shift+R)
4. Check CORS config in `backend/src/main/resources/application.yml`

---

### Issue: Tests Failing

**Error**: `TestContainers: Could not start container`

**Solution**:
```bash
# Ensure Docker is running
docker ps

# Pull required test images
docker pull postgres:16-alpine
docker pull redis:7-alpine
docker pull testcontainers/ryuk:0.5.1

# Increase Docker memory (Docker Desktop → Settings → Resources)
# Minimum: 4GB RAM
```

---

## Stopping Services

```bash
# Stop all services (keeps data)
docker-compose stop

# Stop and remove containers (keeps data in volumes)
docker-compose down

# Stop and remove everything including data
docker-compose down -v

# Remove specific service
docker-compose rm -f backend
```

---

## Cleanup & Reset

```bash
# Complete cleanup (WARNING: Deletes all data!)
docker-compose down -v --remove-orphans
docker system prune -a
```

---

## Environment Variables

### Backend (`backend/.env` or `application-dev.yml`)

```yaml
# Database
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/todoapp
SPRING_DATASOURCE_USERNAME: todoapp
SPRING_DATASOURCE_PASSWORD: todoapp_dev

# Redis
SPRING_DATA_REDIS_HOST: redis
SPRING_DATA_REDIS_PORT: 6379

# RabbitMQ
SPRING_RABBITMQ_HOST: rabbitmq
SPRING_RABBITMQ_PORT: 5672
SPRING_RABBITMQ_USERNAME: todoapp
SPRING_RABBITMQ_PASSWORD: todoapp_dev

# MinIO
MINIO_URL: http://minio:9000
MINIO_ACCESS_KEY: todoapp
MINIO_SECRET_KEY: todoapp_dev
MINIO_BUCKET_NAME: todoapp-files

# JWT
JWT_SECRET: your-256-bit-secret-change-in-production
JWT_EXPIRATION_MS: 86400000  # 24 hours

# CORS
CORS_ALLOWED_ORIGINS: http://localhost:3000
```

### Frontend (`frontend/.env.local`)

```env
VITE_API_URL=http://localhost:8080
VITE_WS_URL=ws://localhost:8080/ws
VITE_FILE_MAX_SIZE_MB=25
```

---

## Next Steps

1. **Read the Spec**: [spec.md](spec.md) for all 15 user stories
2. **Explore API**: http://localhost:8080/swagger-ui.html
3. **Run Tests**: `docker-compose -f docker-compose.test.yml up`
4. **Implement P1**: Start with basic task CRUD (User Story 1-3)
5. **Follow TDD**: Write tests first for domain logic

---

## Development Workflow

1. **Pick a task** from `tasks.md` (after running `/speckit.tasks`)
2. **Write test first** (TDD for domain logic)
3. **Run test** - should fail (red)
4. **Implement** minimal code
5. **Run test** - should pass (green)
6. **Refactor** if needed
7. **Commit** with conventional commit message
8. **Push** and create PR

---

## Support & Resources

- **API Docs**: [contracts/api-v1-summary.md](contracts/api-v1-summary.md)
- **Data Model**: [data-model.md](data-model.md)
- **Tech Decisions**: [research.md](research.md)
- **Spring Boot Docs**: https://docs.spring.io/spring-boot/docs/current/reference/html/
- **React Docs**: https://react.dev/
- **PostgreSQL Docs**: https://www.postgresql.org/docs/16/

---

**Estimated Setup Time**: 3-5 minutes (first time), < 1 minute (subsequent)
**Ready to Code**: Once all health checks pass at http://localhost:8080/actuator/health
