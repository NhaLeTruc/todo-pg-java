export type NotificationType =
  | 'TASK_DUE_SOON'
  | 'TASK_OVERDUE'
  | 'TASK_SHARED'
  | 'TASK_COMMENTED'
  | 'TASK_MENTIONED'
  | 'TASK_ASSIGNED'
  | 'REMINDER';

export interface Notification {
  id: string;
  userId: number;
  type: NotificationType;
  message: string;
  relatedTaskId?: number;
  relatedTaskDescription?: string;
  isRead: boolean;
  createdAt: string;
  readAt?: string;
}

export interface NotificationPreference {
  id: string;
  userId: number;
  notificationType: NotificationType;
  inAppEnabled: boolean;
  emailEnabled: boolean;
}
