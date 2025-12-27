import React from 'react';

import { format } from 'date-fns';
import { Edit2, Trash2 } from 'lucide-react';

import { Comment } from '@/types/comment';

interface CommentItemProps {
  comment: Comment;
  currentUserId: number;
  onEdit: (comment: Comment) => void;
  onDelete: (commentId: number) => void;
}

export const CommentItem: React.FC<CommentItemProps> = ({
  comment,
  currentUserId,
  onEdit,
  onDelete,
}) => {
  const isAuthor = comment.authorId === currentUserId;

  return (
    <div className="border border-gray-200 rounded-lg p-4 hover:bg-gray-50 transition-colors">
      <div className="flex items-start justify-between gap-3">
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 mb-2">
            <span className="text-sm font-medium text-gray-900">{comment.authorEmail}</span>
            <span className="text-xs text-gray-500">
              {format(new Date(comment.createdAt), 'MMM d, yyyy h:mm a')}
            </span>
            {comment.isEdited && (
              <span className="text-xs text-gray-400 italic">(edited)</span>
            )}
          </div>
          <p className="text-sm text-gray-700 whitespace-pre-wrap break-words">{comment.content}</p>
        </div>

        {isAuthor && (
          <div className="flex gap-1 flex-shrink-0">
            <button
              onClick={() => onEdit(comment)}
              className="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded transition-colors"
              aria-label="Edit comment"
            >
              <Edit2 className="h-4 w-4" />
            </button>
            <button
              onClick={() => onDelete(comment.id)}
              className="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded transition-colors"
              aria-label="Delete comment"
            >
              <Trash2 className="h-4 w-4" />
            </button>
          </div>
        )}
      </div>
    </div>
  );
};
