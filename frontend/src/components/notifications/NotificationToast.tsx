import React, { useEffect, useState } from 'react';

import { X } from 'lucide-react';

import { Notification } from '../../types/notification';

interface NotificationToastProps {
  notification: Notification;
  onClose: () => void;
  duration?: number;
}

export const NotificationToast: React.FC<NotificationToastProps> = ({
  notification,
  onClose,
  duration = 5000,
}) => {
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    // Trigger animation
    setTimeout(() => setIsVisible(true), 10);

    // Auto-dismiss after duration
    const timer = setTimeout(() => {
      setIsVisible(false);
      setTimeout(onClose, 300); // Wait for animation
    }, duration);

    return () => clearTimeout(timer);
  }, [duration, onClose]);

  const handleClose = () => {
    setIsVisible(false);
    setTimeout(onClose, 300);
  };

  return (
    <div className={`notification-toast ${isVisible ? 'notification-toast-visible' : ''}`}>
      <div className="notification-toast-content">
        <p className="notification-toast-message">{notification.message}</p>
        {notification.relatedTaskDescription && (
          <p className="notification-toast-task">{notification.relatedTaskDescription}</p>
        )}
      </div>
      <button
        onClick={handleClose}
        className="notification-toast-close"
        aria-label="Close notification"
      >
        <X size={16} />
      </button>
    </div>
  );
};

interface NotificationToastContainerProps {
  notifications: Notification[];
  onDismiss: (id: string) => void;
}

export const NotificationToastContainer: React.FC<NotificationToastContainerProps> = ({
  notifications,
  onDismiss,
}) => {
  return (
    <div className="notification-toast-container">
      {notifications.map((notification) => (
        <NotificationToast
          key={notification.id}
          notification={notification}
          onClose={() => onDismiss(notification.id)}
        />
      ))}
    </div>
  );
};
