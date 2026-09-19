import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { studentProfileService } from '../services/studentProfileService';
import {
  fetchEvents,
  fetchAnnouncements,
  fetchMyClubs,
  fetchAcademicResources,
  fetchOpportunities,
  fetchMyBookmarks,
  fetchMyApplications,
} from '../services/campusService';
import { StudentProfileResponse } from '../types/studentProfile.types';
import {
  EventItem,
  AnnouncementItem,
  ClubItem,
  AcademicResourceItem,
} from '../types/campus.types';
import { StatCard } from '../components/common/StatCard';
import { Badge } from '../components/common/Badge';
import { EmptyState } from '../components/common/EmptyState';
import { SkeletonLoader } from '../components/common/SkeletonLoader';

import {
  CalendarIcon,
  BookOpenIcon,
  UsersIcon,
  BriefcaseIcon,
  MegaphoneIcon,
  UserIcon,
  ArrowRightIcon,
  SparklesIcon,
  GraduationCapIcon,
} from '../components/common/Icons';

export const DashboardPage: React.FC = () => {
  const { user } = useAuth();

  // Individual Section States
  const [profile, setProfile] = useState<StudentProfileResponse | null>(null);
  const [profileLoading, setProfileLoading] = useState<boolean>(true);
  const [profileError, setProfileError] = useState<string | null>(null);

  const [events, setEvents] = useState<EventItem[]>([]);
  const [eventsLoading, setEventsLoading] = useState<boolean>(true);
  const [eventsError, setEventsError] = useState<string | null>(null);

  const [announcements, setAnnouncements] = useState<AnnouncementItem[]>([]);
  const [announcementsLoading, setAnnouncementsLoading] = useState<boolean>(true);
  const [announcementsError, setAnnouncementsError] = useState<string | null>(null);

  const [myClubs, setMyClubs] = useState<ClubItem[]>([]);
  const [clubsLoading, setClubsLoading] = useState<boolean>(true);
  const [clubsError, setClubsError] = useState<string | null>(null);

  const [resources, setResources] = useState<AcademicResourceItem[]>([]);
  const [resourcesLoading, setResourcesLoading] = useState<boolean>(true);
  const [resourcesError, setResourcesError] = useState<string | null>(null);

  const [oppStats, setOppStats] = useState<{ total: number; bookmarked: number; applied: number }>({
    total: 0,
    bookmarked: 0,
    applied: 0,
  });
  const [oppLoading, setOppLoading] = useState<boolean>(true);

  // Profile completion calculation
  const calculateProfileCompletion = (prof: StudentProfileResponse | null): number => {
    if (!prof) return 0;
    let score = 0;
    if (prof.studentId && prof.studentId.trim() !== '') score += 15;
    if (prof.course && prof.course.trim() !== '') score += 15;
    if (prof.department && prof.department.trim() !== '') score += 15;
    if (prof.year && prof.year.trim() !== '') score += 15;
    if (prof.semester && prof.semester.trim() !== '') score += 15;
    if (prof.skills && prof.skills.trim() !== '') score += 9;
    if (prof.interests && prof.interests.trim() !== '') score += 8;
    if (prof.bio && prof.bio.trim() !== '') score += 8;
    return score;
  };

  useEffect(() => {
    const loadProfile = async () => {
      setProfileLoading(true);
      setProfileError(null);
      try {
        const data = await studentProfileService.getProfile();
        setProfile(data);
      } catch (err: any) {
        if (err.response && err.response.status === 404) {
          setProfile(null);
        } else {
          setProfileError('Unable to load profile data.');
        }
      } finally {
        setProfileLoading(false);
      }
    };

    const loadEvents = async () => {
      setEventsLoading(true);
      setEventsError(null);
      try {
        const data = await fetchEvents();
        setEvents(data.slice(0, 3));
      } catch (err: any) {
        setEventsError('Unable to load upcoming events.');
      } finally {
        setEventsLoading(false);
      }
    };

    const loadAnnouncements = async () => {
      setAnnouncementsLoading(true);
      setAnnouncementsError(null);
      try {
        const data = await fetchAnnouncements();
        setAnnouncements(data.slice(0, 3));
      } catch (err: any) {
        setAnnouncementsError('Unable to load announcements.');
      } finally {
        setAnnouncementsLoading(false);
      }
    };

    const loadClubs = async () => {
      setClubsLoading(true);
      setClubsError(null);
      try {
        const data = await fetchMyClubs();
        setMyClubs(data);
      } catch (err: any) {
        setClubsError('Unable to load joined clubs.');
      } finally {
        setClubsLoading(false);
      }
    };

    const loadResources = async () => {
      setResourcesLoading(true);
      setResourcesError(null);
      try {
        const data = await fetchAcademicResources();
        setResources(data.slice(0, 3));
      } catch (err: any) {
        setResourcesError('Unable to load academic resources.');
      } finally {
        setResourcesLoading(false);
      }
    };

    const loadOpportunitiesStats = async () => {
      setOppLoading(true);
      try {
        const [allOpps, bms, apps] = await Promise.all([
          fetchOpportunities(),
          fetchMyBookmarks(),
          fetchMyApplications(),
        ]);
        setOppStats({
          total: allOpps.length,
          bookmarked: bms.length,
          applied: apps.length,
        });
      } catch (err: any) {
        // graceful fallthrough
      } finally {
        setOppLoading(false);
      }
    };

    loadProfile();
    loadEvents();
    loadAnnouncements();
    loadClubs();
    loadResources();
    loadOpportunitiesStats();
  }, []);

  const profileCompletion = calculateProfileCompletion(profile);

  return (
    <div className="space-y-8 animate-fade-in">
      {/* Welcome Banner */}
      <div className="p-6 sm:p-8 rounded-3xl bg-gradient-to-r from-brand-600 via-brand-700 to-indigo-800 text-white shadow-xl relative overflow-hidden flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div className="absolute -top-12 -right-12 w-64 h-64 bg-white/10 rounded-full blur-2xl pointer-events-none" />
        <div className="space-y-2 relative z-10 max-w-xl">
          <div className="inline-flex items-center gap-2 px-3 py-1 bg-white/10 backdrop-blur-md rounded-full text-xs font-bold text-blue-100 uppercase tracking-wider">
            <SparklesIcon size={14} className="text-cyan-300" />
            <span>Student Dashboard</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight">
            Welcome back, {user?.firstName || 'Student'}! 👋
          </h1>
          <p className="text-blue-100 text-sm leading-relaxed font-medium">
            Here is your daily campus snapshot. Explore academic resources, join active student clubs, track career applications, and discover published events.
          </p>
        </div>

        <div className="relative z-10 shrink-0">
          <Link
            to="/profile"
            className="inline-flex items-center gap-2 px-5 py-3 bg-white hover:bg-slate-50 text-brand-700 font-bold text-sm rounded-2xl transition-all shadow-md hover:shadow-lg active:scale-95"
          >
            <UserIcon size={18} />
            <span>View Profile</span>
            <ArrowRightIcon size={16} />
          </Link>
        </div>
      </div>

      {/* Quick Stats Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        <StatCard
          title="Upcoming Events"
          value={eventsLoading ? '...' : events.length}
          icon={CalendarIcon}
          color="blue"
          description="Published campus events"
        />
        <StatCard
          title="Joined Clubs"
          value={clubsLoading ? '...' : myClubs.length}
          icon={UsersIcon}
          color="purple"
          description="Active memberships"
        />
        <StatCard
          title="Tracked Applications"
          value={oppLoading ? '...' : oppStats.applied}
          icon={BriefcaseIcon}
          color="emerald"
          description="Job & internship applications"
        />
        <StatCard
          title="Study Resources"
          value={resourcesLoading ? '...' : resources.length}
          icon={BookOpenIcon}
          color="cyan"
          description="Available course files"
        />
      </div>

      {/* Profile Summary Card */}
      <div className="bg-white rounded-2xl p-6 border border-slate-200/80 shadow-subtle space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-100 pb-4">
          <div>
            <h3 className="text-base font-bold text-slate-900 flex items-center gap-2">
              <GraduationCapIcon size={20} className="text-brand-600" />
              <span>Academic Profile Overview</span>
            </h3>
            <p className="text-xs text-slate-500 font-medium mt-0.5">
              Verified student data linked to your account.
            </p>
          </div>
          <Link
            to="/profile"
            className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold text-xs rounded-xl transition-colors self-start sm:self-auto"
          >
            {profile ? 'Manage Profile' : 'Setup Profile'}
          </Link>
        </div>

        {profileLoading ? (
          <SkeletonLoader type="table" count={1} />
        ) : profileError ? (
          <p className="text-xs text-rose-500 font-medium">{profileError}</p>
        ) : !profile ? (
          <div className="p-4 bg-amber-50 border border-amber-200/80 rounded-2xl flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <div>
              <h4 className="text-xs font-bold text-amber-800">Setup your student profile</h4>
              <p className="text-xs text-amber-700/90 mt-0.5">
                Add your Student ID, Course, Department, Year, and Semester to receive tailored resources.
              </p>
            </div>
            <Link
              to="/profile"
              className="px-4 py-2 bg-amber-600 hover:bg-amber-700 text-white font-bold text-xs rounded-xl transition-colors whitespace-nowrap self-start sm:self-auto"
            >
              Complete Profile
            </Link>
          </div>
        ) : (
          <div className="space-y-4">
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-xs">
              <div className="p-3 bg-slate-50 rounded-xl border border-slate-200/60">
                <span className="text-slate-400 font-medium block">Full Name</span>
                <span className="text-slate-900 font-bold truncate block">{profile.firstName} {profile.lastName}</span>
              </div>
              <div className="p-3 bg-slate-50 rounded-xl border border-slate-200/60">
                <span className="text-slate-400 font-medium block">Course</span>
                <span className="text-slate-900 font-bold truncate block">{profile.course || 'N/A'}</span>
              </div>
              <div className="p-3 bg-slate-50 rounded-xl border border-slate-200/60">
                <span className="text-slate-400 font-medium block">Department</span>
                <span className="text-slate-900 font-bold truncate block">{profile.department || 'N/A'}</span>
              </div>
              <div className="p-3 bg-slate-50 rounded-xl border border-slate-200/60">
                <span className="text-slate-400 font-medium block">Year / Sem</span>
                <span className="text-slate-900 font-bold truncate block">{profile.year || 'N/A'} / Sem {profile.semester || 'N/A'}</span>
              </div>
            </div>

            {/* Profile Completion Bar */}
            <div className="space-y-1.5 pt-1">
              <div className="flex justify-between text-xs font-medium">
                <span className="text-slate-600">Profile Completion</span>
                <span className="text-brand-600 font-bold">{profileCompletion}%</span>
              </div>
              <div className="w-full h-2 bg-slate-100 rounded-full overflow-hidden">
                <div
                  className="h-full bg-brand-600 transition-all duration-500 rounded-full"
                  style={{ width: `${profileCompletion}%` }}
                />
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Main Content Grid: Events & Bulletins */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">

        {/* Upcoming Events Card */}
        <div className="bg-white rounded-2xl p-6 border border-slate-200/80 shadow-subtle flex flex-col justify-between space-y-4">
          <div className="space-y-4">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="text-base font-bold text-slate-900 flex items-center gap-2">
                <CalendarIcon size={18} className="text-brand-600" />
                <span>Upcoming Campus Events</span>
              </h3>
              <Link to="/events" className="text-xs font-bold text-brand-600 hover:text-brand-700 hover:underline">
                View All →
              </Link>
            </div>

            {eventsLoading ? (
              <SkeletonLoader type="table" count={2} />
            ) : eventsError ? (
              <p className="text-xs text-rose-500">{eventsError}</p>
            ) : events.length === 0 ? (
              <EmptyState
                title="No Upcoming Events"
                description="Your campus hasn't published any upcoming events yet."
                icon={CalendarIcon}
              />
            ) : (
              <div className="space-y-3">
                {events.map((ev) => (
                  <div key={ev.id} className="p-3.5 bg-slate-50 hover:bg-slate-100/80 rounded-xl border border-slate-200/60 transition-all flex items-center justify-between gap-3">
                    <div className="min-w-0 space-y-0.5">
                      <h4 className="text-xs font-bold text-slate-900 truncate">{ev.title}</h4>
                      <p className="text-[11px] text-slate-500 font-medium truncate">
                        📍 {ev.venue} {ev.eventDate && `• 🗓️ ${ev.eventDate}`}
                      </p>
                    </div>
                    {ev.category && (
                      <Badge variant="info" size="sm">
                        {ev.category}
                      </Badge>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Latest Bulletins Card */}
        <div className="bg-white rounded-2xl p-6 border border-slate-200/80 shadow-subtle flex flex-col justify-between space-y-4">
          <div className="space-y-4">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="text-base font-bold text-slate-900 flex items-center gap-2">
                <MegaphoneIcon size={18} className="text-purple-600" />
                <span>Bulletins & Announcements</span>
              </h3>
              <Link to="/announcements" className="text-xs font-bold text-purple-600 hover:text-purple-700 hover:underline">
                View All →
              </Link>
            </div>

            {announcementsLoading ? (
              <SkeletonLoader type="table" count={2} />
            ) : announcementsError ? (
              <p className="text-xs text-rose-500">{announcementsError}</p>
            ) : announcements.length === 0 ? (
              <EmptyState
                title="No Announcements"
                description="No official bulletins have been posted recently."
                icon={MegaphoneIcon}
              />
            ) : (
              <div className="space-y-3">
                {announcements.map((anc) => (
                  <div key={anc.id} className="p-3.5 bg-slate-50 hover:bg-slate-100/80 rounded-xl border border-slate-200/60 transition-all flex items-center justify-between gap-3">
                    <div className="min-w-0 space-y-0.5">
                      <h4 className="text-xs font-bold text-slate-900 truncate">{anc.title}</h4>
                      <p className="text-[11px] text-slate-500 font-medium truncate">{anc.content}</p>
                    </div>
                    {anc.category && (
                      <Badge variant="secondary" size="sm">
                        {anc.category}
                      </Badge>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

      </div>

      {/* Clubs & Resources Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        
        {/* Joined Clubs */}
        <div className="bg-white rounded-2xl p-6 border border-slate-200/80 shadow-subtle space-y-4">
          <div className="flex items-center justify-between border-b border-slate-100 pb-3">
            <h3 className="text-base font-bold text-slate-900 flex items-center gap-2">
              <UsersIcon size={18} className="text-purple-600" />
              <span>My Joined Clubs</span>
            </h3>
            <Link to="/clubs" className="text-xs font-bold text-purple-600 hover:text-purple-700 hover:underline">
              Explore Clubs →
            </Link>
          </div>

          {clubsLoading ? (
            <SkeletonLoader type="table" count={1} />
          ) : clubsError ? (
            <p className="text-xs text-rose-500">{clubsError}</p>
          ) : myClubs.length === 0 ? (
            <EmptyState
              title="No Clubs Joined Yet"
              description="Join academic, technical, or cultural student clubs."
              icon={UsersIcon}
              action={{
                label: 'Browse Clubs',
                onClick: () => window.location.assign('/clubs'),
              }}
            />
          ) : (
            <div className="space-y-2.5">
              {myClubs.slice(0, 3).map((club) => (
                <div key={club.id} className="p-3 bg-slate-50 rounded-xl border border-slate-200/60 flex items-center justify-between">
                  <span className="text-xs font-bold text-slate-900">{club.name}</span>
                  <Badge variant="secondary" size="sm">
                    {club.category || 'Club'}
                  </Badge>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Academic Resources */}
        <div className="bg-white rounded-2xl p-6 border border-slate-200/80 shadow-subtle space-y-4">
          <div className="flex items-center justify-between border-b border-slate-100 pb-3">
            <h3 className="text-base font-bold text-slate-900 flex items-center gap-2">
              <BookOpenIcon size={18} className="text-cyan-600" />
              <span>Recent Academic Resources</span>
            </h3>
            <Link to="/resources" className="text-xs font-bold text-cyan-600 hover:text-cyan-700 hover:underline">
              Browse All →
            </Link>
          </div>

          {resourcesLoading ? (
            <SkeletonLoader type="table" count={1} />
          ) : resourcesError ? (
            <p className="text-xs text-rose-500">{resourcesError}</p>
          ) : resources.length === 0 ? (
            <EmptyState
              title="No Resources Available"
              description="Your faculty hasn't published study materials for your courses yet."
              icon={BookOpenIcon}
            />
          ) : (
            <div className="space-y-2.5">
              {resources.map((res) => (
                <div key={res.id} className="p-3 bg-slate-50 rounded-xl border border-slate-200/60 flex items-center justify-between gap-2">
                  <div className="min-w-0">
                    <h4 className="text-xs font-bold text-slate-900 truncate">{res.title}</h4>
                    <p className="text-[11px] text-slate-500 font-medium">Subject: {res.subject}</p>
                  </div>
                  <Badge variant="info" size="sm">
                    {res.resourceType || 'File'}
                  </Badge>
                </div>
              ))}
            </div>
          )}
        </div>

      </div>

    </div>
  );
};

export default DashboardPage;
