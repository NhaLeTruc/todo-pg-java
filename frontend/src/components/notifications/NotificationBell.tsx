import React, { useState, useEffect } from 'react';

import { Bell } from 'lucide-react';

import { notificationService } from '../../services/notificationService';

import { NotificationList } from './NotificationList';

interface NotificationBellProps {
  userId: number;
}

export const NotificationBell: React.FC<NotificationBellProps> = ({ userId }) => {
  const [isOpen, setIsOpen] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    loadUnreadCount();

    // Poll for new notifications every 30 seconds
    const interval = setInterval(loadUnreadCount, 30000);

    return () => clearInterval(interval);
  }, [userId]);

  const loadUnreadCount = async () => {
    try {
      const count = await notificationService.getUnreadCount();
      setUnreadCount(count);
    } catch (error) {
      console.error('Failed to load unread count:', error);
    }
  };

  const handleMarkAsRead = () => {
    loadUnreadCount();
  };

  const toggleDropdown = () => {
    setIsOpen(!isOpen);
  };

  return (
    <div className="notification-bell">
      <button
        onClick={toggleDropdown}
        className="notification-bell-button"
        aria-label="Notifications"
      >
        <Bell size={24} />
        {unreadCount > 0 && (
          <span className="notification-badge">{unreadCount > 99 ? '99+' : unreadCount}</span>
        )}
      </button>

      {isOpen && (
        <div className="notification-dropdown">
          <div className="notification-dropdown-header">
            <h3>Notifications</h3>
            <button
              onClick={async () => {
                await notificationService.markAllAsRead();
                loadUnreadCount();
              }}
              className="mark-all-read-button"
            >
              Mark all as read
            </button>
          </div>
          <NotificationList onMarkAsRead={handleMarkAsRead} />
        </div>
      )}
    </div>
  );
};
