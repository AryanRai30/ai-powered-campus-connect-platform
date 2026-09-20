import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { NotificationItem } from '../types/notification.types';
import notificationService from '../services/notificationService';
import {
  getNotificationIcon,
  getNotificationTypeLabel,
  getNotificationBadgeColor,
  formatRelativeTime,
} from '../utils/notificationUtils';
import { PageHeader } from '../components/common/PageHeader';
import { EmptyState } from '../components/common/EmptyState';
import { SkeletonLoader } from '../components/common/SkeletonLoader';
import {
  BellIcon,
  CheckCircleIcon,
  ArrowRightIcon,
  SparklesIcon,
  FilterIcon,
} from '../components/common/Icons';

export const NotificationsPage: React.FC = () => {
  const navigate = useNavigate();
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'all' | 'unread'>('all');
  const [markingAll, setMarkingAll] = useState<boolean>(false);
  const [markingId, setMarkingId] = useState<number | null>(null);

  const fetchNotifications = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await notificationService.getNotifications();
      setNotifications(data || []);
    } catch (err) {
      setError('Unable to load notifications. Please check your connection and try again.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNotifications();
  }, []);

  const unreadCount = notifications.filter((n) => !n.isRead).length;

  const handleMarkAsRead = async (e: React.MouseEvent, item: NotificationItem) => {
    e.stopPropagation();
    if (item.isRead || markingId === item.id) return;
    setMarkingId(item.id);
    try {
      await notificationService.markAsRead(item.id);
      setNotifications((prev) =>
        prev.map((n) => (n.id === item.id ? { ...n, isRead: true } : n))
      );
    } catch (err) {
      // Safe fail
    } finally {
      setMarkingId(null);
    }
  };

  const handleMarkAllAsRead = async () => {
    if (markingAll || unreadCount === 0) return;
    setMarkingAll(true);
    try {
      await notificationService.markAllAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
    } catch (err) {
      // Safe fail
    } finally {
      setMarkingAll(false);
    }
  };

  const handleCardClick = async (item: NotificationItem) => {
    if (!item.isRead) {
      try {
        await notificationService.markAsRead(item.id);
        setNotifications((prev) =>
          prev.map((n) => (n.id === item.id ? { ...n, isRead: true } : n))
        );
      } catch (err) {
        // Safe fail
      }
    }

    if (item.actionUrl && item.actionUrl.trim() !== '') {
      const url = item.actionUrl.trim();
      if (url.startsWith('http://') || url.startsWith('https://')) {
        window.open(url, '_blank', 'noopener,noreferrer');
      } else {
        navigate(url);
      }
    }
  };

  const filteredNotifications = notifications.filter((n) => {
    if (activeTab === 'unread') return !n.isRead;
    return true;
  });

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <PageHeader
        title="Notification Center"
        subtitle="View, organize, and manage activity alerts across your campus account."
        action={
          unreadCount > 0
            ? {
                label: markingAll ? 'Marking read...' : 'Mark all as read',
                onClick: () => {
                  handleMarkAllAsRead();
                },
                icon: <CheckCircleIcon size={18} />,
              }
            : undefined
        }
      />

      {/* Filter Bar & Unread Badge */}
      <div className="bg-white rounded-2xl border border-slate-200/80 p-4 flex flex-col sm:flex-row items-center justify-between gap-4 shadow-subtle">
        <div className="flex items-center gap-2 w-full sm:w-auto">
          <button
            onClick={() => setActiveTab('all')}
            className={`px-4 py-2 rounded-xl text-xs font-bold transition-all flex items-center gap-2 ${
              activeTab === 'all'
                ? 'bg-brand-600 text-white shadow-sm'
                : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
            }`}
          >
            <span>All Notifications</span>
            <span className="px-2 py-0.5 rounded-full text-[10px] bg-white/20">
              {notifications.length}
            </span>
          </button>
          <button
            onClick={() => setActiveTab('unread')}
            className={`px-4 py-2 rounded-xl text-xs font-bold transition-all flex items-center gap-2 ${
              activeTab === 'unread'
                ? 'bg-brand-600 text-white shadow-sm'
                : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
            }`}
          >
            <span>Unread Only</span>
            {unreadCount > 0 && (
              <span className="px-2 py-0.5 rounded-full text-[10px] bg-rose-500 text-white font-extrabold">
                {unreadCount}
              </span>
            )}
          </button>
        </div>

        <div className="text-xs text-slate-400 font-medium flex items-center gap-1.5 self-end sm:self-center">
          <FilterIcon size={14} />
          <span>Showing {filteredNotifications.length} items</span>
        </div>
      </div>

      {/* Main List / Content */}
      {loading ? (
        <SkeletonLoader type="table" count={4} />
      ) : error ? (
        <div className="bg-white border border-rose-200 rounded-2xl p-8 text-center max-w-lg mx-auto shadow-subtle">
          <div className="w-12 h-12 bg-rose-50 text-rose-600 rounded-2xl flex items-center justify-center mx-auto mb-3">
            <BellIcon size={24} />
          </div>
          <h4 className="text-base font-bold text-slate-900 mb-1">Error Loading Notifications</h4>
          <p className="text-xs text-slate-500 mb-4">{error}</p>
          <button
            onClick={fetchNotifications}
            className="px-4 py-2 bg-brand-600 hover:bg-brand-700 text-white font-medium text-xs rounded-xl transition-all shadow-sm"
          >
            Retry
          </button>
        </div>
      ) : filteredNotifications.length === 0 ? (
        <EmptyState
          title={activeTab === 'unread' ? 'No unread notifications' : 'No notifications yet'}
          description={
            activeTab === 'unread'
              ? 'You are all caught up! Switch to All Notifications to view past alerts.'
              : 'New activity related to your campus account will appear here.'
          }
          icon={SparklesIcon}
        />
      ) : (
        <div className="space-y-3">
          {filteredNotifications.map((item) => {
            const Icon = getNotificationIcon(item.type);
            const typeLabel = getNotificationTypeLabel(item.type);
            const badgeColor = getNotificationBadgeColor(item.type);

            return (
              <div
                key={item.id}
                onClick={() => handleCardClick(item)}
                className={`bg-white rounded-2xl border transition-all duration-200 p-4 md:p-5 flex items-start gap-4 cursor-pointer shadow-subtle hover:shadow-md ${
                  !item.isRead
                    ? 'border-brand-300 bg-brand-50/20 border-l-4 border-l-brand-600'
                    : 'border-slate-200/80 hover:border-slate-300'
                }`}
              >
                {/* Icon */}
                <div
                  className={`w-10 h-10 rounded-xl shrink-0 flex items-center justify-center border ${badgeColor}`}
                >
                  <Icon size={20} />
                </div>

                {/* Details */}
                <div className="flex-grow min-w-0">
                  <div className="flex flex-wrap items-center justify-between gap-2 mb-1">
                    <div className="flex items-center gap-2">
                      <span
                        className={`px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wide border ${badgeColor}`}
                      >
                        {typeLabel}
                      </span>
                      {!item.isRead && (
                        <span className="px-2 py-0.5 rounded-full text-[10px] font-extrabold bg-brand-600 text-white">
                          NEW
                        </span>
                      )}
                    </div>
                    <span className="text-xs text-slate-400 font-medium">
                      {formatRelativeTime(item.createdAt)}
                    </span>
                  </div>

                  <h3
                    className={`text-sm text-slate-900 ${
                      !item.isRead ? 'font-extrabold' : 'font-semibold'
                    }`}
                  >
                    {item.title}
                  </h3>
                  <p className="text-xs text-slate-600 mt-1 leading-relaxed">{item.message}</p>
                </div>

                {/* Actions */}
                <div className="flex items-center gap-2 shrink-0 self-center">
                  {!item.isRead && (
                    <button
                      onClick={(e) => handleMarkAsRead(e, item)}
                      disabled={markingId === item.id}
                      className="p-2 text-slate-400 hover:text-brand-600 hover:bg-brand-50 rounded-xl transition-colors"
                      title="Mark as read"
                    >
                      <CheckCircleIcon size={18} />
                    </button>
                  )}
                  {item.actionUrl && (
                    <div className="p-2 text-slate-400 hover:text-slate-700 rounded-xl">
                      <ArrowRightIcon size={18} />
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default NotificationsPage;
