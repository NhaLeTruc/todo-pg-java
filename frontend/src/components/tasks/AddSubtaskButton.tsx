import { useState } from 'react';

import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus, X } from 'lucide-react';
import { toast } from 'react-hot-toast';

import { taskService } from '@/services/taskService';
import { Task, TaskCreateRequest } from '@/types/task';

import { PrioritySelector } from './PrioritySelector';

interface AddSubtaskButtonProps {
  parentTask: Task;
  disabled?: boolean;
}

export function AddSubtaskButton({ parentTask, disabled = false }: AddSubtaskButtonProps) {
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [description, setDescription] = useState('');
  const [priority, setPriority] = useState<'LOW' | 'MEDIUM' | 'HIGH'>('MEDIUM');
  const queryClient = useQueryClient();

  const createMutation = useMutation({
    mutationFn: (data: TaskCreateRequest) =>
      taskService.createSubtask(parentTask.id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['subtasks', parentTask.id] });
      queryClient.invalidateQueries({ queryKey: ['tasks'] });
      toast.success('Subtask created');
      setDescription('');
      setPriority('MEDIUM');
      setIsFormOpen(false);
    },
    onError: (error: Error) => {
      const message = (error as { response?: { data?: { message?: string } } }).response?.data?.message || 'Failed to create subtask';
      if (message.includes('depth')) {
        toast.error('Maximum subtask depth (5 levels) reached');
      } else {
        toast.error(message);
      }
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!description.trim()) {
      toast.error('Description is required');
      return;
    }

    createMutation.mutate({
      description: description.trim(),
      priority,
    });
  };

  const handleCancel = () => {
    setDescription('');
    setPriority('MEDIUM');
    setIsFormOpen(false);
  };

  // Check if max depth would be exceeded
  const isMaxDepth = parentTask.depth >= 5;

  if (isMaxDepth) {
    return (
      <div className="rounded-lg bg-yellow-50 p-3 text-sm text-yellow-700">
        Maximum subtask depth (5 levels) reached
      </div>
    );
  }

  if (!isFormOpen) {
    return (
      <button
        onClick={() => setIsFormOpen(true)}
        disabled={disabled}
        className="btn btn-secondary w-full sm:w-auto"
      >
        <Plus className="h-4 w-4" />
        Add Subtask
      </button>
    );
  }

  return (
    <form onSubmit={handleSubmit} className="rounded-lg border border-gray-200 bg-white p-4">
      <div className="space-y-3">
        <div>
          <label htmlFor="subtask-description" className="label">
            Description
          </label>
          <input
            id="subtask-description"
            type="text"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Enter subtask description"
            className="input"
            autoFocus
            disabled={createMutation.isPending}
          />
        </div>

        <div>
          <label className="label">Priority</label>
          <PrioritySelector
            value={priority}
            onChange={setPriority}
            disabled={createMutation.isPending}
          />
        </div>

        <div className="flex gap-2">
          <button
            type="submit"
            disabled={createMutation.isPending || !description.trim()}
            className="btn btn-primary"
          >
            {createMutation.isPending ? 'Creating...' : 'Create'}
          </button>
          <button
            type="button"
            onClick={handleCancel}
            disabled={createMutation.isPending}
            className="btn btn-secondary"
          >
            <X className="h-4 w-4" />
            Cancel
          </button>
        </div>
      </div>
    </form>
  );
}
