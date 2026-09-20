import React from 'react';
import { NotificationType } from '../types/notification.types';
import {
  MegaphoneIcon,
  CalendarIcon,
  UsersIcon,
  BookOpenIcon,
  BriefcaseIcon,
  BellIcon,
  ShieldIcon,
  IconProps,
} from '../components/common/Icons';

export const getNotificationTypeLabel = (type: NotificationType): string => {
  switch (type) {
    case 'ANNOUNCEMENT':
      return 'Announcement';
    case 'EVENT':
      return 'Campus Event';
    case 'EVENT_REGISTRATION':
      return 'Event Registration';
    case 'CLUB':
      return 'Club Activity';
    case 'CLUB_MEMBERSHIP':
      return 'Club Membership';
    case 'RESOURCE':
      return 'Academic Resource';
    case 'OPPORTUNITY':
      return 'Career Opportunity';
    case 'OPPORTUNITY_APPLICATION':
      return 'Opportunity Application';
    case 'SYSTEM':
      return 'System';
    case 'ADMIN':
      return 'Admin';
    default:
      return 'Notification';
  }
};

export const getNotificationIcon = (type: NotificationType): React.FC<IconProps> => {
  switch (type) {
    case 'ANNOUNCEMENT':
      return MegaphoneIcon;
    case 'EVENT':
    case 'EVENT_REGISTRATION':
      return CalendarIcon;
    case 'CLUB':
    case 'CLUB_MEMBERSHIP':
      return UsersIcon;
    case 'RESOURCE':
      return BookOpenIcon;
    case 'OPPORTUNITY':
    case 'OPPORTUNITY_APPLICATION':
      return BriefcaseIcon;
    case 'ADMIN':
      return ShieldIcon;
    case 'SYSTEM':
    default:
      return BellIcon;
  }
};

export const getNotificationBadgeColor = (type: NotificationType): string => {
  switch (type) {
    case 'ANNOUNCEMENT':
      return 'bg-amber-100 text-amber-700 border-amber-200';
    case 'EVENT':
    case 'EVENT_REGISTRATION':
      return 'bg-purple-100 text-purple-700 border-purple-200';
    case 'CLUB':
    case 'CLUB_MEMBERSHIP':
      return 'bg-emerald-100 text-emerald-700 border-emerald-200';
    case 'RESOURCE':
      return 'bg-blue-100 text-blue-700 border-blue-200';
    case 'OPPORTUNITY':
    case 'OPPORTUNITY_APPLICATION':
      return 'bg-indigo-100 text-indigo-700 border-indigo-200';
    case 'ADMIN':
      return 'bg-rose-100 text-rose-700 border-rose-200';
    case 'SYSTEM':
    default:
      return 'bg-slate-100 text-slate-700 border-slate-200';
  }
};

export const formatRelativeTime = (dateString?: string): string => {
  if (!dateString) return '';
  const date = new Date(dateString);
  const now = new Date();
  const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);

  if (diffInSeconds < 60) {
    return 'Just now';
  }
  const diffInMinutes = Math.floor(diffInSeconds / 60);
  if (diffInMinutes < 60) {
    return `${diffInMinutes}m ago`;
  }
  const diffInHours = Math.floor(diffInMinutes / 60);
  if (diffInHours < 24) {
    return `${diffInHours}h ago`;
  }
  const diffInDays = Math.floor(diffInHours / 24);
  if (diffInDays < 7) {
    return `${diffInDays}d ago`;
  }
  return date.toLocaleDateString(undefined, {
    month: 'short',
    day: 'numeric',
    year: date.getFullYear() !== now.getFullYear() ? 'numeric' : undefined,
  });
};
