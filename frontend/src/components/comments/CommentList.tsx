import React from 'react';

import { Comment } from '@/types/comment';

import { CommentItem } from './CommentItem';

interface CommentListProps {
  comments: Comment[];
  currentUserId: number;
  onEdit: (comment: Comment) => void;
  onDelete: (commentId: number) => void;
}

export const CommentList: React.FC<CommentListProps> = ({
  comments,
  currentUserId,
  onEdit,
  onDelete,
}) => {
  if (comments.length === 0) {
    return (
      <div className="py-8 text-center text-gray-500">
        <p>No comments yet. Be the first to comment!</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {comments.map((comment) => (
        <CommentItem
          key={comment.id}
          comment={comment}
          currentUserId={currentUserId}
          onEdit={onEdit}
          onDelete={onDelete}
        />
      ))}
    </div>
  );
};
