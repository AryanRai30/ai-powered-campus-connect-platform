import React, { useEffect, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { getAdminStats } from '../../services/adminService';
import { AdminStatsResponse } from '../../types/admin.types';
import { StatCard } from '../../components/common/StatCard';
import { SkeletonLoader } from '../../components/common/SkeletonLoader';

import {
  ShieldIcon,
  GraduationCapIcon,
  UsersIcon,
  CalendarIcon,
  MegaphoneIcon,
  BookOpenIcon,
  BriefcaseIcon,
  SparklesIcon,
} from '../../components/common/Icons';

export const AdminDashboardPage: React.FC = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState<AdminStatsResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchStats = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getAdminStats();
      setStats(data);
    } catch (err: any) {
      const status = err?.response?.status;
      if (status === 403) {
        setError('Access Denied: You do not have administrator permissions to view system statistics.');
      } else if (status === 401) {
        setError('Authentication session expired. Please sign in again.');
      } else {
        setError('Unable to load administrator system statistics. Please check your network connection.');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStats();
  }, []);

  const statCards = [
    {
      title: 'Total Students',
      value: stats?.totalStudents ?? 0,
      icon: GraduationCapIcon,
      color: 'emerald' as const,
      description: 'Active student accounts',
    },
    {
      title: 'Total Faculty',
      value: stats?.totalFaculty ?? 0,
      icon: UsersIcon,
      color: 'blue' as const,
      description: 'Verified academic faculty',
    },
    {
      title: 'Total Admins',
      value: stats?.totalAdmins ?? 0,
      icon: ShieldIcon,
      color: 'purple' as const,
      description: 'System administrators',
    },
    {
      title: 'Total Events',
      value: stats?.totalEvents ?? 0,
      icon: CalendarIcon,
      color: 'cyan' as const,
      description: 'Campus events',
    },
    {
      title: 'Announcements',
      value: stats?.totalAnnouncements ?? 0,
      icon: MegaphoneIcon,
      color: 'purple' as const,
      description: 'Published bulletins',
    },
    {
      title: 'Total Clubs',
      value: stats?.totalClubs ?? 0,
      icon: UsersIcon,
      color: 'rose' as const,
      description: 'Student organizations',
    },
    {
      title: 'Study Resources',
      value: stats?.totalAcademicResources ?? stats?.totalResources ?? 0,
      icon: BookOpenIcon,
      color: 'blue' as const,
      description: 'Uploaded course materials',
    },
    {
      title: 'Opportunities',
      value: stats?.totalOpportunities ?? 0,
      icon: BriefcaseIcon,
      color: 'amber' as const,
      description: 'Internships & career listings',
    },
  ];

  return (
    <div className="space-y-8 animate-fade-in">
      {/* Admin Hero Banner */}
      <div className="p-6 sm:p-8 rounded-3xl bg-gradient-to-r from-purple-700 via-indigo-700 to-brand-800 text-white shadow-xl relative overflow-hidden flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div className="absolute -top-12 -right-12 w-64 h-64 bg-white/10 rounded-full blur-2xl pointer-events-none" />
        
        <div className="space-y-2 relative z-10 max-w-xl">
          <div className="inline-flex items-center gap-2 px-3 py-1 bg-white/10 backdrop-blur-md rounded-full text-xs font-bold text-purple-100 uppercase tracking-wider">
            <SparklesIcon size={14} className="text-amber-300" />
            <span>System Administration Center</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight">
            Admin Overview & System Analytics
          </h1>
          <p className="text-purple-100 text-sm leading-relaxed font-medium">
            Welcome back, <span className="text-white font-bold">{user?.firstName || 'Administrator'}</span>. Live platform analytics aggregated across all campus entities.
          </p>
        </div>

        <div className="relative z-10 shrink-0">
          <button
            onClick={fetchStats}
            disabled={loading}
            className="inline-flex items-center gap-2 px-4 py-2.5 bg-white hover:bg-slate-50 text-purple-700 font-bold text-xs rounded-xl transition-all shadow-md active:scale-95 disabled:opacity-50"
          >
            <span className={loading ? 'animate-spin' : ''}>🔄</span>
            <span>{loading ? 'Refreshing...' : 'Refresh Metrics'}</span>
          </button>
        </div>
      </div>

      {/* Error Alert */}
      {error && (
        <div className="p-5 bg-rose-50 border border-rose-200 rounded-2xl text-rose-700 text-sm font-medium text-center space-y-2">
          <p>{error}</p>
          <button
            onClick={fetchStats}
            className="px-4 py-1.5 bg-rose-600 text-white rounded-lg text-xs font-bold"
          >
            Retry Loading
          </button>
        </div>
      )}

      {/* System Statistics Grid */}
      {loading ? (
        <SkeletonLoader type="stat" count={8} />
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
          {statCards.map((card, idx) => (
            <StatCard
              key={idx}
              title={card.title}
              value={card.value.toLocaleString()}
              icon={card.icon}
              color={card.color}
              description={card.description}
            />
          ))}
        </div>
      )}
    </div>
  );
};

export default AdminDashboardPage;
