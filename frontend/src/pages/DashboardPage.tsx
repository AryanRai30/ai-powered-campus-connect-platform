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

export const DashboardPage: React.FC = () => {
  const { user, logout } = useAuth();

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
  const [oppError, setOppError] = useState<string | null>(null);

  // Calculate dynamic profile completion percentage
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
    // 1. Fetch Profile
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

    // 2. Fetch Events
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

    // 3. Fetch Announcements
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

    // 4. Fetch My Clubs
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

    // 5. Fetch Resources
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

    // 6. Fetch Opportunities Stats
    const loadOpportunitiesStats = async () => {
      setOppLoading(true);
      setOppError(null);
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
        setOppError('Unable to load opportunity statistics.');
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
    <div className="space-y-8 max-w-7xl mx-auto">
      {/* 1. Welcome Header Section */}
      <div className="p-8 rounded-3xl bg-gradient-to-r from-slate-900 via-slate-900 to-emerald-950/40 border border-slate-800 shadow-2xl flex flex-col md:flex-row md:items-center justify-between gap-6 relative overflow-hidden">
        <div className="space-y-3 z-10 max-w-2xl">
          <div className="inline-flex items-center space-x-2 px-3 py-1 bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs font-semibold rounded-full">
            <span>🎓 Student Portal Dashboard</span>
          </div>
          <h1 className="text-3xl sm:text-4xl font-extrabold text-white tracking-tight">
            Welcome back, {user?.firstName || 'Student'}!
          </h1>
          <p className="text-slate-400 text-sm leading-relaxed">
            Here’s what’s happening around your campus today. Stay updated with upcoming events, announcements, club activities, resources, and career opportunities.
          </p>
        </div>

        <div className="flex items-center space-x-3 z-10 self-start md:self-auto">
          <Link
            to="/student-profile"
            className="px-5 py-3 bg-emerald-500 hover:bg-emerald-400 text-slate-950 rounded-xl text-xs font-bold transition-all shadow-lg shadow-emerald-500/20 flex items-center space-x-2"
          >
            <span>My Profile</span>
            <span>→</span>
          </Link>

          <button
            onClick={logout}
            className="px-4 py-3 bg-slate-800 hover:bg-slate-700 text-slate-300 border border-slate-700 rounded-xl text-xs font-semibold transition-colors"
          >
            Sign Out
          </button>
        </div>
      </div>

      {/* 2 & 3. Student Profile Summary & Profile Completion Indicator */}
      <div className="p-6 bg-slate-900/90 border border-slate-800 rounded-2xl shadow-xl space-y-5">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-800 pb-4">
          <div>
            <h2 className="text-lg font-bold text-slate-100 flex items-center space-x-2">
              <span>👤 Student Profile Summary</span>
            </h2>
            <p className="text-xs text-slate-400">Academic & personal details linked to your account.</p>
          </div>

          <Link
            to="/student-profile"
            className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold rounded-xl border border-slate-700 transition-colors self-start sm:self-auto"
          >
            {profile ? 'View / Edit Profile' : 'Complete Profile'}
          </Link>
        </div>

        {profileLoading ? (
          <div className="animate-pulse space-y-3">
            <div className="h-4 bg-slate-800 rounded w-1/3"></div>
            <div className="h-10 bg-slate-800/50 rounded"></div>
          </div>
        ) : profileError ? (
          <p className="text-xs text-red-400">{profileError}</p>
        ) : !profile ? (
          <div className="p-4 bg-amber-500/10 border border-amber-500/20 rounded-xl flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <div>
              <h4 className="text-xs font-bold text-amber-300">Complete your student profile</h4>
              <p className="text-xs text-amber-200/80">Add your Student ID, Course, Department, Year, and Semester to unlock personalized campus services.</p>
            </div>
            <Link
              to="/student-profile"
              className="px-4 py-2 bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold text-xs rounded-lg transition-colors whitespace-nowrap self-start sm:self-auto"
            >
              Complete Profile
            </Link>
          </div>
        ) : (
          <div className="space-y-4">
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-xs">
              <div className="p-3 bg-slate-950 border border-slate-800/80 rounded-xl">
                <span className="text-slate-500 block">Full Name</span>
                <span className="text-slate-200 font-semibold truncate block">{profile.firstName} {profile.lastName}</span>
              </div>
              <div className="p-3 bg-slate-950 border border-slate-800/80 rounded-xl">
                <span className="text-slate-500 block">Course</span>
                <span className="text-slate-200 font-semibold truncate block">{profile.course || 'N/A'}</span>
              </div>
              <div className="p-3 bg-slate-950 border border-slate-800/80 rounded-xl">
                <span className="text-slate-500 block">Department</span>
                <span className="text-slate-200 font-semibold truncate block">{profile.department || 'N/A'}</span>
              </div>
              <div className="p-3 bg-slate-950 border border-slate-800/80 rounded-xl">
                <span className="text-slate-500 block">Year & Semester</span>
                <span className="text-slate-200 font-semibold truncate block">{profile.year || 'N/A'} / Sem {profile.semester || 'N/A'}</span>
              </div>
            </div>

            {/* Profile Completion Bar */}
            <div className="space-y-1.5 pt-2">
              <div className="flex justify-between text-xs">
                <span className="text-slate-400 font-semibold">Profile Completion Status</span>
                <span className="text-emerald-400 font-bold">{profileCompletion}%</span>
              </div>
              <div className="w-full h-2 bg-slate-800 rounded-full overflow-hidden">
                <div
                  className="h-full bg-gradient-to-r from-emerald-500 to-teal-400 transition-all duration-500 rounded-full"
                  style={{ width: `${profileCompletion}%` }}
                ></div>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* 9. Quick Actions Grid */}
      <div className="space-y-3">
        <h2 className="text-lg font-bold text-slate-100">🚀 Quick Portal Actions</h2>
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-4">
          <Link
            to="/student-profile"
            className="p-4 bg-slate-900 border border-slate-800 hover:border-emerald-500/50 rounded-2xl flex flex-col items-center justify-center space-y-2 text-center transition-all hover:shadow-lg group"
          >
            <div className="text-2xl group-hover:scale-110 transition-transform">👤</div>
            <span className="text-xs font-bold text-slate-200 group-hover:text-emerald-400">Profile</span>
          </Link>

          <Link
            to="/events"
            className="p-4 bg-slate-900 border border-slate-800 hover:border-emerald-500/50 rounded-2xl flex flex-col items-center justify-center space-y-2 text-center transition-all hover:shadow-lg group"
          >
            <div className="text-2xl group-hover:scale-110 transition-transform">📅</div>
            <span className="text-xs font-bold text-slate-200 group-hover:text-emerald-400">Events</span>
          </Link>

          <Link
            to="/announcements"
            className="p-4 bg-slate-900 border border-slate-800 hover:border-indigo-500/50 rounded-2xl flex flex-col items-center justify-center space-y-2 text-center transition-all hover:shadow-lg group"
          >
            <div className="text-2xl group-hover:scale-110 transition-transform">📢</div>
            <span className="text-xs font-bold text-slate-200 group-hover:text-indigo-400">Bulletins</span>
          </Link>

          <Link
            to="/clubs"
            className="p-4 bg-slate-900 border border-slate-800 hover:border-purple-500/50 rounded-2xl flex flex-col items-center justify-center space-y-2 text-center transition-all hover:shadow-lg group"
          >
            <div className="text-2xl group-hover:scale-110 transition-transform">🏫</div>
            <span className="text-xs font-bold text-slate-200 group-hover:text-purple-400">Clubs</span>
          </Link>

          <Link
            to="/resources"
            className="p-4 bg-slate-900 border border-slate-800 hover:border-cyan-500/50 rounded-2xl flex flex-col items-center justify-center space-y-2 text-center transition-all hover:shadow-lg group"
          >
            <div className="text-2xl group-hover:scale-110 transition-transform">📚</div>
            <span className="text-xs font-bold text-slate-200 group-hover:text-cyan-400">Resources</span>
          </Link>

          <Link
            to="/opportunities"
            className="p-4 bg-slate-900 border border-slate-800 hover:border-amber-500/50 rounded-2xl flex flex-col items-center justify-center space-y-2 text-center transition-all hover:shadow-lg group"
          >
            <div className="text-2xl group-hover:scale-110 transition-transform">💼</div>
            <span className="text-xs font-bold text-slate-200 group-hover:text-amber-400">Careers</span>
          </Link>
        </div>
      </div>

      {/* Main Dashboard Grid: Events, Bulletins, Clubs, Resources, Opportunities */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">

        {/* 4. Upcoming Events Section */}
        <div className="p-6 bg-slate-900/90 border border-slate-800 rounded-2xl space-y-4 flex flex-col justify-between shadow-xl">
          <div className="space-y-3">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <h2 className="text-base font-bold text-slate-100 flex items-center space-x-2">
                <span>📅 Upcoming Campus Events</span>
              </h2>
              <Link to="/events" className="text-xs font-semibold text-emerald-400 hover:underline">
                View All Events →
              </Link>
            </div>

            {eventsLoading ? (
              <div className="space-y-3 animate-pulse">
                <div className="h-12 bg-slate-800/60 rounded-xl"></div>
                <div className="h-12 bg-slate-800/60 rounded-xl"></div>
              </div>
            ) : eventsError ? (
              <p className="text-xs text-red-400">{eventsError}</p>
            ) : events.length === 0 ? (
              <p className="text-xs text-slate-400 italic">No upcoming events have been published yet.</p>
            ) : (
              <div className="space-y-3">
                {events.map((ev) => (
                  <div key={ev.id} className="p-3 bg-slate-950 border border-slate-800/80 rounded-xl flex items-center justify-between gap-3">
                    <div className="space-y-0.5 min-w-0">
                      <h4 className="text-xs font-bold text-slate-200 truncate">{ev.title}</h4>
                      <p className="text-[11px] text-slate-400 truncate">
                        📍 {ev.venue} {ev.eventDate && `• 🗓️ ${ev.eventDate}`}
                      </p>
                    </div>
                    {ev.category && (
                      <span className="px-2 py-0.5 bg-emerald-500/10 text-emerald-400 text-[10px] font-bold rounded border border-emerald-500/20 whitespace-nowrap">
                        {ev.category}
                      </span>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* 5. Latest Announcements Section */}
        <div className="p-6 bg-slate-900/90 border border-slate-800 rounded-2xl space-y-4 flex flex-col justify-between shadow-xl">
          <div className="space-y-3">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <h2 className="text-base font-bold text-slate-100 flex items-center space-x-2">
                <span>📢 Latest Announcements</span>
              </h2>
              <Link to="/announcements" className="text-xs font-semibold text-indigo-400 hover:underline">
                View All Bulletins →
              </Link>
            </div>

            {announcementsLoading ? (
              <div className="space-y-3 animate-pulse">
                <div className="h-12 bg-slate-800/60 rounded-xl"></div>
                <div className="h-12 bg-slate-800/60 rounded-xl"></div>
              </div>
            ) : announcementsError ? (
              <p className="text-xs text-red-400">{announcementsError}</p>
            ) : announcements.length === 0 ? (
              <p className="text-xs text-slate-400 italic">No announcements have been published yet.</p>
            ) : (
              <div className="space-y-3">
                {announcements.map((anc) => (
                  <div key={anc.id} className="p-3 bg-slate-950 border border-slate-800/80 rounded-xl flex items-center justify-between gap-3">
                    <div className="space-y-0.5 min-w-0">
                      <h4 className="text-xs font-bold text-slate-200 truncate">{anc.title}</h4>
                      <p className="text-[11px] text-slate-400 truncate">{anc.content}</p>
                    </div>
                    {anc.category && (
                      <span className="px-2 py-0.5 bg-indigo-500/10 text-indigo-400 text-[10px] font-bold rounded border border-indigo-500/20 whitespace-nowrap">
                        {anc.category}
                      </span>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* 6. My Clubs Summary Section */}
        <div className="p-6 bg-slate-900/90 border border-slate-800 rounded-2xl space-y-4 flex flex-col justify-between shadow-xl">
          <div className="space-y-3">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <h2 className="text-base font-bold text-slate-100 flex items-center space-x-2">
                <span>🏫 My Clubs & Communities</span>
              </h2>
              <div className="flex space-x-3">
                <Link to="/my-clubs" className="text-xs font-semibold text-purple-400 hover:underline">
                  My Clubs
                </Link>
                <Link to="/clubs" className="text-xs font-semibold text-slate-400 hover:underline">
                  Explore Clubs →
                </Link>
              </div>
            </div>

            {clubsLoading ? (
              <div className="space-y-3 animate-pulse">
                <div className="h-12 bg-slate-800/60 rounded-xl"></div>
              </div>
            ) : clubsError ? (
              <p className="text-xs text-red-400">{clubsError}</p>
            ) : myClubs.length === 0 ? (
              <div className="space-y-2">
                <p className="text-xs text-slate-400 italic">No clubs joined yet.</p>
                <Link
                  to="/clubs"
                  className="inline-block px-3 py-1.5 bg-purple-500/10 hover:bg-purple-500/20 text-purple-400 border border-purple-500/20 text-xs font-semibold rounded-lg transition-colors"
                >
                  Explore Clubs
                </Link>
              </div>
            ) : (
              <div className="space-y-2">
                <p className="text-xs text-slate-400 font-semibold">
                  Joined Clubs ({myClubs.length}):
                </p>
                <div className="space-y-2">
                  {myClubs.slice(0, 3).map((club) => (
                    <div key={club.id} className="p-2.5 bg-slate-950 border border-slate-800/80 rounded-xl flex items-center justify-between">
                      <span className="text-xs font-bold text-slate-200 truncate">{club.name}</span>
                      <span className="text-[10px] text-purple-400 font-semibold px-2 py-0.5 bg-purple-500/10 rounded">
                        {club.category || 'Club'}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>

        {/* 7. Academic Resources Section */}
        <div className="p-6 bg-slate-900/90 border border-slate-800 rounded-2xl space-y-4 flex flex-col justify-between shadow-xl">
          <div className="space-y-3">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <h2 className="text-base font-bold text-slate-100 flex items-center space-x-2">
                <span>📚 Study Resources</span>
              </h2>
              <Link to="/resources" className="text-xs font-semibold text-cyan-400 hover:underline">
                Browse Resources →
              </Link>
            </div>

            {resourcesLoading ? (
              <div className="space-y-3 animate-pulse">
                <div className="h-12 bg-slate-800/60 rounded-xl"></div>
              </div>
            ) : resourcesError ? (
              <p className="text-xs text-red-400">{resourcesError}</p>
            ) : resources.length === 0 ? (
              <p className="text-xs text-slate-400 italic">No academic resources available yet.</p>
            ) : (
              <div className="space-y-2">
                {resources.map((res) => (
                  <div key={res.id} className="p-2.5 bg-slate-950 border border-slate-800/80 rounded-xl flex items-center justify-between gap-2">
                    <div className="min-w-0">
                      <h4 className="text-xs font-bold text-slate-200 truncate">{res.title}</h4>
                      <p className="text-[11px] text-slate-400 truncate">Subject: {res.subject}</p>
                    </div>
                    <span className="text-[10px] font-bold text-cyan-400 px-2 py-0.5 bg-cyan-500/10 rounded border border-cyan-500/20 whitespace-nowrap">
                      {res.resourceType || 'Resource'}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

      </div>

      {/* 8. Opportunities Summary Section */}
      <div className="p-6 bg-slate-900/90 border border-slate-800 rounded-2xl shadow-xl space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-800 pb-4">
          <div>
            <h2 className="text-lg font-bold text-slate-100 flex items-center space-x-2">
              <span>💼 Student Opportunities & Career Support</span>
            </h2>
            <p className="text-xs text-slate-400">Discover internships, job postings, hackathons, and track application submissions.</p>
          </div>

          <div className="flex space-x-2">
            <Link
              to="/opportunities"
              className="px-4 py-2 bg-amber-500 hover:bg-amber-400 text-slate-950 text-xs font-bold rounded-xl transition-colors shadow-md shadow-amber-500/20"
            >
              Explore Opportunities
            </Link>
            <Link
              to="/my-opportunities"
              className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold rounded-xl border border-slate-700 transition-colors"
            >
              Saved & Applications
            </Link>
          </div>
        </div>

        {oppLoading ? (
          <div className="animate-pulse h-16 bg-slate-800/60 rounded-xl"></div>
        ) : oppError ? (
          <p className="text-xs text-red-400">{oppError}</p>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 text-center">
            <div className="p-4 bg-slate-950 border border-slate-800/80 rounded-xl space-y-1">
              <span className="text-slate-400 text-xs font-semibold block">Available Opportunities</span>
              <span className="text-2xl font-extrabold text-amber-400">{oppStats.total}</span>
            </div>

            <div className="p-4 bg-slate-950 border border-slate-800/80 rounded-xl space-y-1">
              <span className="text-slate-400 text-xs font-semibold block">Saved Bookmarks</span>
              <span className="text-2xl font-extrabold text-slate-200">{oppStats.bookmarked}</span>
            </div>

            <div className="p-4 bg-slate-950 border border-slate-800/80 rounded-xl space-y-1">
              <span className="text-slate-400 text-xs font-semibold block">Tracked Applications</span>
              <span className="text-2xl font-extrabold text-emerald-400">{oppStats.applied}</span>
            </div>
          </div>
        )}
      </div>

    </div>
  );
};

export default DashboardPage;
