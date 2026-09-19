import React from 'react';
import { useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { MenuIcon, BellIcon } from '../common/Icons';

interface TopHeaderProps {
  onMobileOpen: () => void;
}

const getBreadcrumbTitle = (pathname: string): string => {
  if (pathname.includes('/faculty/dashboard')) return 'Faculty Portal';
  if (pathname.includes('/faculty/resources')) return 'Faculty Academic Resources';
  if (pathname.includes('/faculty/announcements')) return 'Faculty Bulletins & Announcements';
  if (pathname.includes('/faculty/events')) return 'Faculty Campus Events';
  if (pathname.includes('/faculty/opportunities')) return 'Faculty Career Opportunities';
  if (pathname.includes('/faculty/clubs')) return 'Faculty Student Clubs';
  if (pathname.includes('/faculty/students')) return 'Faculty Student Directory';

  if (pathname.includes('/admin/dashboard')) return 'Admin Dashboard';
  if (pathname.includes('/admin/faculty')) return 'Admin Faculty Management';
  if (pathname.includes('/admin/students')) return 'Admin Student Management';

  if (pathname.includes('/dashboard')) return 'Student Dashboard';
  if (pathname.includes('/resources')) return 'Academic Resources';
  if (pathname.includes('/announcements')) return 'Bulletins & Announcements';
  if (pathname.includes('/events')) return 'Campus Events';
  if (pathname.includes('/my-events')) return 'My Registered Events';
  if (pathname.includes('/opportunities')) return 'Career Opportunities';
  if (pathname.includes('/my-opportunities')) return 'My Opportunity Applications';
  if (pathname.includes('/clubs')) return 'Student Clubs & Organizations';
  if (pathname.includes('/my-clubs')) return 'My Club Memberships';
  if (pathname.includes('/profile')) return 'My Profile';

  return 'Campus Connect';
};

export const TopHeader: React.FC<TopHeaderProps> = ({ onMobileOpen }) => {
  const { user } = useAuth();
  const location = useLocation();
  const pageTitle = getBreadcrumbTitle(location.pathname);

  const userRoles = user?.roles || [];
  const isAdmin = userRoles.some((r) => ['ADMIN', 'SUPER_ADMIN', 'CLUB_ADMIN'].includes(r));
  const isFaculty = !isAdmin && userRoles.includes('FACULTY');
  const primaryRole = isAdmin ? 'ADMIN' : isFaculty ? 'FACULTY' : 'STUDENT';
  const fullName = `${user?.firstName || ''} ${user?.lastName || ''}`.trim() || 'User';

  const getRoleBadgeStyle = (role?: string) => {
    switch (role) {
      case 'ADMIN':
        return 'bg-purple-100 text-purple-700 border-purple-200';
      case 'FACULTY':
        return 'bg-blue-100 text-blue-700 border-blue-200';
      default:
        return 'bg-emerald-100 text-emerald-700 border-emerald-200';
    }
  };

  return (
    <header className="h-16 bg-white border-b border-slate-200/80 sticky top-0 z-30 px-4 md:px-8 flex items-center justify-between shadow-subtle">
      {/* Left side: Mobile Toggle & Page Title */}
      <div className="flex items-center gap-3">
        <button
          onClick={onMobileOpen}
          className="md:hidden p-2 text-slate-600 hover:text-slate-900 hover:bg-slate-100 rounded-xl transition-colors"
          aria-label="Open mobile menu"
        >
          <MenuIcon size={22} />
        </button>
        <div>
          <h2 className="text-base md:text-lg font-bold text-slate-900 tracking-tight">
            {pageTitle}
          </h2>
        </div>
      </div>

      {/* Right side: Role badge & User summary */}
      <div className="flex items-center gap-3">
        <span
          className={`hidden sm:inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold uppercase tracking-wide border ${getRoleBadgeStyle(
            primaryRole
          )}`}
        >
          {primaryRole}
        </span>

        {/* Notifications Icon */}
        <button
          className="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-xl transition-colors relative"
          title="Notifications"
        >
          <BellIcon size={20} />
          <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-brand-600 rounded-full ring-2 ring-white" />
        </button>

        {/* User Pill */}
        {user && (
          <div className="flex items-center gap-2.5 pl-2 border-l border-slate-200">
            <div className="w-8 h-8 rounded-full bg-gradient-to-tr from-brand-600 to-indigo-600 text-white font-bold text-xs flex items-center justify-center shadow-sm">
              {fullName ? fullName.charAt(0).toUpperCase() : 'U'}
            </div>
            <div className="hidden lg:flex flex-col text-left">
              <span className="text-xs font-bold text-slate-900 leading-tight">{fullName}</span>
              <span className="text-[10px] text-slate-400 font-medium">{primaryRole}</span>
            </div>
          </div>
        )}
      </div>
    </header>
  );
};
