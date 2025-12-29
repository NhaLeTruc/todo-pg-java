import type { Category, CategoryCreateRequest } from '@/types/category';
import type { Tag, TagCreateRequest } from '@/types/tag';
import type { Priority, Task, TaskCreateRequest } from '@/types/task';

// Counter for generating unique IDs
let taskIdCounter = 1;
let categoryIdCounter = 1;
let tagIdCounter = 1;

// Reset ID counters (useful for test isolation)
export const resetFactoryCounters = () => {
  taskIdCounter = 1;
  categoryIdCounter = 1;
  tagIdCounter = 1;
};

// Task factory
export const createMockTask = (overrides: Partial<Task> = {}): Task => {
  const id = overrides.id ?? taskIdCounter++;
  return {
    id,
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
  };
};

// Create multiple tasks
export const createMockTasks = (count: number, overrides: Partial<Task> = {}): Task[] => {
  return Array.from({ length: count }, (_, i) =>
    createMockTask({
      ...overrides,
      description: `${overrides.description || 'Test task'} ${i + 1}`,
    })
  );
};

// Task with specific states
export const createCompletedTask = (overrides: Partial<Task> = {}): Task =>
  createMockTask({
    isCompleted: true,
    completedAt: '2024-01-01T12:00:00Z',
    ...overrides,
  });

export const createOverdueTask = (overrides: Partial<Task> = {}): Task =>
  createMockTask({
    dueDate: '2023-12-31T23:59:59Z',
    isOverdue: true,
    isCompleted: false,
    ...overrides,
  });

export const createHighPriorityTask = (overrides: Partial<Task> = {}): Task =>
  createMockTask({
    priority: 'HIGH' as Priority,
    ...overrides,
  });

export const createTaskWithCategory = (
  categoryName: string,
  overrides: Partial<Task> = {}
): Task =>
  createMockTask({
    categoryId: 1,
    categoryName,
    categoryColor: '#3b82f6',
    ...overrides,
  });

export const createTaskWithTags = (tags: Tag[], overrides: Partial<Task> = {}): Task =>
  createMockTask({
    tags,
    ...overrides,
  });

export const createSubtask = (parentId: number, overrides: Partial<Task> = {}): Task =>
  createMockTask({
    parentTaskId: parentId,
    depth: 1,
    ...overrides,
  });

// TaskCreateRequest factory
export const createMockTaskRequest = (
  overrides: Partial<TaskCreateRequest> = {}
): TaskCreateRequest => ({
  description: 'New task',
  priority: 'MEDIUM' as Priority,
  dueDate: null,
  categoryId: null,
  tagIds: [],
  estimatedDurationMinutes: null,
  ...overrides,
});

// Category factory
export const createMockCategory = (overrides: Partial<Category> = {}): Category => {
  const id = overrides.id ?? categoryIdCounter++;
  return {
    id,
    name: 'Work',
    color: '#3b82f6',
    createdAt: '2024-01-01T10:00:00Z',
    updatedAt: '2024-01-01T10:00:00Z',
    ...overrides,
  };
};

// Create multiple categories
export const createMockCategories = (
  count: number,
  overrides: Partial<Category> = {}
): Category[] => {
  return Array.from({ length: count }, (_, i) =>
    createMockCategory({
      ...overrides,
      name: `${overrides.name || 'Category'} ${i + 1}`,
    })
  );
};

// Predefined categories
export const createWorkCategory = (): Category =>
  createMockCategory({ name: 'Work', color: '#3b82f6' });

export const createPersonalCategory = (): Category =>
  createMockCategory({ name: 'Personal', color: '#10b981' });

export const createUrgentCategory = (): Category =>
  createMockCategory({ name: 'Urgent', color: '#ef4444' });

// CategoryCreateRequest factory
export const createMockCategoryRequest = (
  overrides: Partial<CategoryCreateRequest> = {}
): CategoryCreateRequest => ({
  name: 'New Category',
  color: '#6366f1',
  ...overrides,
});

// Tag factory
export const createMockTag = (overrides: Partial<Tag> = {}): Tag => {
  const id = overrides.id ?? tagIdCounter++;
  return {
    id,
    name: 'important',
    color: '#f59e0b',
    createdAt: '2024-01-01T10:00:00Z',
    updatedAt: '2024-01-01T10:00:00Z',
    ...overrides,
  };
};

// Create multiple tags
export const createMockTags = (count: number, overrides: Partial<Tag> = {}): Tag[] => {
  return Array.from({ length: count }, (_, i) =>
    createMockTag({
      ...overrides,
      name: `${overrides.name || 'tag'}-${i + 1}`,
    })
  );
};

// Predefined tags
export const createImportantTag = (): Tag =>
  createMockTag({ name: 'important', color: '#ef4444' });

export const createUrgentTag = (): Tag => createMockTag({ name: 'urgent', color: '#f59e0b' });

export const createBugTag = (): Tag => createMockTag({ name: 'bug', color: '#dc2626' });

export const createFeatureTag = (): Tag =>
  createMockTag({ name: 'feature', color: '#3b82f6' });

// TagCreateRequest factory
export const createMockTagRequest = (
  overrides: Partial<TagCreateRequest> = {}
): TagCreateRequest => ({
  name: 'new-tag',
  color: '#8b5cf6',
  ...overrides,
});

// Collection factories for common test scenarios
export const createTaskList = () => ({
  tasks: createMockTasks(5),
  completedTasks: [createCompletedTask(), createCompletedTask()],
  overdueTasks: [createOverdueTask(), createOverdueTask()],
  highPriorityTasks: [createHighPriorityTask(), createHighPriorityTask()],
});

export const createCategoryList = () => ({
  categories: [createWorkCategory(), createPersonalCategory(), createUrgentCategory()],
});

export const createTagList = () => ({
  tags: [createImportantTag(), createUrgentTag(), createBugTag(), createFeatureTag()],
});
