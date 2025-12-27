import { Tag, TagCreateRequest, TagUpdateRequest } from '../types/tag';

import api from './api';

export const tagService = {
  async getAll(): Promise<Tag[]> {
    const response = await api.get<Tag[]>('/tags');
    return response.data;
  },

  async getById(id: number): Promise<Tag> {
    const response = await api.get<Tag>(`/tags/${id}`);
    return response.data;
  },

  async create(data: TagCreateRequest): Promise<Tag> {
    const response = await api.post<Tag>('/tags', data);
    return response.data;
  },

  async update(id: number, data: TagUpdateRequest): Promise<Tag> {
    const response = await api.put<Tag>(`/tags/${id}`, data);
    return response.data;
  },

  async delete(id: number): Promise<void> {
    await api.delete(`/tags/${id}`);
  },
};
