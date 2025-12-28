# Test Suite Improvement Plan - todo-pg-java

**Status**: Ready for Review
**Created**: 2025-12-28
**Estimated Total Effort**: 24-31 hours (phased implementation)

---

## Executive Summary

This plan addresses critical test failures and expands test coverage across the todo-pg-java full-stack application. The implementation follows a phased approach:

1. **Phase 1** (CRITICAL): Fix failing tests - 2-3 hours
2. **Phase 2** (HIGH): Create test infrastructure - 3-4 hours
3. **Phase 3** (HIGH): Add component tests (balanced approach) - 12-15 hours
4. **Phase 4** (MEDIUM): Backend infrastructure tests - 4-5 hours
5. **Phase 5** (MEDIUM): Service & hook tests - 2-3 hours
6. **Phase 6** (LOW): Accessibility & security tests - 3-4 hours

**User Preferences Applied**:
- ✅ Sudo access available for fixing backend permissions
- ✅ Balanced test approach: Comprehensive for critical components, basic for others
- ✅ Accessibility tests in separate phase (Phase 6)

---

## Current State Analysis

### Backend (Java/Spring Boot)
- **Status**: ✅ All 34 tests passing (22 unit + 12 integration)
- **Problem**: ❌ Cannot run locally - `backend/target/` owned by root from Docker builds
- **Coverage**: 70% enforced by JaCoCo
- **Test Frameworks**: JUnit 5, Mockito, Spring Boot Test, MockMvc, REST Assured, TestContainers
- **Major Gaps**:
  - VirusScanService: 0 tests
  - TaskCacheService: 0 tests
  - WebSocket edge cases: Limited coverage
  - Concurrent operations: Not tested
  - Security vulnerabilities: Limited testing

### Frontend (React/TypeScript)
- **Status**: ❌ 2 of 3 test suites FAILING
- **Coverage**: ~5% (only 3 component test files out of 40+ components)
- **Test Frameworks**: Jest 29, ts-jest, React Testing Library, jsdom, Playwright (E2E)
- **Critical Failures**:
  1. **import.meta.env syntax error** - Vite-specific, Jest incompatible (affects TaskForm.test.tsx)
  2. **Priority enum error** - Importing as type, using as value (TaskItem.test.tsx:16)
  3. **Missing fetch polyfill** - Node environment lacks fetch (TaskItem component errors)
- **Major Gaps**:
  - 40+ components untested (auth, shared, task, comment, notification)
  - 5 hooks untested (useTasks, useNotifications, useTaskWebSocket, etc.)
  - 9 services untested (API, auth, task, WebSocket, etc.)
  - No test infrastructure (test-utils, factories, mocks)

### E2E Tests (Playwright)
- **Status**: Unknown (requires Docker environment to run)
- **Coverage**: 3 test files (auth.spec.ts, tasks.spec.ts, notifications.spec.ts)
- **Configuration**: Multi-browser support (Chromium, Firefox, WebKit, mobile)

---

## Phase 1: Fix Critical Test Failures ⚡

**Priority**: CRITICAL
**Estimated Time**: 2-3 hours
**Blockers**: None

### Task 1.1: Fix Backend Permission Issues

**File**: `backend/target/` directory

**Problem**: Directory owned by root from previous Docker builds, preventing Maven from writing test results.

**Solution** (with sudo access):
```bash
sudo chown -R bob:bob /home/bob/WORK/todo-pg-java/backend/target/
```

**Verification**:
```bash
cd backend && mvn test
# Expected: Tests run: 34, Failures: 0, Errors: 0, Skipped: 0
```

---

### Task 1.2: Fix Jest Configuration for import.meta.env

**File**: `frontend/jest.config.ts`

**Problem**: Jest runs in Node.js environment and doesn't understand Vite's `import.meta.env` syntax.

**Solution**: Add globals configuration to mock `import.meta.env`:

```typescript
const config: Config.InitialOptions = {
  preset: 'ts-jest',
  testEnvironment: 'jsdom',
  roots: ['<rootDir>/src'],
  testMatch: ['**/__tests__/**/*.{ts,tsx}', '**/*.{spec,test}.{ts,tsx}'],

  // ADD THIS SECTION
  globals: {
    'ts-jest': {
      tsconfig: {
        jsx: 'react',
        esModuleInterop: true,
        allowSyntheticDefaultImports: true,
      },
    },
  },

  moduleNameMapper: {
    '^@/(.*)$': '<rootDir>/src/$1',
    '\\.(css|less|scss|sass)$': 'identity-obj-proxy',
  },
  setupFilesAfterEnv: ['<rootDir>/src/setupTests.ts'],
  // ... rest of config
};
```

---

### Task 1.3: Create Enhanced setupTests.ts

