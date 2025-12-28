import React from 'react';

import { formatDistanceToNow } from 'date-fns';
import { AlertCircle, AtSign, Bell, Check, Clock, MessageCircle, Share2, X } from 'lucide-react';

import { Notification, NotificationType } from '../../types/notification';

interface NotificationItemProps {
  notification: Notification;
  onMarkAsRead: (notificationId: string) => void;
  onDelete: (notificationId: string) => void;
}

export const NotificationItem: React.FC<NotificationItemProps> = ({
  notification,
  onMarkAsRead,
  onDelete,
}) => {
  const getNotificationIcon = (type: NotificationType) => {
    switch (type) {
      case 'TASK_DUE_SOON':
        return <Clock size={20} className="notification-icon notification-icon-warning" />;
      case 'TASK_OVERDUE':
        return <AlertCircle size={20} className="notification-icon notification-icon-danger" />;
      case 'TASK_SHARED':
        return <Share2 size={20} className="notification-icon notification-icon-info" />;
      case 'TASK_COMMENTED':
        return <MessageCircle size={20} className="notification-icon notification-icon-info" />;
      case 'TASK_MENTIONED':
        return <AtSign size={20} className="notification-icon notification-icon-info" />;
      case 'TASK_ASSIGNED':
        return <Bell size={20} className="notification-icon notification-icon-info" />;
      case 'REMINDER':
        return <Bell size={20} className="notification-icon notification-icon-warning" />;
      default:
        return <Bell size={20} className="notification-icon notification-icon-info" />;
    }
  };

  const formatTimeAgo = (dateString: string) => {
    try {
      return formatDistanceToNow(new Date(dateString), { addSuffix: true });
    } catch {
      return 'Recently';
    }
  };

  return (
    <div className={`notification-item ${!notification.isRead ? 'notification-item-unread' : ''}`}>
      <div className="notification-item-icon">{getNotificationIcon(notification.type)}</div>

      <div className="notification-item-content">
        <p className="notification-item-message">{notification.message}</p>
        {notification.relatedTaskDescription && (
          <p className="notification-item-task">{notification.relatedTaskDescription}</p>
        )}
        <p className="notification-item-time">{formatTimeAgo(notification.createdAt)}</p>
      </div>

      <div className="notification-item-actions">
        {!notification.isRead && (
          <button
            onClick={() => onMarkAsRead(notification.id)}
            className="notification-action-button"
            title="Mark as read"
            aria-label="Mark as read"
          >
            <Check size={16} />
          </button>
        )}
        <button
          onClick={() => onDelete(notification.id)}
          className="notification-action-button"
          title="Delete"
          aria-label="Delete notification"
        >
          <X size={16} />
        </button>
      </div>
    </div>
  );
};
