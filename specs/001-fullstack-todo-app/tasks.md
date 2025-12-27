# Tasks: Full-Stack TODO List Application

**Input**: Design documents from `/specs/001-fullstack-todo-app/`
**Prerequisites**: plan.md, spec.md, data-model.md, contracts/, research.md, quickstart.md

**Tests**: The constitution mandates TDD for core logic. Tests for domain/business logic MUST be written BEFORE implementation. Integration tests are written AFTER implementation.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Web app**: `backend/src/`, `frontend/src/`
- Backend Java: `backend/src/main/java/com/todoapp/`
- Backend Tests: `backend/src/test/java/com/todoapp/`
- Backend Resources: `backend/src/main/resources/`
- Frontend: `frontend/src/`

---

## Phase 1: Setup (Shared Infrastructure) ✅ COMPLETED

**Purpose**: Project initialization and basic structure

- [x] T001 Create backend Maven project structure with Spring Boot 3.2.x parent POM in backend/pom.xml
- [x] T002 Create frontend npm project with Vite 5.x and TypeScript 5.x config in frontend/package.json
- [x] T003 [P] Configure Docker Compose services (PostgreSQL, Redis, RabbitMQ, MinIO) in docker-compose.yml
- [x] T004 [P] Create backend Dockerfile with multi-stage build in backend/Dockerfile
- [x] T005 [P] Create frontend Dockerfile with multi-stage build in frontend/Dockerfile
- [x] T006 [P] Configure Spotless and Checkstyle for Google Java Style Guide in backend/pom.xml
- [x] T007 [P] Configure ESLint and Prettier for TypeScript in frontend/.eslintrc.json
- [x] T008 [P] Create .gitignore with Java, Node, Docker, IDE exclusions in repository root
- [x] T009 Create README.md with quickstart instructions referencing specs/001-fullstack-todo-app/quickstart.md
- [x] T010 [P] Configure Spring Boot application.yml with externalized config in backend/src/main/resources/application.yml
- [x] T011 [P] Configure Spring Boot application-dev.yml for local development in backend/src/main/resources/application-dev.yml
- [x] T012 [P] Configure Spring Boot application-test.yml for testing in backend/src/test/resources/application-test.yml

---

## Phase 2: Foundational (Blocking Prerequisites) ✅ COMPLETED

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T013 Create Flyway migration V1__create_users_table.sql in backend/src/main/resources/db/migration/
- [x] T014 Create Flyway migration V2__create_categories_table.sql in backend/src/main/resources/db/migration/
- [x] T015 Create Flyway migration V3__create_tags_table.sql in backend/src/main/resources/db/migration/
- [x] T016 Create Flyway migration V4__create_recurrence_patterns_table.sql in backend/src/main/resources/db/migration/
- [x] T017 Create Flyway migration V5__create_tasks_table.sql in backend/src/main/resources/db/migration/
- [x] T018 Create Flyway migration V6__create_task_tags_table.sql in backend/src/main/resources/db/migration/
- [x] T019 Create Flyway migration V7__create_comments_table.sql in backend/src/main/resources/db/migration/
- [x] T020 Create Flyway migration V8__create_task_shares_table.sql in backend/src/main/resources/db/migration/
- [x] T021 Create Flyway migration V9__create_time_entries_table.sql in backend/src/main/resources/db/migration/
- [x] T022 Create Flyway migration V10__create_file_attachments_table.sql in backend/src/main/resources/db/migration/
- [x] T023 Create Flyway migration V11__create_notifications_table.sql in backend/src/main/resources/db/migration/
- [x] T024 Create Flyway migration V12__create_notification_preferences_table.sql in backend/src/main/resources/db/migration/
- [x] T025 Create Flyway migration V13__create_indexes.sql in backend/src/main/resources/db/migration/
- [x] T026 [P] Configure Spring Data JPA with Hibernate in backend/src/main/resources/application.yml
- [x] T027 [P] Configure Spring Security basic setup (disabled initially) in backend/src/main/java/com/todoapp/infrastructure/config/SecurityConfig.java
- [x] T028 [P] Configure CORS for frontend origin in backend/src/main/java/com/todoapp/infrastructure/config/CorsConfig.java
- [x] T029 [P] Create global exception handler in backend/src/main/java/com/todoapp/presentation/exception/GlobalExceptionHandler.java
- [x] T030 [P] Configure Logback with JSON encoder in backend/src/main/resources/logback-spring.xml
- [x] T031 [P] Configure Spring Boot Actuator health endpoints in backend/src/main/resources/application.yml
- [x] T032 [P] Configure Redis connection settings in backend/src/main/java/com/todoapp/infrastructure/config/RedisConfig.java
- [x] T033 [P] Configure RabbitMQ connection settings in backend/src/main/java/com/todoapp/infrastructure/config/RabbitMQConfig.java
- [x] T034 [P] Configure MinIO client in backend/src/main/java/com/todoapp/infrastructure/config/MinIOConfig.java
- [x] T035 [P] Configure SpringDoc OpenAPI in backend/src/main/java/com/todoapp/infrastructure/config/OpenAPIConfig.java
- [x] T036 [P] Create API base URL constants in frontend/src/utils/constants.ts
- [x] T037 [P] Configure Axios HTTP client with interceptors in frontend/src/services/api.ts
- [x] T038 [P] Configure TanStack Query client in frontend/src/services/queryClient.ts
- [x] T039 [P] Setup Tailwind CSS configuration in frontend/tailwind.config.js
- [x] T040 [P] Create base layout components in frontend/src/components/layout/
- [x] T041 Configure TestContainers for integration tests in backend/src/test/java/com/todoapp/integration/TestContainersConfig.java

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Create and View Tasks (Priority: P1) 🎯 MVP ✅ COMPLETED

**Goal**: Users can create TODO tasks with descriptions and view them in a list

**Independent Test**: Create multiple tasks via UI/API, verify they appear in list, persist across page refresh

### Tests for User Story 1 (TDD - Write FIRST) ✅

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [x] T042 [P] [US1] Write unit test for Task entity validation (empty description rejection) in backend/src/test/java/com/todoapp/unit/domain/TaskTest.java
- [x] T043 [P] [US1] Write unit test for TaskService.createTask() method in backend/src/test/java/com/todoapp/unit/application/TaskServiceTest.java
- [x] T044 [P] [US1] Write unit test for TaskService.getUserTasks() method in backend/src/test/java/com/todoapp/unit/application/TaskServiceTest.java

### Implementation for User Story 1 ✅

- [x] T045 [P] [US1] Create User entity in backend/src/main/java/com/todoapp/domain/model/User.java
- [x] T046 [P] [US1] Create Task entity with basic fields (id, user_id, description, is_completed) in backend/src/main/java/com/todoapp/domain/model/Task.java
- [x] T047 [P] [US1] Create Priority enum in backend/src/main/java/com/todoapp/domain/model/Priority.java
- [x] T048 [P] [US1] Create TaskRepository interface (Spring Data JPA) in backend/src/main/java/com/todoapp/domain/repository/TaskRepository.java
- [x] T049 [P] [US1] Create UserRepository interface in backend/src/main/java/com/todoapp/domain/repository/UserRepository.java
- [x] T050 [P] [US1] Create TaskCreateDTO in backend/src/main/java/com/todoapp/application/dto/TaskCreateDTO.java
- [x] T051 [P] [US1] Create TaskResponseDTO in backend/src/main/java/com/todoapp/application/dto/TaskResponseDTO.java
- [x] T052 [P] [US1] Create TaskMapper for entity-DTO conversion in backend/src/main/java/com/todoapp/application/mapper/TaskMapper.java
- [x] T053 [US1] Implement TaskService with createTask() and getUserTasks() in backend/src/main/java/com/todoapp/application/service/TaskService.java
- [x] T054 [US1] Create TaskController with POST /api/v1/tasks endpoint in backend/src/main/java/com/todoapp/presentation/rest/TaskController.java
- [x] T055 [US1] Add GET /api/v1/tasks endpoint to TaskController with pagination in backend/src/main/java/com/todoapp/presentation/rest/TaskController.java
- [x] T056 [P] [US1] Create Task type definition in frontend/src/types/task.ts
- [x] T057 [P] [US1] Create task API service with createTask() and getTasks() in frontend/src/services/taskService.ts
- [x] T058 [P] [US1] Create TaskForm component for task creation in frontend/src/components/tasks/TaskForm.tsx
- [x] T059 [P] [US1] Create TaskList component for displaying tasks in frontend/src/components/tasks/TaskList.tsx
- [x] T060 [P] [US1] Create TaskItem component for individual task display in frontend/src/components/tasks/TaskItem.tsx
- [x] T061 [US1] Create TasksPage with TaskForm and TaskList integration in frontend/src/pages/TasksPage.tsx
- [x] T062 [US1] Add routing for TasksPage in frontend/src/App.tsx
- [x] T063 [US1] Add validation and error handling for empty task descriptions in frontend/src/components/tasks/TaskForm.tsx

