import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { notificationService } from '../services/notificationService';
import { Notification, NotificationPreference } from '../types/notification';

const NOTIFICATIONS_QUERY_KEY = 'notifications';
const NOTIFICATION_PREFERENCES_QUERY_KEY = 'notificationPreferences';
const NOTIFICATION_COUNT_QUERY_KEY = 'notificationCount';

/**
 * Hook for fetching unread notifications
 */
export function useNotifications() {
  return useQuery({
    queryKey: [NOTIFICATIONS_QUERY_KEY],
    queryFn: () => notificationService.getUnreadNotifications(),
    staleTime: 5000, // Consider data fresh for 5 seconds
    refetchInterval: 30000, // Refetch every 30 seconds
  });
}

/**
 * Hook for fetching unread notification count
 */
export function useNotificationCount() {
  return useQuery({
    queryKey: [NOTIFICATION_COUNT_QUERY_KEY],
    queryFn: () => notificationService.getUnreadCount(),
    staleTime: 5000,
    refetchInterval: 30000,
  });
}

/**
 * Hook for marking a notification as read with optimistic update
 */
export function useMarkNotificationAsRead() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (notificationId: string) => notificationService.markAsRead(notificationId),
    onMutate: async (notificationId) => {
      // Cancel any outgoing refetches
      await queryClient.cancelQueries({ queryKey: [NOTIFICATIONS_QUERY_KEY] });
      await queryClient.cancelQueries({ queryKey: [NOTIFICATION_COUNT_QUERY_KEY] });

      // Snapshot the previous values
      const previousNotifications = queryClient.getQueryData([NOTIFICATIONS_QUERY_KEY]);
      const previousCount = queryClient.getQueryData([NOTIFICATION_COUNT_QUERY_KEY]);

      // Optimistically update notifications
      queryClient.setQueryData(
        [NOTIFICATIONS_QUERY_KEY],
        (old: Notification[] | undefined) => {
          if (!old) return old;
          return old.filter((notification) => notification.id !== notificationId);
        }
      );

      // Optimistically update count
      queryClient.setQueryData([NOTIFICATION_COUNT_QUERY_KEY], (old: number | undefined) => {
        if (old === undefined) return old;
        return Math.max(0, old - 1);
      });

      return { previousNotifications, previousCount };
    },
    onError: (_err, _notificationId, context) => {
      // Rollback on error
      if (context?.previousNotifications) {
        queryClient.setQueryData([NOTIFICATIONS_QUERY_KEY], context.previousNotifications);
      }
      if (context?.previousCount !== undefined) {
        queryClient.setQueryData([NOTIFICATION_COUNT_QUERY_KEY], context.previousCount);
      }
    },
    onSettled: () => {
      // Always refetch after error or success
      queryClient.invalidateQueries({ queryKey: [NOTIFICATIONS_QUERY_KEY] });
      queryClient.invalidateQueries({ queryKey: [NOTIFICATION_COUNT_QUERY_KEY] });
    },
  });
}

/**
 * Hook for deleting a notification with optimistic update
 */
export function useDeleteNotification() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (notificationId: string) => notificationService.deleteNotification(notificationId),
    onMutate: async (notificationId) => {
      // Cancel any outgoing refetches
      await queryClient.cancelQueries({ queryKey: [NOTIFICATIONS_QUERY_KEY] });
      await queryClient.cancelQueries({ queryKey: [NOTIFICATION_COUNT_QUERY_KEY] });

      // Snapshot the previous values
      const previousNotifications = queryClient.getQueryData([NOTIFICATIONS_QUERY_KEY]);
      const previousCount = queryClient.getQueryData([NOTIFICATION_COUNT_QUERY_KEY]);

      // Optimistically remove the notification
      queryClient.setQueryData(
        [NOTIFICATIONS_QUERY_KEY],
        (old: Notification[] | undefined) => {
          if (!old) return old;
          return old.filter((notification) => notification.id !== notificationId);
        }
      );

      // Optimistically update count
      queryClient.setQueryData([NOTIFICATION_COUNT_QUERY_KEY], (old: number | undefined) => {
        if (old === undefined) return old;
        return Math.max(0, old - 1);
      });

      return { previousNotifications, previousCount };
    },
    onError: (_err, _notificationId, context) => {
      // Rollback on error
      if (context?.previousNotifications) {
        queryClient.setQueryData([NOTIFICATIONS_QUERY_KEY], context.previousNotifications);
      }
      if (context?.previousCount !== undefined) {
        queryClient.setQueryData([NOTIFICATION_COUNT_QUERY_KEY], context.previousCount);
      }
    },
    onSettled: () => {
      // Always refetch after error or success
      queryClient.invalidateQueries({ queryKey: [NOTIFICATIONS_QUERY_KEY] });
      queryClient.invalidateQueries({ queryKey: [NOTIFICATION_COUNT_QUERY_KEY] });
    },
  });
}

