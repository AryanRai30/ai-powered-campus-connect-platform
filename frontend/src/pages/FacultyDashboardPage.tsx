import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getFacultyDashboard, getFacultyStats } from '../services/facultyService';
import { FacultyDashboardResponse, FacultyDashboardStats } from '../types/faculty.types';
import { StatCard } from '../components/common/StatCard';
import { Badge } from '../components/common/Badge';
import { SkeletonLoader } from '../components/common/SkeletonLoader';
import {
  BookOpenIcon,
  MegaphoneIcon,
  CalendarIcon,
  BriefcaseIcon,
  UsersIcon,
  GraduationCapIcon,
  SparklesIcon,
  CheckCircleIcon,

} from '../components/common/Icons';

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
      title: 'Academic Resources',
      description: 'Create & manage study materials, lecture notes, and PDFs',
      icon: BookOpenIcon,
      count: stats?.resourceCount ?? 0,
      link: '/faculty/resources',
      buttonText: 'Manage Resources',
      color: 'blue' as const,
    },
    {
      title: 'Bulletins & Notices',
      description: 'Publish official academic notices and department updates',
      icon: MegaphoneIcon,
      count: stats?.announcementCount ?? 0,
      link: '/faculty/announcements',
      buttonText: 'Manage Bulletins',
      color: 'purple' as const,
    },
    {
      title: 'Campus Events',
      description: 'Schedule campus events and review student registrations',
      icon: CalendarIcon,
      count: stats?.eventCount ?? 0,
      link: '/faculty/events',
      buttonText: 'Manage Events',
      color: 'emerald' as const,
    },
    {
      title: 'Opportunities',
      description: 'Post internships, job listings, and career workshops',
      icon: BriefcaseIcon,
      count: stats?.opportunityCount ?? 0,
      link: '/faculty/opportunities',
      buttonText: 'Manage Opportunities',
      color: 'amber' as const,
    },
    {
      title: 'Supervised Clubs',
      description: 'Supervise campus student clubs and member rosters',
      icon: UsersIcon,
      count: stats?.clubCount ?? 0,
      link: '/faculty/clubs',
      buttonText: 'Manage Clubs',
      color: 'cyan' as const,
    },
  ];

  return (
    <div className="space-y-8 animate-fade-in">
      {/* Faculty Welcome Hero */}
      <div className="p-6 sm:p-8 rounded-3xl bg-gradient-to-r from-brand-600 via-brand-700 to-indigo-800 text-white shadow-xl relative overflow-hidden flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div className="absolute -top-12 -right-12 w-64 h-64 bg-white/10 rounded-full blur-2xl pointer-events-none" />
        
        <div className="space-y-2 relative z-10 max-w-xl">
          <div className="inline-flex items-center gap-2 px-3 py-1 bg-white/10 backdrop-blur-md rounded-full text-xs font-bold text-blue-100 uppercase tracking-wider">
            <SparklesIcon size={14} className="text-cyan-300" />
            <span>Faculty Management Portal</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight">
            Welcome, Professor {displayFirstName} {displayLastName} 👋
          </h1>
          <p className="text-blue-100 text-sm leading-relaxed font-medium">
            Manage academic resources, publish announcements, supervise clubs, schedule campus events, and oversee student participation.
          </p>
        </div>

        <div className="relative z-10 flex items-center gap-3 bg-white/10 backdrop-blur-md p-3.5 rounded-2xl border border-white/20">
          <div className="w-10 h-10 rounded-xl bg-white/20 flex items-center justify-center font-bold text-white text-lg shrink-0">
            <GraduationCapIcon size={22} />
          </div>
          <div className="text-xs">
            <span className="text-blue-100 font-medium block uppercase tracking-wider text-[10px]">Account Status</span>
            <div className="flex items-center gap-1.5 font-bold text-white mt-0.5">
              <CheckCircleIcon size={14} className="text-emerald-300" />
              <span>{displayStatus}</span>
            </div>
          </div>
        </div>
      </div>

      {error && (
        <div className="p-4 bg-rose-50 border border-rose-200 rounded-2xl text-rose-700 text-sm font-medium">
          {error}
        </div>
      )}

      {/* Account Info Bar */}
      <div className="bg-white rounded-2xl p-5 border border-slate-200/80 shadow-subtle">
        <h3 className="text-xs font-bold uppercase tracking-wider text-slate-500 mb-3">
          Faculty Credentials & Ownership Profile
        </h3>
        {loading ? (
          <SkeletonLoader type="table" count={1} />
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 text-xs">
            <div className="p-3 bg-slate-50 rounded-xl border border-slate-200/60">
              <span className="text-slate-400 font-medium block">Full Name</span>
              <span className="text-slate-900 font-bold truncate block">{displayFirstName} {displayLastName}</span>
            </div>
            <div className="p-3 bg-slate-50 rounded-xl border border-slate-200/60">
              <span className="text-slate-400 font-medium block">Faculty Email</span>
              <span className="text-slate-900 font-bold truncate block">{displayEmail}</span>
            </div>
            <div className="p-3 bg-slate-50 rounded-xl border border-slate-200/60">
              <span className="text-slate-400 font-medium block">Role Clearance</span>
              <Badge variant="published" size="sm">FACULTY MEMBER</Badge>
            </div>
          </div>
        )}
      </div>

      {/* Quick Action Shortcuts */}
      <div className="space-y-3">
        <h3 className="text-base font-bold text-slate-900">Quick Creation Actions</h3>
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3">
          <Link
            to="/faculty/resources"
            className="p-4 bg-white border border-slate-200/80 hover:border-brand-500 rounded-2xl text-center transition-all hover:shadow-card-hover group flex flex-col items-center justify-center gap-2"
          >
            <div className="p-2.5 rounded-xl bg-blue-50 text-brand-600 group-hover:scale-110 transition-transform">
              <BookOpenIcon size={20} />
            </div>
            <span className="text-xs font-bold text-slate-800 group-hover:text-brand-600">Resource</span>
          </Link>

          <Link
            to="/faculty/announcements"
            className="p-4 bg-white border border-slate-200/80 hover:border-purple-500 rounded-2xl text-center transition-all hover:shadow-card-hover group flex flex-col items-center justify-center gap-2"
          >
            <div className="p-2.5 rounded-xl bg-purple-50 text-purple-600 group-hover:scale-110 transition-transform">
              <MegaphoneIcon size={20} />
            </div>
            <span className="text-xs font-bold text-slate-800 group-hover:text-purple-600">Bulletin</span>
          </Link>

          <Link
            to="/faculty/events"
            className="p-4 bg-white border border-slate-200/80 hover:border-emerald-500 rounded-2xl text-center transition-all hover:shadow-card-hover group flex flex-col items-center justify-center gap-2"
          >
            <div className="p-2.5 rounded-xl bg-emerald-50 text-emerald-600 group-hover:scale-110 transition-transform">
              <CalendarIcon size={20} />
            </div>
            <span className="text-xs font-bold text-slate-800 group-hover:text-emerald-600">Event</span>
          </Link>

          <Link
            to="/faculty/opportunities"
            className="p-4 bg-white border border-slate-200/80 hover:border-amber-500 rounded-2xl text-center transition-all hover:shadow-card-hover group flex flex-col items-center justify-center gap-2"
          >
            <div className="p-2.5 rounded-xl bg-amber-50 text-amber-600 group-hover:scale-110 transition-transform">
              <BriefcaseIcon size={20} />
            </div>
            <span className="text-xs font-bold text-slate-800 group-hover:text-amber-600">Opportunity</span>
          </Link>

          <Link
            to="/faculty/clubs"
            className="p-4 bg-white border border-slate-200/80 hover:border-cyan-500 rounded-2xl text-center transition-all hover:shadow-card-hover group flex flex-col items-center justify-center gap-2"
          >
            <div className="p-2.5 rounded-xl bg-cyan-50 text-cyan-600 group-hover:scale-110 transition-transform">
              <UsersIcon size={20} />
            </div>
            <span className="text-xs font-bold text-slate-800 group-hover:text-cyan-600">Club</span>
          </Link>

          <Link
            to="/faculty/students"
            className="p-4 bg-white border border-slate-200/80 hover:border-indigo-500 rounded-2xl text-center transition-all hover:shadow-card-hover group flex flex-col items-center justify-center gap-2"
          >
            <div className="p-2.5 rounded-xl bg-indigo-50 text-indigo-600 group-hover:scale-110 transition-transform">
              <GraduationCapIcon size={20} />
            </div>
            <span className="text-xs font-bold text-slate-800 group-hover:text-indigo-600">Students</span>
          </Link>
        </div>
      </div>

      {/* Participation Stats Grid */}
      <div className="space-y-3">
        <h3 className="text-base font-bold text-slate-900">Student Engagement Overview</h3>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-5">
          <StatCard
            title="Event Registrations"
            value={stats?.totalEventRegistrations ?? 0}
            icon={CalendarIcon}
            color="emerald"
            description="Total registered students"
          />
          <StatCard
            title="Club Roster Members"
            value={stats?.totalClubMembers ?? 0}
            icon={UsersIcon}
            color="purple"
            description="Active club memberships"
          />
          <StatCard
            title="Opportunity Submissions"
            value={stats?.totalOpportunityApplications ?? 0}
            icon={BriefcaseIcon}
            color="amber"
            description="Submitted applications"
          />
        </div>
      </div>

      {/* Content Management Cards */}
      <div className="space-y-4">
        <div>
          <h3 className="text-base font-bold text-slate-900">Content Management Modules</h3>
          <p className="text-xs text-slate-500">
            Publish, edit, unpublish, and filter academic materials by department and course targets.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {contentManagementSections.map((item, index) => {
            const Icon = item.icon;
            return (
              <div
                key={index}
                className="bg-white rounded-2xl p-6 border border-slate-200/80 shadow-subtle hover:shadow-card-hover transition-all flex flex-col justify-between"
              >
                <div>
                  <div className="flex items-center justify-between mb-4">
                    <div className="p-3 rounded-xl bg-slate-50 text-slate-700 border border-slate-200/60">
                      <Icon size={24} />
                    </div>
                    <span className="text-xs font-extrabold px-3 py-1 rounded-full bg-slate-100 text-slate-700">
                      {item.count} Items
                    </span>
                  </div>
                  <h4 className="text-base font-bold text-slate-900 mb-1">{item.title}</h4>
                  <p className="text-xs text-slate-500 leading-relaxed mb-4">{item.description}</p>
                </div>

                <div className="pt-4 border-t border-slate-100">
                  <Link
                    to={item.link}
                    className="w-full py-2.5 px-4 bg-slate-50 hover:bg-slate-100 text-slate-700 font-semibold text-xs rounded-xl flex items-center justify-center gap-1.5 transition-colors border border-slate-200/60"
                  >
                    <span>{item.buttonText}</span>
                    <span>→</span>
                  </Link>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};

export default FacultyDashboardPage;