### Integration Tests for User Story 1 (Write AFTER implementation) ✅

- [x] T064 [P] [US1] Write integration test for POST /api/v1/tasks in backend/src/test/java/com/todoapp/integration/TaskControllerIntegrationTest.java
- [x] T065 [P] [US1] Write integration test for GET /api/v1/tasks in backend/src/test/java/com/todoapp/integration/TaskControllerIntegrationTest.java
- [x] T066 [P] [US1] Write frontend component test for TaskForm in frontend/src/components/tasks/__tests__/TaskForm.test.tsx
- [x] T067 [P] [US1] Write frontend component test for TaskList in frontend/src/components/tasks/__tests__/TaskList.test.tsx

**Checkpoint**: ✅ User Story 1 is fully functional and tested independently

**Test Results**:

- Backend: 14/14 tests passing (8 Task entity + 6 TaskService)
- Frontend: 29/29 tests passing (14 TaskForm + 15 TaskList)
- Integration: 11 TaskController integration tests created
- Total: 43 tests passing

---

## Phase 4: User Story 2 - Mark Tasks Complete (Priority: P2) ✅ COMPLETED

**Goal**: Users can toggle task completion status

**Independent Test**: Create tasks, mark complete/incomplete, verify visual indicators and persistence

### Tests for User Story 2 (TDD - Write FIRST) ✅

- [x] T068 [P] [US2] Write unit test for Task.markComplete() and Task.markIncomplete() in backend/src/test/java/com/todoapp/unit/domain/TaskTest.java
- [x] T069 [P] [US2] Write unit test for TaskService.toggleCompletion() in backend/src/test/java/com/todoapp/unit/application/TaskServiceTest.java

### Implementation for User Story 2 ✅

- [x] T070 [P] [US2] Add markComplete() and markIncomplete() methods to Task entity in backend/src/main/java/com/todoapp/domain/model/Task.java
- [x] T071 [US2] Implement TaskService.toggleCompletion() method in backend/src/main/java/com/todoapp/application/service/TaskService.java
- [x] T072 [US2] Add PATCH /api/v1/tasks/{id}/complete endpoint in backend/src/main/java/com/todoapp/presentation/rest/TaskController.java
- [x] T073 [US2] Add PATCH /api/v1/tasks/{id}/uncomplete endpoint in backend/src/main/java/com/todoapp/presentation/rest/TaskController.java
- [x] T074 [P] [US2] Add toggleComplete() to task API service in frontend/src/services/taskService.ts
- [x] T075 [US2] Add checkbox/toggle UI to TaskItem component in frontend/src/components/tasks/TaskItem.tsx
- [x] T076 [US2] Add visual styling for completed tasks (strikethrough) in frontend/src/components/tasks/TaskItem.tsx
- [x] T077 [US2] Add optimistic updates with TanStack Query mutations in frontend/src/components/tasks/TaskItem.tsx

### Integration Tests for User Story 2 ✅

- [x] T078 [P] [US2] Write integration test for PATCH /api/v1/tasks/{id}/complete in backend/src/test/java/com/todoapp/integration/TaskControllerIntegrationTest.java
- [x] T079 [P] [US2] Write frontend test for task completion toggle in frontend/src/components/tasks/__tests__/TaskItem.test.tsx

**Checkpoint**: ✅ User Story 2 is fully functional and tested independently

---

## Phase 5: User Story 3 - Edit and Delete Tasks (Priority: P3) ✅ COMPLETED

**Goal**: Users can modify task descriptions and delete tasks

**Independent Test**: Edit task descriptions, delete tasks, verify persistence and proper deletion

### Tests for User Story 3 (TDD - Write FIRST) ✅

- [x] T080 [P] [US3] Write unit test for Task.updateDescription() validation in backend/src/test/java/com/todoapp/unit/domain/TaskTest.java
- [x] T081 [P] [US3] Write unit test for TaskService.updateTask() in backend/src/test/java/com/todoapp/unit/application/TaskServiceTest.java
- [x] T082 [P] [US3] Write unit test for TaskService.deleteTask() in backend/src/test/java/com/todoapp/unit/application/TaskServiceTest.java

### Implementation for User Story 3 ✅

- [x] T083 [P] [US3] Create TaskUpdateDTO in backend/src/main/java/com/todoapp/application/dto/TaskUpdateDTO.java
- [x] T084 [US3] Implement TaskService.updateTask() method in backend/src/main/java/com/todoapp/application/service/TaskService.java
- [x] T085 [US3] Implement TaskService.deleteTask() method in backend/src/main/java/com/todoapp/application/service/TaskService.java
- [x] T086 [US3] Add PUT /api/v1/tasks/{id} endpoint in backend/src/main/java/com/todoapp/presentation/rest/TaskController.java
- [x] T087 [US3] Add DELETE /api/v1/tasks/{id} endpoint in backend/src/main/java/com/todoapp/presentation/rest/TaskController.java
- [x] T088 [US3] Verified GET /api/v1/tasks/{id} endpoint exists in backend/src/main/java/com/todoapp/presentation/rest/TaskController.java
- [x] T089 [P] [US3] Verified updateTask() and deleteTask() exist in frontend/src/services/taskService.ts
- [x] T090 [P] [US3] Create TaskEditModal component for inline editing in frontend/src/components/tasks/TaskEditModal.tsx
- [x] T091 [US3] Edit/delete buttons already exist in TaskItem component in frontend/src/components/tasks/TaskItem.tsx
- [x] T092 [US3] Create ConfirmDialog component for task deletion in frontend/src/components/shared/ConfirmDialog.tsx
- [x] T093 [US3] Integrate edit modal and delete confirmation in TasksPage in frontend/src/pages/TasksPage.tsx

### Integration Tests for User Story 3 ✅

- [x] T094 [P] [US3] Write integration test for PUT /api/v1/tasks/{id} in backend/src/test/java/com/todoapp/integration/TaskControllerIntegrationTest.java
- [x] T095 [P] [US3] Write integration test for DELETE /api/v1/tasks/{id} in backend/src/test/java/com/todoapp/integration/TaskControllerIntegrationTest.java

**Checkpoint**: ✅ Full CRUD operations on tasks are now functional and tested

---

## Phase 6: User Story 4 - Filter and Search Tasks (Priority: P4) ✅ COMPLETED

**Goal**: Users can filter by completion status and search by keyword

**Independent Test**: Create tasks with various keywords, apply filters and search, verify correct results

### Tests for User Story 4 (TDD - Write FIRST) ✅

- [x] T096 [P] [US4] Write unit test for TaskRepository.searchByDescription() in backend/src/test/java/com/todoapp/unit/domain/TaskRepositoryTest.java
- [x] T097 [P] [US4] Write unit test for TaskService.searchTasks() in backend/src/test/java/com/todoapp/unit/application/TaskServiceTest.java

### Implementation for User Story 4 ✅