/**
 * Hook for marking all notifications as read with optimistic update
 */
export function useMarkAllNotificationsAsRead() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => notificationService.markAllAsRead(),
    onMutate: async () => {
      // Cancel any outgoing refetches
      await queryClient.cancelQueries({ queryKey: [NOTIFICATIONS_QUERY_KEY] });
      await queryClient.cancelQueries({ queryKey: [NOTIFICATION_COUNT_QUERY_KEY] });

      // Snapshot the previous values
      const previousNotifications = queryClient.getQueryData([NOTIFICATIONS_QUERY_KEY]);
      const previousCount = queryClient.getQueryData([NOTIFICATION_COUNT_QUERY_KEY]);

      // Optimistically clear all notifications
      queryClient.setQueryData([NOTIFICATIONS_QUERY_KEY], []);
      queryClient.setQueryData([NOTIFICATION_COUNT_QUERY_KEY], 0);

      return { previousNotifications, previousCount };
    },
    onError: (_err, _variables, context) => {
      // Rollback on error
      if (context?.previousNotifications) {
        queryClient.setQueryData([NOTIFICATIONS_QUERY_KEY], context.previousNotifications);
      }
      if (context?.previousCount !== undefined) {
        queryClient.setQueryData([NOTIFICATION_COUNT_QUERY_KEY], context.previousCount);
      }
    },
    onSettled: () => {
      // Always refetch after error or success
      queryClient.invalidateQueries({ queryKey: [NOTIFICATIONS_QUERY_KEY] });
      queryClient.invalidateQueries({ queryKey: [NOTIFICATION_COUNT_QUERY_KEY] });
    },
  });
}

/**
 * Hook for fetching notification preferences
 */
export function useNotificationPreferences() {
  return useQuery({
    queryKey: [NOTIFICATION_PREFERENCES_QUERY_KEY],
    queryFn: () => notificationService.getPreferences(),
    staleTime: 60000, // Consider data fresh for 1 minute
  });
}

/**
 * Hook for updating notification preference with optimistic update
 */
export function useUpdateNotificationPreference() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (preference: NotificationPreference) =>
      notificationService.updatePreference(preference),
    onMutate: async (updatedPreference) => {
      // Cancel any outgoing refetches
      await queryClient.cancelQueries({ queryKey: [NOTIFICATION_PREFERENCES_QUERY_KEY] });

      // Snapshot the previous value
      const previousPreferences = queryClient.getQueryData([NOTIFICATION_PREFERENCES_QUERY_KEY]);

      // Optimistically update the preference
      queryClient.setQueryData(
        [NOTIFICATION_PREFERENCES_QUERY_KEY],
        (old: NotificationPreference[] | undefined) => {
          if (!old) return old;
          return old.map((pref) =>
            pref.id === updatedPreference.id ? updatedPreference : pref
          );
        }
      );

      return { previousPreferences };
    },
    onError: (_err, _updatedPreference, context) => {
      // Rollback on error
      if (context?.previousPreferences) {
        queryClient.setQueryData(
          [NOTIFICATION_PREFERENCES_QUERY_KEY],
          context.previousPreferences
        );
      }
    },
    onSettled: () => {
      // Always refetch after error or success
      queryClient.invalidateQueries({ queryKey: [NOTIFICATION_PREFERENCES_QUERY_KEY] });
    },
  });
}