**File**: `frontend/src/setupTests.ts`

**Problem**: Missing global mocks for Node environment (fetch, localStorage, WebSocket, import.meta).

**Solution**: Replace minimal setup with comprehensive mocks:

```typescript
import '@testing-library/jest-dom';

// Mock import.meta.env for Jest compatibility
Object.defineProperty(global, 'importMeta', {
  value: {
    env: {
      VITE_API_BASE_URL: 'http://localhost:8080/api/v1',
      VITE_WS_URL: 'http://localhost:8080/ws',
      MODE: 'test',
      DEV: false,
      PROD: false,
      SSR: false,
    },
  },
  writable: true,
});

// Polyfill fetch for Node environment
global.fetch = jest.fn();

// Mock localStorage
const localStorageMock = {
  getItem: jest.fn(),
  setItem: jest.fn(),
  removeItem: jest.fn(),
  clear: jest.fn(),
  length: 0,
  key: jest.fn(),
};
global.localStorage = localStorageMock as Storage;
global.sessionStorage = localStorageMock as Storage;

// Mock window.matchMedia (for responsive components)
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: jest.fn().mockImplementation((query) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: jest.fn(),
    removeListener: jest.fn(),
    addEventListener: jest.fn(),
    removeEventListener: jest.fn(),
    dispatchEvent: jest.fn(),
  })),
});

// Mock WebSocket
global.WebSocket = jest.fn().mockImplementation(() => ({
  close: jest.fn(),
  send: jest.fn(),
  addEventListener: jest.fn(),
  removeEventListener: jest.fn(),
  readyState: 1, // OPEN
  CONNECTING: 0,
  OPEN: 1,
  CLOSING: 2,
  CLOSED: 3,
})) as any;

// Mock IntersectionObserver (for lazy loading)
global.IntersectionObserver = class IntersectionObserver {
  constructor() {}
  observe() {}
  unobserve() {}
  disconnect() {}
  takeRecords() { return []; }
  root = null;
  rootMargin = '';
  thresholds = [];
};

// Suppress console errors during tests (optional)
const originalError = console.error;
beforeAll(() => {
  console.error = (...args: any[]) => {
    if (
      typeof args[0] === 'string' &&
      args[0].includes('Warning: ReactDOM.render')
    ) {
      return;
    }
    originalError.call(console, ...args);
  };
});

afterAll(() => {
  console.error = originalError;
});
```

**Install Dependencies**:
```bash
cd frontend
npm install --save-dev whatwg-fetch
```

---

### Task 1.4: Fix Priority Enum Usage

**File**: `frontend/src/components/tasks/__tests__/TaskItem.test.tsx`

**Problem**: Line 16 uses `Priority.MEDIUM` but Priority is defined as a type, not an enum:
```typescript
// In types/task.ts
export type Priority = 'LOW' | 'MEDIUM' | 'HIGH';  // Type, not enum!
```

**Solution**: Update test to use string literals with type assertion:

```typescript
// BEFORE (Line 4)
import { Priority, Task } from '@/types/task';

// AFTER
import type { Priority } from '@/types/task';  // Import as type only
import { Task } from '@/types/task';

// BEFORE (Line 16)
const baseTask: Task = {
  id: 1,
  description: 'Test task',
  priority: Priority.MEDIUM,  // ❌ Doesn't exist
  // ...
};

// AFTER
const baseTask: Task = {
  id: 1,
  description: 'Test task',
  priority: 'MEDIUM' as Priority,  // ✅ String literal with type
  // ...
};
```

**Alternative**: Use constants from `utils/constants.ts`:
```typescript
import { PRIORITY_LEVELS } from '@/utils/constants';

const baseTask: Task = {
  priority: PRIORITY_LEVELS.MEDIUM as Priority,
  // ...
};
```

---

### Task 1.5: Verification

**Run all frontend tests**:
```bash
cd frontend
npm test
# Expected: All 3 test suites pass
# Expected: Tests run: 42, Failures: 0
```

**Run backend tests**:
```bash
cd backend
mvn test
# Expected: Tests run: 34, Failures: 0, Errors: 0, Skipped: 0
```

---

## Phase 2: Frontend Test Infrastructure 🏗️

**Priority**: HIGH
**Estimated Time**: 3-4 hours
**Dependencies**: Phase 1 complete

### Task 2.1: Create test-utils.tsx

**File**: `frontend/src/test-utils.tsx` (NEW)

**Purpose**: Custom render function that wraps components with all necessary providers (AuthContext, QueryClient, Router).

