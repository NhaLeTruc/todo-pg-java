.PHONY: help install start stop restart logs clean test build lint format check deploy-local status

# Colors for output
BLUE := \033[1;34m
GREEN := \033[1;32m
YELLOW := \033[1;33m
RED := \033[1;31m
NC := \033[0m # No Color

## help: Show this help message
help:
	@echo "$(BLUE)Todo Application - Development Commands$(NC)"
	@echo ""
	@echo "$(GREEN)Quick Start:$(NC)"
	@echo "  make install       Install all dependencies (backend + frontend)"
	@echo "  make start         Start all services with Docker Compose"
	@echo "  make dev           Start services for local development"
	@echo ""
	@echo "$(GREEN)Development:$(NC)"
	@echo "  make dev-backend   Run backend locally (requires 'make infra')"
	@echo "  make dev-frontend  Run frontend locally"
	@echo "  make infra         Start infrastructure only (DB, Redis, RabbitMQ, MinIO)"
	@echo "  make logs          Tail logs from all services"
	@echo "  make logs-backend  Tail backend logs only"
	@echo "  make logs-frontend Tail frontend logs only"
	@echo "  make status        Show status of all services"
	@echo ""
	@echo "$(GREEN)Testing:$(NC)"
	@echo "  make test          Run all tests (backend + frontend)"
	@echo "  make test-backend  Run backend tests only"
	@echo "  make test-frontend Run frontend tests only"
	@echo "  make test-e2e      Run end-to-end tests"
	@echo "  make test-coverage Show test coverage report"
	@echo ""
	@echo "$(GREEN)Code Quality:$(NC)"
	@echo "  make lint          Lint all code (backend + frontend)"
	@echo "  make lint-backend  Lint backend code"
	@echo "  make lint-frontend Lint frontend code"
	@echo "  make format        Format all code (backend + frontend)"
	@echo "  make format-backend Format backend code"
	@echo "  make format-frontend Format frontend code"
	@echo "  make check         Run all quality checks"
	@echo ""
	@echo "$(GREEN)Build & Deploy:$(NC)"
	@echo "  make build         Build all artifacts (backend + frontend)"
	@echo "  make build-backend Build backend JAR"
	@echo "  make build-frontend Build frontend production bundle"
	@echo "  make docker-build  Build Docker images"
	@echo ""
	@echo "$(GREEN)Database:$(NC)"
	@echo "  make db-migrate    Run database migrations"
	@echo "  make db-rollback   Rollback last migration"
	@echo "  make db-clean      Clean database (rollback all)"
	@echo "  make db-info       Show migration status"
	@echo ""
	@echo "$(GREEN)Cleanup:$(NC)"
	@echo "  make stop          Stop all services"
	@echo "  make restart       Restart all services"
	@echo "  make clean         Clean build artifacts"
	@echo "  make clean-all     Stop services and remove all data"
	@echo ""

## install: Install all dependencies (backend + frontend)
install: install-backend install-frontend
	@echo "$(GREEN)✓ All dependencies installed$(NC)"

install-backend:
	@echo "$(BLUE)Installing backend dependencies...$(NC)"
	cd backend && mvn clean install -DskipTests

install-frontend:
	@echo "$(BLUE)Installing frontend dependencies...$(NC)"
	cd frontend && npm install

## start: Start all services with Docker Compose
start:
	@echo "$(BLUE)Starting all services...$(NC)"
	docker compose up -d
	@echo "$(GREEN)✓ Services started$(NC)"
	@echo "  Frontend:         http://localhost:3000"
	@echo "  Backend API:      http://localhost:8080"
	@echo "  API Docs:         http://localhost:8080/swagger-ui.html"
	@echo "  RabbitMQ:         http://localhost:15672 (todoapp/todoapp)"
	@echo "  MinIO Console:    http://localhost:9001 (todoapp/todoapp123)"

## infra: Start infrastructure services only (PostgreSQL, Redis, RabbitMQ, MinIO)
infra:
	@echo "$(BLUE)Starting infrastructure services...$(NC)"
	docker compose up -d postgres redis rabbitmq minio
	@echo "$(GREEN)✓ Infrastructure services started$(NC)"

## stop: Stop all services
stop:
	@echo "$(BLUE)Stopping all services...$(NC)"
	docker compose down -v --remove-orphans
	@echo "$(GREEN)✓ Services stopped$(NC)"

## restart: Restart all services
restart: stop start

## dev: Start services for local development (infra + backend + frontend)
dev:
	@echo "$(YELLOW)Starting infrastructure services...$(NC)"
	@make infra
	@echo "$(YELLOW)Waiting for services to be ready...$(NC)"
	@sleep 5
	@echo "$(BLUE)Start backend and frontend in separate terminals:$(NC)"
	@echo "  Terminal 1: make dev-backend"
	@echo "  Terminal 2: make dev-frontend"

## dev-backend: Run backend locally
dev-backend:
	@echo "$(BLUE)Starting backend in development mode...$(NC)"
	cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=dev

## dev-frontend: Run frontend locally
dev-frontend:
	@echo "$(BLUE)Starting frontend development server...$(NC)"
	cd frontend && npm run dev

## logs: Tail logs from all services
logs:
	docker compose logs -f

## logs-backend: Tail backend logs only
logs-backend:
	docker compose logs -f backend

## logs-frontend: Tail frontend logs only
logs-frontend:
	docker compose logs -f frontend

## status: Show status of all services
status:
	@echo "$(BLUE)Docker Compose Services:$(NC)"
	@docker compose ps
	@echo ""
	@echo "$(BLUE)Health Checks:$(NC)"
	@echo -n "Backend API:      "
	@curl -s http://localhost:8080/actuator/health > /dev/null 2>&1 && echo "$(GREEN)✓ Healthy$(NC)" || echo "$(RED)✗ Down$(NC)"
	@echo -n "Frontend:         "
	@curl -s http://localhost:3000 > /dev/null 2>&1 && echo "$(GREEN)✓ Running$(NC)" || echo "$(RED)✗ Down$(NC)"
	@echo -n "PostgreSQL:       "
	@docker compose exec -T postgres pg_isready > /dev/null 2>&1 && echo "$(GREEN)✓ Ready$(NC)" || echo "$(RED)✗ Down$(NC)"
	@echo -n "Redis:            "
	@docker compose exec -T redis redis-cli ping > /dev/null 2>&1 && echo "$(GREEN)✓ Ready$(NC)" || echo "$(RED)✗ Down$(NC)"

## test: Run all tests (backend + frontend)
test: test-backend test-frontend
	@echo "$(GREEN)✓ All tests completed$(NC)"

## test-backend: Run backend tests only
test-backend:
	@echo "$(BLUE)Running backend tests...$(NC)"
	cd backend && mvn test

## test-integration: Run backend integration tests
test-integration:
	@echo "$(BLUE)Running backend integration tests...$(NC)"
	cd backend && mvn verify

## test-frontend: Run frontend tests only
test-frontend:
	@echo "$(BLUE)Running frontend tests...$(NC)"
	cd frontend && npm test

## test-e2e: Run end-to-end tests
test-e2e:
	@echo "$(BLUE)Running E2E tests...$(NC)"
	cd frontend && npm run test:e2e

## test-coverage: Show test coverage report
test-coverage:
	@echo "$(BLUE)Generating test coverage reports...$(NC)"
	cd backend && mvn clean verify
	cd frontend && npm run test:coverage
	@echo "$(GREEN)✓ Coverage reports generated$(NC)"
	@echo "  Backend:  backend/target/site/jacoco/index.html"
	@echo "  Frontend: frontend/coverage/lcov-report/index.html"

## lint: Lint all code (backend + frontend)
lint: lint-backend lint-frontend
	@echo "$(GREEN)✓ All linting completed$(NC)"

## lint-backend: Lint backend code
lint-backend:
	@echo "$(BLUE)Linting backend code...$(NC)"
	cd backend && mvn checkstyle:check

## lint-frontend: Lint frontend code
lint-frontend:
	@echo "$(BLUE)Linting frontend code...$(NC)"
	cd frontend && npm run lint

## format: Format all code (backend + frontend)
format: format-backend format-frontend
	@echo "$(GREEN)✓ All code formatted$(NC)"

## format-backend: Format backend code
format-backend:
	@echo "$(BLUE)Formatting backend code...$(NC)"
	cd backend && mvn spotless:apply

## format-frontend: Format frontend code
format-frontend:
	@echo "$(BLUE)Formatting frontend code...$(NC)"
	cd frontend && npm run format

## check: Run all quality checks
check:
	@echo "$(BLUE)Running all quality checks...$(NC)"
	@make lint
	@make test
	@echo "$(GREEN)✓ All quality checks passed$(NC)"

## build: Build all artifacts (backend + frontend)
build: build-backend build-frontend
	@echo "$(GREEN)✓ All builds completed$(NC)"

## build-backend: Build backend JAR
build-backend:
	@echo "$(BLUE)Building backend...$(NC)"
	cd backend && mvn clean package -DskipTests

## build-frontend: Build frontend production bundle
build-frontend:
	@echo "$(BLUE)Building frontend...$(NC)"
	cd frontend && npm run build

## docker-build: Build Docker images
docker-build:
	@echo "$(BLUE)Building Docker images...$(NC)"
	docker compose build
	@echo "$(GREEN)✓ Docker images built$(NC)"

## db-migrate: Run database migrations
db-migrate:
	@echo "$(BLUE)Running database migrations...$(NC)"
	cd backend && mvn flyway:migrate

## db-rollback: Rollback last migration
db-rollback:
	@echo "$(BLUE)Rolling back last migration...$(NC)"
	cd backend && mvn flyway:undo

## db-clean: Clean database (rollback all)
db-clean:
	@echo "$(BLUE)Cleaning database...$(NC)"
	cd backend && mvn flyway:clean

## db-info: Show migration status
db-info:
	@echo "$(BLUE)Database migration status:$(NC)"
	cd backend && mvn flyway:info

## clean: Clean build artifacts
clean:
	@echo "$(BLUE)Cleaning build artifacts...$(NC)"
	cd backend && mvn clean
	cd frontend && rm -rf dist node_modules/.vite
	@echo "$(GREEN)✓ Build artifacts cleaned$(NC)"

## clean-all: Stop services and remove all data
clean-all:
	@echo "$(RED)WARNING: This will remove all data including databases!$(NC)"
	@echo -n "Are you sure? [y/N] " && read ans && [ $${ans:-N} = y ]
	@echo "$(BLUE)Stopping services and removing volumes...$(NC)"
	docker compose down -v
	@make clean
	@echo "$(GREEN)✓ All services stopped and data removed$(NC)"

## quickstart: Quick validation of the application setup
quickstart:
	@echo "$(BLUE)Running quickstart validation...$(NC)"
	@if [ -f ./validate-quickstart.sh ]; then \
		./validate-quickstart.sh; \
	else \
		echo "$(YELLOW)Note: validate-quickstart.sh not found$(NC)"; \
	fi

## security-audit: Run security audit
security-audit:
	@echo "$(BLUE)Running security audit...$(NC)"
	@if [ -f ./security-audit.sh ]; then \
		./security-audit.sh; \
	else \
		echo "$(YELLOW)Note: security-audit.sh not found$(NC)"; \
	fi

## performance-test: Run performance tests
performance-test:
	@echo "$(BLUE)Running performance tests...$(NC)"
	@if [ -f ./performance-test.sh ]; then \
		./performance-test.sh; \
	else \
		echo "$(YELLOW)Note: performance-test.sh not found$(NC)"; \
	fi
