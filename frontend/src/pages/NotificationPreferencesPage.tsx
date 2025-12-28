import React, { useEffect, useState } from 'react';

import { notificationService } from '../services/notificationService';
import { NotificationPreference, NotificationType } from '../types/notification';

export const NotificationPreferencesPage: React.FC = () => {
  const [preferences, setPreferences] = useState<NotificationPreference[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState<string | null>(null);

  useEffect(() => {
    loadPreferences();
  }, []);

  const loadPreferences = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await notificationService.getPreferences();
      setPreferences(data);
    } catch (err) {
      console.error('Failed to load preferences:', err);
      setError('Failed to load notification preferences');
    } finally {
      setLoading(false);
    }
  };

  const handleToggle = async (
    preference: NotificationPreference,
    field: 'inAppEnabled' | 'emailEnabled'
  ) => {
    setSaving(preference.id);
    try {
      const updated = {
        ...preference,
        [field]: !preference[field],
      };
      const result = await notificationService.updatePreference(updated);
      setPreferences((prev) => prev.map((p) => (p.id === result.id ? result : p)));
    } catch (err) {
      console.error('Failed to update preference:', err);
      setError('Failed to update preference');
    } finally {
      setSaving(null);
    }
  };

  const getNotificationTypeLabel = (type: NotificationType): string => {
    const labels: Record<NotificationType, string> = {
      TASK_DUE_SOON: 'Task Due Soon',
      TASK_OVERDUE: 'Task Overdue',
      TASK_SHARED: 'Task Shared With You',
      TASK_COMMENTED: 'New Comment on Task',
      TASK_MENTIONED: 'Mentioned in Comment',
      TASK_ASSIGNED: 'Task Assigned to You',
      REMINDER: 'Task Reminder',
    };
    return labels[type] || type;
  };

  if (loading) {
    return <div className="preferences-page-loading">Loading preferences...</div>;
  }

  if (error) {
    return (
      <div className="preferences-page-error">
        <p>{error}</p>
        <button onClick={loadPreferences}>Retry</button>
      </div>
    );
  }

  return (
    <div className="notification-preferences-page">
      <h1>Notification Preferences</h1>
      <p className="preferences-description">
        Choose how you want to receive notifications for different types of events.
      </p>

      <div className="preferences-list">
        <div className="preferences-header">
          <div className="preference-name">Notification Type</div>
          <div className="preference-channel">In-App</div>
          <div className="preference-channel">Email</div>
        </div>

        {preferences.map((preference) => (
          <div
            key={preference.id}
            className={`preference-item ${saving === preference.id ? 'preference-item-saving' : ''}`}
          >
            <div className="preference-name">
              {getNotificationTypeLabel(preference.notificationType)}
            </div>
            <div className="preference-channel">
              <label className="preference-toggle">
                <input
                  type="checkbox"
                  checked={preference.inAppEnabled}
                  onChange={() => handleToggle(preference, 'inAppEnabled')}
                  disabled={saving === preference.id}
                />
                <span className="preference-toggle-slider"></span>
              </label>
            </div>
            <div className="preference-channel">
              <label className="preference-toggle">
                <input
                  type="checkbox"
                  checked={preference.emailEnabled}
                  onChange={() => handleToggle(preference, 'emailEnabled')}
                  disabled={saving === preference.id}
                />
                <span className="preference-toggle-slider"></span>
              </label>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
