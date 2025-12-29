/**
 * Shared mock data for tests
 * Use these predefined data sets for consistent testing across components
 */

import {
  createMockTask,
  createCompletedTask,
  createOverdueTask,
  createHighPriorityTask,
  createWorkCategory,
  createPersonalCategory,
  createUrgentCategory,
  createImportantTag,
  createUrgentTag,
  createBugTag,
  createFeatureTag,
} from './factories';

// Common tasks
export const mockTasks = {
  basic: createMockTask({ id: 1, description: 'Buy groceries' }),
  completed: createCompletedTask({ id: 2, description: 'Finish homework' }),
  overdue: createOverdueTask({ id: 3, description: 'Submit report' }),
  highPriority: createHighPriorityTask({ id: 4, description: 'Fix critical bug' }),
  withCategory: createMockTask({
    id: 5,
    description: 'Team meeting',
    categoryId: 1,
    categoryName: 'Work',
    categoryColor: '#3b82f6',
  }),
  withTags: createMockTask({
    id: 6,
    description: 'Review PR',
    tags: [
      { id: 1, name: 'important', color: '#ef4444', createdAt: '', updatedAt: '' },
      { id: 2, name: 'urgent', color: '#f59e0b', createdAt: '', updatedAt: '' },
    ],
  }),
  withDueDate: createMockTask({
    id: 7,
    description: 'Project deadline',
    dueDate: '2024-12-31T23:59:59Z',
  }),
  subtask: createMockTask({
    id: 8,
    description: 'Subtask 1',
    parentTaskId: 1,
    depth: 1,
  }),
};

// Task lists for different scenarios
export const mockTaskLists = {
  empty: [],
  single: [mockTasks.basic],
  mixed: [
    mockTasks.basic,
    mockTasks.completed,
    mockTasks.overdue,
    mockTasks.highPriority,
    mockTasks.withCategory,
  ],
  allCompleted: [
    createCompletedTask({ id: 10, description: 'Task 1' }),
    createCompletedTask({ id: 11, description: 'Task 2' }),
    createCompletedTask({ id: 12, description: 'Task 3' }),
  ],
  allOverdue: [
    createOverdueTask({ id: 20, description: 'Overdue 1' }),
    createOverdueTask({ id: 21, description: 'Overdue 2' }),
  ],
};

// Common categories
export const mockCategories = {
  work: createWorkCategory(),
  personal: createPersonalCategory(),
  urgent: createUrgentCategory(),
};

export const mockCategoryList = [mockCategories.work, mockCategories.personal, mockCategories.urgent];

// Common tags
export const mockTags = {
  important: createImportantTag(),
  urgent: createUrgentTag(),
  bug: createBugTag(),
  feature: createFeatureTag(),
};

export const mockTagList = [mockTags.important, mockTags.urgent, mockTags.bug, mockTags.feature];

// Pagination response mock
export const createMockPaginatedResponse = <T>(
  content: T[],
  page: number = 0,
  size: number = 20
) => ({
  content,
  totalElements: content.length,
  totalPages: Math.ceil(content.length / size),
  size,
  number: page,
  first: page === 0,
  last: page === Math.ceil(content.length / size) - 1,
  empty: content.length === 0,
});

// API response helpers
export const mockApiResponses = {
  success: <T>(data: T) => ({
    ok: true,
    status: 200,
    json: async () => data,
    text: async () => JSON.stringify(data),
  }),
  created: <T>(data: T) => ({
    ok: true,
    status: 201,
    json: async () => data,
    text: async () => JSON.stringify(data),
  }),
  noContent: () => ({
    ok: true,
    status: 204,
    json: async () => null,
    text: async () => '',
  }),
  error: (status: number, message: string) => ({
    ok: false,
    status,
    json: async () => ({ message }),
    text: async () => JSON.stringify({ message }),
  }),
  notFound: () => ({
    ok: false,
    status: 404,
    json: async () => ({ message: 'Not found' }),
    text: async () => JSON.stringify({ message: 'Not found' }),
  }),
  unauthorized: () => ({
    ok: false,
    status: 401,
    json: async () => ({ message: 'Unauthorized' }),
    text: async () => JSON.stringify({ message: 'Unauthorized' }),
  }),
  serverError: () => ({
    ok: false,
    status: 500,
    json: async () => ({ message: 'Internal server error' }),
    text: async () => JSON.stringify({ message: 'Internal server error' }),
  }),
};

// Common test scenarios
export const testScenarios = {
  emptyState: {
    tasks: [],
    categories: [],
    tags: [],
  },
  withData: {
    tasks: mockTaskLists.mixed,
    categories: mockCategoryList,
    tags: mockTagList,
  },
  loadingState: {
    isLoading: true,
    data: null,
  },
  errorState: {
    isLoading: false,
    error: new Error('Failed to load data'),
  },
};
