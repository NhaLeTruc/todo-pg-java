import { QueryClient } from '@tanstack/react-query';
import { AxiosError } from 'axios';
import { toast } from 'react-hot-toast';

import { getErrorMessage } from './api';

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 5,
      gcTime: 1000 * 60 * 10,
      refetchOnWindowFocus: true,
      refetchOnReconnect: true,
      retry: (failureCount, error) => {
        if (failureCount >= 3) return false;

        const status = error instanceof AxiosError ? error.response?.status : undefined;
        if (status === 401 || status === 403 || status === 404) {
          return false;
        }

        return true;
      },
      retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 30000),
    },
    mutations: {
      retry: false,
      onError: (error) => {
        const message = getErrorMessage(error);
        toast.error(message);
      },
    },
  },
});

export function invalidateTaskQueries() {
  queryClient.invalidateQueries({ queryKey: ['tasks'] });
  queryClient.invalidateQueries({ queryKey: ['shared-tasks'] });
}

export function invalidateCategoryQueries() {
  queryClient.invalidateQueries({ queryKey: ['categories'] });
}

export function invalidateTagQueries() {
  queryClient.invalidateQueries({ queryKey: ['tags'] });
}

export function invalidateCommentQueries(taskId: number) {
  queryClient.invalidateQueries({ queryKey: ['comments', taskId] });
}

export function invalidateNotificationQueries() {
  queryClient.invalidateQueries({ queryKey: ['notifications'] });
}

export function invalidateTimeEntryQueries(taskId: number) {
  queryClient.invalidateQueries({ queryKey: ['time-entries', taskId] });
}

export function invalidateFileAttachmentQueries(taskId: number) {
  queryClient.invalidateQueries({ queryKey: ['file-attachments', taskId] });
}
