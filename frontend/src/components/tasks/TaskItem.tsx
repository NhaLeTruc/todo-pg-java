import { format } from 'date-fns';
import { Check, Circle, Pencil, Trash2 } from 'lucide-react';

import { Task } from '@/types/task';
import { cn } from '@/utils/cn';

interface TaskItemProps {
  task: Task;
  onToggleComplete: (id: number, isCompleted: boolean) => void;
  onDelete: (id: number) => void;
  onEdit: (id: number) => void;
}

const priorityBadges = {
  LOW: 'badge-gray',
  MEDIUM: 'badge-primary',
  HIGH: 'badge-danger',
};

export function TaskItem({ task, onToggleComplete, onDelete, onEdit }: TaskItemProps) {
  const handleToggle = () => {
    onToggleComplete(task.id, !task.isCompleted);
  };

  return (
    <div
      className={cn(
        'card group flex items-start gap-3 transition-all hover:shadow-md',
        task.isCompleted && 'bg-gray-50',
        task.isOverdue && !task.isCompleted && 'border-l-4 border-red-500'
      )}
    >
      <button
        onClick={handleToggle}
        className={cn(
          'mt-0.5 flex-shrink-0 rounded-full p-1 transition-colors hover:bg-gray-100',
          task.isCompleted && 'text-green-600'
        )}
        aria-label={task.isCompleted ? 'Mark incomplete' : 'Mark complete'}
      >
        {task.isCompleted ? <Check className="h-5 w-5" /> : <Circle className="h-5 w-5" />}
      </button>

      <div className="min-w-0 flex-1">
        <div className="flex items-start justify-between gap-2">
          <p
            className={cn(
              'text-sm font-medium text-gray-900',
              task.isCompleted && 'text-gray-500 line-through'
            )}
          >
            {task.description}
          </p>

          <div className="flex flex-shrink-0 gap-1 opacity-0 transition-opacity group-hover:opacity-100">
            <button
              onClick={() => onEdit(task.id)}
              className="rounded p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600"
              aria-label="Edit task"
            >
              <Pencil className="h-4 w-4" />
            </button>
            <button
              onClick={() => onDelete(task.id)}
              className="rounded p-1 text-gray-400 hover:bg-red-50 hover:text-red-600"
              aria-label="Delete task"
            >
              <Trash2 className="h-4 w-4" />
            </button>
          </div>
        </div>

        <div className="mt-2 flex flex-wrap items-center gap-2 text-xs text-gray-500">
          <span className={priorityBadges[task.priority]}>{task.priority}</span>

          {task.dueDate && (
            <span className={cn(task.isOverdue && !task.isCompleted && 'font-medium text-red-600')}>
              Due: {format(new Date(task.dueDate), 'MMM d, yyyy')}
            </span>
          )}

          {task.categoryName && (
            <span
              className="badge-primary"
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
                className="badge-secondary"
                style={
                  tag.color ? { backgroundColor: tag.color, borderColor: tag.color } : undefined
                }
              >
                {tag.name}
              </span>
            ))}

          <span className="text-gray-400">
            Created {format(new Date(task.createdAt), 'MMM d, yyyy')}
          </span>
        </div>
      </div>
    </div>
  );
}
