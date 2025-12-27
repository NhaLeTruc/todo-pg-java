import { Comment, CommentCreateRequest, CommentUpdateRequest } from '../types/comment';

import api from './api';

const USER_ID_HEADER = 'X-User-Id';
const DEFAULT_USER_ID = '1';

export const commentService = {
  async getTaskComments(taskId: number): Promise<Comment[]> {
    const response = await api.get<Comment[]>(`/tasks/${taskId}/comments`, {
      headers: {
        [USER_ID_HEADER]: DEFAULT_USER_ID,
      },
    });
    return response.data;
  },

  async createComment(taskId: number, data: CommentCreateRequest): Promise<Comment> {
    const response = await api.post<Comment>(`/tasks/${taskId}/comments`, data, {
      headers: {
        [USER_ID_HEADER]: DEFAULT_USER_ID,
      },
    });
    return response.data;
  },

  async updateComment(commentId: number, data: CommentUpdateRequest): Promise<Comment> {
    const response = await api.put<Comment>(`/comments/${commentId}`, data, {
      headers: {
        [USER_ID_HEADER]: DEFAULT_USER_ID,
      },
    });
    return response.data;
  },

  async deleteComment(commentId: number): Promise<void> {
    await api.delete(`/comments/${commentId}`, {
      headers: {
        [USER_ID_HEADER]: DEFAULT_USER_ID,
      },
    });
  },
};
