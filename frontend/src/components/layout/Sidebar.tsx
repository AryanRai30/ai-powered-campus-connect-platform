import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { IconProps } from '../common/Icons';
import {
  DashboardIcon,
  BookOpenIcon,
  MegaphoneIcon,
  CalendarIcon,
  BriefcaseIcon,
  UsersIcon,
  UserIcon,
  LogOutIcon,
  ChevronLeftIcon,
  ChevronRightIcon,
  GraduationCapIcon,
} from '../common/Icons';

interface SidebarProps {
  collapsed: boolean;
  onToggleCollapse: () => void;
  onMobileClose?: () => void;
}

interface NavItem {
  label: string;
  path: string;
  icon: React.FC<IconProps>;
}

export const Sidebar: React.FC<SidebarProps> = ({
  collapsed,
  onToggleCollapse,
  onMobileClose,
}) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const userRoles = user?.roles || [];
  const isAdmin = userRoles.some((r) => ['ADMIN', 'SUPER_ADMIN', 'CLUB_ADMIN'].includes(r));
  const isFaculty = !isAdmin && userRoles.includes('FACULTY');
  const isStudent = !isAdmin && !isFaculty;

  const primaryRole = isAdmin ? 'ADMIN' : isFaculty ? 'FACULTY' : 'STUDENT';
  const fullName = `${user?.firstName || ''} ${user?.lastName || ''}`.trim() || 'User';

  let navItems: NavItem[] = [];

  if (isStudent) {
    navItems = [
      { label: 'Dashboard', path: '/dashboard', icon: DashboardIcon },
      { label: 'Resources', path: '/resources', icon: BookOpenIcon },
      { label: 'Bulletins', path: '/announcements', icon: MegaphoneIcon },
      { label: 'Events', path: '/events', icon: CalendarIcon },
      { label: 'Opportunities', path: '/opportunities', icon: BriefcaseIcon },
      { label: 'Clubs', path: '/clubs', icon: UsersIcon },
    ];
  } else if (isFaculty) {
    navItems = [
      { label: 'Dashboard', path: '/faculty/dashboard', icon: DashboardIcon },
      { label: 'Resources', path: '/faculty/resources', icon: BookOpenIcon },
      { label: 'Bulletins', path: '/faculty/announcements', icon: MegaphoneIcon },
      { label: 'Events', path: '/faculty/events', icon: CalendarIcon },
      { label: 'Opportunities', path: '/faculty/opportunities', icon: BriefcaseIcon },
      { label: 'Clubs', path: '/faculty/clubs', icon: UsersIcon },
      { label: 'Students', path: '/faculty/students', icon: GraduationCapIcon },
    ];
  } else if (isAdmin) {
    navItems = [
      { label: 'Dashboard', path: '/admin/dashboard', icon: DashboardIcon },
      { label: 'Faculty', path: '/admin/faculty', icon: UsersIcon },
      { label: 'Students', path: '/admin/students', icon: GraduationCapIcon },
      { label: 'Profile', path: '/profile', icon: UserIcon },
    ];
  }

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <aside
      className={`h-full bg-white border-r border-slate-200/80 shadow-subtle flex flex-col justify-between transition-all duration-300 relative select-none ${
        collapsed ? 'w-20' : 'w-64'
      }`}
    >
      {/* Top Branding Section */}
      <div>
        <div className="h-16 flex items-center justify-between px-4 border-b border-slate-100">
          <div className="flex items-center gap-3 overflow-hidden">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-brand-600 to-indigo-600 flex items-center justify-center text-white font-black text-lg shadow-sm shrink-0">
              CC
            </div>
            {!collapsed && (
              <div className="flex flex-col truncate">
                <span className="font-extrabold text-slate-900 tracking-tight text-base leading-tight">
                  Campus<span className="text-brand-600">Connect</span>
                </span>
                <span className="text-[10px] font-semibold tracking-wider uppercase text-slate-400">
                  {primaryRole}
                </span>
              </div>
            )}
          </div>

          {/* Collapse Button */}
          <button
            onClick={onToggleCollapse}
            className="hidden md:flex p-1.5 rounded-lg text-slate-400 hover:text-slate-700 hover:bg-slate-100 transition-colors"
            title={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
          >
            {collapsed ? <ChevronRightIcon size={18} /> : <ChevronLeftIcon size={18} />}
          </button>
        </div>

        {/* Navigation Items */}
        <nav className="p-3 space-y-1 overflow-y-auto max-h-[calc(100vh-140px)]">
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.path}
                to={item.path}
                onClick={onMobileClose}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-3 py-2.5 rounded-xl font-medium text-sm transition-all duration-150 ${
                    isActive
                      ? 'bg-brand-50 text-brand-600 font-semibold shadow-2xs'
                      : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'
                  } ${collapsed ? 'justify-center px-0' : ''}`
                }
                title={collapsed ? item.label : undefined}
              >
                <Icon size={20} className="shrink-0" />
                {!collapsed && <span className="truncate">{item.label}</span>}
              </NavLink>
            );
          })}
        </nav>
      </div>

      {/* Bottom Profile & Logout Footer */}
      <div className="p-3 border-t border-slate-100 space-y-1">
        <NavLink
          to="/profile"
          onClick={onMobileClose}
          className={({ isActive }) =>
            `flex items-center gap-3 px-3 py-2.5 rounded-xl font-medium text-sm transition-all duration-150 ${
              isActive
                ? 'bg-brand-50 text-brand-600 font-semibold'
                : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'
            } ${collapsed ? 'justify-center px-0' : ''}`
          }
          title={collapsed ? 'Profile' : undefined}
        >
          <UserIcon size={20} className="shrink-0" />
          {!collapsed && <span className="truncate">Profile</span>}
        </NavLink>

        <button
          onClick={handleLogout}
          className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-xl font-medium text-sm text-rose-600 hover:bg-rose-50 hover:text-rose-700 transition-all duration-150 ${
            collapsed ? 'justify-center px-0' : ''
          }`}
          title={collapsed ? 'Logout' : undefined}
        >
          <LogOutIcon size={20} className="shrink-0" />
          {!collapsed && <span className="truncate">Logout</span>}
        </button>

        {!collapsed && user && (
          <div className="mt-3 pt-3 border-t border-slate-100 flex items-center gap-3 px-2">
            <div className="w-8 h-8 rounded-full bg-slate-100 border border-slate-200 flex items-center justify-center font-bold text-xs text-brand-600 shrink-0">
              {fullName ? fullName.charAt(0).toUpperCase() : 'U'}
            </div>
            <div className="flex flex-col truncate">
              <span className="text-xs font-semibold text-slate-800 truncate">{fullName}</span>
              <span className="text-[10px] text-slate-400 truncate">{user.email}</span>
            </div>
          </div>
        )}
      </div>
    </aside>
  );
};
