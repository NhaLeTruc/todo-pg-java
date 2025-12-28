import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { GetTasksParams, taskService } from '../services/taskService';
import { Task, TaskCreateRequest, TaskUpdateRequest } from '../types/task';

const TASKS_QUERY_KEY = 'tasks';

/**
 * Hook for fetching tasks with pagination and filtering
 */
export function useTasks(params: GetTasksParams = {}) {
  return useQuery({
    queryKey: [TASKS_QUERY_KEY, params],
    queryFn: () => taskService.getTasks(params),
    staleTime: 30000, // Consider data fresh for 30 seconds
  });
}

/**
 * Hook for fetching a single task by ID
 */
export function useTask(id: number) {
  return useQuery({
    queryKey: [TASKS_QUERY_KEY, id],
    queryFn: () => taskService.getTaskById(id),
    enabled: !!id,
  });
}

/**
 * Hook for creating a new task with optimistic update
 */
export function useCreateTask() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: TaskCreateRequest) => taskService.createTask(data),
    onMutate: async (newTask) => {
      // Cancel any outgoing refetches
      await queryClient.cancelQueries({ queryKey: [TASKS_QUERY_KEY] });

      // Snapshot the previous value
      const previousTasks = queryClient.getQueryData([TASKS_QUERY_KEY]);

      // Optimistically update to the new value
      queryClient.setQueryData([TASKS_QUERY_KEY, {}], (old: any) => {
        if (!old) return old;
        const optimisticTask: Task = {
          id: Date.now(), // Temporary ID
          ...newTask,
          completed: false,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
        } as Task;
        return {
          ...old,
          content: [optimisticTask, ...old.content],
          totalElements: old.totalElements + 1,
        };
      });

      return { previousTasks };
    },
    onError: (_err, _newTask, context) => {
      // Rollback on error
      if (context?.previousTasks) {
        queryClient.setQueryData([TASKS_QUERY_KEY], context.previousTasks);
      }
    },
    onSettled: () => {
      // Always refetch after error or success
      queryClient.invalidateQueries({ queryKey: [TASKS_QUERY_KEY] });
    },
  });
}

/**
 * Hook for updating a task with optimistic update
 */
export function useUpdateTask() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: TaskUpdateRequest }) =>
      taskService.updateTask(id, data),
    onMutate: async ({ id, data }) => {
      // Cancel any outgoing refetches
      await queryClient.cancelQueries({ queryKey: [TASKS_QUERY_KEY, id] });

      // Snapshot the previous value
      const previousTask = queryClient.getQueryData([TASKS_QUERY_KEY, id]);

      // Optimistically update to the new value
      queryClient.setQueryData([TASKS_QUERY_KEY, id], (old: Task | undefined) => {
        if (!old) return old;
        return {
          ...old,
          ...data,
          updatedAt: new Date().toISOString(),
        };
      });

      // Also update in the list
      queryClient.setQueriesData({ queryKey: [TASKS_QUERY_KEY] }, (old: any) => {
        if (!old || !old.content) return old;
        return {
          ...old,
          content: old.content.map((task: Task) =>
            task.id === id ? { ...task, ...data, updatedAt: new Date().toISOString() } : task
          ),
        };
      });

      return { previousTask };
    },
    onError: (_err, { id }, context) => {
      // Rollback on error
      if (context?.previousTask) {
        queryClient.setQueryData([TASKS_QUERY_KEY, id], context.previousTask);
      }
    },
    onSettled: (_data, _error, { id }) => {
      // Always refetch after error or success
      queryClient.invalidateQueries({ queryKey: [TASKS_QUERY_KEY, id] });
      queryClient.invalidateQueries({ queryKey: [TASKS_QUERY_KEY] });
    },
  });
}

/**
 * Hook for deleting a task with optimistic update
 */
export function useDeleteTask() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => taskService.deleteTask(id),
    onMutate: async (id) => {
      // Cancel any outgoing refetches
      await queryClient.cancelQueries({ queryKey: [TASKS_QUERY_KEY] });

      // Snapshot the previous value
      const previousTasks = queryClient.getQueryData([TASKS_QUERY_KEY]);

      // Optimistically remove the task
      queryClient.setQueriesData({ queryKey: [TASKS_QUERY_KEY] }, (old: any) => {
        if (!old || !old.content) return old;
        return {
          ...old,
          content: old.content.filter((task: Task) => task.id !== id),
          totalElements: old.totalElements - 1,
        };
      });

      return { previousTasks };
    },
    onError: (_err, _id, context) => {
      // Rollback on error
      if (context?.previousTasks) {
        queryClient.setQueryData([TASKS_QUERY_KEY], context.previousTasks);
      }
    },
    onSettled: () => {
      // Always refetch after error or success
      queryClient.invalidateQueries({ queryKey: [TASKS_QUERY_KEY] });
    },
  });
}

