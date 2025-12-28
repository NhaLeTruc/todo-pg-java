#!/bin/bash

# Quickstart Validation Script
# Validates that all services and features are working correctly

set -e

BOLD='\033[1m'
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BOLD}================================${NC}"
echo -e "${BOLD}Quickstart Validation Script${NC}"
echo -e "${BOLD}================================${NC}"
echo ""

# Function to print success message
success() {
    echo -e "${GREEN}✓ $1${NC}"
}

# Function to print error message
error() {
    echo -e "${RED}✗ $1${NC}"
}

# Function to print warning message
warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

# Function to print section header
section() {
    echo -e "\n${BOLD}$1${NC}"
    echo "----------------------------------------"
}

# Check if Docker is running
section "1. Checking Docker"
if docker info > /dev/null 2>&1; then
    success "Docker is running"
else
    error "Docker is not running"
    exit 1
fi

# Check if docker-compose.yml exists
if [ -f "docker-compose.yml" ]; then
    success "docker-compose.yml found"
else
    error "docker-compose.yml not found"
    exit 1
fi

# Check Java version
section "2. Checking Java"
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}')
    success "Java found: $JAVA_VERSION"
else
    warning "Java not found (required for local development)"
fi

# Check Maven version
section "3. Checking Maven"
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -n 1 | awk '{print $3}')
    success "Maven found: $MVN_VERSION"
else
    warning "Maven not found (required for local development)"
fi

# Check Node.js version
section "4. Checking Node.js"
if command -v node &> /dev/null; then
    NODE_VERSION=$(node -v)
    success "Node.js found: $NODE_VERSION"
else
    warning "Node.js not found (required for local development)"
fi

# Check if services are running
section "5. Checking Docker Services"

SERVICES=("todoapp-postgres" "todoapp-redis" "todoapp-rabbitmq" "todoapp-minio" "todoapp-backend" "todoapp-frontend")

for service in "${SERVICES[@]}"; do
    if docker ps --format '{{.Names}}' | grep -q "^${service}$"; then
        success "$service is running"
    else
        error "$service is not running"
        echo "  Run: docker compose up -d"
        exit 1
    fi
done

# Wait for services to be healthy
section "6. Waiting for Services to be Healthy (max 60s)"
sleep 5

MAX_WAIT=60
ELAPSED=0

while [ $ELAPSED -lt $MAX_WAIT ]; do
    ALL_HEALTHY=true

    for service in "${SERVICES[@]}"; do
        STATUS=$(docker inspect --format='{{.State.Health.Status}}' "$service" 2>/dev/null || echo "no-health")
        if [ "$STATUS" != "healthy" ] && [ "$STATUS" != "no-health" ]; then
            ALL_HEALTHY=false
            break
        fi
    done

    if [ "$ALL_HEALTHY" = true ]; then
        success "All services are healthy"
        break
    fi

    echo "  Waiting for services to be healthy... (${ELAPSED}s)"
    sleep 5
    ELAPSED=$((ELAPSED + 5))
done

if [ $ELAPSED -ge $MAX_WAIT ]; then
    warning "Timeout waiting for services to be healthy"
fi

# Test Backend API
section "7. Testing Backend API"

# Test health endpoint
if curl -sf http://localhost:8080/actuator/health > /dev/null; then
    success "Backend health endpoint responding"
else
    error "Backend health endpoint not responding"
    exit 1
fi

# Test API docs
if curl -sf http://localhost:8080/swagger-ui.html > /dev/null; then
    success "Swagger UI accessible"
else
    error "Swagger UI not accessible"
fi

# Test API endpoint
if curl -sf http://localhost:8080/api/v1/auth/health > /dev/null 2>&1 || \
   curl -sf http://localhost:8080/actuator/health > /dev/null; then
    success "Backend API responding"
else
    warning "Backend API test endpoint not responding (may need authentication)"
fi

# Test Frontend
section "8. Testing Frontend"

if curl -sf http://localhost:3000 > /dev/null; then
    success "Frontend accessible"
else
    error "Frontend not accessible"
    exit 1
fi

# Test Database Connection
section "9. Testing Database"

if docker exec todoapp-postgres pg_isready -U todoapp > /dev/null 2>&1; then
    success "PostgreSQL accepting connections"
else
    error "PostgreSQL not accepting connections"
    exit 1
fi

# Test Redis Connection
section "10. Testing Redis"

if docker exec todoapp-redis redis-cli ping > /dev/null 2>&1; then
    success "Redis responding to PING"
else
    error "Redis not responding"
    exit 1
fi

# Test RabbitMQ
section "11. Testing RabbitMQ"

if docker exec todoapp-rabbitmq rabbitmq-diagnostics -q ping > /dev/null 2>&1; then
    success "RabbitMQ responding"
else
    error "RabbitMQ not responding"
    exit 1
fi

# Test MinIO
section "12. Testing MinIO"

if curl -sf http://localhost:9000/minio/health/live > /dev/null 2>&1; then
    success "MinIO responding"
else
    error "MinIO not responding"
    exit 1
fi

# Check volumes
section "13. Checking Docker Volumes"

VOLUMES=("todoapp-postgres-data" "todoapp-redis-data" "todoapp-rabbitmq-data" "todoapp-minio-data")

for volume in "${VOLUMES[@]}"; do
    if docker volume ls --format '{{.Name}}' | grep -q "^${volume}$"; then
        success "Volume $volume exists"
    else
        warning "Volume $volume not found"
    fi
done

# Check logs for errors
section "14. Checking for Critical Errors in Logs"

if docker logs todoapp-backend --tail 50 2>&1 | grep -qi "error\|exception\|failed" | head -5; then
    warning "Found potential errors in backend logs (check manually)"
else
    success "No critical errors found in backend logs"
fi

# Summary
section "Validation Summary"

echo -e "\n${GREEN}${BOLD}✓ Quickstart validation completed successfully!${NC}\n"
echo "Services available at:"
echo "  - Frontend:          http://localhost:3000"
echo "  - Backend API:       http://localhost:8080"
echo "  - API Documentation: http://localhost:8080/swagger-ui.html"
echo "  - RabbitMQ Management: http://localhost:15672"
echo "  - MinIO Console:     http://localhost:9001"
echo ""
echo "Default credentials:"
echo "  - RabbitMQ: todoapp / todoapp"
echo "  - MinIO:    todoapp / todoapp123"
echo ""
echo -e "${BOLD}Next steps:${NC}"
echo "  1. Register a new user at http://localhost:3000/register"
echo "  2. Login and start creating tasks"
echo "  3. Explore the API docs at http://localhost:8080/swagger-ui.html"
echo ""
