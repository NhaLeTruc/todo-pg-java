import { Task, TaskCreateRequest, TaskUpdateRequest } from '@/types/task';

import api, { PaginatedResponse } from './api';

const USER_ID_HEADER = 'X-User-Id';
const DEFAULT_USER_ID = '1';

export interface GetTasksParams {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
  search?: string;
  completed?: boolean;
}

export const taskService = {
  async getTasks(params: GetTasksParams = {}): Promise<PaginatedResponse<Task>> {
    const {
      page = 0,
      size = 20,
      sortBy = 'createdAt',
      sortDirection = 'desc',
      search,
      completed,
    } = params;

    const response = await api.get<PaginatedResponse<Task>>('/tasks', {
      params: {
        page,
        size,
        sortBy,
        sortDirection,
        search,
        completed,
      },
      headers: {
        [USER_ID_HEADER]: DEFAULT_USER_ID,
      },
    });

    return response.data;
  },

  async getTaskById(id: number): Promise<Task> {
    const response = await api.get<Task>(`/tasks/${id}`, {
      headers: {
        [USER_ID_HEADER]: DEFAULT_USER_ID,
      },
    });
    return response.data;
  },

  async createTask(data: TaskCreateRequest): Promise<Task> {
    const response = await api.post<Task>('/tasks', data, {
      headers: {
        [USER_ID_HEADER]: DEFAULT_USER_ID,
      },
    });
    return response.data;
  },

  async updateTask(id: number, data: TaskUpdateRequest): Promise<Task> {
    const response = await api.put<Task>(`/tasks/${id}`, data, {
      headers: {
        [USER_ID_HEADER]: DEFAULT_USER_ID,
      },
    });
    return response.data;
  },

  async deleteTask(id: number): Promise<void> {
    await api.delete(`/tasks/${id}`, {
      headers: {
        [USER_ID_HEADER]: DEFAULT_USER_ID,
      },
    });
  },

  async toggleComplete(id: number, isCompleted: boolean): Promise<Task> {
    const endpoint = isCompleted ? `/tasks/${id}/complete` : `/tasks/${id}/uncomplete`;
    const response = await api.patch<Task>(endpoint, null, {
      headers: {
        [USER_ID_HEADER]: DEFAULT_USER_ID,
      },
    });
    return response.data;
  },

  async getTaskCount(completed?: boolean): Promise<number> {
    const response = await api.get<number>('/tasks/count', {
      params: { completed },
      headers: {
        [USER_ID_HEADER]: DEFAULT_USER_ID,
      },
    });
    return response.data;
  },
};
