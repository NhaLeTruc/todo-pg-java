import { useState } from 'react';

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'react-hot-toast';

import { ConfirmDialog } from '@/components/shared/ConfirmDialog';
import { TaskEditModal } from '@/components/tasks/TaskEditModal';
import { TaskForm } from '@/components/tasks/TaskForm';
import { TaskList } from '@/components/tasks/TaskList';
import { taskService } from '@/services/taskService';
import { Task, TaskCreateRequest, TaskUpdateRequest } from '@/types/task';

export function TasksPage() {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [size] = useState(20);
  const [search, setSearch] = useState<string>('');
  const [completedFilter, setCompletedFilter] = useState<boolean | undefined>(undefined);
  const [sortBy, setSortBy] = useState<string>('createdAt');
  const [sortDirection] = useState<'asc' | 'desc'>('desc');
  const [editingTask, setEditingTask] = useState<Task | null>(null);
  const [deletingTaskId, setDeletingTaskId] = useState<number | null>(null);

  const {
    data: tasksData,
    isLoading,
    error,
  } = useQuery({
    queryKey: ['tasks', page, size, search, completedFilter, sortBy, sortDirection],
    queryFn: () =>
      taskService.getTasks({
        page,
        size,
        search: search || undefined,
        completed: completedFilter,
        sortBy,
        sortDirection,
      }),
  });

  const createMutation = useMutation({
    mutationFn: (data: TaskCreateRequest) => taskService.createTask(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks'] });
      toast.success('Task created successfully!');
    },
    onError: (error: Error) => {
      toast.error(`Failed to create task: ${error.message}`);
    },
  });

  const toggleCompleteMutation = useMutation({
    mutationFn: ({ id, isCompleted }: { id: number; isCompleted: boolean }) =>
      taskService.toggleComplete(id, isCompleted),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks'] });
      toast.success('Task updated!');
    },
    onError: (error: Error) => {
      toast.error(`Failed to update task: ${error.message}`);
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: TaskUpdateRequest }) =>
      taskService.updateTask(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks'] });
      toast.success('Task updated successfully!');
      setEditingTask(null);
    },
    onError: (error: Error) => {
      toast.error(`Failed to update task: ${error.message}`);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => taskService.deleteTask(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks'] });
      toast.success('Task deleted!');
      setDeletingTaskId(null);
    },
    onError: (error: Error) => {
      toast.error(`Failed to delete task: ${error.message}`);
      setDeletingTaskId(null);
    },
  });

  const handleCreateTask = async (data: TaskCreateRequest) => {
    await createMutation.mutateAsync(data);
  };

  const handleToggleComplete = (id: number, isCompleted: boolean) => {
    toggleCompleteMutation.mutate({ id, isCompleted });
  };

  const handleEditTask = (id: number) => {
    const task = tasksData?.content.find((t) => t.id === id);
    if (task) {
      setEditingTask(task);
    }
  };

  const handleSaveTask = (id: number, data: TaskUpdateRequest) => {
    updateMutation.mutate({ id, data });
  };

  const handleDeleteTask = (id: number) => {
    setDeletingTaskId(id);
  };

  const confirmDelete = () => {
    if (deletingTaskId) {
      deleteMutation.mutate(deletingTaskId);
    }
  };

  const handleSearchChange = (value: string) => {
    setSearch(value);
    setPage(0);
  };

  const handleFilterChange = (value: string) => {
    if (value === 'all') {
      setCompletedFilter(undefined);
    } else if (value === 'completed') {
      setCompletedFilter(true);
    } else if (value === 'active') {
      setCompletedFilter(false);
    }
    setPage(0);
  };

  const handleSortChange = (value: string) => {
    setSortBy(value);
    setPage(0);
  };

  if (error) {
    return (
      <div className="container mx-auto max-w-4xl px-4 py-8">
        <div className="card bg-red-50 text-red-800">
          <p className="font-medium">Error loading tasks</p>
          <p className="text-sm">{error.message}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto max-w-4xl px-4 py-8">
      <div className="mb-8">
        <h1 className="mb-2 text-3xl font-bold text-gray-900">My Tasks</h1>
        <p className="text-gray-600">Manage your tasks efficiently</p>
      </div>

      <div className="mb-6">
        <TaskForm onSubmit={handleCreateTask} isLoading={createMutation.isPending} />
      </div>

      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex-1">
          <input
            type="text"
            placeholder="Search tasks..."
            value={search}
            onChange={(e) => handleSearchChange(e.target.value)}
            className="w-full rounded-lg border border-gray-300 px-4 py-2 text-sm transition-colors focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-200"
          />
        </div>

        <div className="flex gap-2">
          <select
            value={completedFilter === undefined ? 'all' : completedFilter ? 'completed' : 'active'}
            onChange={(e) => handleFilterChange(e.target.value)}
            className="rounded-lg border border-gray-300 px-4 py-2 text-sm transition-colors focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-200"
          >
            <option value="all">All Tasks</option>
            <option value="active">Active</option>
            <option value="completed">Completed</option>
          </select>

          <select
            value={sortBy}
            onChange={(e) => handleSortChange(e.target.value)}
            className="rounded-lg border border-gray-300 px-4 py-2 text-sm transition-colors focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-200"
          >
            <option value="createdAt">Sort by Date</option>
            <option value="priority">Sort by Priority</option>
            <option value="dueDate">Sort by Due Date</option>
          </select>
        </div>
      </div>

      <div className="mb-6">
        <TaskList
          tasks={tasksData?.content || []}
          onToggleComplete={handleToggleComplete}
          onDelete={handleDeleteTask}
          onEdit={handleEditTask}
          isLoading={isLoading}
        />
      </div>

      {tasksData && tasksData.totalPages > 1 && (
        <div className="flex items-center justify-center gap-2">
          <button
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            disabled={page === 0}
            className="rounded-lg border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 transition-colors hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
          >
            Previous
          </button>

          <span className="text-sm text-gray-600">
            Page {page + 1} of {tasksData.totalPages}
          </span>

          <button
            onClick={() => setPage((p) => Math.min(tasksData.totalPages - 1, p + 1))}
            disabled={page >= tasksData.totalPages - 1}
            className="rounded-lg border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 transition-colors hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
          >
            Next
          </button>
        </div>
      )}

      {tasksData && (
        <div className="mt-4 text-center text-sm text-gray-500">
          Showing {tasksData.content.length} of {tasksData.totalElements} tasks
        </div>
      )}

      {editingTask && (
        <TaskEditModal
          task={editingTask}
          isOpen={!!editingTask}
          onClose={() => setEditingTask(null)}
          onSave={handleSaveTask}
          isLoading={updateMutation.isPending}
        />
      )}

      <ConfirmDialog
        isOpen={deletingTaskId !== null}
        title="Delete Task"
        message="Are you sure you want to delete this task? This action cannot be undone."
        confirmLabel="Delete"
        cancelLabel="Cancel"
        onConfirm={confirmDelete}
        onCancel={() => setDeletingTaskId(null)}
        isDestructive
      />
    </div>
  );
}
