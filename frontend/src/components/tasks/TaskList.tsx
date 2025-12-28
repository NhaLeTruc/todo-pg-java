import { Task } from '@/types/task';

import { TaskItem } from './TaskItem';

interface TaskListProps {
  tasks: Task[];
  onToggleComplete: (id: number, isCompleted: boolean) => void;
  onDelete: (id: number) => void;
  onEdit: (id: number) => void;
  onViewDetails?: (task: Task) => void;
  isLoading?: boolean;
  selectedTaskIds?: number[];
  onSelectionChange?: (id: number, selected: boolean) => void;
  onSelectAll?: (selected: boolean) => void;
}

export function TaskList({
  tasks,
  onToggleComplete,
  onDelete,
  onEdit,
  onViewDetails,
  isLoading = false,
  selectedTaskIds = [],
  onSelectionChange,
  onSelectAll,
}: TaskListProps) {
  if (isLoading) {
    return (
      <div className="space-y-2">
        {[...Array(3)].map((_, i) => (
          <div key={i} className="card animate-pulse">
            <div className="h-6 w-3/4 rounded bg-gray-200"></div>
          </div>
        ))}
      </div>
    );
  }

  if (tasks.length === 0) {
    return (
      <div className="card py-12 text-center">
        <div className="text-gray-400">
          <svg
            className="mx-auto h-12 w-12"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            aria-hidden="true"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
            />
          </svg>
          <h3 className="mt-2 text-sm font-medium text-gray-900">No tasks</h3>
          <p className="mt-1 text-sm text-gray-500">Get started by creating a new task.</p>
        </div>
      </div>
    );
  }

  const allSelected = tasks.length > 0 && selectedTaskIds.length === tasks.length;
  const someSelected = selectedTaskIds.length > 0 && selectedTaskIds.length < tasks.length;

  const handleSelectAllChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (onSelectAll) {
      onSelectAll(e.target.checked);
    }
  };

  return (
    <div className="space-y-2">
      {onSelectAll && tasks.length > 0 && (
        <div className="card flex items-center gap-3 bg-gray-50">
          <input
            type="checkbox"
            checked={allSelected}
            ref={(input) => {
              if (input) {
                input.indeterminate = someSelected;
              }
            }}
            onChange={handleSelectAllChange}
            className="h-4 w-4 rounded border-gray-300 text-indigo-600 focus:ring-indigo-500"
            aria-label="Select all tasks"
          />
          <span className="text-sm font-medium text-gray-700">
            {allSelected
              ? `All ${tasks.length} tasks selected`
              : someSelected
                ? `${selectedTaskIds.length} of ${tasks.length} tasks selected`
                : 'Select all tasks'}
          </span>
        </div>
      )}

      {tasks.map((task) => (
        <TaskItem
          key={task.id}
          task={task}
          onToggleComplete={onToggleComplete}
          onDelete={onDelete}
          onEdit={onEdit}
          onViewDetails={onViewDetails}
          isSelected={selectedTaskIds.includes(task.id)}
          onSelectionChange={onSelectionChange}
        />
      ))}
    </div>
  );
}