- [x] T098 [P] [US4] Verified full-text search query exists in TaskRepository in backend/src/main/java/com/todoapp/domain/repository/TaskRepository.java
- [x] T099 [US4] Verified TaskService.searchTasks() exists with filters in backend/src/main/java/com/todoapp/application/service/TaskService.java
- [x] T100 [US4] Verified query parameters (completed, search) exist in GET /api/v1/tasks in backend/src/main/java/com/todoapp/presentation/rest/TaskController.java
- [x] T101 [P] [US4] Verified search input exists in TasksPage in frontend/src/pages/TasksPage.tsx
- [x] T102 [P] [US4] Verified filter dropdown (All, Active, Completed) exists in TasksPage in frontend/src/pages/TasksPage.tsx
- [x] T103 [US4] Verified search and filter integration exists in TasksPage in frontend/src/pages/TasksPage.tsx
- [x] T104 [US4] Verified task service includes filter/search params in frontend/src/services/taskService.ts

### Integration Tests for User Story 4 ✅

- [x] T105 [P] [US4] Write integration test for search functionality in backend/src/test/java/com/todoapp/integration/TaskControllerIntegrationTest.java
- [x] T106 [P] [US4] Verified frontend search and filter functionality integrated in TasksPage

**Checkpoint**: ✅ Users can efficiently find tasks in large lists with search and filters

---

## Phase 7: User Story 5 - Task Priority and Due Dates (Priority: P5) ✅ COMPLETED

**Goal**: Users can assign priority levels and due dates, with visual indicators for overdue tasks

**Independent Test**: Create tasks with priorities/due dates, verify sorting and overdue highlighting

### Tests for User Story 5 (TDD - Write FIRST) ✅

- [x] T107 [P] [US5] Write unit test for Task priority validation in backend/src/test/java/com/todoapp/unit/domain/TaskTest.java
- [x] T108 [P] [US5] Write unit test for Task.isOverdue() method in backend/src/test/java/com/todoapp/unit/domain/TaskTest.java
- [x] T109 [P] [US5] Write unit test for TaskService with priority/date sorting in backend/src/test/java/com/todoapp/unit/application/TaskServiceTest.java

### Implementation for User Story 5 ✅

- [x] T110 [P] [US5] Add priority and dueDate fields to Task entity (already in schema) in backend/src/main/java/com/todoapp/domain/model/Task.java
- [x] T111 [P] [US5] Add isOverdue() helper method to Task entity in backend/src/main/java/com/todoapp/domain/model/Task.java
- [x] T112 [US5] Add priority and dueDate to TaskCreateDTO and TaskUpdateDTO in backend/src/main/java/com/todoapp/application/dto/
- [x] T113 [US5] Update TaskService to handle priority and due date filtering in backend/src/main/java/com/todoapp/application/service/TaskService.java
- [x] T114 [US5] Add sort parameter support (priority, dueDate) to GET /api/v1/tasks in backend/src/main/java/com/todoapp/presentation/rest/TaskController.java
- [x] T115 [P] [US5] Create PrioritySelector component (High/Medium/Low) in frontend/src/components/tasks/PrioritySelector.tsx
- [x] T116 [P] [US5] Create DatePicker component for due dates in frontend/src/components/shared/DatePicker.tsx
- [x] T117 [US5] Add priority and due date fields to TaskForm in frontend/src/components/tasks/TaskForm.tsx
- [x] T118 [US5] Add priority badge and due date display to TaskItem in frontend/src/components/tasks/TaskItem.tsx
- [x] T119 [US5] Add overdue visual indicator (red highlight) to TaskItem in frontend/src/components/tasks/TaskItem.tsx
- [x] T120 [US5] Add sort dropdown (by priority, by due date) to TasksPage in frontend/src/pages/TasksPage.tsx

### Integration Tests for User Story 5 ✅

- [x] T121 [P] [US5] Write integration test for priority/due date filtering in backend/src/test/java/com/todoapp/integration/TaskControllerIntegrationTest.java

**Checkpoint**: ✅ MVP complete - all basic task management features functional (P1-P5)

---

## Phase 8: User Story 6 - User Accounts and Authentication (Priority: P6) ✅ COMPLETE

**Goal**: Multi-user support with secure registration, login, logout, and data isolation

**Independent Test**: Register multiple users, login/logout, verify each user sees only their own tasks

### Tests for User Story 6 (TDD - Write FIRST) ✅

- [x] T122 [P] [US6] Write unit test for User entity email validation in backend/src/test/java/com/todoapp/unit/domain/UserTest.java
- [x] T123 [P] [US6] Write unit test for UserService.registerUser() (BCrypt hashing) in backend/src/test/java/com/todoapp/unit/application/UserServiceTest.java
- [x] T124 [P] [US6] Write unit test for AuthService.authenticate() in backend/src/test/java/com/todoapp/unit/application/AuthServiceTest.java
- [x] T125 [P] [US6] Write unit test for JwtTokenProvider in backend/src/test/java/com/todoapp/unit/infrastructure/JwtTokenProviderTest.java

### Implementation for User Story 6 ✅

- [x] T126 [P] [US6] Implement User entity with password hashing in backend/src/main/java/com/todoapp/domain/model/User.java
- [x] T127 [P] [US6] Create UserDTO and RegisterDTO in backend/src/main/java/com/todoapp/application/dto/
- [x] T128 [P] [US6] Create UserMapper in backend/src/main/java/com/todoapp/application/mapper/UserMapper.java
- [x] T129 [US6] Implement UserService with registerUser(), findByEmail() in backend/src/main/java/com/todoapp/application/service/UserService.java
- [x] T130 [US6] Implement AuthService with authenticate(), login(), logout() in backend/src/main/java/com/todoapp/application/service/AuthService.java
- [x] T131 [P] [US6] Create JwtTokenProvider for token generation/validation in backend/src/main/java/com/todoapp/infrastructure/security/JwtTokenProvider.java
- [x] T132 [P] [US6] Create JwtAuthenticationFilter for request authentication in backend/src/main/java/com/todoapp/infrastructure/security/JwtAuthenticationFilter.java
- [x] T133 [P] [US6] Update SecurityConfig to enable JWT authentication in backend/src/main/java/com/todoapp/infrastructure/config/SecurityConfig.java
- [x] T134 [US6] Create AuthController with POST /api/v1/auth/register endpoint in backend/src/main/java/com/todoapp/presentation/rest/AuthController.java
- [x] T135 [US6] Add POST /api/v1/auth/login endpoint to AuthController in backend/src/main/java/com/todoapp/presentation/rest/AuthController.java
- [x] T136 [US6] Add POST /api/v1/auth/logout endpoint to AuthController in backend/src/main/java/com/todoapp/presentation/rest/AuthController.java
- [x] T137 [US6] Update TaskService to enforce user ownership on all operations in backend/src/main/java/com/todoapp/application/service/TaskService.java (already enforced)
- [x] T138 [P] [US6] Create auth types (User, AuthState) in frontend/src/types/auth.ts
- [x] T139 [P] [US6] Create auth API service (register, login, logout) in frontend/src/services/authService.ts
- [x] T140 [P] [US6] Create AuthContext for global auth state in frontend/src/context/AuthContext.tsx
- [x] T141 [P] [US6] Create LoginForm component in frontend/src/components/auth/LoginForm.tsx
- [x] T142 [P] [US6] Create RegisterForm component in frontend/src/components/auth/RegisterForm.tsx
- [x] T143 [P] [US6] Create LoginPage in frontend/src/pages/LoginPage.tsx
- [x] T144 [P] [US6] Create RegisterPage in frontend/src/pages/RegisterPage.tsx
- [x] T145 [US6] Add protected route wrapper (PrivateRoute) in frontend/src/components/auth/PrivateRoute.tsx
- [x] T146 [US6] Update App.tsx with auth routing and PrivateRoute in frontend/src/App.tsx
- [x] T147 [US6] Add JWT token storage and auto-login in frontend/src/services/authService.ts
- [x] T148 [US6] Update Axios interceptor to include JWT token in headers in frontend/src/services/api.ts

### Integration Tests for User Story 6 ✅

