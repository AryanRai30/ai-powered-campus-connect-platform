import React, { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { NotificationItem } from '../../types/notification.types';
import notificationService from '../../services/notificationService';
import {
  getNotificationIcon,
  getNotificationTypeLabel,
  getNotificationBadgeColor,
  formatRelativeTime,
} from '../../utils/notificationUtils';
import { CheckCircleIcon, ArrowRightIcon, SparklesIcon } from '../common/Icons';

interface NotificationDropdownProps {
  isOpen: boolean;
  onClose: () => void;
  onNotificationsUpdated: () => void;
}

export const NotificationDropdown: React.FC<NotificationDropdownProps> = ({
  isOpen,
  onClose,
  onNotificationsUpdated,
}) => {
  const navigate = useNavigate();
  const dropdownRef = useRef<HTMLDivElement>(null);
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [markingAll, setMarkingAll] = useState<boolean>(false);

  const fetchNotifications = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await notificationService.getNotifications();
      setNotifications(data || []);
    } catch (err) {
      setError('Unable to load notifications.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isOpen) {
      fetchNotifications();
    }
  }, [isOpen]);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        onClose();
      }
    };

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        onClose();
      }
    };

    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside);
      document.addEventListener('keydown', handleKeyDown);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  const unreadCount = notifications.filter((n) => !n.isRead).length;

  const handleNotificationClick = async (item: NotificationItem) => {
    if (!item.isRead) {
      try {
        await notificationService.markAsRead(item.id);
        setNotifications((prev) =>
          prev.map((n) => (n.id === item.id ? { ...n, isRead: true } : n))
        );
        onNotificationsUpdated();
      } catch (err) {
        // Safe fail
      }
    }
    onClose();

    if (item.actionUrl && item.actionUrl.trim() !== '') {
      const url = item.actionUrl.trim();
      if (url.startsWith('http://') || url.startsWith('https://')) {
        window.open(url, '_blank', 'noopener,noreferrer');
      } else {
        navigate(url);
      }
    }
  };

  const handleMarkAllAsRead = async () => {
    if (markingAll || unreadCount === 0) return;
    setMarkingAll(true);
    try {
      await notificationService.markAllAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
      onNotificationsUpdated();
    } catch (err) {
      // Safe fail
    } finally {
      setMarkingAll(false);
    }
  };

  const handleViewAll = () => {
    onClose();
    navigate('/notifications');
  };

  // Show top 6 items in dropdown
  const recentItems = notifications.slice(0, 6);

  return (
    <div
      ref={dropdownRef}
      className="absolute right-0 top-full mt-2 w-80 sm:w-96 max-w-[calc(100vw-2rem)] bg-white rounded-2xl shadow-xl border border-slate-200/90 z-50 overflow-hidden animate-fade-in"
      role="dialog"
      aria-label="Notifications Dropdown"
    >
      {/* Dropdown Header */}
      <div className="px-4 py-3 bg-slate-50/80 border-b border-slate-100 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <h3 className="text-sm font-bold text-slate-900">Notifications</h3>
          {unreadCount > 0 && (
            <span className="px-2 py-0.5 text-[10px] font-extrabold bg-brand-100 text-brand-700 rounded-full">
              {unreadCount} new
            </span>
          )}
        </div>
        {unreadCount > 0 && (
          <button
            onClick={handleMarkAllAsRead}
            disabled={markingAll}
            className="text-xs font-semibold text-brand-600 hover:text-brand-700 hover:underline disabled:opacity-50 flex items-center gap-1"
          >
            <CheckCircleIcon size={14} />
            <span>Mark all read</span>
          </button>
        )}
      </div>

      {/* Content area */}
      <div className="max-h-80 overflow-y-auto divide-y divide-slate-100">
        {loading ? (
          <div className="p-6 text-center space-y-3">
            <div className="w-6 h-6 border-2 border-brand-600 border-t-transparent rounded-full animate-spin mx-auto" />
            <p className="text-xs text-slate-400 font-medium">Loading notifications...</p>
          </div>
        ) : error ? (
          <div className="p-6 text-center text-xs text-slate-500">
            <p className="font-semibold text-rose-600 mb-1">{error}</p>
            <button
              onClick={fetchNotifications}
              className="text-brand-600 font-semibold hover:underline mt-1"
            >
              Retry
            </button>
          </div>
        ) : recentItems.length === 0 ? (
          <div className="p-8 text-center space-y-2">
            <div className="w-10 h-10 bg-slate-100 text-slate-400 rounded-xl flex items-center justify-center mx-auto">
              <SparklesIcon size={20} />
            </div>
            <h4 className="text-sm font-bold text-slate-800">No notifications yet</h4>
            <p className="text-xs text-slate-400 max-w-xs mx-auto">
              New activity related to your campus account will appear here.
            </p>
          </div>
        ) : (
          recentItems.map((item) => {
            const Icon = getNotificationIcon(item.type);
            const typeLabel = getNotificationTypeLabel(item.type);
            const badgeColor = getNotificationBadgeColor(item.type);

            return (
              <button
                key={item.id}
                onClick={() => handleNotificationClick(item)}
                className={`w-full text-left p-3.5 transition-colors flex items-start gap-3 hover:bg-slate-50 ${
                  !item.isRead ? 'bg-brand-50/40' : ''
                }`}
              >
                <div
                  className={`w-8 h-8 rounded-xl shrink-0 flex items-center justify-center border ${badgeColor}`}
                >
                  <Icon size={16} />
                </div>
                <div className="flex-grow min-w-0">
                  <div className="flex items-center justify-between gap-2 mb-0.5">
                    <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400 truncate">
                      {typeLabel}
                    </span>
                    <span className="text-[10px] text-slate-400 shrink-0">
                      {formatRelativeTime(item.createdAt)}
                    </span>
                  </div>
                  <h4
                    className={`text-xs font-semibold text-slate-900 truncate ${
                      !item.isRead ? 'font-bold' : ''
                    }`}
                  >
                    {item.title}
                  </h4>
                  <p className="text-xs text-slate-500 line-clamp-2 mt-0.5 leading-relaxed">
                    {item.message}
                  </p>
                </div>
                {!item.isRead && (
                  <span
                    className="w-2 h-2 rounded-full bg-brand-600 shrink-0 mt-1.5"
                    title="Unread"
                  />
                )}
              </button>
            );
          })
        )}
      </div>

      {/* Dropdown Footer */}
      <div className="p-2 bg-slate-50/80 border-t border-slate-100 text-center">
        <button
          onClick={handleViewAll}
          className="w-full py-2 px-3 text-xs font-bold text-brand-600 hover:text-brand-700 hover:bg-brand-50 rounded-xl transition-colors flex items-center justify-center gap-1.5"
        >
          <span>View all notifications</span>
          <ArrowRightIcon size={14} />
        </button>
      </div>
    </div>
  );
};

export default NotificationDropdown;
