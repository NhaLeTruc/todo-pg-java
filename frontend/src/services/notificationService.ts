import { Notification, NotificationPreference } from '../types/notification';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api/v1';

class NotificationService {
  private async fetchWithAuth(url: string, options: RequestInit = {}) {
    const token = localStorage.getItem('authToken');
    const headers = {
      'Content-Type': 'application/json',
      ...(token && { Authorization: `Bearer ${token}` }),
      ...options.headers,
    };

    const response = await fetch(`${API_BASE_URL}${url}`, {
      ...options,
      headers,
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    // Handle 204 No Content responses
    if (response.status === 204) {
      return null;
    }

    return response.json();
  }

  async getUnreadNotifications(): Promise<Notification[]> {
    return this.fetchWithAuth('/notifications');
  }

  async getUnreadCount(): Promise<number> {
    return this.fetchWithAuth('/notifications/count');
  }

  async markAsRead(notificationId: string): Promise<Notification> {
    return this.fetchWithAuth(`/notifications/${notificationId}/read`, {
      method: 'PUT',
    });
  }

  async deleteNotification(notificationId: string): Promise<void> {
    return this.fetchWithAuth(`/notifications/${notificationId}`, {
      method: 'DELETE',
    });
  }

  async markAllAsRead(): Promise<void> {
    return this.fetchWithAuth('/notifications/read-all', {
      method: 'PUT',
    });
  }

  async getPreferences(): Promise<NotificationPreference[]> {
    return this.fetchWithAuth('/notifications/preferences');
  }

  async updatePreference(preference: NotificationPreference): Promise<NotificationPreference> {
    return this.fetchWithAuth('/notifications/preferences', {
      method: 'PUT',
      body: JSON.stringify(preference),
    });
  }
}

export const notificationService = new NotificationService();