- [x] T149 [P] [US6] Write integration test for POST /api/v1/auth/register in backend/src/test/java/com/todoapp/integration/AuthControllerIntegrationTest.java
- [x] T150 [P] [US6] Write integration test for POST /api/v1/auth/login in backend/src/test/java/com/todoapp/integration/AuthControllerIntegrationTest.java
- [x] T151 [P] [US6] Write integration test verifying task isolation between users in backend/src/test/java/com/todoapp/integration/TaskControllerIntegrationTest.java

**Checkpoint**: 🔄 Backend multi-user support complete with secure JWT authentication. Frontend remaining (T138-T148).

---

## Phase 9: User Story 7 - Task Categories and Tags (Priority: P7)

**Goal**: Users can organize tasks with categories and multiple tags

**Independent Test**: Create categories/tags, assign to tasks, filter by category/tags

### Tests for User Story 7 (TDD - Write FIRST) ⚠️

- [ ] T152 [P] [US7] Write unit test for Category name uniqueness per user in backend/src/test/java/com/todoapp/unit/domain/CategoryTest.java
- [ ] T153 [P] [US7] Write unit test for Tag name uniqueness per user in backend/src/test/java/com/todoapp/unit/domain/TagTest.java
- [ ] T154 [P] [US7] Write unit test for CategoryService.createCategory() in backend/src/test/java/com/todoapp/unit/application/CategoryServiceTest.java

### Implementation for User Story 7

- [ ] T155 [P] [US7] Create Category entity in backend/src/main/java/com/todoapp/domain/model/Category.java
- [ ] T156 [P] [US7] Create Tag entity in backend/src/main/java/com/todoapp/domain/model/Tag.java
- [ ] T157 [P] [US7] Create TaskTag join entity in backend/src/main/java/com/todoapp/domain/model/TaskTag.java
- [ ] T158 [P] [US7] Create CategoryRepository in backend/src/main/java/com/todoapp/domain/repository/CategoryRepository.java
- [ ] T159 [P] [US7] Create TagRepository in backend/src/main/java/com/todoapp/domain/repository/TagRepository.java
- [ ] T160 [P] [US7] Create CategoryDTO and TagDTO in backend/src/main/java/com/todoapp/application/dto/
- [ ] T161 [US7] Implement CategoryService with CRUD operations in backend/src/main/java/com/todoapp/application/service/CategoryService.java
- [ ] T162 [US7] Implement TagService with CRUD operations in backend/src/main/java/com/todoapp/application/service/TagService.java
- [ ] T163 [US7] Update TaskService to handle category and tag assignments in backend/src/main/java/com/todoapp/application/service/TaskService.java
- [ ] T164 [US7] Create CategoryController with GET/POST /api/v1/categories in backend/src/main/java/com/todoapp/presentation/rest/CategoryController.java
- [ ] T165 [US7] Create TagController with GET/POST /api/v1/tags in backend/src/main/java/com/todoapp/presentation/rest/TagController.java
- [ ] T166 [US7] Add category and tag filtering to GET /api/v1/tasks in backend/src/main/java/com/todoapp/presentation/rest/TaskController.java
- [ ] T167 [P] [US7] Create category and tag types in frontend/src/types/category.ts and frontend/src/types/tag.ts
- [ ] T168 [P] [US7] Create category API service in frontend/src/services/categoryService.ts
- [ ] T169 [P] [US7] Create tag API service in frontend/src/services/tagService.ts
- [ ] T170 [P] [US7] Create CategorySelector component in frontend/src/components/tasks/CategorySelector.tsx
- [ ] T171 [P] [US7] Create TagSelector component (multi-select) in frontend/src/components/tasks/TagSelector.tsx
- [ ] T172 [US7] Add category and tag selectors to TaskForm in frontend/src/components/tasks/TaskForm.tsx
- [ ] T173 [US7] Display category and tags in TaskItem in frontend/src/components/tasks/TaskItem.tsx
- [ ] T174 [US7] Add category filter dropdown to TasksPage in frontend/src/pages/TasksPage.tsx
- [ ] T175 [US7] Add tag filter (multi-select) to TasksPage in frontend/src/pages/TasksPage.tsx

### Integration Tests for User Story 7

- [ ] T176 [P] [US7] Write integration test for category CRUD in backend/src/test/java/com/todoapp/integration/api/CategoryApiTest.java
- [ ] T177 [P] [US7] Write integration test for tag CRUD in backend/src/test/java/com/todoapp/integration/api/TagApiTest.java
- [ ] T178 [P] [US7] Write integration test for category/tag filtering in backend/src/test/java/com/todoapp/integration/api/TaskApiTest.java

**Checkpoint**: Advanced organization with categories and tags functional

---

## Phase 10: User Story 8 - Task Comments and Notes (Priority: P8)

**Goal**: Users can add, edit, and view comments on tasks

**Independent Test**: Add comments to tasks, edit comments, verify chronological display

### Tests for User Story 8 (TDD - Write FIRST) ⚠️

- [X] T179 [P] [US8] Write unit test for Comment content validation in backend/src/test/java/com/todoapp/unit/domain/CommentTest.java
- [X] T180 [P] [US8] Write unit test for CommentService.addComment() in backend/src/test/java/com/todoapp/unit/application/CommentServiceTest.java
- [X] T181 [P] [US8] Write unit test for CommentService.updateComment() with isEdited flag in backend/src/test/java/com/todoapp/unit/application/CommentServiceTest.java

### Implementation for User Story 8

- [X] T182 [P] [US8] Create Comment entity in backend/src/main/java/com/todoapp/domain/model/Comment.java
- [X] T183 [P] [US8] Create CommentRepository in backend/src/main/java/com/todoapp/domain/repository/CommentRepository.java
- [X] T184 [P] [US8] Create CommentDTO in backend/src/main/java/com/todoapp/application/dto/CommentDTO.java
- [X] T185 [US8] Implement CommentService with CRUD and authorization checks in backend/src/main/java/com/todoapp/application/service/CommentService.java
- [X] T186 [US8] Create CommentController with GET/POST /api/v1/tasks/{taskId}/comments in backend/src/main/java/com/todoapp/presentation/rest/CommentController.java
- [X] T187 [US8] Add PUT /api/v1/comments/{id} endpoint in backend/src/main/java/com/todoapp/presentation/rest/CommentController.java
- [X] T188 [US8] Add DELETE /api/v1/comments/{id} endpoint in backend/src/main/java/com/todoapp/presentation/rest/CommentController.java
- [X] T189 [P] [US8] Create Comment type in frontend/src/types/comment.ts
- [X] T190 [P] [US8] Create comment API service in frontend/src/services/commentService.ts
- [X] T191 [P] [US8] Create CommentList component in frontend/src/components/comments/CommentList.tsx
- [X] T192 [P] [US8] Create CommentItem component with edit/delete in frontend/src/components/comments/CommentItem.tsx
- [X] T193 [P] [US8] Create CommentForm component in frontend/src/components/comments/CommentForm.tsx
- [X] T194 [US8] Create TaskDetailModal with comments section in frontend/src/components/tasks/TaskDetailModal.tsx
- [X] T195 [US8] Integrate TaskDetailModal in TaskItem (click to expand) in frontend/src/components/tasks/TaskItem.tsx

### Integration Tests for User Story 8

- [X] T196 [P] [US8] Write integration test for comment CRUD in backend/src/test/java/com/todoapp/integration/api/CommentApiTest.java
- [X] T197 [P] [US8] Write integration test for comment authorization in backend/src/test/java/com/todoapp/integration/api/CommentApiTest.java

**Checkpoint**: Rich task context with comments enabled

---

## Phase 11: User Story 9 - Task Sharing and Collaboration (Priority: P9)

**Goal**: Users can share tasks with collaborators and receive real-time updates

**Independent Test**: Share task between users, verify both can view/edit, test real-time sync

### Tests for User Story 9 (TDD - Write FIRST) ⚠️

- [X] T198 [P] [US9] Write unit test for TaskShare permission validation in backend/src/test/java/com/todoapp/unit/domain/TaskShareTest.java
- [X] T199 [P] [US9] Write unit test for TaskShareService.shareTask() in backend/src/test/java/com/todoapp/unit/application/TaskShareServiceTest.java
- [X] T200 [P] [US9] Write unit test for TaskService authorization with shared tasks in backend/src/test/java/com/todoapp/unit/application/TaskServiceTest.java

