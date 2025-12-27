export interface Tag {
  id: number;
  name: string;
  color?: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface TagCreateRequest {
  name: string;
  color?: string;
}

export interface TagUpdateRequest {
  name: string;
  color?: string;
}
