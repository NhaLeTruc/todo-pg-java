import React, { useEffect, useState } from 'react';

import { notificationService } from '../../services/notificationService';
import { Notification } from '../../types/notification';

import { NotificationItem } from './NotificationItem';

interface NotificationListProps {
  onMarkAsRead?: () => void;
}

export const NotificationList: React.FC<NotificationListProps> = ({ onMarkAsRead }) => {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadNotifications();
  }, []);

  const loadNotifications = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await notificationService.getUnreadNotifications();
      setNotifications(data);
    } catch (err) {
      console.error('Failed to load notifications:', err);
      setError('Failed to load notifications');
    } finally {
      setLoading(false);
    }
  };

  const handleMarkAsRead = async (notificationId: string) => {
    try {
      await notificationService.markAsRead(notificationId);
      setNotifications((prev) => prev.filter((n) => n.id !== notificationId));
      if (onMarkAsRead) {
        onMarkAsRead();
      }
    } catch (error) {
      console.error('Failed to mark notification as read:', error);
    }
  };

  const handleDelete = async (notificationId: string) => {
    try {
      await notificationService.deleteNotification(notificationId);
      setNotifications((prev) => prev.filter((n) => n.id !== notificationId));
      if (onMarkAsRead) {
        onMarkAsRead();
      }
    } catch (error) {
      console.error('Failed to delete notification:', error);
    }
  };

  if (loading) {
    return <div className="notification-list-loading">Loading notifications...</div>;
  }

  if (error) {
    return <div className="notification-list-error">{error}</div>;
  }

  if (notifications.length === 0) {
    return <div className="notification-list-empty">No new notifications</div>;
  }

  return (
    <div className="notification-list">
      {notifications.map((notification) => (
        <NotificationItem
          key={notification.id}
          notification={notification}
          onMarkAsRead={handleMarkAsRead}
          onDelete={handleDelete}
        />
      ))}
    </div>
  );
};
