import React, { useEffect, useState } from 'react';

import { Send, X } from 'lucide-react';

import { Comment } from '@/types/comment';

interface CommentFormProps {
  onSubmit: (content: string) => Promise<void>;
  onCancel?: () => void;
  initialValue?: string;
  editingComment?: Comment | null;
  isLoading?: boolean;
  placeholder?: string;
}

const MIN_CONTENT_LENGTH = 1;
const MAX_CONTENT_LENGTH = 5000;

export const CommentForm: React.FC<CommentFormProps> = ({
  onSubmit,
  onCancel,
  initialValue = '',
  editingComment,
  isLoading = false,
  placeholder = 'Add a comment...',
}) => {
  const [content, setContent] = useState(initialValue);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (editingComment) {
      setContent(editingComment.content);
    } else if (initialValue) {
      setContent(initialValue);
    }
  }, [editingComment, initialValue]);

  const validateContent = (value: string): string | null => {
    const trimmed = value.trim();

    if (trimmed.length === 0) {
      return 'Comment cannot be empty';
    }

    if (trimmed.length < MIN_CONTENT_LENGTH) {
      return `Comment must be at least ${MIN_CONTENT_LENGTH} character`;
    }

    if (value.length > MAX_CONTENT_LENGTH) {
      return `Comment cannot exceed ${MAX_CONTENT_LENGTH} characters`;
    }

    return null;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    const validationError = validateContent(content);
    if (validationError) {
      setError(validationError);
      return;
    }

    try {
      await onSubmit(content.trim());
      setContent('');
      setError(null);
    } catch (err) {
      setError('Failed to submit comment. Please try again.');
      console.error('Failed to submit comment:', err);
    }
  };

  const handleCancel = () => {
    setContent('');
    setError(null);
    if (onCancel) {
      onCancel();
    }
  };

  const isValid = !error && content.trim().length > 0;
  const isEditing = !!editingComment;

  return (
    <form onSubmit={handleSubmit} className="space-y-2">
      <div>
        <textarea
          value={content}
          onChange={(e) => {
            setContent(e.target.value);
            setError(null);
          }}
          placeholder={placeholder}
          rows={3}
          maxLength={MAX_CONTENT_LENGTH}
          disabled={isLoading}
          className={`w-full rounded-lg border ${
            error
              ? 'border-red-500 focus:border-red-500 focus:ring-red-200'
              : 'border-gray-300 focus:border-blue-500 focus:ring-blue-200'
          } resize-none px-3 py-2 text-sm transition-colors focus:outline-none focus:ring-2`}
        />
        <div className="mt-1 flex items-center justify-between">
          <span className="text-xs text-gray-500">
            {content.length}/{MAX_CONTENT_LENGTH}
          </span>
          {error && <span className="text-xs text-red-600">{error}</span>}
        </div>
      </div>

      <div className="flex justify-end gap-2">
        {(isEditing || onCancel) && (
          <button
            type="button"
            onClick={handleCancel}
            disabled={isLoading}
            className="rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 transition-colors hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
          >
            <X className="mr-1 inline h-4 w-4" />
            Cancel
          </button>
        )}
        <button
          type="submit"
          disabled={isLoading || !isValid}
          className="flex items-center gap-2 rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50"
        >
          <Send className="h-4 w-4" />
          {isEditing ? 'Update' : 'Post'} Comment
        </button>
      </div>
    </form>
  );
};