### Implementation for User Story 9

- [X] T201 [P] [US9] Create TaskShare entity in backend/src/main/java/com/todoapp/domain/model/TaskShare.java
- [X] T202 [P] [US9] Create PermissionLevel enum (VIEW, EDIT) in backend/src/main/java/com/todoapp/domain/model/PermissionLevel.java
- [X] T203 [P] [US9] Create TaskShareRepository in backend/src/main/java/com/todoapp/domain/repository/TaskShareRepository.java
- [X] T204 [P] [US9] Create TaskShareDTO in backend/src/main/java/com/todoapp/application/dto/TaskShareDTO.java
- [X] T205 [US9] Implement TaskShareService with share/revoke operations in backend/src/main/java/com/todoapp/application/service/TaskShareService.java
- [X] T206 [US9] Update TaskService to include shared tasks in queries in backend/src/main/java/com/todoapp/application/service/TaskService.java
- [X] T207 [US9] Update TaskService authorization to check TaskShare permissions in backend/src/main/java/com/todoapp/application/service/TaskService.java
- [X] T208 [US9] Create TaskShareController with POST /api/v1/tasks/{taskId}/share in backend/src/main/java/com/todoapp/presentation/rest/TaskShareController.java
- [X] T209 [US9] Add DELETE /api/v1/tasks/{taskId}/share/{userId} endpoint in backend/src/main/java/com/todoapp/presentation/rest/TaskShareController.java
- [X] T210 [US9] Add GET /api/v1/tasks/shared-with-me endpoint in backend/src/main/java/com/todoapp/presentation/rest/TaskController.java
- [ ] T211 [P] [US9] Configure WebSocket STOMP over SockJS in backend/src/main/java/com/todoapp/infrastructure/config/WebSocketConfig.java
- [ ] T212 [P] [US9] Create WebSocket authentication interceptor in backend/src/main/java/com/todoapp/infrastructure/websocket/AuthChannelInterceptor.java
- [ ] T213 [P] [US9] Create TaskUpdateMessage event DTO in backend/src/main/java/com/todoapp/application/dto/TaskUpdateMessage.java
- [ ] T214 [US9] Create WebSocket handler for task updates in backend/src/main/java/com/todoapp/presentation/websocket/TaskWebSocketHandler.java
- [ ] T215 [US9] Update TaskService to broadcast WebSocket messages on task changes in backend/src/main/java/com/todoapp/application/service/TaskService.java
- [ ] T216 [P] [US9] Create WebSocket client with STOMP in frontend/src/services/websocket.ts
- [ ] T217 [P] [US9] Create useWebSocket hook for task subscriptions in frontend/src/hooks/useWebSocket.ts
- [ ] T218 [P] [US9] Create ShareTaskDialog component in frontend/src/components/tasks/ShareTaskDialog.tsx
- [ ] T219 [US9] Add share button to TaskDetailModal in frontend/src/components/tasks/TaskDetailModal.tsx
- [ ] T220 [US9] Integrate WebSocket updates in TasksPage for real-time sync in frontend/src/pages/TasksPage.tsx
- [ ] T221 [US9] Add "Shared with me" view in TasksPage in frontend/src/pages/TasksPage.tsx

### Integration Tests for User Story 9

- [X] T222 [P] [US9] Write integration test for task sharing in backend/src/test/java/com/todoapp/integration/api/TaskShareApiTest.java
- [ ] T223 [P] [US9] Write WebSocket contract test in backend/src/test/java/com/todoapp/contract/WebSocketContractTest.java
- [X] T224 [P] [US9] Write integration test for shared task authorization in backend/src/test/java/com/todoapp/integration/api/TaskApiTest.java

**Checkpoint**: Full collaboration features with real-time updates enabled

---

## Phase 12: User Story 10 - Subtasks and Task Hierarchies (Priority: P10)

**Goal**: Users can create subtasks and view hierarchical task structures

**Independent Test**: Create parent tasks with subtasks, verify nesting, test completion progress

### Tests for User Story 10 (TDD - Write FIRST) ⚠️

- [ ] T225 [P] [US10] Write unit test for Task subtask depth validation (max 5 levels) in backend/src/test/java/com/todoapp/unit/domain/TaskTest.java
- [ ] T226 [P] [US10] Write unit test for Task.calculateSubtaskProgress() in backend/src/test/java/com/todoapp/unit/domain/TaskTest.java

### Implementation for User Story 10

- [ ] T227 [P] [US10] Add parentTask relationship to Task entity (already in schema) in backend/src/main/java/com/todoapp/domain/model/Task.java
- [ ] T228 [P] [US10] Add subtask validation (max depth 5) to TaskValidator in backend/src/main/java/com/todoapp/domain/validator/TaskValidator.java
- [ ] T229 [US10] Add calculateSubtaskProgress() method to Task entity in backend/src/main/java/com/todoapp/domain/model/Task.java
- [ ] T230 [US10] Update TaskService to handle subtask creation in backend/src/main/java/com/todoapp/application/service/TaskService.java
- [ ] T231 [US10] Add GET /api/v1/tasks/{id}/subtasks endpoint in backend/src/main/java/com/todoapp/presentation/rest/TaskController.java
- [ ] T232 [US10] Add POST /api/v1/tasks/{id}/subtasks endpoint in backend/src/main/java/com/todoapp/presentation/rest/TaskController.java
- [ ] T233 [US10] Update DELETE /api/v1/tasks/{id} to handle subtask confirmation in backend/src/main/java/com/todoapp/presentation/rest/TaskController.java
- [ ] T234 [P] [US10] Create SubtaskList component with nested display in frontend/src/components/tasks/SubtaskList.tsx
- [ ] T235 [P] [US10] Create AddSubtaskButton component in frontend/src/components/tasks/AddSubtaskButton.tsx
- [ ] T236 [US10] Add subtask display to TaskDetailModal in frontend/src/components/tasks/TaskDetailModal.tsx
- [ ] T237 [US10] Add progress bar for subtask completion in frontend/src/components/tasks/TaskItem.tsx
- [ ] T238 [US10] Add delete confirmation for tasks with subtasks in frontend/src/components/tasks/TaskItem.tsx

### Integration Tests for User Story 10

- [ ] T239 [P] [US10] Write integration test for subtask CRUD in backend/src/test/java/com/todoapp/integration/api/TaskApiTest.java
- [ ] T240 [P] [US10] Write integration test for subtask depth validation in backend/src/test/java/com/todoapp/integration/api/TaskApiTest.java

**Checkpoint**: Complex task breakdown with hierarchies enabled

---

## Phase 13: User Story 11 - Recurring Tasks (Priority: P11)

**Goal**: Automatic task generation based on recurrence schedules

**Independent Test**: Create recurring tasks (daily, weekly, monthly), verify automatic generation

### Tests for User Story 11 (TDD - Write FIRST) ⚠️

- [ ] T241 [P] [US11] Write unit test for RecurrencePattern validation in backend/src/test/java/com/todoapp/unit/domain/RecurrencePatternTest.java
- [ ] T242 [P] [US11] Write unit test for RecurrenceService.generateNextInstance() in backend/src/test/java/com/todoapp/unit/application/RecurrenceServiceTest.java

### Implementation for User Story 11

