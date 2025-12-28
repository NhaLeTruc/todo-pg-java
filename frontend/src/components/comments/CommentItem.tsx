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
    <div className="rounded-lg border border-gray-200 p-4 transition-colors hover:bg-gray-50">
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0 flex-1">
          <div className="mb-2 flex items-center gap-2">
            <span className="text-sm font-medium text-gray-900">{comment.authorEmail}</span>
            <span className="text-xs text-gray-500">
              {format(new Date(comment.createdAt), 'MMM d, yyyy h:mm a')}
            </span>
            {comment.isEdited && <span className="text-xs italic text-gray-400">(edited)</span>}
          </div>
          <p className="whitespace-pre-wrap break-words text-sm text-gray-700">{comment.content}</p>
        </div>

        {isAuthor && (
          <div className="flex flex-shrink-0 gap-1">
            <button
              onClick={() => onEdit(comment)}
              className="rounded p-1.5 text-gray-400 transition-colors hover:bg-blue-50 hover:text-blue-600"
              aria-label="Edit comment"
            >
              <Edit2 className="h-4 w-4" />
            </button>
            <button
              onClick={() => onDelete(comment.id)}
              className="rounded p-1.5 text-gray-400 transition-colors hover:bg-red-50 hover:text-red-600"
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
