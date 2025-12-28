import { useState } from 'react';

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'react-hot-toast';

import { ConfirmDialog } from '@/components/shared/ConfirmDialog';
import BatchActionBar from '@/components/tasks/BatchActionBar';
import { CategorySelector } from '@/components/tasks/CategorySelector';
import { TagSelector } from '@/components/tasks/TagSelector';
import { TaskDetailModal } from '@/components/tasks/TaskDetailModal';
import { TaskEditModal } from '@/components/tasks/TaskEditModal';
import { TaskForm } from '@/components/tasks/TaskForm';
import { TaskList } from '@/components/tasks/TaskList';
import { useAuth } from '@/context/AuthContext';
import { useTaskWebSocket } from '@/hooks/useTaskWebSocket';
import { taskService } from '@/services/taskService';
import { Task, TaskCreateRequest, TaskUpdateRequest } from '@/types/task';

export function TasksPage() {
  const queryClient = useQueryClient();
  const { user, token } = useAuth();
  const [page, setPage] = useState(0);
  const [size] = useState(20);
  const [search, setSearch] = useState<string>('');
  const [completedFilter, setCompletedFilter] = useState<boolean | undefined>(undefined);
  const [categoryFilter, setCategoryFilter] = useState<number | null>(null);
  const [tagFilter, setTagFilter] = useState<number[]>([]);
  const [sortBy, setSortBy] = useState<string>('createdAt');
  const [sortDirection] = useState<'asc' | 'desc'>('desc');
  const [editingTask, setEditingTask] = useState<Task | null>(null);
  const [deletingTaskId, setDeletingTaskId] = useState<number | null>(null);
  const [taskToDeleteHasSubtasks, setTaskToDeleteHasSubtasks] = useState(false);
  const [viewingTask, setViewingTask] = useState<Task | null>(null);
  const [selectedTaskIds, setSelectedTaskIds] = useState<number[]>([]);
  const [view, setView] = useState<'my-tasks' | 'shared-with-me'>('my-tasks');

  // Enable real-time task updates via WebSocket
  useTaskWebSocket({
    userId: user?.id ?? null,
    token: token ?? null,
    enabled: !!user && !!token,
  });

  const {
    data: tasksData,
    isLoading,
    error,
  } = useQuery({
    queryKey: [
      view === 'my-tasks' ? 'tasks' : 'shared-tasks',
      page,
      size,
      search,
      completedFilter,
      categoryFilter,
      tagFilter,
      sortBy,
      sortDirection,
    ],
    queryFn: () => {
      const params = {
        page,
        size,
        search: search || undefined,
        completed: completedFilter,
        categoryId: categoryFilter || undefined,
        tagIds: tagFilter.length > 0 ? tagFilter : undefined,
        sortBy,
        sortDirection,
      };

      return view === 'my-tasks'
        ? taskService.getTasks(params)
        : taskService.getSharedWithMeTasks(params);
    },
  });

  const { data: categoriesData } = useQuery({
    queryKey: ['categories'],
    queryFn: async () => {
      const response = await fetch('/api/v1/categories', {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`,
        },
      });
      if (!response.ok) throw new Error('Failed to fetch categories');
      return response.json();
    },
  });

  const { data: tagsData } = useQuery({
    queryKey: ['tags'],
    queryFn: async () => {
      const response = await fetch('/api/v1/tags', {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`,
        },
      });
      if (!response.ok) throw new Error('Failed to fetch tags');
      return response.json();
    },
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

  const handleDeleteTask = async (id: number) => {
    try {
      const hasSubtasks = await taskService.taskHasSubtasks(id);
      setTaskToDeleteHasSubtasks(hasSubtasks);
      setDeletingTaskId(id);
    } catch (error) {
      toast.error('Failed to check task status');
    }
  };

  const confirmDelete = () => {
    if (deletingTaskId) {
      deleteMutation.mutate(deletingTaskId);
    }
  };

  const handleCancelDelete = () => {
    setDeletingTaskId(null);
    setTaskToDeleteHasSubtasks(false);
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

  const handleCategoryFilterChange = (categoryId: number | null) => {
    setCategoryFilter(categoryId);
    setPage(0);
  };

  const handleTagFilterChange = (tagIds: number[]) => {
    setTagFilter(tagIds);
    setPage(0);
  };

  const handleViewTaskDetails = (task: Task) => {
    setViewingTask(task);
  };

  const handleSelectionChange = (id: number, selected: boolean) => {
    if (selected) {
      setSelectedTaskIds((prev) => [...prev, id]);
    } else {
      setSelectedTaskIds((prev) => prev.filter((taskId) => taskId !== id));
    }
  };

  const handleSelectAll = (selected: boolean) => {
    if (selected) {
      const allTaskIds = tasksData?.content.map((task) => task.id) || [];
      setSelectedTaskIds(allTaskIds);
    } else {
      setSelectedTaskIds([]);
    }
  };

  const handleClearSelection = () => {
    setSelectedTaskIds([]);
  };

  const handleBatchOperationComplete = () => {
    queryClient.invalidateQueries({ queryKey: ['tasks'] });
    setSelectedTaskIds([]);
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
        <h1 className="mb-2 text-3xl font-bold text-gray-900">
          {view === 'my-tasks' ? 'My Tasks' : 'Shared with Me'}
        </h1>
        <p className="text-gray-600">
          {view === 'my-tasks' ? 'Manage your tasks efficiently' : 'Tasks shared by others'}
        </p>
      </div>

      {/* View Toggle */}
      <div className="mb-6 flex gap-2">
        <button
          onClick={() => {
            setView('my-tasks');
            setPage(0);
          }}
          className={`rounded-lg px-4 py-2 text-sm font-medium transition-colors ${
            view === 'my-tasks'
              ? 'bg-blue-600 text-white'
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          }`}
        >
          My Tasks
        </button>
        <button
          onClick={() => {
            setView('shared-with-me');
            setPage(0);
          }}
          className={`rounded-lg px-4 py-2 text-sm font-medium transition-colors ${
            view === 'shared-with-me'
              ? 'bg-blue-600 text-white'
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          }`}
        >
          Shared with Me
        </button>
      </div>

      {view === 'my-tasks' && (
        <div className="mb-6">
          <TaskForm onSubmit={handleCreateTask} isLoading={createMutation.isPending} />
        </div>
      )}

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

      <div className="mb-6 flex flex-col gap-4 sm:flex-row">
        <div className="flex-1">
          <label className="mb-1 block text-sm font-medium text-gray-700">Filter by Category</label>
          <CategorySelector value={categoryFilter} onChange={handleCategoryFilterChange} />
        </div>
        <div className="flex-1">
          <label className="mb-1 block text-sm font-medium text-gray-700">Filter by Tags</label>
          <TagSelector value={tagFilter} onChange={handleTagFilterChange} />
        </div>
      </div>

      <div className="mb-6">
        <TaskList
          tasks={tasksData?.content || []}
          onToggleComplete={handleToggleComplete}
          onDelete={handleDeleteTask}
          onEdit={handleEditTask}
          onViewDetails={handleViewTaskDetails}
          isLoading={isLoading}
          selectedTaskIds={selectedTaskIds}
          onSelectionChange={handleSelectionChange}
          onSelectAll={handleSelectAll}
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

      {viewingTask && (
        <TaskDetailModal
          task={viewingTask}
          isOpen={!!viewingTask}
          onClose={() => setViewingTask(null)}
          currentUserId={user?.id ?? 0}
        />
      )}

      <ConfirmDialog
        isOpen={deletingTaskId !== null}
        title="Delete Task"
        message={
          taskToDeleteHasSubtasks
            ? 'This task has subtasks. Deleting it will also delete all its subtasks. This action cannot be undone.'
            : 'Are you sure you want to delete this task? This action cannot be undone.'
        }
        confirmLabel="Delete"
        cancelLabel="Cancel"
        onConfirm={confirmDelete}
        onCancel={handleCancelDelete}
        isDestructive
      />

      <BatchActionBar
        selectedTaskIds={selectedTaskIds}
        onClearSelection={handleClearSelection}
        onOperationComplete={handleBatchOperationComplete}
        availableCategories={categoriesData || []}
        availableTags={tagsData || []}
      />
    </div>
  );
}
