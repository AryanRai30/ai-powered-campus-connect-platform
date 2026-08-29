import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getFacultyDashboard, getFacultyStats } from '../services/facultyService';
import { FacultyDashboardResponse, FacultyDashboardStats } from '../types/faculty.types';

export const FacultyDashboardPage: React.FC = () => {
  const { user } = useAuth();
  const [facultyInfo, setFacultyInfo] = useState<FacultyDashboardResponse | null>(null);
  const [stats, setStats] = useState<FacultyDashboardStats | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchDashboard = async () => {
      try {
        setLoading(true);
        const [dashData, statsData] = await Promise.all([
          getFacultyDashboard(),
          getFacultyStats(),
        ]);
        setFacultyInfo(dashData);
        setStats(statsData);
      } catch (err: any) {
        console.error('Failed to load faculty dashboard data:', err);
        setError('Failed to fetch faculty profile details from server.');
      } finally {
        setLoading(false);
      }
    };

    fetchDashboard();
  }, []);

  const displayFirstName = facultyInfo?.firstName || user?.firstName || 'Faculty';
  const displayLastName = facultyInfo?.lastName || user?.lastName || 'Member';
  const displayEmail = facultyInfo?.email || user?.email || 'N/A';
  const displayStatus = facultyInfo?.status || 'ACTIVE';

  const contentManagementSections = [
    {
      title: 'My Resources',
      description: 'Create & manage study materials, lecture notes, and PDFs',
      icon: '📚',
      count: stats?.resourceCount ?? 0,
      link: '/faculty/resources',
      buttonText: 'Manage Resources',
    },
    {
      title: 'My Announcements',
      description: 'Publish official academic notices and department updates',
      icon: '📢',
      count: stats?.announcementCount ?? 0,
      link: '/faculty/announcements',
      buttonText: 'Manage Bulletins',
    },
    {
      title: 'My Events',
      description: 'Schedule campus events and review student registrations',
      icon: '📅',
      count: stats?.eventCount ?? 0,
      link: '/faculty/events',
      buttonText: 'Manage Events',
    },
    {
      title: 'My Opportunities',
      description: 'Post internships, job listings, and career workshops',
      icon: '💼',
      count: stats?.opportunityCount ?? 0,
      link: '/faculty/opportunities',
      buttonText: 'Manage Opportunities',
    },
    {
      title: 'My Clubs',
      description: 'Supervise campus student clubs and member rosters',
      icon: '🤝',
      count: stats?.clubCount ?? 0,
      link: '/faculty/clubs',
      buttonText: 'Manage Clubs',
    },
  ];

  return (
    <div className="space-y-8 max-w-7xl mx-auto">
      {/* Header Banner */}
      <div className="bg-gradient-to-r from-slate-900 via-emerald-950/40 to-slate-900 border border-emerald-500/20 rounded-2xl p-6 sm:p-8 shadow-xl relative overflow-hidden">
        <div className="absolute right-0 top-0 translate-x-4 -translate-y-4 w-64 h-64 bg-emerald-500/5 rounded-full blur-3xl pointer-events-none"></div>
        <div className="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div>
            <div className="inline-flex items-center space-x-2 px-3 py-1 bg-emerald-500/10 border border-emerald-500/30 rounded-full text-emerald-400 text-xs font-semibold uppercase tracking-wider mb-3">
              <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
              <span>Faculty Portal</span>
            </div>
            <h1 className="text-3xl font-extrabold text-slate-100 tracking-tight">
              Welcome, {displayFirstName} {displayLastName}
            </h1>
            <p className="text-slate-400 text-sm mt-1">
              Authorized Faculty Content Management Portal
            </p>
          </div>

          <div className="bg-slate-900/80 border border-slate-800 rounded-xl p-4 flex items-center space-x-4 backdrop-blur">
            <div className="w-12 h-12 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 flex items-center justify-center font-bold text-xl">
              🎓
            </div>
            <div>
              <div className="text-xs text-slate-400 uppercase tracking-wider font-semibold">Account Status</div>
              <div className="flex items-center space-x-2 text-sm font-medium text-slate-200">
                <span className="w-2 h-2 rounded-full bg-emerald-400"></span>
                <span>{displayStatus}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {error && (
        <div className="p-4 bg-red-500/10 border border-red-500/30 rounded-xl text-red-400 text-sm">
          {error}
        </div>
      )}

      {/* Account Info Card */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 shadow-lg">
        <h2 className="text-lg font-bold text-slate-100 mb-4 flex items-center space-x-2">
          <span>Faculty Profile</span>
        </h2>
        {loading ? (
          <div className="flex items-center space-x-3 text-slate-400 py-4 text-sm">
            <div className="w-5 h-5 border-2 border-emerald-500/30 border-t-emerald-500 rounded-full animate-spin"></div>
            <span>Loading faculty information...</span>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div className="bg-slate-950/60 border border-slate-800/80 p-4 rounded-lg">
              <div className="text-xs text-slate-400 uppercase font-semibold">Full Name</div>
              <div className="text-base font-semibold text-slate-200 mt-1">
                {displayFirstName} {displayLastName}
              </div>
            </div>
            <div className="bg-slate-950/60 border border-slate-800/80 p-4 rounded-lg">
              <div className="text-xs text-slate-400 uppercase font-semibold">Email</div>
              <div className="text-base font-semibold text-slate-200 mt-1">
                {displayEmail}
              </div>
            </div>
            <div className="bg-slate-950/60 border border-slate-800/80 p-4 rounded-lg">
              <div className="text-xs text-slate-400 uppercase font-semibold">Role</div>
              <div className="text-base font-semibold text-emerald-400 mt-1">
                Faculty
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Quick Actions & Navigation */}
      <div className="space-y-4">
        <h2 className="text-xl font-bold text-slate-100">Quick Actions</h2>
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-6 gap-3">
          <Link
            to="/faculty/resources"
            className="p-3 bg-slate-900 border border-slate-800 hover:border-emerald-500/50 rounded-xl text-center transition-colors group"
          >
            <div className="text-2xl mb-1 group-hover:scale-110 transition-transform">📚</div>
            <div className="text-xs font-semibold text-slate-200">Create Resource</div>
          </Link>

          <Link
            to="/faculty/announcements"
            className="p-3 bg-slate-900 border border-slate-800 hover:border-emerald-500/50 rounded-xl text-center transition-colors group"
          >
            <div className="text-2xl mb-1 group-hover:scale-110 transition-transform">📢</div>
            <div className="text-xs font-semibold text-slate-200">Create Bulletin</div>
          </Link>

          <Link
            to="/faculty/events"
            className="p-3 bg-slate-900 border border-slate-800 hover:border-emerald-500/50 rounded-xl text-center transition-colors group"
          >
            <div className="text-2xl mb-1 group-hover:scale-110 transition-transform">📅</div>
            <div className="text-xs font-semibold text-slate-200">Create Event</div>
          </Link>

          <Link
            to="/faculty/opportunities"
            className="p-3 bg-slate-900 border border-slate-800 hover:border-emerald-500/50 rounded-xl text-center transition-colors group"
          >
            <div className="text-2xl mb-1 group-hover:scale-110 transition-transform">💼</div>
            <div className="text-xs font-semibold text-slate-200">Create Opportunity</div>
          </Link>

          <Link
            to="/faculty/clubs"
            className="p-3 bg-slate-900 border border-slate-800 hover:border-emerald-500/50 rounded-xl text-center transition-colors group"
          >
            <div className="text-2xl mb-1 group-hover:scale-110 transition-transform">🤝</div>
            <div className="text-xs font-semibold text-slate-200">Create Club</div>
          </Link>

          <Link
            to="/faculty/students"
            className="p-3 bg-slate-900 border border-slate-800 hover:border-emerald-500/50 rounded-xl text-center transition-colors group"
          >
            <div className="text-2xl mb-1 group-hover:scale-110 transition-transform">👥</div>
            <div className="text-xs font-semibold text-emerald-400">View Students</div>
          </Link>
        </div>
      </div>

      {/* Participation Stats */}
      <div className="space-y-4">
        <h2 className="text-xl font-bold text-slate-100">Student Participation Overview</h2>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-6">
          <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 shadow-lg flex items-center justify-between">
            <div>
              <div className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
                Event Registrations
              </div>
              <div className="text-3xl font-extrabold text-emerald-400 mt-2">
                {stats?.totalEventRegistrations ?? 0}
              </div>
              <div className="text-[11px] text-slate-500 mt-1">Across your published events</div>
            </div>
            <div className="text-4xl text-emerald-500/20">🎟️</div>
          </div>

          <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 shadow-lg flex items-center justify-between">
            <div>
              <div className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
                Club Members
              </div>
              <div className="text-3xl font-extrabold text-indigo-400 mt-2">
                {stats?.totalClubMembers ?? 0}
              </div>
              <div className="text-[11px] text-slate-500 mt-1">Across your supervised clubs</div>
            </div>
            <div className="text-4xl text-indigo-500/20">👥</div>
          </div>

          <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 shadow-lg flex items-center justify-between">
            <div>
              <div className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
                Opportunity Applications
              </div>
              <div className="text-3xl font-extrabold text-amber-400 mt-2">
                {stats?.totalOpportunityApplications ?? 0}
              </div>
              <div className="text-[11px] text-slate-500 mt-1">Submitted for your listings</div>
            </div>
            <div className="text-4xl text-amber-500/20">📝</div>
          </div>
        </div>
      </div>

      {/* Content Management Cards */}
      <div className="space-y-4">
        <div>
          <h2 className="text-xl font-bold text-slate-100">Content Management</h2>
          <p className="text-slate-400 text-sm">
            Create, publish, and target academic content for student audiences
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {contentManagementSections.map((item, index) => (
            <div
              key={index}
              className="bg-slate-900 border border-slate-800 hover:border-slate-700 rounded-xl p-6 relative overflow-hidden flex flex-col justify-between transition-all shadow-lg"
            >
              <div>
                <div className="flex items-center justify-between mb-3">
                  <span className="text-3xl">{item.icon}</span>
                  <span className="text-sm font-extrabold px-3 py-1 rounded-full bg-slate-950 text-slate-200 border border-slate-800">
                    {item.count} Items
                  </span>
                </div>
                <h3 className="text-lg font-bold text-slate-100 mb-1">
                  {item.title}
                </h3>
                <p className="text-slate-400 text-sm mb-4">
                  {item.description}
                </p>
              </div>

              <div className="pt-4 border-t border-slate-800">
                <Link
                  to={item.link}
                  className="w-full py-2.5 px-4 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-lg text-xs font-semibold flex items-center justify-center space-x-2 border border-slate-700 transition-colors"
                >
                  <span>{item.buttonText} &rarr;</span>
                </Link>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default FacultyDashboardPage;