/**
 * Hook for toggling task completion with optimistic update
 */
export function useToggleTaskComplete() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, isCompleted }: { id: number; isCompleted: boolean }) =>
      taskService.toggleComplete(id, isCompleted),
    onMutate: async ({ id, isCompleted }) => {
      // Cancel any outgoing refetches
      await queryClient.cancelQueries({ queryKey: [TASKS_QUERY_KEY, id] });

      // Snapshot the previous value
      const previousTask = queryClient.getQueryData([TASKS_QUERY_KEY, id]);

      // Optimistically update the task
      queryClient.setQueryData([TASKS_QUERY_KEY, id], (old: Task | undefined) => {
        if (!old) return old;
        return {
          ...old,
          completed: isCompleted,
          completedAt: isCompleted ? new Date().toISOString() : null,
          updatedAt: new Date().toISOString(),
        };
      });

      // Also update in the list
      queryClient.setQueriesData({ queryKey: [TASKS_QUERY_KEY] }, (old: any) => {
        if (!old || !old.content) return old;
        return {
          ...old,
          content: old.content.map((task: Task) =>
            task.id === id
              ? {
                  ...task,
                  completed: isCompleted,
                  completedAt: isCompleted ? new Date().toISOString() : null,
                  updatedAt: new Date().toISOString(),
                }
              : task
          ),
        };
      });

      return { previousTask };
    },
    onError: (_err, { id }, context) => {
      // Rollback on error
      if (context?.previousTask) {
        queryClient.setQueryData([TASKS_QUERY_KEY, id], context.previousTask);
      }
    },
    onSettled: (_data, _error, { id }) => {
      // Always refetch after error or success
      queryClient.invalidateQueries({ queryKey: [TASKS_QUERY_KEY, id] });
      queryClient.invalidateQueries({ queryKey: [TASKS_QUERY_KEY] });
    },
  });
}

/**
 * Hook for fetching subtasks
 */
export function useSubtasks(parentTaskId: number) {
  return useQuery({
    queryKey: [TASKS_QUERY_KEY, parentTaskId, 'subtasks'],
    queryFn: () => taskService.getSubtasks(parentTaskId),
    enabled: !!parentTaskId,
  });
}

/**
 * Hook for creating a subtask with optimistic update
 */
export function useCreateSubtask(parentTaskId: number) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: TaskCreateRequest) => taskService.createSubtask(parentTaskId, data),
    onMutate: async (newSubtask) => {
      // Cancel any outgoing refetches
      await queryClient.cancelQueries({
        queryKey: [TASKS_QUERY_KEY, parentTaskId, 'subtasks'],
      });

      // Snapshot the previous value
      const previousSubtasks = queryClient.getQueryData([
        TASKS_QUERY_KEY,
        parentTaskId,
        'subtasks',
      ]);

      // Optimistically update to the new value
      queryClient.setQueryData(
        [TASKS_QUERY_KEY, parentTaskId, 'subtasks'],
        (old: Task[] | undefined) => {
          if (!old) return old;
          const optimisticSubtask: Task = {
            id: Date.now(), // Temporary ID
            ...newSubtask,
            parentTaskId,
            completed: false,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString(),
          } as Task;
          return [optimisticSubtask, ...old];
        }
      );

      return { previousSubtasks };
    },
    onError: (_err, _newSubtask, context) => {
      // Rollback on error
      if (context?.previousSubtasks) {
        queryClient.setQueryData(
          [TASKS_QUERY_KEY, parentTaskId, 'subtasks'],
          context.previousSubtasks
        );
      }
    },
    onSettled: () => {
      // Always refetch after error or success
      queryClient.invalidateQueries({ queryKey: [TASKS_QUERY_KEY, parentTaskId, 'subtasks'] });
      queryClient.invalidateQueries({ queryKey: [TASKS_QUERY_KEY, parentTaskId] });
    },
  });
}
