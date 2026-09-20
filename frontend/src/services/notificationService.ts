import api from './api';
import { NotificationItem, UnreadCountResponse } from '../types/notification.types';

export const notificationService = {
  getNotifications: async (): Promise<NotificationItem[]> => {
    const response = await api.get<NotificationItem[]>('/notifications');
    return response.data;
  },

  getUnreadCount: async (): Promise<UnreadCountResponse> => {
    const response = await api.get<UnreadCountResponse>('/notifications/unread-count');
    return response.data;
  },

  markAsRead: async (id: number): Promise<NotificationItem> => {
    const response = await api.patch<NotificationItem>(`/notifications/${id}/read`);
    return response.data;
  },

  markAllAsRead: async (): Promise<{ message: string; updatedCount: number }> => {
    const response = await api.patch<{ message: string; updatedCount: number }>('/notifications/read-all');
    return response.data;
  },
};

export default notificationService;
