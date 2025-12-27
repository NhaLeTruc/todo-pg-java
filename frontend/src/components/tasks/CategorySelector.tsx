import React, { useEffect, useState } from 'react';

import { categoryService } from '../../services/categoryService';
import { Category } from '../../types/category';

interface CategorySelectorProps {
  value: number | null;
  onChange: (categoryId: number | null) => void;
}

export const CategorySelector: React.FC<CategorySelectorProps> = ({ value, onChange }) => {
  const [categories, setCategories] = useState<Category[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    loadCategories();
  }, []);

  const loadCategories = async () => {
    try {
      const data = await categoryService.getAll();
      setCategories(data);
    } catch (error) {
      console.error('Failed to load categories:', error);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="form-group">
      <label htmlFor="category">Category</label>
      <select
        id="category"
        value={value || ''}
        onChange={(e) => onChange(e.target.value ? Number(e.target.value) : null)}
        disabled={isLoading}
        className="input"
      >
        <option value="">No Category</option>
        {categories.map((category) => (
          <option key={category.id} value={category.id}>
            {category.name}
          </option>
        ))}
      </select>
    </div>
  );
};
