import React, { useEffect, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { getAdminStats } from '../../services/adminService';
import { AdminStatsResponse } from '../../types/admin.types';

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
      icon: '🎓',
      accent: 'border-emerald-500/20 bg-emerald-500/5 text-emerald-400',
      badgeBg: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20',
      description: 'Active enrolled student accounts',
    },
    {
      title: 'Total Faculty',
      value: stats?.totalFaculty ?? 0,
      icon: '👨‍🏫',
      accent: 'border-cyan-500/20 bg-cyan-500/5 text-cyan-400',
      badgeBg: 'bg-cyan-500/10 text-cyan-400 border-cyan-500/20',
      description: 'Verified academic faculty members',
    },
    {
      title: 'Total Admins',
      value: stats?.totalAdmins ?? 0,
      icon: '🛡️',
      accent: 'border-amber-500/20 bg-amber-500/5 text-amber-400',
      badgeBg: 'bg-amber-500/10 text-amber-400 border-amber-500/20',
      description: 'System and club administrators',
    },
    {
      title: 'Total Events',
      value: stats?.totalEvents ?? 0,
      icon: '📅',
      accent: 'border-indigo-500/20 bg-indigo-500/5 text-indigo-400',
      badgeBg: 'bg-indigo-500/10 text-indigo-400 border-indigo-500/20',
      description: 'Campus activities & gatherings',
    },
    {
      title: 'Total Announcements',
      value: stats?.totalAnnouncements ?? 0,
      icon: '📢',
      accent: 'border-rose-500/20 bg-rose-500/5 text-rose-400',
      badgeBg: 'bg-rose-500/10 text-rose-400 border-rose-500/20',
      description: 'Published campus bulletins',
    },
    {
      title: 'Total Clubs',
      value: stats?.totalClubs ?? 0,
      icon: '🤝',
      accent: 'border-purple-500/20 bg-purple-500/5 text-purple-400',
      badgeBg: 'bg-purple-500/10 text-purple-400 border-purple-500/20',
      description: 'Registered student organizations',
    },
    {
      title: 'Academic Resources',
      value: stats?.totalAcademicResources ?? stats?.totalResources ?? 0,
      icon: '📚',
      accent: 'border-teal-500/20 bg-teal-500/5 text-teal-400',
      badgeBg: 'bg-teal-500/10 text-teal-400 border-teal-500/20',
      description: 'Course materials & study guides',
    },
    {
      title: 'Total Opportunities',
      value: stats?.totalOpportunities ?? 0,
      icon: '💼',
      accent: 'border-sky-500/20 bg-sky-500/5 text-sky-400',
      badgeBg: 'bg-sky-500/10 text-sky-400 border-sky-500/20',
      description: 'Internships & career listings',
    },
  ];

  return (
    <div className="space-y-8">
      {/* Header Banner */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-6 sm:p-8 backdrop-blur shadow-xl relative overflow-hidden">
        <div className="absolute top-0 right-0 -mt-8 -mr-8 w-64 h-64 bg-amber-500/5 rounded-full blur-3xl pointer-events-none"></div>
        <div className="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div>
            <div className="flex items-center space-x-3 mb-2">
              <span className="text-2xl">🛡️</span>
              <span className="px-3 py-1 bg-amber-500/10 text-amber-400 border border-amber-500/20 rounded-full text-xs font-semibold tracking-wider uppercase">
                System Administration
              </span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-bold text-white tracking-tight">
              Admin Overview & System Analytics
            </h1>
            <p className="text-slate-400 text-sm mt-1 max-w-2xl">
              Welcome back, <span className="text-slate-200 font-medium">{user?.firstName || 'Administrator'}</span>. Real-time operational metric counts aggregated live from system records.
            </p>
          </div>
          <div className="flex items-center space-x-3">
            <button
              onClick={fetchStats}
              disabled={loading}
              className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold rounded-xl border border-slate-700 transition-all flex items-center space-x-2 disabled:opacity-50"
            >
              <span className={loading ? 'animate-spin' : ''}>🔄</span>
              <span>{loading ? 'Refreshing...' : 'Refresh Metrics'}</span>
            </button>
          </div>
        </div>
      </div>

      {/* Error State */}
      {error && (
        <div className="bg-red-500/10 border border-red-500/30 rounded-2xl p-6 text-center max-w-2xl mx-auto shadow-lg">
          <div className="w-12 h-12 rounded-full bg-red-500/20 text-red-400 flex items-center justify-center mx-auto mb-3 font-bold text-xl">
            ⚠️
          </div>
          <h3 className="text-lg font-semibold text-red-200 mb-1">Failed to Load Dashboard</h3>
          <p className="text-slate-400 text-sm mb-4">{error}</p>
          <button
            onClick={fetchStats}
            className="px-4 py-2 bg-red-500/20 hover:bg-red-500/30 text-red-300 text-xs font-semibold rounded-lg transition-colors border border-red-500/30"
          >
            Retry Loading
          </button>
        </div>
      )}

      {/* Statistics Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        {loading
          ? Array.from({ length: 8 }).map((_, idx) => (
              <div
                key={idx}
                className="bg-slate-900/40 border border-slate-800/80 rounded-2xl p-6 animate-pulse space-y-4"
              >
                <div className="flex justify-between items-center">
                  <div className="w-24 h-4 bg-slate-800 rounded"></div>
                  <div className="w-8 h-8 bg-slate-800 rounded-lg"></div>
                </div>
                <div className="w-16 h-8 bg-slate-800 rounded"></div>
                <div className="w-32 h-3 bg-slate-800/60 rounded"></div>
              </div>
            ))
          : statCards.map((card, idx) => (
              <div
                key={idx}
                className={`border rounded-2xl p-6 backdrop-blur transition-all duration-200 hover:translate-y-[-2px] hover:shadow-lg ${card.accent}`}
              >
                <div className="flex justify-between items-start mb-4">
                  <span className="text-slate-400 text-xs font-semibold uppercase tracking-wider">
                    {card.title}
                  </span>
                  <div className={`w-9 h-9 rounded-xl flex items-center justify-center text-lg border ${card.badgeBg}`}>
                    {card.icon}
                  </div>
                </div>
                <div className="text-3xl font-extrabold text-white tracking-tight mb-2">
                  {card.value.toLocaleString()}
                </div>
                <p className="text-slate-400 text-xs leading-relaxed">
                  {card.description}
                </p>
              </div>
            ))}
      </div>
    </div>
  );
};

export default AdminDashboardPage;