```typescript
import { ReactElement } from 'react';
import { render, RenderOptions } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';

// Create a custom query client for testing
const createTestQueryClient = () =>
  new QueryClient({
    defaultOptions: {
      queries: {
        retry: false, // Don't retry failed queries in tests
        cacheTime: 0, // Don't cache queries
        staleTime: 0,
      },
      mutations: {
        retry: false,
      },
    },
    logger: {
      log: console.log,
      warn: console.warn,
      error: () => {}, // Suppress errors in tests
    },
  });

interface AllProvidersProps {
  children: React.ReactNode;
}

// All providers wrapper
function AllProviders({ children }: AllProvidersProps) {
  const queryClient = createTestQueryClient();

  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AuthProvider>{children}</AuthProvider>
      </BrowserRouter>
    </QueryClientProvider>
  );
}

// Custom render function
function customRender(
  ui: ReactElement,
  options?: Omit<RenderOptions, 'wrapper'>
) {
  return render(ui, { wrapper: AllProviders, ...options });
}

// Re-export everything from React Testing Library
export * from '@testing-library/react';
export { customRender as render };
export { createTestQueryClient };
```

**Usage in tests**:
```typescript
// Instead of:
import { render } from '@testing-library/react';

// Use:
import { render } from '@/test-utils';  // Automatically includes all providers
```

---

### Task 2.2: Create Mock Factories

**File**: `frontend/src/__mocks__/factories.ts` (NEW)

**Purpose**: Factory functions to generate consistent mock data for tests.

```typescript
import { Task, Priority } from '@/types/task';
import { User } from '@/types/auth';
import { Comment } from '@/types/comment';
import { Notification } from '@/types/notification';
import { Category } from '@/types/category';
import { Tag } from '@/types/tag';

// Task factory
export const createMockTask = (overrides: Partial<Task> = {}): Task => ({
  id: 1,
  description: 'Test task',
  isCompleted: false,
  priority: 'MEDIUM' as Priority,
  dueDate: null,
  completedAt: null,
  position: 0,
  categoryId: null,
  categoryName: null,
  categoryColor: null,
  tags: [],
  estimatedDurationMinutes: null,
  actualDurationMinutes: null,
  isOverdue: false,
  parentTaskId: null,
  depth: 0,
  subtaskProgress: 0,
  createdAt: '2024-01-01T10:00:00Z',
  updatedAt: '2024-01-01T10:00:00Z',
  ...overrides,
});

// User factory
export const createMockUser = (overrides: Partial<User> = {}): User => ({
  id: 1,
  email: 'test@example.com',
  fullName: 'Test User',
  isActive: true,
  emailVerified: true,
  createdAt: '2024-01-01T10:00:00Z',
  updatedAt: '2024-01-01T10:00:00Z',
  lastLoginAt: '2024-01-01T10:00:00Z',
  ...overrides,
});

// Comment factory
export const createMockComment = (overrides: Partial<Comment> = {}): Comment => ({
  id: 1,
  taskId: 1,
  userId: 1,
  userName: 'Test User',
  content: 'Test comment',
  createdAt: '2024-01-01T10:00:00Z',
  updatedAt: '2024-01-01T10:00:00Z',
  ...overrides,
});

// Notification factory
export const createMockNotification = (
  overrides: Partial<Notification> = {}
): Notification => ({
  id: 1,
  userId: 1,
  type: 'TASK_DUE_SOON',
  title: 'Test notification',
  message: 'Test notification message',
  read: false,
  createdAt: '2024-01-01T10:00:00Z',
  ...overrides,
});

// Category factory
export const createMockCategory = (overrides: Partial<Category> = {}): Category => ({
  id: 1,
  name: 'Test Category',
  color: '#3B82F6',
  userId: 1,
  createdAt: '2024-01-01T10:00:00Z',
  updatedAt: '2024-01-01T10:00:00Z',
  ...overrides,
});

// Tag factory
export const createMockTag = (overrides: Partial<Tag> = {}): Tag => ({
  id: 1,
  name: 'Test Tag',
  userId: 1,
  createdAt: '2024-01-01T10:00:00Z',
  updatedAt: '2024-01-01T10:00:00Z',
  ...overrides,
});

// Helper to create multiple items
export const createMockTasks = (count: number, overrides: Partial<Task> = {}): Task[] =>
  Array.from({ length: count }, (_, i) =>
    createMockTask({ id: i + 1, ...overrides })
  );

export const createMockComments = (count: number, overrides: Partial<Comment> = {}): Comment[] =>
  Array.from({ length: count }, (_, i) =>
    createMockComment({ id: i + 1, ...overrides })
  );
```

**Usage in tests**:
```typescript
import { createMockTask, createMockTasks } from '@/__mocks__/factories';

// Single task
const task = createMockTask({ priority: 'HIGH', isCompleted: true });

// Multiple tasks
const tasks = createMockTasks(5, { categoryId: 1 });
```

