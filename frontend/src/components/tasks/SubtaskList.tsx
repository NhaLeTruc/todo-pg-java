import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Check, Circle, Trash2 } from 'lucide-react';
import { toast } from 'react-hot-toast';

import { taskService } from '@/services/taskService';
import { Task } from '@/types/task';
import { cn } from '@/utils/cn';

interface SubtaskListProps {
  parentTaskId: number;
}

const priorityBadges = {
  LOW: 'badge-gray',
  MEDIUM: 'badge-primary',
  HIGH: 'badge-danger',
};

function SubtaskItem({ subtask, onToggle, onDelete }: {
  subtask: Task;
  onToggle: (id: number, isCompleted: boolean) => void;
  onDelete: (id: number) => void;
}) {
  return (
    <div
      className={cn(
        'flex items-start gap-2 rounded-lg border border-gray-200 bg-white p-3 transition-all hover:shadow-sm',
        subtask.isCompleted && 'bg-gray-50',
        subtask.isOverdue && !subtask.isCompleted && 'border-l-4 border-l-red-500'
      )}
      style={{ marginLeft: `${Math.min(subtask.depth, 5) * 12}px` }}
    >
      <button
        onClick={() => onToggle(subtask.id, !subtask.isCompleted)}
        className={cn(
          'mt-0.5 flex-shrink-0 rounded-full p-0.5 transition-colors hover:bg-gray-100',
          subtask.isCompleted && 'text-green-600'
        )}
        aria-label={subtask.isCompleted ? 'Mark incomplete' : 'Mark complete'}
      >
        {subtask.isCompleted ? <Check className="h-4 w-4" /> : <Circle className="h-4 w-4" />}
      </button>

      <div className="min-w-0 flex-1">
        <div className="flex items-start justify-between gap-2">
          <p
            className={cn(
              'text-sm font-medium text-gray-800',
              subtask.isCompleted && 'text-gray-500 line-through'
            )}
          >
            {subtask.description}
          </p>

          <div className="flex items-center gap-2">
            <span className={cn('badge text-xs', priorityBadges[subtask.priority])}>
              {subtask.priority}
            </span>
            <button
              onClick={() => onDelete(subtask.id)}
              className="rounded p-1 text-gray-400 hover:bg-red-50 hover:text-red-600"
              aria-label="Delete subtask"
            >
              <Trash2 className="h-3.5 w-3.5" />
            </button>
          </div>
        </div>

        {subtask.depth < 5 && (
          <span className="mt-1 inline-block text-xs text-gray-500">
            Depth: {subtask.depth}
          </span>
        )}
      </div>
    </div>
  );
}

export function SubtaskList({ parentTaskId }: SubtaskListProps) {
  const queryClient = useQueryClient();

  const { data: subtasks = [], isLoading, error } = useQuery({
    queryKey: ['subtasks', parentTaskId],
    queryFn: () => taskService.getSubtasks(parentTaskId),
    staleTime: 30000,
  });

  const toggleMutation = useMutation({
    mutationFn: ({ id, isCompleted }: { id: number; isCompleted: boolean }) =>
      taskService.toggleComplete(id, isCompleted),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['subtasks', parentTaskId] });
      queryClient.invalidateQueries({ queryKey: ['tasks'] });
      toast.success('Subtask updated');
    },
    onError: () => {
      toast.error('Failed to update subtask');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => taskService.deleteTask(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['subtasks', parentTaskId] });
      queryClient.invalidateQueries({ queryKey: ['tasks'] });
      toast.success('Subtask deleted');
    },
    onError: () => {
      toast.error('Failed to delete subtask');
    },
  });

  const handleToggle = (id: number, isCompleted: boolean) => {
    toggleMutation.mutate({ id, isCompleted });
  };

  const handleDelete = (id: number) => {
    if (window.confirm('Are you sure you want to delete this subtask?')) {
      deleteMutation.mutate(id);
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-4">
        <div className="h-6 w-6 animate-spin rounded-full border-2 border-blue-500 border-t-transparent"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="rounded-lg bg-red-50 p-3 text-sm text-red-600">
        Failed to load subtasks
      </div>
    );
  }

  if (subtasks.length === 0) {
    return (
      <div className="rounded-lg bg-gray-50 p-4 text-center text-sm text-gray-500">
        No subtasks yet
      </div>
    );
  }

  return (
    <div className="space-y-2">
      {subtasks.map((subtask) => (
        <SubtaskItem
          key={subtask.id}
          subtask={subtask}
          onToggle={handleToggle}
          onDelete={handleDelete}
        />
      ))}
    </div>
  );
}
