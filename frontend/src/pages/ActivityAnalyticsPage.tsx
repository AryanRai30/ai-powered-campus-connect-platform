import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  getStudentAnalytics,
  getFacultyAnalytics,
  getAdminAnalytics,
} from '../services/analyticsService';
import {
  StudentActivityAnalytics,
  FacultyActivityAnalytics,
  AdminPlatformAnalytics,
} from '../types/analytics.types';
import { StatCard } from '../components/common/StatCard';
import { Badge } from '../components/common/Badge';
import { SkeletonLoader } from '../components/common/SkeletonLoader';
import { EmptyState } from '../components/common/EmptyState';
import {
  CalendarIcon,
  UsersIcon,
  BriefcaseIcon,
  BookOpenIcon,
  MegaphoneIcon,
  GraduationCapIcon,
  SparklesIcon,
  ShieldIcon,
} from '../components/common/Icons';

export const ActivityAnalyticsPage: React.FC = () => {
  const { user } = useAuth();
  const userRoles = user?.roles || [];
  const isAdmin = userRoles.some((r) => ['ADMIN', 'SUPER_ADMIN', 'CLUB_ADMIN'].includes(r));
  const isFaculty = !isAdmin && userRoles.includes('FACULTY');
  const isStudent = !isAdmin && !isFaculty;

  const [studentData, setStudentData] = useState<StudentActivityAnalytics | null>(null);
  const [facultyData, setFacultyData] = useState<FacultyActivityAnalytics | null>(null);
  const [adminData, setAdminData] = useState<AdminPlatformAnalytics | null>(null);

  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const loadAnalytics = async () => {
    setLoading(true);
    setError(null);
    try {
      if (isAdmin) {
        const data = await getAdminAnalytics();
        setAdminData(data);
      } else if (isFaculty) {
        const data = await getFacultyAnalytics();
        setFacultyData(data);
      } else {
        const data = await getStudentAnalytics();
        setStudentData(data);
      }
    } catch (err: any) {
      console.error('Failed to load activity analytics:', err);
      setError('Unable to load analytics data from real system database.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAnalytics();
  }, [isAdmin, isFaculty]);

  const formatDate = (isoString?: string) => {
    if (!isoString) return 'N/A';
    try {
      return new Date(isoString).toLocaleDateString(undefined, {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return isoString;
    }
  };

  return (
    <div className="space-y-8 animate-fade-in">
      {/* Hero Banner */}
      <div className="p-6 sm:p-8 rounded-3xl bg-gradient-to-r from-brand-700 via-indigo-700 to-purple-800 text-white shadow-xl relative overflow-hidden flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div className="absolute -top-12 -right-12 w-64 h-64 bg-white/10 rounded-full blur-2xl pointer-events-none" />

        <div className="space-y-2 relative z-10 max-w-xl">
          <div className="inline-flex items-center gap-2 px-3 py-1 bg-white/10 backdrop-blur-md rounded-full text-xs font-bold text-blue-100 uppercase tracking-wider">
            <SparklesIcon size={14} className="text-cyan-300" />
            <span>
              {isAdmin ? 'System Platform Analytics' : isFaculty ? 'Faculty Content Analytics' : 'Student Activity Analytics'}
            </span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight">
            Activity & Performance Analytics
          </h1>
          <p className="text-blue-100 text-sm leading-relaxed font-medium">
            {isAdmin
              ? 'Real-time platform activity metrics aggregated across campus students, faculty, clubs, and resources.'
              : isFaculty
              ? 'Comprehensive analytics tracking your published resources, scheduled events, student registrations, and club rosters.'
              : 'Detailed breakdown of your campus engagements, event registrations, joined clubs, and career applications.'}
          </p>
        </div>

        <div className="relative z-10 shrink-0">
          <button
            onClick={loadAnalytics}
            disabled={loading}
            className="inline-flex items-center gap-2 px-4 py-2.5 bg-white hover:bg-slate-50 text-brand-700 font-bold text-xs rounded-xl transition-all shadow-md active:scale-95 disabled:opacity-50"
          >
            <span className={loading ? 'animate-spin' : ''}>🔄</span>
            <span>{loading ? 'Refreshing...' : 'Refresh Real Data'}</span>
          </button>
        </div>
      </div>

      {error && (
        <div className="p-4 bg-rose-50 border border-rose-200 rounded-2xl text-rose-700 text-sm font-medium text-center space-y-2">
          <p>{error}</p>
          <button
            onClick={loadAnalytics}
            className="px-4 py-1.5 bg-rose-600 text-white rounded-lg text-xs font-bold"
          >
            Retry
          </button>
        </div>
      )}

      {/* STUDENT ANALYTICS VIEW */}
      {isStudent && (
        <div className="space-y-8">
          {loading ? (
            <SkeletonLoader type="stat" count={4} />
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
              <StatCard
                title="Event Registrations"
                value={studentData?.totalEventRegistrations ?? 0}
                icon={CalendarIcon}
                color="blue"
                description="Events registered"
              />
              <StatCard
                title="Joined Clubs"
                value={studentData?.totalClubMemberships ?? 0}
                icon={UsersIcon}
                color="purple"
                description="Active memberships"
              />
              <StatCard
                title="Applications"
                value={studentData?.totalOpportunityApplications ?? 0}
                icon={BriefcaseIcon}
                color="emerald"
                description="Career applications"
              />
              <StatCard
                title="Accessible Resources"
                value={studentData?.totalAccessibleResources ?? 0}
                icon={BookOpenIcon}
                color="cyan"
                description="Department study materials"
              />
            </div>
          )}

          {/* Activity Timeline & Joined Clubs Grid */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Timeline */}
            <div className="bg-white rounded-2xl p-6 border border-slate-200/80 shadow-subtle space-y-4">
              <h3 className="text-base font-bold text-slate-900 border-b border-slate-100 pb-3 flex items-center gap-2">
                <SparklesIcon size={18} className="text-brand-600" />
                <span>Recent Activity Log</span>
              </h3>

              {loading ? (
                <SkeletonLoader type="table" count={3} />
              ) : !studentData?.recentActivities || studentData.recentActivities.length === 0 ? (
                <EmptyState
                  title="No Activity Yet"
                  description="Register for campus events, join clubs, or apply to opportunities to see your log."
                  icon={CalendarIcon}
                />
              ) : (
                <div className="space-y-3 max-h-96 overflow-y-auto pr-1">
                  {studentData.recentActivities.map((act, i) => (
                    <div
                      key={i}
                      className="p-3.5 bg-slate-50 rounded-xl border border-slate-200/60 flex items-start justify-between gap-3"
                    >
                      <div className="space-y-1">
                        <div className="flex items-center gap-2">
                          <Badge
                            variant={
                              act.type === 'EVENT_REGISTRATION'
                                ? 'info'
                                : act.type === 'CLUB_JOIN'
                                ? 'secondary'
                                : act.type === 'OPPORTUNITY_APPLY'
                                ? 'published'
                                : 'draft'
                            }
                            size="sm"
                          >
                            {act.type.replace('_', ' ')}
                          </Badge>
                          <span className="text-xs font-bold text-slate-900">{act.title}</span>
                        </div>
                        <p className="text-xs text-slate-500 font-medium">{act.description}</p>
                      </div>
                      <span className="text-[10px] font-semibold text-slate-400 whitespace-nowrap">
                        {formatDate(act.timestamp)}
                      </span>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Registered Events & Joined Clubs */}
            <div className="space-y-6">
              {/* Registered Events */}
              <div className="bg-white rounded-2xl p-6 border border-slate-200/80 shadow-subtle space-y-4">
                <div className="flex items-center justify-between border-b border-slate-100 pb-3">
                  <h3 className="text-base font-bold text-slate-900 flex items-center gap-2">
                    <CalendarIcon size={18} className="text-blue-600" />
                    <span>My Registered Events</span>
                  </h3>
                  <Link to="/events" className="text-xs font-bold text-brand-600 hover:underline">
                    Browse All →
                  </Link>
                </div>

                {!studentData?.registeredEvents || studentData.registeredEvents.length === 0 ? (
                  <p className="text-xs text-slate-400">No event registrations found.</p>
                ) : (
                  <div className="space-y-2.5">
                    {studentData.registeredEvents.slice(0, 4).map((ev) => (
                      <div key={ev.id} className="p-3 bg-slate-50 rounded-xl border border-slate-200/60 flex justify-between items-center">
                        <div>
                          <h4 className="text-xs font-bold text-slate-900">{ev.title}</h4>
                          <p className="text-[11px] text-slate-500">📍 {ev.venue} • 🗓️ {ev.eventDate}</p>
                        </div>
                        <Badge variant="published" size="sm">REGISTERED</Badge>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* Joined Clubs */}
              <div className="bg-white rounded-2xl p-6 border border-slate-200/80 shadow-subtle space-y-4">
                <div className="flex items-center justify-between border-b border-slate-100 pb-3">
                  <h3 className="text-base font-bold text-slate-900 flex items-center gap-2">
                    <UsersIcon size={18} className="text-purple-600" />
                    <span>My Joined Clubs</span>
                  </h3>
                  <Link to="/clubs" className="text-xs font-bold text-purple-600 hover:underline">
                    Explore Clubs →
                  </Link>
                </div>

                {!studentData?.joinedClubs || studentData.joinedClubs.length === 0 ? (
                  <p className="text-xs text-slate-400">No club memberships found.</p>
                ) : (
                  <div className="space-y-2.5">
                    {studentData.joinedClubs.slice(0, 4).map((club) => (
                      <div key={club.id} className="p-3 bg-slate-50 rounded-xl border border-slate-200/60 flex justify-between items-center">
                        <h4 className="text-xs font-bold text-slate-900">{club.name}</h4>
                        <Badge variant="secondary" size="sm">{club.category || 'Club'}</Badge>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* FACULTY ANALYTICS VIEW */}
      {isFaculty && (
        <div className="space-y-8">
          {loading ? (
            <SkeletonLoader type="stat" count={4} />
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
              <StatCard
                title="Published Resources"
                value={facultyData?.publishedResourcesCount ?? 0}
                icon={BookOpenIcon}
                color="blue"
                description="Study materials"
              />
              <StatCard
                title="Events Created"
                value={facultyData?.eventsCreatedCount ?? 0}
                icon={CalendarIcon}
                color="emerald"
                description="Campus events"
              />
              <StatCard
                title="Announcements"
                value={facultyData?.announcementsCreatedCount ?? 0}
                icon={MegaphoneIcon}
                color="purple"
                description="Posted notices"
              />
              <StatCard
                title="Opportunities"
                value={facultyData?.opportunitiesCreatedCount ?? 0}
                icon={BriefcaseIcon}
                color="amber"
                description="Career listings"
              />
            </div>
          )}

          {/* Received Student Engagement Summary */}
          <div className="bg-white rounded-2xl p-6 border border-slate-200/80 shadow-subtle space-y-4">
            <h3 className="text-base font-bold text-slate-900 border-b border-slate-100 pb-3 flex items-center gap-2">
              <GraduationCapIcon size={20} className="text-indigo-600" />
              <span>Student Engagement Received Across My Content</span>
            </h3>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 text-xs">
              <div className="p-4 bg-emerald-50 rounded-2xl border border-emerald-200/60 text-center">
                <span className="text-emerald-700 font-medium block">Total Event Registrations</span>
                <span className="text-2xl font-black text-emerald-900 mt-1 block">
                  {facultyData?.totalEventRegistrationsReceived ?? 0}
                </span>
              </div>
              <div className="p-4 bg-purple-50 rounded-2xl border border-purple-200/60 text-center">
                <span className="text-purple-700 font-medium block">Total Club Roster Members</span>
                <span className="text-2xl font-black text-purple-900 mt-1 block">
                  {facultyData?.totalClubMembersReceived ?? 0}
                </span>
              </div>
              <div className="p-4 bg-amber-50 rounded-2xl border border-amber-200/60 text-center">
                <span className="text-amber-700 font-medium block">Total Opportunity Applications</span>
                <span className="text-2xl font-black text-amber-900 mt-1 block">
                  {facultyData?.totalOpportunityApplicationsReceived ?? 0}
                </span>
              </div>
            </div>
          </div>

          {/* Event & Opportunity Engagement Breakdown Tables */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Event Engagement Breakdown */}
            <div className="bg-white rounded-2xl p-6 border border-slate-200/80 shadow-subtle space-y-4">
              <h3 className="text-base font-bold text-slate-900 border-b border-slate-100 pb-3 flex items-center gap-2">
                <CalendarIcon size={18} className="text-emerald-600" />
                <span>Event Registrations Breakdown</span>
              </h3>

              {!facultyData?.eventEngagementBreakdown || facultyData.eventEngagementBreakdown.length === 0 ? (
                <p className="text-xs text-slate-400">No events created yet.</p>
              ) : (
                <div className="space-y-2.5">
                  {facultyData.eventEngagementBreakdown.map((item) => (
                    <div key={item.id} className="p-3 bg-slate-50 rounded-xl border border-slate-200/60 flex items-center justify-between">
                      <div>
                        <h4 className="text-xs font-bold text-slate-900">{item.title}</h4>
                        <p className="text-[11px] text-slate-500">{item.subTitle}</p>
                      </div>
                      <Badge variant="published" size="sm">{item.totalEngagement} Registered</Badge>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Opportunity Applications Breakdown */}
            <div className="bg-white rounded-2xl p-6 border border-slate-200/80 shadow-subtle space-y-4">
              <h3 className="text-base font-bold text-slate-900 border-b border-slate-100 pb-3 flex items-center gap-2">
                <BriefcaseIcon size={18} className="text-amber-600" />
                <span>Opportunity Submissions Breakdown</span>
              </h3>

              {!facultyData?.opportunityEngagementBreakdown || facultyData.opportunityEngagementBreakdown.length === 0 ? (
                <p className="text-xs text-slate-400">No opportunities posted yet.</p>
              ) : (
                <div className="space-y-2.5">
                  {facultyData.opportunityEngagementBreakdown.map((item) => (
                    <div key={item.id} className="p-3 bg-slate-50 rounded-xl border border-slate-200/60 flex items-center justify-between">
                      <div>
                        <h4 className="text-xs font-bold text-slate-900">{item.title}</h4>
                        <p className="text-[11px] text-slate-500">{item.subTitle}</p>
                      </div>
                      <Badge variant="warning" size="sm">{item.totalEngagement} Applied</Badge>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* ADMIN ANALYTICS VIEW */}
      {isAdmin && (
        <div className="space-y-8">
          {loading ? (
            <SkeletonLoader type="stat" count={8} />
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
              <StatCard
                title="Total Students"
                value={adminData?.totalStudents ?? 0}
                icon={GraduationCapIcon}
                color="emerald"
                description="Active student accounts"
              />
              <StatCard
                title="Total Faculty"
                value={adminData?.totalFaculty ?? 0}
                icon={UsersIcon}
                color="blue"
                description="Verified faculty members"
              />
              <StatCard
                title="Total Admins"
                value={adminData?.totalAdmins ?? 0}
                icon={ShieldIcon}
                color="purple"
                description="System administrators"
              />
              <StatCard
                title="Published Resources"
                value={adminData?.publishedResourcesCount ?? 0}
                icon={BookOpenIcon}
                color="cyan"
                description="Study materials available"
              />
              <StatCard
                title="Total Events"
                value={adminData?.eventsCount ?? 0}
                icon={CalendarIcon}
                color="emerald"
                description={`Total registrations: ${adminData?.totalEventRegistrations ?? 0}`}
              />
              <StatCard
                title="Total Clubs"
                value={adminData?.clubsCount ?? 0}
                icon={UsersIcon}
                color="purple"
                description={`Total club members: ${adminData?.totalClubMemberships ?? 0}`}
              />
              <StatCard
                title="Total Opportunities"
                value={adminData?.opportunitiesCount ?? 0}
                icon={BriefcaseIcon}
                color="amber"
                description={`Total applications: ${adminData?.totalOpportunityApplications ?? 0}`}
              />
              <StatCard
                title="Total Bulletins"
                value={adminData?.announcementsCount ?? 0}
                icon={MegaphoneIcon}
                color="blue"
                description="Announcements published"
              />
            </div>
          )}

          {/* Department Distribution & Global Activity */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Department Student Distribution */}
            <div className="bg-white rounded-2xl p-6 border border-slate-200/80 shadow-subtle space-y-4">
              <h3 className="text-base font-bold text-slate-900 border-b border-slate-100 pb-3 flex items-center gap-2">
                <GraduationCapIcon size={18} className="text-emerald-600" />
                <span>Department Student Distribution</span>
              </h3>

              {!adminData?.departmentDistribution || adminData.departmentDistribution.length === 0 ? (
                <p className="text-xs text-slate-400">No student department data available.</p>
              ) : (
                <div className="space-y-3">
                  {adminData.departmentDistribution.map((dept, i) => (
                    <div key={i} className="p-3 bg-slate-50 rounded-xl border border-slate-200/60 flex justify-between items-center">
                      <span className="text-xs font-bold text-slate-900">{dept.department}</span>
                      <Badge variant="info" size="sm">{dept.studentCount} Students</Badge>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Global Recent Platform Activities */}
            <div className="bg-white rounded-2xl p-6 border border-slate-200/80 shadow-subtle space-y-4">
              <h3 className="text-base font-bold text-slate-900 border-b border-slate-100 pb-3 flex items-center gap-2">
                <SparklesIcon size={18} className="text-purple-600" />
                <span>Recent Platform Activity Timeline</span>
              </h3>

              {!adminData?.recentPlatformActivities || adminData.recentPlatformActivities.length === 0 ? (
                <p className="text-xs text-slate-400">No recent platform activities.</p>
              ) : (
                <div className="space-y-2.5 max-h-96 overflow-y-auto pr-1">
                  {adminData.recentPlatformActivities.map((act, i) => (
                    <div key={i} className="p-3 bg-slate-50 rounded-xl border border-slate-200/60 flex items-start justify-between gap-2">
                      <div>
                        <span className="text-xs font-bold text-slate-900 block">{act.title}</span>
                        <span className="text-[11px] text-slate-500 font-medium block">{act.description}</span>
                      </div>
                      <span className="text-[10px] text-slate-400 font-semibold shrink-0">{formatDate(act.timestamp)}</span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ActivityAnalyticsPage;