---

### Task 2.3: Create Service Mocks

**Directory**: `frontend/src/__mocks__/`

**File 1**: `frontend/src/__mocks__/axios.ts` (NEW)

```typescript
export default {
  get: jest.fn(),
  post: jest.fn(),
  put: jest.fn(),
  patch: jest.fn(),
  delete: jest.fn(),
  create: jest.fn(() => ({
    get: jest.fn(),
    post: jest.fn(),
    put: jest.fn(),
    patch: jest.fn(),
    delete: jest.fn(),
    interceptors: {
      request: { use: jest.fn(), eject: jest.fn() },
      response: { use: jest.fn(), eject: jest.fn() },
    },
  })),
  interceptors: {
    request: { use: jest.fn(), eject: jest.fn() },
    response: { use: jest.fn(), eject: jest.fn() },
  },
};
```

**File 2**: `frontend/src/__mocks__/authService.ts` (NEW)

```typescript
export const authService = {
  login: jest.fn(),
  register: jest.fn(),
  logout: jest.fn(),
  getToken: jest.fn(),
  getUser: jest.fn(),
  isAuthenticated: jest.fn(),
};
```

**File 3**: `frontend/src/__mocks__/taskService.ts` (NEW)

```typescript
import { createMockTask, createMockTasks } from './factories';

export const taskService = {
  getTasks: jest.fn().mockResolvedValue({
    content: createMockTasks(5),
    totalElements: 5,
    totalPages: 1,
    size: 20,
    number: 0,
    first: true,
    last: true,
    empty: false,
  }),
  getTaskById: jest.fn().mockResolvedValue(createMockTask()),
  createTask: jest.fn().mockResolvedValue(createMockTask()),
  updateTask: jest.fn().mockResolvedValue(createMockTask()),
  deleteTask: jest.fn().mockResolvedValue(undefined),
  toggleComplete: jest.fn().mockResolvedValue(createMockTask({ isCompleted: true })),
};
```

---

## Phase 3: Expand Frontend Component Tests 🧪

**Priority**: HIGH
**Estimated Time**: 12-15 hours
**Dependencies**: Phase 2 complete
**Strategy**: Balanced - Comprehensive tests for Tier 1-2, basic tests for Tier 3-4

### Implementation Priority Order

**Tier 1: Critical Auth Components** (Comprehensive Testing - Security Critical)
1. **LoginForm.test.tsx**
   - Form rendering and field validation
   - Successful login flow with auth context integration
   - Error handling (invalid credentials, network errors)
   - Loading states during submission
   - Navigation after successful login
   - Accessibility (ARIA labels, keyboard navigation)

2. **RegisterForm.test.tsx**
   - Form validation (email format, password strength, confirmation match)
   - Successful registration flow
   - Error handling (duplicate email, weak password)
   - Terms acceptance checkbox
   - Accessibility compliance

3. **PrivateRoute.test.tsx**
   - Redirects unauthenticated users to login
   - Allows authenticated users to access protected routes
   - Preserves redirect URL after login

**Tier 2: Shared Components** (Comprehensive Testing - Foundation Components)
4. **ConfirmDialog.test.tsx**
   - Dialog open/close states
   - Confirm and cancel button callbacks
   - Backdrop click handling
   - Keyboard shortcuts (Escape to cancel, Enter to confirm)
   - Custom button labels
   - Destructive action styling
   - ARIA attributes (role, labelledby, describedby)

5. **DatePicker.test.tsx**
   - Date selection and display
   - Date format validation
   - Clear date functionality
   - Keyboard navigation
   - Accessibility (labels, focus management)

6. **FileUpload.test.tsx**
   - File selection via input
   - Drag and drop functionality
   - File type validation
   - File size validation (25MB limit)
   - Multiple file upload
   - Upload progress display
   - Error handling (invalid file type, too large)

7. **RichTextEditor.test.tsx**
   - Text input and formatting
   - Bold, italic, underline commands
   - Link insertion
   - Mention functionality (@user)
   - Character count display
   - Accessibility (toolbar buttons, keyboard shortcuts)

**Tier 3: Core Task Components** (Basic Testing - Business Logic)
8. **CategorySelector.test.tsx** - Selection, creation, display
9. **PrioritySelector.test.tsx** - Priority selection, display
10. **ShareTaskDialog.test.tsx** - User search, permission levels
11. **TimeTracker.test.tsx** - Start/stop timer, manual entry
12. **SubtaskList.test.tsx** - Add/remove subtasks, progress display