- [ ] T243 [P] [US11] Create RecurrencePattern entity in backend/src/main/java/com/todoapp/domain/model/RecurrencePattern.java
- [ ] T244 [P] [US11] Create Frequency enum (DAILY, WEEKLY, MONTHLY) in backend/src/main/java/com/todoapp/domain/model/Frequency.java
- [ ] T245 [P] [US11] Create RecurrencePatternRepository in backend/src/main/java/com/todoapp/domain/repository/RecurrencePatternRepository.java
- [ ] T246 [P] [US11] Create RecurrencePatternDTO in backend/src/main/java/com/todoapp/application/dto/RecurrencePatternDTO.java
- [ ] T247 [US11] Implement RecurrenceService with instance generation logic in backend/src/main/java/com/todoapp/application/service/RecurrenceService.java
- [ ] T248 [US11] Update TaskService to handle recurrence pattern assignments in backend/src/main/java/com/todoapp/application/service/TaskService.java
- [ ] T249 [P] [US11] Create RecurrenceProcessor scheduled job (RabbitMQ consumer) in backend/src/main/java/com/todoapp/infrastructure/messaging/RecurrenceProcessor.java
- [ ] T250 [P] [US11] Configure RabbitMQ queue for recurring tasks in backend/src/main/java/com/todoapp/infrastructure/config/RabbitMQConfig.java
- [ ] T251 [US11] Add recurrence pattern to POST /api/v1/tasks in backend/src/main/java/com/todoapp/presentation/rest/TaskController.java
- [ ] T252 [US11] Add PUT /api/v1/tasks/{id}/recurrence endpoint in backend/src/main/java/com/todoapp/presentation/rest/TaskController.java
- [ ] T253 [P] [US11] Create RecurrenceSelector component in frontend/src/components/tasks/RecurrenceSelector.tsx
- [ ] T254 [US11] Add recurrence selector to TaskForm in frontend/src/components/tasks/TaskForm.tsx
- [ ] T255 [US11] Display recurrence indicator in TaskItem in frontend/src/components/tasks/TaskItem.tsx

### Integration Tests for User Story 11

- [ ] T256 [P] [US11] Write integration test for recurring task creation in backend/src/test/java/com/todoapp/integration/api/TaskApiTest.java
- [ ] T257 [P] [US11] Write integration test for RecurrenceProcessor job in backend/src/test/java/com/todoapp/integration/messaging/RecurrenceProcessorTest.java

**Checkpoint**: Automated recurring tasks functional

---

## Phase 14: User Story 12 - Time Tracking and Duration (Priority: P12)

**Goal**: Track time spent on tasks with start/stop timer and manual logging

**Independent Test**: Start/stop timers, manually log time, verify time reports

### Tests for User Story 12 (TDD - Write FIRST) ⚠️

- [ ] T258 [P] [US12] Write unit test for TimeEntry duration calculation in backend/src/test/java/com/todoapp/unit/domain/TimeEntryTest.java
- [ ] T259 [P] [US12] Write unit test for TimeTrackingService.startTimer() in backend/src/test/java/com/todoapp/unit/application/TimeTrackingServiceTest.java
- [ ] T260 [P] [US12] Write unit test for TimeTrackingService.stopTimer() in backend/src/test/java/com/todoapp/unit/application/TimeTrackingServiceTest.java

### Implementation for User Story 12

- [ ] T261 [P] [US12] Create TimeEntry entity in backend/src/main/java/com/todoapp/domain/model/TimeEntry.java
- [ ] T262 [P] [US12] Create EntryType enum (MANUAL, TIMER) in backend/src/main/java/com/todoapp/domain/model/EntryType.java
- [ ] T263 [P] [US12] Create TimeEntryRepository in backend/src/main/java/com/todoapp/domain/repository/TimeEntryRepository.java
- [ ] T264 [P] [US12] Create TimeEntryDTO in backend/src/main/java/com/todoapp/application/dto/TimeEntryDTO.java
- [ ] T265 [US12] Implement TimeTrackingService with timer and manual logging in backend/src/main/java/com/todoapp/application/service/TimeTrackingService.java
- [ ] T266 [US12] Create TimeTrackingController with POST /api/v1/tasks/{taskId}/time-entries/start in backend/src/main/java/com/todoapp/presentation/rest/TimeTrackingController.java
- [ ] T267 [US12] Add POST /api/v1/time-entries/{id}/stop endpoint in backend/src/main/java/com/todoapp/presentation/rest/TimeTrackingController.java
- [ ] T268 [US12] Add POST /api/v1/tasks/{taskId}/time-entries (manual log) endpoint in backend/src/main/java/com/todoapp/presentation/rest/TimeTrackingController.java
- [ ] T269 [US12] Add GET /api/v1/time-entries/report endpoint in backend/src/main/java/com/todoapp/presentation/rest/TimeTrackingController.java
- [ ] T270 [P] [US12] Create TimeTracker component with start/stop button in frontend/src/components/tasks/TimeTracker.tsx
- [ ] T271 [P] [US12] Create TimerDisplay component (real-time elapsed time) in frontend/src/components/tasks/TimerDisplay.tsx
- [ ] T272 [P] [US12] Create ManualTimeLogDialog component in frontend/src/components/tasks/ManualTimeLogDialog.tsx
- [ ] T273 [US12] Add TimeTracker to TaskDetailModal in frontend/src/components/tasks/TaskDetailModal.tsx
- [ ] T274 [US12] Display total tracked time in TaskItem in frontend/src/components/tasks/TaskItem.tsx
- [ ] T275 [P] [US12] Create TimeReportPage with charts in frontend/src/pages/TimeReportPage.tsx

### Integration Tests for User Story 12

- [ ] T276 [P] [US12] Write integration test for time tracking in backend/src/test/java/com/todoapp/integration/api/TimeTrackingApiTest.java

**Checkpoint**: Full time tracking and productivity analysis enabled

---

## Phase 15: User Story 13 - Batch Operations (Priority: P13)

**Goal**: Bulk actions on multiple tasks (complete, delete, assign category/tags)

**Independent Test**: Select multiple tasks, apply batch operations, verify all affected

### Tests for User Story 13 (TDD - Write FIRST) ⚠️

- [ ] T277 [P] [US13] Write unit test for TaskService.batchComplete() in backend/src/test/java/com/todoapp/unit/application/TaskServiceTest.java
- [ ] T278 [P] [US13] Write unit test for TaskService.batchDelete() in backend/src/test/java/com/todoapp/unit/application/TaskServiceTest.java

### Implementation for User Story 13

- [ ] T279 [P] [US13] Create BatchOperationDTO in backend/src/main/java/com/todoapp/application/dto/BatchOperationDTO.java
- [ ] T280 [US13] Implement TaskService.batchOperation() with transactional handling in backend/src/main/java/com/todoapp/application/service/TaskService.java
- [ ] T281 [US13] Add POST /api/v1/tasks/batch endpoint in backend/src/main/java/com/todoapp/presentation/rest/TaskController.java
- [ ] T282 [P] [US13] Create BatchActionBar component in frontend/src/components/tasks/BatchActionBar.tsx
- [ ] T283 [US13] Add checkbox selection to TaskItem in frontend/src/components/tasks/TaskItem.tsx
- [ ] T284 [US13] Add "Select All" functionality to TaskList in frontend/src/components/tasks/TaskList.tsx
- [ ] T285 [US13] Integrate BatchActionBar in TasksPage in frontend/src/pages/TasksPage.tsx
- [ ] T286 [US13] Add batch confirmation dialog in frontend/src/components/tasks/BatchActionBar.tsx

### Integration Tests for User Story 13

- [ ] T287 [P] [US13] Write integration test for batch operations in backend/src/test/java/com/todoapp/integration/api/TaskApiTest.java

**Checkpoint**: Efficient bulk operations enabled

---

## Phase 16: User Story 14 - File Attachments and Rich Text (Priority: P14)

**Goal**: Attach files to tasks and support rich text formatting in descriptions

**Independent Test**: Upload files (PDF, images), download, verify virus scanning, test rich text

### Tests for User Story 14 (TDD - Write FIRST) ⚠️

- [ ] T288 [P] [US14] Write unit test for FileAttachment size validation in backend/src/test/java/com/todoapp/unit/domain/FileAttachmentTest.java
- [ ] T289 [P] [US14] Write unit test for FileStorageService.uploadFile() in backend/src/test/java/com/todoapp/unit/infrastructure/FileStorageServiceTest.java

### Implementation for User Story 14

