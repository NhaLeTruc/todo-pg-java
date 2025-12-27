import { Task } from '@/types/task';
import { cn } from '@/utils/cn';

interface SubtaskProgressBarProps {
  task: Task;
  className?: string;
}

export function SubtaskProgressBar({ task, className }: SubtaskProgressBarProps) {
  const progress = task.subtaskProgress || 0;

  if (progress === 0 && task.subtaskProgress === 0) {
    return null;
  }

  return (
    <div className={cn('space-y-1', className)}>
      <div className="flex items-center justify-between text-xs text-gray-600">
        <span>Subtask Progress</span>
        <span className="font-medium">{progress}%</span>
      </div>
      <div className="h-2 w-full overflow-hidden rounded-full bg-gray-200">
        <div
          className={cn(
            'h-full transition-all duration-300',
            progress === 100 ? 'bg-green-500' : 'bg-blue-500'
          )}
          style={{ width: `${progress}%` }}
        />
      </div>
    </div>
  );
}