**Tier 4: Comment & Notification Components** (Basic Testing)
13. **CommentForm.test.tsx** - Comment creation, mention support
14. **CommentItem.test.tsx** - Comment display, edit/delete
15. **CommentList.test.tsx** - List rendering, loading states
16. **NotificationBell.test.tsx** - Badge display, unread count
17. **NotificationItem.test.tsx** - Notification display, mark as read
18. **NotificationList.test.tsx** - List rendering, filter by type

### Example Test Implementation (LoginForm)

**File**: `frontend/src/components/auth/__tests__/LoginForm.test.tsx` (NEW)

```typescript
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { render } from '@/test-utils';
import { LoginForm } from '../LoginForm';
import * as authService from '@/services/authService';

jest.mock('@/services/authService');

const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
}));

describe('LoginForm', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders login form with email and password fields', () => {
    render(<LoginForm />);

    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /login/i })).toBeInTheDocument();
  });

  it('validates required fields', async () => {
    const user = userEvent.setup();
    render(<LoginForm />);

    const submitButton = screen.getByRole('button', { name: /login/i });
    await user.click(submitButton);

    const emailInput = screen.getByLabelText(/email/i) as HTMLInputElement;
    expect(emailInput.validity.valid).toBe(false);
  });

  it('submits form with valid credentials', async () => {
    const user = userEvent.setup();
    const mockLogin = jest.spyOn(authService, 'login').mockResolvedValue({
      token: 'test-token',
      userId: 1,
      email: 'test@example.com',
      fullName: 'Test User',
    });

    render(<LoginForm />);

    await user.type(screen.getByLabelText(/email/i), 'test@example.com');
    await user.type(screen.getByLabelText(/password/i), 'Password123');
    await user.click(screen.getByRole('button', { name: /login/i }));

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith({
        email: 'test@example.com',
        password: 'Password123',
      });
    });
  });

  it('displays error message on failed login', async () => {
    const user = userEvent.setup();
    jest.spyOn(authService, 'login').mockRejectedValue({
      response: { data: { message: 'Invalid credentials' } },
    });

    render(<LoginForm />);

    await user.type(screen.getByLabelText(/email/i), 'wrong@example.com');
    await user.type(screen.getByLabelText(/password/i), 'wrongpass');
    await user.click(screen.getByRole('button', { name: /login/i }));

    await waitFor(() => {
      expect(screen.getByText(/invalid credentials/i)).toBeInTheDocument();
    });
  });

  it('disables form during submission', async () => {
    const user = userEvent.setup();
    jest.spyOn(authService, 'login').mockImplementation(
      () => new Promise((resolve) => setTimeout(resolve, 100))
    );

    render(<LoginForm />);

    await user.type(screen.getByLabelText(/email/i), 'test@example.com');
    await user.type(screen.getByLabelText(/password/i), 'Password123');

    const submitButton = screen.getByRole('button', { name: /login/i });
    await user.click(submitButton);

    expect(submitButton).toBeDisabled();
    expect(screen.getByLabelText(/email/i)).toBeDisabled();
  });

  it('navigates to dashboard on successful login', async () => {
    const user = userEvent.setup();
    jest.spyOn(authService, 'login').mockResolvedValue({
      token: 'test-token',
      userId: 1,
      email: 'test@example.com',
      fullName: 'Test User',
    });

    render(<LoginForm />);

    await user.type(screen.getByLabelText(/email/i), 'test@example.com');
    await user.type(screen.getByLabelText(/password/i), 'Password123');
    await user.click(screen.getByRole('button', { name: /login/i }));

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/');
    });
  });

  it('has proper ARIA labels for accessibility', () => {
    render(<LoginForm />);

    const form = screen.getByRole('form');
    expect(form).toHaveAttribute('aria-label', 'Login form');

    const emailInput = screen.getByLabelText(/email/i);
    expect(emailInput).toHaveAttribute('type', 'email');
    expect(emailInput).toHaveAttribute('aria-required', 'true');

    const passwordInput = screen.getByLabelText(/password/i);
    expect(passwordInput).toHaveAttribute('type', 'password');
    expect(passwordInput).toHaveAttribute('aria-required', 'true');
  });
});
```

### Test Pattern Guidelines

**All component tests should follow this pattern**:

```typescript
// 1. Imports
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { render } from '@/test-utils';  // Custom render with providers
import { Component } from '../Component';
import { createMockX } from '@/__mocks__/factories';  // Mock data

// 2. Mock external dependencies
jest.mock('@/services/someService');

// 3. Describe block with component name
describe('ComponentName', () => {
  // 4. Setup and cleanup
  beforeEach(() => {
    jest.clearAllMocks();
  });

  // 5. Test cases
  it('should do something when condition', async () => {
    const user = userEvent.setup();
    const mockCallback = jest.fn();

    render(<Component prop={value} onAction={mockCallback} />);

    // Assertions
    expect(screen.getByRole('...')).toBeInTheDocument();

    // User interactions
    await user.click(screen.getByRole('button'));

    // Verify callbacks
    expect(mockCallback).toHaveBeenCalledWith(expectedArg);
  });
});
```

