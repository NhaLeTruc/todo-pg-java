import { useCallback, useEffect, useState } from 'react';

import { useQueryClient } from '@tanstack/react-query';

import { getWebSocketClient, TaskUpdate } from '../services/websocket';

interface UseTaskWebSocketOptions {
  userId: number | null;
  token: string | null;
  enabled?: boolean;
}

export const useTaskWebSocket = ({ userId, token, enabled = true }: UseTaskWebSocketOptions) => {
  const [isConnected, setIsConnected] = useState(false);
  const [lastUpdate, setLastUpdate] = useState<TaskUpdate | null>(null);
  const queryClient = useQueryClient();

  const handleTaskUpdate = useCallback(
    (update: TaskUpdate) => {
      setLastUpdate(update);

      // Invalidate relevant queries based on the update action
      switch (update.action) {
        case 'CREATED':
        case 'UPDATED':
        case 'COMPLETED':
          // Invalidate task list and specific task queries
          queryClient.invalidateQueries({ queryKey: ['tasks'] });
          queryClient.invalidateQueries({ queryKey: ['task', update.taskId] });
          break;

        case 'DELETED':
          // Remove the task from cache and invalidate list
          queryClient.removeQueries({ queryKey: ['task', update.taskId] });
          queryClient.invalidateQueries({ queryKey: ['tasks'] });
          break;

        case 'SHARED':
          // Invalidate shared tasks queries
          queryClient.invalidateQueries({ queryKey: ['tasks'] });
          queryClient.invalidateQueries({ queryKey: ['shared-tasks'] });
          break;

        default:
          console.warn('Unknown task update action:', update.action);
      }
    },
    [queryClient]
  );

  useEffect(() => {
    if (!enabled || !userId || !token) {
      return;
    }

    const client = getWebSocketClient();

    // Connect to WebSocket
    client
      .connect(token, userId)
      .then(() => {
        setIsConnected(true);

        // Subscribe to task updates
        const unsubscribe = client.subscribe(handleTaskUpdate);

        // Cleanup on unmount
        return () => {
          unsubscribe();
        };
      })
      .catch((error) => {
        console.error('Failed to connect to WebSocket:', error);
        setIsConnected(false);
      });

    return () => {
      client.disconnect();
      setIsConnected(false);
    };
  }, [userId, token, enabled, handleTaskUpdate]);

  const clearLastUpdate = useCallback(() => {
    setLastUpdate(null);
  }, []);

  return {
    isConnected,
    lastUpdate,
    clearLastUpdate,
  };
};
