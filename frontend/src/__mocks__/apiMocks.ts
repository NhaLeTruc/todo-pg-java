/**
 * API service mocks for testing
 * Provides mock implementations of API services
 */

import type { Category } from '@/types/category';
import type { Tag } from '@/types/tag';
import type { Task } from '@/types/task';

// Mock task service
export const mockTaskService = {
  getAll: jest.fn().mockResolvedValue([]),
  getById: jest.fn().mockResolvedValue(null),
  create: jest.fn().mockResolvedValue({} as Task),
  update: jest.fn().mockResolvedValue({} as Task),
  delete: jest.fn().mockResolvedValue(undefined),
  complete: jest.fn().mockResolvedValue({} as Task),
  uncomplete: jest.fn().mockResolvedValue({} as Task),
  getSharedTasks: jest.fn().mockResolvedValue([]),
  shareTask: jest.fn().mockResolvedValue(undefined),
};

// Mock category service
export const mockCategoryService = {
  getAll: jest.fn().mockResolvedValue([]),
  getById: jest.fn().mockResolvedValue(null),
  create: jest.fn().mockResolvedValue({} as Category),
  update: jest.fn().mockResolvedValue({} as Category),
  delete: jest.fn().mockResolvedValue(undefined),
};

// Mock tag service
export const mockTagService = {
  getAll: jest.fn().mockResolvedValue([]),
  getById: jest.fn().mockResolvedValue(null),
  create: jest.fn().mockResolvedValue({} as Tag),
  update: jest.fn().mockResolvedValue({} as Tag),
  delete: jest.fn().mockResolvedValue(undefined),
};

// Helper to reset all mocks
export const resetAllApiMocks = () => {
  Object.values(mockTaskService).forEach((mock) => {
    if (jest.isMockFunction(mock)) {
      mock.mockClear();
    }
  });
  Object.values(mockCategoryService).forEach((mock) => {
    if (jest.isMockFunction(mock)) {
      mock.mockClear();
    }
  });
  Object.values(mockTagService).forEach((mock) => {
    if (jest.isMockFunction(mock)) {
      mock.mockClear();
    }
  });
};

// Helper to setup common mock responses
export const setupMockResponses = {
  tasks: (tasks: Task[]) => {
    mockTaskService.getAll.mockResolvedValue(tasks);
  },
  taskById: (task: Task | null) => {
    mockTaskService.getById.mockResolvedValue(task);
  },
  createTask: (task: Task) => {
    mockTaskService.create.mockResolvedValue(task);
  },
  updateTask: (task: Task) => {
    mockTaskService.update.mockResolvedValue(task);
  },
  deleteTask: () => {
    mockTaskService.delete.mockResolvedValue(undefined);
  },
  categories: (categories: Category[]) => {
    mockCategoryService.getAll.mockResolvedValue(categories);
  },
  categoryById: (category: Category | null) => {
    mockCategoryService.getById.mockResolvedValue(category);
  },
  tags: (tags: Tag[]) => {
    mockTagService.getAll.mockResolvedValue(tags);
  },
  tagById: (tag: Tag | null) => {
    mockTagService.getById.mockResolvedValue(tag);
  },
  error: (service: any, method: string, error: Error) => {
    service[method].mockRejectedValue(error);
  },
};

// Mock fetch implementation with custom responses
export const createMockFetch = (responses: Record<string, any>) => {
  return jest.fn((url: string) => {
    const response = responses[url];
    if (!response) {
      return Promise.resolve({
        ok: false,
        status: 404,
        json: async () => ({ message: 'Not found' }),
      });
    }
    return Promise.resolve({
      ok: true,
      status: 200,
      json: async () => response,
      ...response,
    });
  });
};

// Common mock handlers for testing
export const mockHandlers = {
  onSubmit: jest.fn(),
  onDelete: jest.fn(),
  onEdit: jest.fn(),
  onToggleComplete: jest.fn(),
  onChange: jest.fn(),
  onClick: jest.fn(),
  onClose: jest.fn(),
  onCancel: jest.fn(),
  onSuccess: jest.fn(),
  onError: jest.fn(),
};

// Reset all mock handlers
export const resetMockHandlers = () => {
  Object.values(mockHandlers).forEach((handler) => {
    if (jest.isMockFunction(handler)) {
      handler.mockClear();
    }
  });
};

// Wait for async operations to complete
export const waitForAsync = () => new Promise((resolve) => setTimeout(resolve, 0));

// Mock local storage
export const mockLocalStorage = {
  getItem: jest.fn(),
  setItem: jest.fn(),
  removeItem: jest.fn(),
  clear: jest.fn(),
};

// Setup localStorage mock
export const setupLocalStorageMock = () => {
  Object.defineProperty(window, 'localStorage', {
    value: mockLocalStorage,
    writable: true,
  });
};