**Query Priorities** (follow this order):
1. `getByRole()` - Most accessible (button, textbox, heading, etc.)
2. `getByLabelText()` - Forms (input labels)
3. `getByPlaceholderText()` - Placeholder text
4. `getByText()` - Text content
5. `getByTestId()` - Last resort only

---

## Phase 4: Frontend Service & Hook Tests 🔌

**Priority**: MEDIUM
**Estimated Time**: 2-3 hours
**Dependencies**: Phase 2 complete

### Service Tests

**File**: `frontend/src/services/__tests__/taskService.test.ts` (NEW)

```typescript
import axios from 'axios';
import { taskService } from '../taskService';
import { createMockTask } from '@/__mocks__/factories';

jest.mock('axios');
const mockedAxios = axios as jest.Mocked<typeof axios>;

describe('taskService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('getTasks', () => {
    it('fetches tasks with default parameters', async () => {
      const mockResponse = {
        data: {
          content: [createMockTask()],
          totalElements: 1,
          totalPages: 1,
        },
      };
      mockedAxios.get.mockResolvedValue(mockResponse);

      const result = await taskService.getTasks();

      expect(mockedAxios.get).toHaveBeenCalledWith('/tasks', {
        params: {
          page: 0,
          size: 20,
          sortBy: 'createdAt',
          sortDirection: 'desc',
          search: undefined,
          completed: undefined,
        },
        headers: { 'X-User-Id': '1' },
      });
      expect(result.content).toHaveLength(1);
    });
  });

  describe('createTask', () => {
    it('creates a new task', async () => {
      const newTask = { description: 'New task', priority: 'HIGH' as const };
      const mockResponse = { data: createMockTask(newTask) };
      mockedAxios.post.mockResolvedValue(mockResponse);

      const result = await taskService.createTask(newTask);

      expect(mockedAxios.post).toHaveBeenCalledWith('/tasks', newTask, {
        headers: { 'X-User-Id': '1' },
      });
      expect(result.description).toBe('New task');
    });
  });

  describe('deleteTask', () => {
    it('deletes a task', async () => {
      mockedAxios.delete.mockResolvedValue({});

      await taskService.deleteTask(1);

      expect(mockedAxios.delete).toHaveBeenCalledWith('/tasks/1', {
        headers: { 'X-User-Id': '1' },
      });
    });
  });
});
```

**Additional Service Tests**:
- `authService.test.ts` - Login/logout/token handling
- `commentService.test.ts` - CRUD operations
- `websocket.test.ts` - WebSocket connections

### Hook Tests

**File**: `frontend/src/hooks/__tests__/useTasks.test.ts` (NEW)

```typescript
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useTasks, useCreateTask } from '../useTasks';
import { taskService } from '@/services/taskService';
import { createMockTask } from '@/__mocks__/factories';

jest.mock('@/services/taskService');

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );
};

describe('useTasks', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('fetches tasks successfully', async () => {
    const mockTasks = {
      content: [createMockTask()],
      totalElements: 1,
      totalPages: 1,
    };
    (taskService.getTasks as jest.Mock).mockResolvedValue(mockTasks);

    const { result } = renderHook(() => useTasks(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data?.content).toHaveLength(1);
    expect(taskService.getTasks).toHaveBeenCalledTimes(1);
  });

  it('handles fetch error', async () => {
    (taskService.getTasks as jest.Mock).mockRejectedValue(new Error('Network error'));

    const { result } = renderHook(() => useTasks(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => expect(result.current.isError).toBe(true));
    expect(result.current.error).toBeDefined();
  });
});

describe('useCreateTask', () => {
  it('creates task successfully', async () => {
    const newTask = { description: 'New task', priority: 'HIGH' as const };
    (taskService.createTask as jest.Mock).mockResolvedValue(createMockTask(newTask));

    const { result } = renderHook(() => useCreateTask(), {
      wrapper: createWrapper(),
    });

    result.current.mutate(newTask);

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(taskService.createTask).toHaveBeenCalledWith(newTask);
  });
});
```

**Additional Hook Tests**:
- `useNotifications.test.ts` - Notification fetching
- `useTaskWebSocket.test.ts` - WebSocket subscriptions

---

## Phase 5: Enhance Backend Tests ☕

**Priority**: MEDIUM
**Estimated Time**: 4-5 hours
**Dependencies**: Phase 1 complete

### Task 5.1: VirusScanService Tests

