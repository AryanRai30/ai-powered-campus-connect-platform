export type NotificationType =
  | 'ANNOUNCEMENT'
  | 'EVENT'
  | 'EVENT_REGISTRATION'
  | 'CLUB'
  | 'CLUB_MEMBERSHIP'
  | 'RESOURCE'
  | 'OPPORTUNITY'
  | 'OPPORTUNITY_APPLICATION'
  | 'SYSTEM'
  | 'ADMIN';

export interface NotificationItem {
  id: number;
  title: string;
  message: string;
  type: NotificationType;
  isRead: boolean;
  createdAt: string;
  readAt?: string | null;
  relatedEntityType?: string | null;
  relatedEntityId?: number | null;
  actionUrl?: string | null;
}

export interface UnreadCountResponse {
  unreadCount: number;
}