- [ ] T290 [P] [US14] Create FileAttachment entity in backend/src/main/java/com/todoapp/domain/model/FileAttachment.java
- [ ] T291 [P] [US14] Create VirusScanStatus enum in backend/src/main/java/com/todoapp/domain/model/VirusScanStatus.java
- [ ] T292 [P] [US14] Create FileAttachmentRepository in backend/src/main/java/com/todoapp/domain/repository/FileAttachmentRepository.java
- [ ] T293 [P] [US14] Create FileAttachmentDTO in backend/src/main/java/com/todoapp/application/dto/FileAttachmentDTO.java
- [ ] T294 [US14] Implement FileStorageService with MinIO integration in backend/src/main/java/com/todoapp/infrastructure/storage/FileStorageService.java
- [ ] T295 [US14] Implement VirusScanService (ClamAV integration via RabbitMQ) in backend/src/main/java/com/todoapp/infrastructure/messaging/VirusScanService.java
- [ ] T296 [US14] Implement FileAttachmentService with upload/download/delete in backend/src/main/java/com/todoapp/application/service/FileAttachmentService.java
- [ ] T297 [US14] Create FileAttachmentController with POST /api/v1/tasks/{taskId}/attachments in backend/src/main/java/com/todoapp/presentation/rest/FileAttachmentController.java
- [ ] T298 [US14] Add GET /api/v1/attachments/{id}/download endpoint in backend/src/main/java/com/todoapp/presentation/rest/FileAttachmentController.java
- [ ] T299 [US14] Add DELETE /api/v1/attachments/{id} endpoint in backend/src/main/java/com/todoapp/presentation/rest/FileAttachmentController.java
- [ ] T300 [P] [US14] Create FileUpload component with drag-and-drop in frontend/src/components/shared/FileUpload.tsx
- [ ] T301 [P] [US14] Create AttachmentList component in frontend/src/components/tasks/AttachmentList.tsx
- [ ] T302 [P] [US14] Create RichTextEditor component (lightweight markdown) in frontend/src/components/shared/RichTextEditor.tsx
- [ ] T303 [US14] Replace TaskForm description input with RichTextEditor in frontend/src/components/tasks/TaskForm.tsx
- [ ] T304 [US14] Add FileUpload to TaskDetailModal in frontend/src/components/tasks/TaskDetailModal.tsx
- [ ] T305 [US14] Display AttachmentList in TaskDetailModal in frontend/src/components/tasks/TaskDetailModal.tsx
- [ ] T306 [US14] Add file type and size validation in frontend/src/components/shared/FileUpload.tsx

### Integration Tests for User Story 14

- [ ] T307 [P] [US14] Write integration test for file upload in backend/src/test/java/com/todoapp/integration/api/FileAttachmentApiTest.java
- [ ] T308 [P] [US14] Write integration test for MinIO storage in backend/src/test/java/com/todoapp/integration/storage/MinIOStorageTest.java

**Checkpoint**: Rich task documentation with file attachments enabled

---

## Phase 17: User Story 15 - Notifications and Reminders (Priority: P15)

**Goal**: Notifications for due dates, mentions, shares, and comments

**Independent Test**: Set due dates, share tasks, add comments, verify notifications delivered

### Tests for User Story 15 (TDD - Write FIRST) ⚠️

- [ ] T309 [P] [US15] Write unit test for NotificationService.createNotification() in backend/src/test/java/com/todoapp/unit/application/NotificationServiceTest.java
- [ ] T310 [P] [US15] Write unit test for DueDateNotifier scheduled job in backend/src/test/java/com/todoapp/unit/infrastructure/DueDateNotifierTest.java

### Implementation for User Story 15

- [ ] T311 [P] [US15] Create Notification entity in backend/src/main/java/com/todoapp/domain/model/Notification.java
- [ ] T312 [P] [US15] Create NotificationType enum in backend/src/main/java/com/todoapp/domain/model/NotificationType.java
- [ ] T313 [P] [US15] Create NotificationPreference entity in backend/src/main/java/com/todoapp/domain/model/NotificationPreference.java
- [ ] T314 [P] [US15] Create NotificationRepository in backend/src/main/java/com/todoapp/domain/repository/NotificationRepository.java
- [ ] T315 [P] [US15] Create NotificationPreferenceRepository in backend/src/main/java/com/todoapp/domain/repository/NotificationPreferenceRepository.java
- [ ] T316 [P] [US15] Create NotificationDTO in backend/src/main/java/com/todoapp/application/dto/NotificationDTO.java
- [ ] T317 [US15] Implement NotificationService with channel dispatch (email, in-app) in backend/src/main/java/com/todoapp/application/service/NotificationService.java
- [ ] T318 [US15] Implement EmailNotifier (RabbitMQ consumer for email sending) in backend/src/main/java/com/todoapp/infrastructure/messaging/EmailNotifier.java
- [ ] T319 [US15] Implement DueDateNotifier (scheduled job checking due dates) in backend/src/main/java/com/todoapp/infrastructure/messaging/DueDateNotifier.java
- [ ] T320 [US15] Update TaskShareService to trigger share notifications in backend/src/main/java/com/todoapp/application/service/TaskShareService.java
- [ ] T321 [US15] Update CommentService to trigger mention notifications in backend/src/main/java/com/todoapp/application/service/CommentService.java
- [ ] T322 [US15] Create NotificationController with GET /api/v1/notifications in backend/src/main/java/com/todoapp/presentation/rest/NotificationController.java
- [ ] T323 [US15] Add PATCH /api/v1/notifications/{id}/read endpoint in backend/src/main/java/com/todoapp/presentation/rest/NotificationController.java
- [ ] T324 [US15] Add GET /api/v1/notification-preferences endpoint in backend/src/main/java/com/todoapp/presentation/rest/NotificationController.java
- [ ] T325 [US15] Add PUT /api/v1/notification-preferences/{type} endpoint in backend/src/main/java/com/todoapp/presentation/rest/NotificationController.java
- [ ] T326 [US15] Configure WebSocket topic for notifications /user/queue/notifications in backend/src/main/java/com/todoapp/presentation/websocket/NotificationWebSocketHandler.java
- [ ] T327 [P] [US15] Create NotificationBell component (icon with badge) in frontend/src/components/notifications/NotificationBell.tsx
- [ ] T328 [P] [US15] Create NotificationList dropdown in frontend/src/components/notifications/NotificationList.tsx
- [ ] T329 [P] [US15] Create NotificationItem component in frontend/src/components/notifications/NotificationItem.tsx
- [ ] T330 [US15] Add NotificationBell to app header/navbar in frontend/src/components/layout/Header.tsx
- [ ] T331 [US15] Subscribe to /user/queue/notifications WebSocket topic in frontend/src/hooks/useWebSocket.ts
- [ ] T332 [P] [US15] Create NotificationPreferencesPage in frontend/src/pages/NotificationPreferencesPage.tsx
- [ ] T333 [US15] Add real-time notification toast on WebSocket message in frontend/src/components/notifications/NotificationToast.tsx

### Integration Tests for User Story 15

- [ ] T334 [P] [US15] Write integration test for notification creation in backend/src/test/java/com/todoapp/integration/api/NotificationApiTest.java
- [ ] T335 [P] [US15] Write integration test for DueDateNotifier job in backend/src/test/java/com/todoapp/integration/messaging/DueDateNotifierTest.java

**Checkpoint**: Full notification system with due date reminders, mentions, and in-app alerts enabled

---