**File**: `backend/src/test/java/com/todoapp/unit/infrastructure/VirusScanServiceTest.java` (NEW)

**Test Coverage**:
- File queuing for virus scan (RabbitMQ)
- Scan processing (clean files)
- Suspicious file detection
- Error handling (file not found, download errors)
- Manual rescan functionality
- Concurrent scan requests
- Stream cleanup

**Pattern**: Mock RabbitTemplate, FileStorageService, FileAttachmentRepository

```java
package com.todoapp.unit.infrastructure;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.todoapp.domain.repository.FileAttachmentRepository;
import com.todoapp.infrastructure.messaging.VirusScanService;
import com.todoapp.infrastructure.storage.FileStorageService;

@ExtendWith(MockitoExtension.class)
public class VirusScanServiceTest {

  @Mock private RabbitTemplate rabbitTemplate;
  @Mock private FileAttachmentRepository fileAttachmentRepository;
  @Mock private FileStorageService fileStorageService;

  @InjectMocks private VirusScanService virusScanService;

  @Test
  @DisplayName("Should queue file for virus scanning")
  public void shouldQueueFileForScanning() {
    UUID attachmentId = UUID.randomUUID();

    virusScanService.queueForScanning(attachmentId);

    verify(rabbitTemplate).convertAndSend(eq("virus-scan-queue"), eq(attachmentId.toString()));
  }

  // Additional tests...
}
```

---

### Task 5.2: TaskCacheService Tests

**File**: `backend/src/test/java/com/todoapp/unit/infrastructure/TaskCacheServiceTest.java` (NEW)

**Test Coverage**:
- Cache task successfully
- Evict task from cache
- Evict all user tasks
- Cache with custom TTL
- Cache statistics
- Handle null values
- Handle empty cache

**Pattern**: Mock RedisTemplate, CacheManager

---

### Task 5.3: WebSocket Edge Case Tests

**File**: `backend/src/test/java/com/todoapp/integration/WebSocketEdgeCasesTest.java` (NEW)

**Test Coverage**:
- Rapid connection/disconnection cycles
- Multiple concurrent subscriptions
- Connection timeout handling
- Message to closed connection
- Large message payloads

**Pattern**: Use Spring WebSocket test client, StompSession

---

### Task 5.4: Security Tests

**File**: `backend/src/test/java/com/todoapp/integration/SecurityTest.java` (NEW)

**Test Coverage**:
- SQL injection prevention in search
- XSS sanitization in task descriptions
- CSRF protection
- Authorization bypass attempts
- Input validation

**Pattern**: Use MockMvc with malicious payloads

---

## Phase 6: Non-Functional Tests 🔒

**Priority**: LOW
**Estimated Time**: 3-4 hours
**Dependencies**: Phases 1-3 complete

### Task 6.1: Accessibility Tests

**Install**:
```bash
npm install --save-dev jest-axe @types/jest-axe
```

**File**: `frontend/src/components/tasks/__tests__/TaskItem.a11y.test.tsx` (NEW)

```typescript
import { render } from '@/test-utils';
import { axe, toHaveNoViolations } from 'jest-axe';
import { TaskItem } from '../TaskItem';
import { createMockTask } from '@/__mocks__/factories';

expect.extend(toHaveNoViolations);

describe('TaskItem Accessibility', () => {
  it('should have no accessibility violations', async () => {
    const { container } = render(
      <TaskItem
        task={createMockTask()}
        onToggleComplete={jest.fn()}
        onDelete={jest.fn()}
        onEdit={jest.fn()}
      />
    );

    const results = await axe(container);
    expect(results).toHaveNoViolations();
  });

  it('should be keyboard navigable', async () => {
    const { getByRole } = render(
      <TaskItem
        task={createMockTask()}
        onToggleComplete={jest.fn()}
        onDelete={jest.fn()}
        onEdit={jest.fn()}
      />
    );

    const completeButton = getByRole('button', { name: /mark complete/i });
    completeButton.focus();
    expect(document.activeElement).toBe(completeButton);
  });
});
```

**Create a11y tests for**:
- LoginForm
- RegisterForm
- TaskForm
- ConfirmDialog
- DatePicker
- FileUpload

---

## Critical Files Summary

### Files to Modify (4)
1. `frontend/jest.config.ts` - Add globals for import.meta.env
2. `frontend/src/setupTests.ts` - Add comprehensive mocks
3. `frontend/src/components/tasks/__tests__/TaskItem.test.tsx` - Fix Priority import
4. `frontend/package.json` - Add dependencies (whatwg-fetch, jest-axe)

### New Files to Create (60+)

**Frontend Infrastructure (5 files)**:
- `frontend/src/test-utils.tsx`
- `frontend/src/__mocks__/factories.ts`
- `frontend/src/__mocks__/axios.ts`
- `frontend/src/__mocks__/authService.ts`
- `frontend/src/__mocks__/taskService.ts`

