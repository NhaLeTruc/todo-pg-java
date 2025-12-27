export interface Category {
  id: number;
  name: string;
  color?: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CategoryCreateRequest {
  name: string;
  color?: string;
}

export interface CategoryUpdateRequest {
  name: string;
  color?: string;
}
