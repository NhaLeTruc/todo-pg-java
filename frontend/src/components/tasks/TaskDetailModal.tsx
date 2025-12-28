import React, { useEffect, useState } from 'react';

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { format } from 'date-fns';
import { MessageSquare, X, ListTree, Clock, PlusCircle, Share2 } from 'lucide-react';
import { toast } from 'react-hot-toast';

import { CommentForm } from '@/components/comments/CommentForm';
import { CommentList } from '@/components/comments/CommentList';
import { commentService } from '@/services/commentService';
import { Comment } from '@/types/comment';
import { Task } from '@/types/task';

import { AddSubtaskButton } from './AddSubtaskButton';
import ManualTimeLogDialog from './ManualTimeLogDialog';
import { ShareTaskDialog } from './ShareTaskDialog';
import { SubtaskList } from './SubtaskList';
import { SubtaskProgressBar } from './SubtaskProgressBar';
import TimeTracker from './TimeTracker';

interface TaskDetailModalProps {
  task: Task;
  isOpen: boolean;
  onClose: () => void;
  currentUserId: number;
}

export const TaskDetailModal: React.FC<TaskDetailModalProps> = ({
  task,
  isOpen,
  onClose,
  currentUserId,
}) => {
  const queryClient = useQueryClient();
  const [editingComment, setEditingComment] = useState<Comment | null>(null);
  const [showManualTimeLog, setShowManualTimeLog] = useState(false);
  const [showShareDialog, setShowShareDialog] = useState(false);

  const { data: comments = [], isLoading: isLoadingComments } = useQuery({
    queryKey: ['comments', task.id],
    queryFn: () => commentService.getTaskComments(task.id),
    enabled: isOpen,
  });

  const createCommentMutation = useMutation({
    mutationFn: (content: string) => commentService.createComment(task.id, { content }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['comments', task.id] });
      toast.success('Comment added!');
    },
    onError: (error: Error) => {
      toast.error(`Failed to add comment: ${error.message}`);
    },
  });

  const updateCommentMutation = useMutation({
    mutationFn: ({ id, content }: { id: number; content: string }) =>
      commentService.updateComment(id, { content }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['comments', task.id] });
      setEditingComment(null);
      toast.success('Comment updated!');
    },
    onError: (error: Error) => {
      toast.error(`Failed to update comment: ${error.message}`);
    },
  });

  const deleteCommentMutation = useMutation({
    mutationFn: (commentId: number) => commentService.deleteComment(commentId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['comments', task.id] });
      toast.success('Comment deleted!');
    },
    onError: (error: Error) => {
      toast.error(`Failed to delete comment: ${error.message}`);
    },
  });

  const handleCreateComment = async (content: string) => {
    await createCommentMutation.mutateAsync(content);
  };

  const handleUpdateComment = async (content: string) => {
    if (editingComment) {
      await updateCommentMutation.mutateAsync({ id: editingComment.id, content });
    }
  };

  const handleDeleteComment = (commentId: number) => {
    if (window.confirm('Are you sure you want to delete this comment?')) {
      deleteCommentMutation.mutate(commentId);
    }
  };

  const handleEditComment = (comment: Comment) => {
    setEditingComment(comment);
  };

  const handleCancelEdit = () => {
    setEditingComment(null);
  };

  useEffect(() => {
    if (!isOpen) {
      setEditingComment(null);
    }
  }, [isOpen]);

  if (!isOpen) {
    return null;
  }

  const priorityColors = {
    LOW: 'text-gray-600 bg-gray-100',
    MEDIUM: 'text-blue-600 bg-blue-100',
    HIGH: 'text-red-600 bg-red-100',
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      <div className="flex min-h-screen items-center justify-center p-4">
        <div
          className="fixed inset-0 bg-black bg-opacity-25 transition-opacity"
          onClick={onClose}
        />

        <div className="relative w-full max-w-2xl rounded-lg bg-white shadow-xl">
          {/* Header */}
          <div className="flex items-center justify-between border-b border-gray-200 p-6">
            <div className="min-w-0 flex-1">
              <h2 className="truncate text-xl font-semibold text-gray-900">{task.description}</h2>
              <div className="mt-2 flex flex-wrap items-center gap-2">
                <span
                  className={`rounded px-2 py-1 text-xs font-medium ${priorityColors[task.priority]}`}
                >
                  {task.priority}
                </span>
                {task.dueDate && (
                  <span className="text-xs text-gray-500">
                    Due: {format(new Date(task.dueDate), 'MMM d, yyyy')}
                  </span>
                )}
                {task.categoryName && (
                  <span
                    className="rounded bg-purple-100 px-2 py-1 text-xs font-medium text-purple-700"
                    style={
                      task.categoryColor
                        ? { backgroundColor: task.categoryColor, borderColor: task.categoryColor }
                        : undefined
                    }
                  >
                    {task.categoryName}
                  </span>
                )}
                {task.tags &&
                  task.tags.map((tag) => (
                    <span
                      key={tag.id}
                      className="rounded bg-green-100 px-2 py-1 text-xs font-medium text-green-700"
                      style={
                        tag.color
                          ? { backgroundColor: tag.color, borderColor: tag.color }
                          : undefined
                      }
                    >
                      {tag.name}
                    </span>
                  ))}
              </div>
            </div>
            <div className="ml-4 flex flex-shrink-0 gap-2">
              <button
                onClick={() => setShowShareDialog(true)}
                className="p-1 text-gray-400 transition-colors hover:text-blue-500"
                title="Share task"
              >
                <Share2 className="h-5 w-5" />
              </button>
              <button
                onClick={onClose}
                className="p-1 text-gray-400 transition-colors hover:text-gray-500"
              >
                <X className="h-6 w-6" />
              </button>
            </div>
          </div>

          {/* Time Tracking Section */}
          <div className="border-b border-gray-200 p-6">
            <div className="mb-4">
              <div className="mb-3 flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <Clock className="h-5 w-5 text-gray-500" />
                  <h3 className="text-lg font-medium text-gray-900">Time Tracking</h3>
                </div>
                <button
                  onClick={() => setShowManualTimeLog(true)}
                  className="flex items-center gap-2 text-sm text-indigo-600 hover:text-indigo-700"
                >
                  <PlusCircle className="h-4 w-4" />
                  Log Time
                </button>
              </div>

              <TimeTracker
                taskId={task.id}
                onTimerStarted={() => queryClient.invalidateQueries({ queryKey: ['tasks'] })}
                onTimerStopped={() => queryClient.invalidateQueries({ queryKey: ['tasks'] })}
              />
            </div>
          </div>

          {/* Subtasks Section */}
          <div className="border-b border-gray-200 p-6">
            <div className="mb-4">
              <div className="mb-3 flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <ListTree className="h-5 w-5 text-gray-500" />
                  <h3 className="text-lg font-medium text-gray-900">Subtasks</h3>
                </div>
              </div>

              <SubtaskProgressBar task={task} className="mb-4" />

              <SubtaskList parentTaskId={task.id} />

              <div className="mt-4">
                <AddSubtaskButton parentTask={task} />
              </div>
            </div>
          </div>

          {/* Comments Section */}
          <div className="p-6">
            <div className="mb-4 flex items-center gap-2">
              <MessageSquare className="h-5 w-5 text-gray-500" />
              <h3 className="text-lg font-medium text-gray-900">Comments ({comments.length})</h3>
            </div>

            {isLoadingComments ? (
              <div className="py-8 text-center text-gray-500">Loading comments...</div>
            ) : (
              <div className="space-y-6">
                <CommentList
                  comments={comments}
                  currentUserId={currentUserId}
                  onEdit={handleEditComment}
                  onDelete={handleDeleteComment}
                />

                <div className="border-t border-gray-200 pt-4">
                  {editingComment ? (
                    <>
                      <h4 className="mb-2 text-sm font-medium text-gray-700">Edit Comment</h4>
                      <CommentForm
                        onSubmit={handleUpdateComment}
                        onCancel={handleCancelEdit}
                        editingComment={editingComment}
                        isLoading={updateCommentMutation.isPending}
                      />
                    </>
                  ) : (
                    <CommentForm
                      onSubmit={handleCreateComment}
                      isLoading={createCommentMutation.isPending}
                    />
                  )}
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Manual Time Log Dialog */}
      <ManualTimeLogDialog
        taskId={task.id}
        isOpen={showManualTimeLog}
        onClose={() => setShowManualTimeLog(false)}
        onTimeLogged={() => {
          queryClient.invalidateQueries({ queryKey: ['tasks'] });
          toast.success('Time logged successfully!');
        }}
      />

      {/* Share Task Dialog */}
      {showShareDialog && (
        <ShareTaskDialog
          taskId={task.id}
          onClose={() => setShowShareDialog(false)}
          onShare={async (email, permission) => {
            // TODO: Implement share API call when backend endpoint is ready
            toast.success(`Task shared with ${email} (${permission} permission)`);
          }}
        />
      )}
    </div>
  );
};