**Frontend Component Tests (18 files)**:
- Auth: LoginForm.test.tsx, RegisterForm.test.tsx, PrivateRoute.test.tsx
- Shared: ConfirmDialog.test.tsx, DatePicker.test.tsx, FileUpload.test.tsx, RichTextEditor.test.tsx
- Tasks: CategorySelector.test.tsx, PrioritySelector.test.tsx, ShareTaskDialog.test.tsx, TimeTracker.test.tsx, SubtaskList.test.tsx
- Comments: CommentForm.test.tsx, CommentItem.test.tsx, CommentList.test.tsx
- Notifications: NotificationBell.test.tsx, NotificationItem.test.tsx, NotificationList.test.tsx

**Frontend Service/Hook Tests (8 files)**:
- Services: taskService.test.ts, authService.test.ts, commentService.test.ts, websocket.test.ts
- Hooks: useTasks.test.ts, useNotifications.test.ts, useTaskWebSocket.test.ts, useComments.test.ts

**Frontend Accessibility Tests (~10 files)**:
- Various component.a11y.test.tsx files

**Backend Tests (4 files)**:
- VirusScanServiceTest.java
- TaskCacheServiceTest.java
- WebSocketEdgeCasesTest.java
- SecurityTest.java

---

## Success Criteria

### Phase 1 (Critical)
- ✅ Backend tests run locally without permission errors
- ✅ All 3 frontend test suites pass (42 tests)
- ✅ No import.meta.env errors
- ✅ No Priority enum errors
- ✅ No fetch polyfill errors

### Phase 2 (High Priority)
- ✅ Test infrastructure in place (test-utils, factories, mocks)
- ✅ All tests import from test-utils, not directly from @testing-library/react

### Phase 3 (High Priority)
- ✅ At least 18 component test files added
- ✅ All Tier 1 (auth) components have comprehensive tests
- ✅ All Tier 2 (shared) components have comprehensive tests
- ✅ Frontend coverage reaches 40-70%

### Phase 4 (Medium Priority)
- ✅ Service tests cover all API interactions
- ✅ Hook tests cover React Query usage
- ✅ Frontend coverage maintains 70%+

### Phase 5 (Medium Priority)
- ✅ Backend infrastructure tests added (VirusScan, Cache, WebSocket)
- ✅ Security tests in place
- ✅ Backend coverage maintains 70%+

### Phase 6 (Low Priority)
- ✅ Accessibility tests added for key components
- ✅ No major a11y violations
- ✅ Keyboard navigation verified

---

## Verification Commands

**Backend Tests**:
```bash
cd backend
mvn clean test
mvn jacoco:report
# View coverage: open target/site/jacoco/index.html
```

**Frontend Tests**:
```bash
cd frontend
npm test
npm run test:coverage
# View coverage: open coverage/index.html
```

**E2E Tests**:
```bash
cd frontend
npm run test:e2e
npm run test:e2e:ui  # With Playwright UI
```

**All Tests**:
```bash
# Backend
cd backend && mvn verify

# Frontend
cd frontend && npm test && npm run test:e2e
```

---

## Risk Mitigation

### Potential Issues

1. **Backend permission issues persist**
   - Mitigation: Document Docker-based test execution as alternative

2. **import.meta.env mock doesn't work**
   - Mitigation: Use webpack define plugin or environment variables

3. **Test coverage doesn't reach 70%**
   - Mitigation: Focus on critical paths first, lower threshold temporarily

4. **E2E tests fail in Docker**
   - Mitigation: Run locally first, debug Docker networking issues

5. **Time overruns**
   - Mitigation: Implement Phase 1-2 first, get approval before continuing

---

## Notes

- **Follow existing patterns**: TaskItem.test.tsx, TaskForm.test.tsx, TaskList.test.tsx
- **Use semantic queries**: getByRole, getByLabelText over test IDs
- **Test behavior, not implementation**: Focus on user interactions and outcomes
- **Mock external dependencies**: axios, WebSocket, localStorage
- **Use factories**: Consistent test data via mock factories
- **Descriptive test names**: `shouldDoSomethingWhenCondition()`
- **Accessibility first**: Include ARIA tests in component tests (not just Phase 6)

---

## Next Steps

1. **Review this plan** - Confirm approach and priorities
2. **Start with Phase 1** - Fix critical failures (2-3 hours)
3. **Get approval** - Verify Phase 1 results before continuing
4. **Proceed with Phases 2-3** - Infrastructure and component tests
5. **Iterate** - Adjust based on coverage reports and feedback

---

**Ready to execute when approved!**