## Phase 18: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] T336 [P] Create comprehensive API documentation with OpenAPI annotations in backend/src/main/java/com/todoapp/presentation/rest/
- [ ] T337 [P] Add rate limiting configuration with Bucket4j in backend/src/main/java/com/todoapp/infrastructure/config/RateLimitConfig.java
- [ ] T338 [P] Configure Redis caching for frequently accessed tasks in backend/src/main/java/com/todoapp/infrastructure/cache/TaskCacheService.java
- [ ] T339 [P] Add correlation IDs (MDC) for request tracing in backend/src/main/java/com/todoapp/infrastructure/config/CorrelationIdFilter.java
- [ ] T340 [P] Create error boundary component in frontend/src/components/shared/ErrorBoundary.tsx
- [ ] T341 [P] Add loading skeletons for better UX in frontend/src/components/shared/Skeleton.tsx
- [ ] T342 [P] Add optimistic updates for all mutations in frontend/src/services/
- [ ] T343 [P] Create docker-compose.test.yml for test environment in repository root
- [ ] T344 [P] Add E2E tests with Playwright in frontend/tests/e2e/
- [ ] T345 [P] Add performance monitoring with Micrometer custom metrics in backend/src/main/java/com/todoapp/infrastructure/config/MetricsConfig.java
- [ ] T346 [P] Configure graceful shutdown in backend/src/main/resources/application.yml
- [ ] T347 Run full quickstart.md validation (docker compose up, test all features)
- [ ] T348 Create production-ready README.md with architecture diagram in repository root
- [ ] T349 [P] Security audit (dependency scanning, OWASP check) using Maven plugins
- [ ] T350 [P] Performance testing (load test with 500 concurrent users) using JMeter or Gatling

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-17)**: All depend on Foundational phase completion
  - User Story 6 (Authentication) SHOULD complete before US7-US15 (they depend on user context)
  - User Story 9 (Sharing) depends on User Story 6 (Auth) for multi-user features
  - User Stories 1-5 can be implemented in parallel with proper coordination
  - User Stories 7-15 can be implemented in parallel after US6 is complete
- **Polish (Phase 18)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational - May integrate with US1 but independently testable
- **User Story 3 (P3)**: Can start after Foundational - Builds on US1 but independently testable
- **User Story 4 (P4)**: Can start after Foundational - Enhances US1 but independently testable
- **User Story 5 (P5)**: Can start after Foundational - Adds fields to US1 but independently testable
- **User Story 6 (P6)**: Can start after Foundational - Foundation for multi-user features
- **User Story 7 (P7)**: Depends on US6 (user context) - Independently testable after auth enabled
- **User Story 8 (P8)**: Depends on US6 (user context) - Independently testable
- **User Story 9 (P9)**: Depends on US6 (requires auth for sharing) - Independently testable
- **User Story 10 (P10)**: Depends on US1 (task model) - Independently testable
- **User Story 11 (P11)**: Depends on US1 (task model) - Independently testable
- **User Story 12 (P12)**: Depends on US1 (task model) - Independently testable
- **User Story 13 (P13)**: Depends on US1 (task operations) - Independently testable
- **User Story 14 (P14)**: Depends on US1 (task model) - Independently testable
- **User Story 15 (P15)**: Depends on US6 (notifications to users) - Independently testable

### Within Each User Story

- Tests (if TDD) MUST be written and FAIL before implementation
- Models before services
- Services before controllers
- Backend endpoints before frontend integration
- Core implementation before integration tests
- Story complete before moving to next priority

### Parallel Opportunities

- **Setup tasks**: T003-T012 marked [P] can run in parallel
- **Foundational database migrations**: T013-T025 can run in parallel (Flyway executes sequentially)
- **Foundational config**: T026-T040 marked [P] can run in parallel
- **Once Foundational completes**: User Stories 1-5 can start in parallel (if team capacity allows)
- **After US6 complete**: User Stories 7-15 can start in parallel
- **Within each story**: All tasks marked [P] can run in parallel
- **Tests within a story**: All test tasks marked [P] can run in parallel
- **Frontend components**: Most frontend component tasks marked [P] can run in parallel

---

## Parallel Example: User Story 1

```bash
# Launch all TDD tests for User Story 1 together:
Task T042: "Write unit test for Task entity validation in backend/src/test/java/com/todoapp/unit/domain/TaskTest.java"
Task T043: "Write unit test for TaskService.createTask() in backend/src/test/java/com/todoapp/unit/application/TaskServiceTest.java"
Task T044: "Write unit test for TaskService.getUserTasks() in backend/src/test/java/com/todoapp/unit/application/TaskServiceTest.java"

# Launch all entities for User Story 1 together:
Task T045: "Create User entity in backend/src/main/java/com/todoapp/domain/model/User.java"
Task T046: "Create Task entity in backend/src/main/java/com/todoapp/domain/model/Task.java"
Task T047: "Create Priority enum in backend/src/main/java/com/todoapp/domain/model/Priority.java"

# Launch all DTOs for User Story 1 together:
Task T050: "Create TaskCreateDTO in backend/src/main/java/com/todoapp/application/dto/TaskCreateDTO.java"
Task T051: "Create TaskResponseDTO in backend/src/main/java/com/todoapp/application/dto/TaskResponseDTO.java"
Task T052: "Create TaskMapper in backend/src/main/java/com/todoapp/application/mapper/TaskMapper.java"

# Launch all frontend components for User Story 1 together:
Task T056: "Create Task type definition in frontend/src/types/task.ts"
Task T057: "Create task API service in frontend/src/services/taskService.ts"
Task T058: "Create TaskForm component in frontend/src/components/tasks/TaskForm.tsx"
Task T059: "Create TaskList component in frontend/src/components/tasks/TaskList.tsx"
Task T060: "Create TaskItem component in frontend/src/components/tasks/TaskItem.tsx"
```

---

## Implementation Strategy

### MVP First (User Stories 1-5 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1 (Create and View)
4. **STOP and VALIDATE**: Test User Story 1 independently
5. Complete Phase 4: User Story 2 (Mark Complete)
6. Complete Phase 5: User Story 3 (Edit/Delete)
7. Complete Phase 6: User Story 4 (Filter/Search)
8. Complete Phase 7: User Story 5 (Priority/Due Dates)
9. **STOP and VALIDATE**: Test all basic task management (US1-US5)
10. Deploy/demo MVP

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (Minimal MVP!)
3. Add User Stories 2-5 → Test independently → Deploy/Demo (Full MVP!)
4. Add User Story 6 (Auth) → Test independently → Deploy/Demo (Multi-user!)
5. Add User Stories 7-9 → Test independently → Deploy/Demo (Collaboration!)
6. Add User Stories 10-15 → Test independently → Deploy/Demo (Power features!)
7. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1
   - Developer B: User Story 2
   - Developer C: User Story 3
3. After US6 (Auth) complete:
   - Developer A: User Story 7 (Categories/Tags)
   - Developer B: User Story 8 (Comments)
   - Developer C: User Story 9 (Sharing)
4. Stories complete and integrate independently

---

## Summary

**Total Tasks**: 350
**User Stories**: 15 (P1-P15)
**Setup Tasks**: 12 (T001-T012)
**Foundational Tasks**: 29 (T013-T041)
**User Story Tasks**: 294 (T042-T335)
**Polish Tasks**: 15 (T336-T350)

**Tasks by Story**:
- US1: 26 tasks (T042-T067)
- US2: 12 tasks (T068-T079)
- US3: 16 tasks (T080-T095)
- US4: 12 tasks (T096-T106)
- US5: 16 tasks (T107-T121)
- US6: 31 tasks (T122-T151) - Foundation for multi-user
- US7: 26 tasks (T152-T178)
- US8: 19 tasks (T179-T197)
- US9: 27 tasks (T198-T224)
- US10: 16 tasks (T225-T240)
- US11: 17 tasks (T241-T257)
- US12: 19 tasks (T258-T276)
- US13: 11 tasks (T277-T287)
- US14: 19 tasks (T288-T308)
- US15: 27 tasks (T309-T335)

**Parallel Opportunities**: 150+ tasks marked [P] can run in parallel within their phase

**MVP Scope Recommendation**:
- **Minimal MVP**: User Story 1 only (26 tasks + 41 foundation = 67 tasks)
- **Full MVP**: User Stories 1-5 (82 tasks + 41 foundation = 123 tasks)
- **Multi-user MVP**: User Stories 1-6 (113 tasks + 41 foundation = 154 tasks)

**Constitutional Compliance**:
- ✅ TDD for core logic: All domain/service tests written BEFORE implementation
- ✅ Integration tests AFTER implementation
- ✅ Clean separation: Domain → Application → Infrastructure → Presentation
- ✅ Progressive enhancement: Each user story is independently deployable
- ✅ All 15-Factor principles embedded in architecture

**Ready to Execute**: All tasks have specific file paths and can be executed with `/speckit.implement`
