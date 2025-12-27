import { Tag } from './tag';

export type Priority = 'LOW' | 'MEDIUM' | 'HIGH';

export interface Task {
  id: number;
  description: string;
  isCompleted: boolean;
  priority: Priority;
  dueDate: string | null;
  completedAt: string | null;
  position: number;
  categoryId: number | null;
  categoryName: string | null;
  categoryColor: string | null;
  tags: Tag[];
  estimatedDurationMinutes: number | null;
  actualDurationMinutes: number | null;
  isOverdue: boolean;
  parentTaskId: number | null;
  depth: number;
  createdAt: string;
  updatedAt: string;
}

export interface TaskCreateRequest {
  description: string;
  priority?: Priority;
  dueDate?: string | null;
  categoryId?: number | null;
  tagIds?: number[];
  estimatedDurationMinutes?: number | null;
}

export interface TaskUpdateRequest {
  description?: string;
  priority?: Priority;
  dueDate?: string | null;
  categoryId?: number | null;
  tagIds?: number[];
  estimatedDurationMinutes?: number | null;
}

export interface TasksResponse {
  content: Task[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}
