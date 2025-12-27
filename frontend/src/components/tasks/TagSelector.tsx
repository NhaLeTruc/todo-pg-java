import React, { useEffect, useState } from 'react';

import { tagService } from '../../services/tagService';
import { Tag } from '../../types/tag';

interface TagSelectorProps {
  value: number[];
  onChange: (tagIds: number[]) => void;
}

export const TagSelector: React.FC<TagSelectorProps> = ({ value, onChange }) => {
  const [tags, setTags] = useState<Tag[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    loadTags();
  }, []);

  const loadTags = async () => {
    try {
      const data = await tagService.getAll();
      setTags(data);
    } catch (error) {
      console.error('Failed to load tags:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleToggleTag = (tagId: number) => {
    if (value.includes(tagId)) {
      onChange(value.filter((id) => id !== tagId));
    } else {
      onChange([...value, tagId]);
    }
  };

  return (
    <div className="form-group">
      <label>Tags</label>
      <div className="flex flex-wrap gap-2">
        {tags.map((tag) => (
          <button
            key={tag.id}
            type="button"
            onClick={() => handleToggleTag(tag.id)}
            disabled={isLoading}
            className={`btn-sm ${
              value.includes(tag.id) ? 'btn-primary' : 'btn-secondary'
            }`}
            style={
              tag.color && value.includes(tag.id)
                ? { backgroundColor: tag.color, borderColor: tag.color }
                : undefined
            }
          >
            {tag.name}
          </button>
        ))}
      </div>
    </div>
  );
};
