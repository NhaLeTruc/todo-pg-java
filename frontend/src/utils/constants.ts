// Handle import.meta.env for both Vite and Jest environments
// In production: uses import.meta.env from Vite
// In tests: uses defaults (import.meta not available in Jest)
let API_BASE_URL_VALUE = 'http://localhost:8080/api/v1';
let WS_URL_VALUE = 'http://localhost:8080/ws';

// Use try-catch to safely access import.meta which doesn't exist in Jest
try {
  // Use eval to prevent Jest from parsing import.meta at parse time
  const meta = eval('import.meta');
  if (meta && meta.env) {
    API_BASE_URL_VALUE = meta.env.VITE_API_BASE_URL || API_BASE_URL_VALUE;
    WS_URL_VALUE = meta.env.VITE_WS_URL || WS_URL_VALUE;
  }
} catch {
  // In Jest environment, this will fail but we already have defaults
}

export const API_BASE_URL = API_BASE_URL_VALUE;
export const WS_URL = WS_URL_VALUE;

export const API_ENDPOINTS = {
  AUTH: {
    REGISTER: '/auth/register',
    LOGIN: '/auth/login',
    LOGOUT: '/auth/logout',
    ME: '/auth/me',
  },
  TASKS: {
    BASE: '/tasks',
    BY_ID: (id: number) => `/tasks/${id}`,
    SUBTASKS: (id: number) => `/tasks/${id}/subtasks`,
    COMPLETE: (id: number) => `/tasks/${id}/complete`,
    UNCOMPLETE: (id: number) => `/tasks/${id}/uncomplete`,
    SHARE: (id: number) => `/tasks/${id}/share`,
    SHARED_WITH_ME: '/tasks/shared-with-me',
    BATCH: '/tasks/batch',
  },
  CATEGORIES: {
    BASE: '/categories',
    BY_ID: (id: number) => `/categories/${id}`,
  },
  TAGS: {
    BASE: '/tags',
    BY_ID: (id: number) => `/tags/${id}`,
  },
  COMMENTS: {
    BY_TASK: (taskId: number) => `/tasks/${taskId}/comments`,
    BY_ID: (id: number) => `/comments/${id}`,
  },
  TIME_ENTRIES: {
    START: (taskId: number) => `/tasks/${taskId}/time-entries/start`,
    STOP: (id: number) => `/time-entries/${id}/stop`,
    MANUAL: (taskId: number) => `/tasks/${taskId}/time-entries`,
    REPORT: '/time-entries/report',
  },
  FILE_ATTACHMENTS: {
    UPLOAD: (taskId: number) => `/tasks/${taskId}/attachments`,
    DOWNLOAD: (id: number) => `/attachments/${id}/download`,
    DELETE: (id: number) => `/attachments/${id}`,
  },
  NOTIFICATIONS: {
    BASE: '/notifications',
    READ: (id: number) => `/notifications/${id}/read`,
    PREFERENCES: '/notification-preferences',
    PREFERENCE: (type: string) => `/notification-preferences/${type}`,
  },
} as const;

export const QUERY_KEYS = {
  TASKS: 'tasks',
  TASK: 'task',
  SUBTASKS: 'subtasks',
  SHARED_TASKS: 'shared-tasks',
  CATEGORIES: 'categories',
  TAGS: 'tags',
  COMMENTS: 'comments',
  TIME_ENTRIES: 'time-entries',
  TIME_REPORT: 'time-report',
  FILE_ATTACHMENTS: 'file-attachments',
  NOTIFICATIONS: 'notifications',
  NOTIFICATION_PREFERENCES: 'notification-preferences',
  USER: 'user',
} as const;

export const PRIORITY_LEVELS = {
  LOW: 'LOW',
  MEDIUM: 'MEDIUM',
  HIGH: 'HIGH',
} as const;

export const FREQUENCY_TYPES = {
  DAILY: 'DAILY',
  WEEKLY: 'WEEKLY',
  MONTHLY: 'MONTHLY',
  YEARLY: 'YEARLY',
} as const;

export const PERMISSION_LEVELS = {
  VIEW: 'VIEW',
  EDIT: 'EDIT',
} as const;

export const ENTRY_TYPES = {
  MANUAL: 'MANUAL',
  TIMER: 'TIMER',
} as const;

export const VIRUS_SCAN_STATUS = {
  PENDING: 'PENDING',
  CLEAN: 'CLEAN',
  INFECTED: 'INFECTED',
  FAILED: 'FAILED',
} as const;

export const NOTIFICATION_TYPES = {
  TASK_DUE_SOON: 'TASK_DUE_SOON',
  TASK_OVERDUE: 'TASK_OVERDUE',
  TASK_SHARED: 'TASK_SHARED',
  COMMENT_MENTION: 'COMMENT_MENTION',
  COMMENT_ADDED: 'COMMENT_ADDED',
  TASK_ASSIGNED: 'TASK_ASSIGNED',
  TASK_COMPLETED: 'TASK_COMPLETED',
  RECURRENCE_CREATED: 'RECURRENCE_CREATED',
} as const;

export const PAGINATION = {
  DEFAULT_PAGE: 0,
  DEFAULT_PAGE_SIZE: 20,
  PAGE_SIZE_OPTIONS: [10, 20, 50, 100],
} as const;

export const FILE_UPLOAD = {
  MAX_FILE_SIZE_MB: 25,
  MAX_FILE_SIZE_BYTES: 25 * 1024 * 1024,
  ALLOWED_TYPES: [
    'application/pdf',
    'image/jpeg',
    'image/jpg',
    'image/png',
    'image/gif',
    'application/msword',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'application/vnd.ms-excel',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'text/plain',
  ],
} as const;

export const DEBOUNCE_DELAY = 300;

export const TOAST_DURATION = 3000;

export const MAX_SUBTASK_DEPTH = 5;
