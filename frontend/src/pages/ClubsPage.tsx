import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ClubItem } from '../types/campus.types';
import { fetchClubs, joinClub } from '../services/campusService';
import { studentProfileService } from '../services/studentProfileService';
import { StudentProfileResponse } from '../types/studentProfile.types';
import { useAuth } from '../context/AuthContext';

const CATEGORIES = ['All', 'Technology', 'Cultural', 'Sports', 'Business'];

export const ClubsPage: React.FC = () => {
  const { user } = useAuth();
  const [clubs, setClubs] = useState<ClubItem[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<string>('All');
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Application Modal State & Student Profile
  const [applicationModalClub, setApplicationModalClub] = useState<ClubItem | null>(null);
  const [studentProfile, setStudentProfile] = useState<StudentProfileResponse | null>(null);
  const [joiningId, setJoiningId] = useState<number | null>(null);
  const [selectedClub, setSelectedClub] = useState<ClubItem | null>(null);
  const [toastMessage, setToastMessage] = useState<{ text: string; type: 'success' | 'error' } | null>(null);

  const loadClubs = async (category: string = selectedCategory, search: string = searchQuery) => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchClubs(category, search);
      setClubs(data);
    } catch (err: any) {
      console.error('Failed to load clubs', err);
      setError(err.response?.data?.message || 'Failed to load campus clubs. Please check server connectivity.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadClubs(selectedCategory, searchQuery);
  }, [selectedCategory]);

  useEffect(() => {
    const loadProfile = async () => {
      try {
        const profile = await studentProfileService.getProfile();
        setStudentProfile(profile);
      } catch (err) {
        console.warn('Student profile not created yet or failed to load profile data', err);
      }
    };
    loadProfile();
  }, []);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    loadClubs(selectedCategory, searchQuery);
  };

  const handleOpenApplicationModal = (club: ClubItem) => {
    setApplicationModalClub(club);
  };

  const handleConfirmJoin = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!applicationModalClub) return;

    const clubId = applicationModalClub.id;
    setJoiningId(clubId);
    setToastMessage(null);
    try {
      await joinClub(clubId);
      setToastMessage({ text: 'Welcome to the club! Membership application confirmed successfully.', type: 'success' });
      setApplicationModalClub(null);
      await loadClubs(selectedCategory, searchQuery);
      if (selectedClub && selectedClub.id === clubId) {
        setSelectedClub((prev) => prev ? { ...prev, joined: true, memberCount: prev.memberCount + 1 } : null);
      }
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Failed to join club. You may already be a member.';
      setToastMessage({ text: msg, type: 'error' });
    } finally {
      setJoiningId(null);
    }
  };

  return (
    <div className="space-y-8">
      {/* Toast Notification Banner */}
      {toastMessage && (
        <div
          className={`p-4 rounded-xl border text-sm font-semibold flex justify-between items-center ${
            toastMessage.type === 'success'
              ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30'
              : 'bg-red-500/10 text-red-400 border-red-500/30'
          }`}
        >
          <span>{toastMessage.text}</span>
          <button
            onClick={() => setToastMessage(null)}
            className="text-xs px-2 py-1 bg-slate-800 rounded hover:bg-slate-700 text-slate-300"
          >
            Dismiss
          </button>
        </div>
      )}

      {/* Header Banner */}
      <div className="relative p-8 rounded-3xl bg-gradient-to-r from-slate-900 via-slate-900 to-purple-950/40 border border-slate-800 overflow-hidden shadow-2xl flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div className="relative z-10 max-w-2xl space-y-3">
          <div className="inline-flex items-center space-x-2 px-3 py-1 bg-purple-500/10 border border-purple-500/20 text-purple-400 text-xs font-semibold rounded-full">
            <span>🤝 Student Organizations & Societies</span>
          </div>
          <h1 className="text-3xl sm:text-4xl font-extrabold text-white tracking-tight">
            Clubs & Communities
          </h1>
          <p className="text-slate-400 text-sm leading-relaxed">
            Connect with student-led communities across technology, arts, sports, and entrepreneurship. Collaborate, network, and grow together.
          </p>
        </div>

        <div className="relative z-10 self-start md:self-auto">
          <Link
            to="/my-clubs"
            className="px-5 py-3 bg-purple-500 hover:bg-purple-400 text-slate-950 rounded-xl text-xs font-bold transition-all shadow-lg shadow-purple-500/20 flex items-center space-x-2 whitespace-nowrap"
          >
            <span>My Enrolled Clubs</span>
            <span>→</span>
          </Link>
        </div>
      </div>

      {/* Controls: Search and Filter Tabs */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        {/* Category Pills */}
        <div className="flex items-center space-x-2 overflow-x-auto pb-2 md:pb-0 scrollbar-none">
          {CATEGORIES.map((cat) => (
            <button
              key={cat}
              onClick={() => setSelectedCategory(cat)}
              className={`px-4 py-2 text-xs font-semibold rounded-xl border transition-all whitespace-nowrap ${
                selectedCategory === cat
                  ? 'bg-purple-500 text-slate-950 border-purple-500 font-bold shadow-lg shadow-purple-500/20'
                  : 'bg-slate-900 text-slate-400 border-slate-800 hover:bg-slate-800 hover:text-slate-200'
              }`}
            >
              {cat}
            </button>
          ))}
        </div>

        {/* Search Bar */}
        <form onSubmit={handleSearchSubmit} className="flex items-center space-x-2">
          <div className="relative flex-grow">
            <input
              type="text"
              placeholder="Search clubs, lead, or venue..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full sm:w-64 px-4 py-2.5 text-xs bg-slate-900 border border-slate-800 rounded-xl text-slate-200 focus:outline-none focus:border-purple-500 transition-colors"
            />
          </div>
          <button
            type="submit"
            className="px-4 py-2.5 text-xs font-semibold bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 rounded-xl transition-colors"
          >
            Search
          </button>
        </form>
      </div>

      {/* Main Grid View */}
      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {[1, 2, 3, 4].map((n) => (
            <div key={n} className="p-6 bg-slate-900/60 border border-slate-800 rounded-2xl animate-pulse space-y-4">
              <div className="h-4 bg-slate-800 rounded w-1/3"></div>
              <div className="h-6 bg-slate-800 rounded w-2/3"></div>
              <div className="h-16 bg-slate-800/50 rounded"></div>
            </div>
          ))}
        </div>
      ) : error ? (
        <div className="p-8 bg-slate-900 border border-slate-800 rounded-2xl text-center space-y-4">
          <div className="w-12 h-12 rounded-full bg-red-500/10 border border-red-500/20 text-red-400 flex items-center justify-center mx-auto text-xl font-bold">
            ⚠️
          </div>
          <h3 className="text-lg font-semibold text-slate-200">Unable to load clubs</h3>
          <p className="text-xs text-slate-400 max-w-md mx-auto">{error}</p>
          <button
            onClick={() => loadClubs()}
            className="px-4 py-2 bg-purple-500 text-slate-950 rounded-xl text-xs font-semibold hover:bg-purple-400 transition-colors"
          >
            Retry Loading
          </button>
        </div>
      ) : clubs.length === 0 ? (
        <div className="p-12 bg-slate-900/50 border border-slate-800 rounded-3xl text-center space-y-3">
          <div className="text-4xl">🏫</div>
          <h3 className="text-lg font-semibold text-slate-300">No clubs available</h3>
          <p className="text-xs text-slate-500 max-w-md mx-auto">
            Campus clubs will appear here when they are published.
          </p>
          {(selectedCategory !== 'All' || searchQuery) && (
            <button
              onClick={() => {
                setSelectedCategory('All');
                setSearchQuery('');
                loadClubs('All', '');
              }}
              className="mt-2 text-xs font-semibold text-purple-400 hover:underline"
            >
              Clear filters
            </button>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {clubs.map((club) => (
            <div
              key={club.id}
              className="p-6 bg-slate-900/90 border border-slate-800 hover:border-slate-700 rounded-2xl flex flex-col justify-between space-y-5 transition-all hover:shadow-xl group"
            >
              <div className="space-y-3">
                <div className="flex items-center justify-between gap-2">
                  <span className="px-3 py-1 bg-purple-500/10 border border-purple-500/20 text-purple-400 text-xs font-semibold rounded-lg">
                    {club.category || 'Community'}
                  </span>
                  {club.joined ? (
                    <span className="px-2.5 py-1 bg-emerald-500/20 text-emerald-300 border border-emerald-500/30 text-xs font-bold rounded-lg flex items-center space-x-1">
                      <span>✓</span>
                      <span>Joined Member</span>
                    </span>
                  ) : (
                    <span className="px-2.5 py-1 bg-slate-800 text-slate-400 border border-slate-700 text-xs font-medium rounded-lg">
                      {club.memberCount} Members
                    </span>
                  )}
                </div>

                <h2 className="text-xl font-bold text-slate-100 group-hover:text-purple-400 transition-colors">
                  {club.name}
                </h2>

                <p className="text-xs text-slate-400 line-clamp-2 leading-relaxed">
                  {club.description}
                </p>

                <div className="pt-2 grid grid-cols-2 gap-2 text-xs text-slate-300 border-t border-slate-800/80">
                  {club.presidentName && (
                    <div className="flex items-center space-x-2 col-span-2">
                      <span className="text-slate-500">👑</span>
                      <span className="text-slate-300 font-medium">President: {club.presidentName}</span>
                    </div>
                  )}
                  {club.meetingDay && (
                    <div className="flex items-center space-x-2">
                      <span className="text-slate-500">📅</span>
                      <span>{club.meetingDay}s</span>
                    </div>
                  )}
                  {club.meetingTime && (
                    <div className="flex items-center space-x-2">
                      <span className="text-slate-500">⏰</span>
                      <span>{club.meetingTime}</span>
                    </div>
                  )}
                  {club.meetingVenue && (
                    <div className="flex items-center space-x-2 col-span-2">
                      <span className="text-slate-500">📍</span>
                      <span className="truncate">{club.meetingVenue}</span>
                    </div>
                  )}
                </div>
              </div>

              {/* Action buttons footer */}
              <div className="pt-4 border-t border-slate-800/80 flex items-center justify-between gap-3">
                <button
                  onClick={() => setSelectedClub(club)}
                  className="text-xs font-semibold text-slate-400 hover:text-slate-200 transition-colors"
                >
                  View Details →
                </button>

                <button
                  onClick={() => club.joined ? null : handleOpenApplicationModal(club)}
                  disabled={club.joined}
                  className={`px-4 py-2 rounded-xl text-xs font-bold transition-all shadow-md ${
                    club.joined
                      ? 'bg-slate-800 text-emerald-400 border border-emerald-500/20 cursor-default'
                      : 'bg-purple-500 hover:bg-purple-400 text-slate-950 shadow-purple-500/20'
                  }`}
                >
                  {club.joined ? 'Already Joined' : 'Join Club'}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Club Details Modal */}
      {selectedClub && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl max-w-xl w-full p-6 sm:p-8 space-y-6 shadow-2xl relative">
            <div className="flex items-start justify-between">
              <span className="px-3 py-1 bg-purple-500/10 border border-purple-500/20 text-purple-400 text-xs font-semibold rounded-lg">
                {selectedClub.category || 'Community'}
              </span>
              <button
                onClick={() => setSelectedClub(null)}
                className="w-8 h-8 rounded-full bg-slate-800 text-slate-400 hover:text-white flex items-center justify-center font-bold"
              >
                ✕
              </button>
            </div>

            <div className="space-y-2">
              <h2 className="text-2xl font-bold text-white">{selectedClub.name}</h2>
              <p className="text-xs text-slate-400">Led by {selectedClub.presidentName || 'Student Board'}</p>
            </div>

            <div className="p-4 bg-slate-950 border border-slate-800 rounded-2xl grid grid-cols-2 gap-4 text-xs">
              <div>
                <span className="text-slate-500 block">Meeting Day</span>
                <span className="text-slate-200 font-semibold">{selectedClub.meetingDay || 'TBA'}</span>
              </div>
              <div>
                <span className="text-slate-500 block">Meeting Time</span>
                <span className="text-slate-200 font-semibold">{selectedClub.meetingTime || 'TBA'}</span>
              </div>
              <div className="col-span-2">
                <span className="text-slate-500 block">Meeting Venue</span>
                <span className="text-slate-200 font-semibold">{selectedClub.meetingVenue || 'TBA'}</span>
              </div>
              <div>
                <span className="text-slate-500 block">Active Members</span>
                <span className="text-slate-200 font-semibold">{selectedClub.memberCount} Enrolled</span>
              </div>
              <div>
                <span className="text-slate-500 block">Membership Status</span>
                <span className="text-slate-200 font-semibold">
                  {selectedClub.joined ? 'Active Member' : 'Not Joined'}
                </span>
              </div>
            </div>

            <div className="space-y-2">
              <h4 className="text-xs font-semibold text-slate-400 uppercase tracking-wider">About the Club</h4>
              <p className="text-sm text-slate-300 whitespace-pre-line leading-relaxed">
                {selectedClub.description}
              </p>
            </div>

            <div className="flex justify-end space-x-3 pt-4 border-t border-slate-800">
              <button
                onClick={() => setSelectedClub(null)}
                className="px-5 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold rounded-xl"
              >
                Close
              </button>

              <button
                onClick={() => {
                  const target = selectedClub;
                  setSelectedClub(null);
                  if (!target.joined) {
                    handleOpenApplicationModal(target);
                  }
                }}
                disabled={selectedClub.joined}
                className={`px-5 py-2.5 rounded-xl text-xs font-bold transition-all ${
                  selectedClub.joined
                    ? 'bg-slate-800 text-emerald-400 border border-emerald-500/20 cursor-default'
                    : 'bg-purple-500 hover:bg-purple-400 text-slate-950'
                }`}
              >
                {selectedClub.joined ? 'Already Joined' : 'Join Club'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Dedicated Club Application Form Modal */}
      {applicationModalClub && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl max-w-xl w-full p-6 sm:p-8 space-y-6 shadow-2xl relative max-h-[90vh] overflow-y-auto">
            <div className="flex items-start justify-between border-b border-slate-800 pb-4">
              <div>
                <span className="px-3 py-1 bg-purple-500/10 border border-purple-500/20 text-purple-400 text-xs font-semibold rounded-lg">
                  Club Membership Application
                </span>
                <h3 className="text-xl font-bold text-white mt-2">{applicationModalClub.name}</h3>
              </div>
              <button
                onClick={() => setApplicationModalClub(null)}
                className="w-8 h-8 rounded-full bg-slate-800 text-slate-400 hover:text-white flex items-center justify-center font-bold"
              >
                ✕
              </button>
            </div>

            {/* Club Summary Details */}
            <div className="p-4 bg-slate-950 border border-slate-800/80 rounded-2xl space-y-2 text-xs">
              <h4 className="text-xs font-bold text-purple-400 uppercase tracking-wider mb-1">
                Club Information
              </h4>
              <div className="grid grid-cols-2 gap-2">
                <div>
                  <span className="text-slate-500 block">Category</span>
                  <span className="text-purple-300 font-semibold">{applicationModalClub.category || 'Community'}</span>
                </div>
                <div>
                  <span className="text-slate-500 block">President / Lead</span>
                  <span className="text-slate-200 font-semibold">{applicationModalClub.presidentName || 'Student Board'}</span>
                </div>
                <div>
                  <span className="text-slate-500 block">Schedule</span>
                  <span className="text-slate-200 font-semibold">
                    {applicationModalClub.meetingDay ? `${applicationModalClub.meetingDay}s` : 'TBA'} {applicationModalClub.meetingTime ? `at ${applicationModalClub.meetingTime}` : ''}
                  </span>
                </div>
                <div>
                  <span className="text-slate-500 block">Meeting Venue</span>
                  <span className="text-slate-200 font-semibold">{applicationModalClub.meetingVenue || 'Campus Venue'}</span>
                </div>
              </div>
              <div className="pt-2 border-t border-slate-800/60">
                <span className="text-slate-500 block">Description</span>
                <p className="text-slate-300 text-[11px] leading-relaxed line-clamp-3">{applicationModalClub.description}</p>
              </div>
            </div>

            {/* Application Form */}
            <form onSubmit={handleConfirmJoin} className="space-y-4">
              <div className="p-4 bg-slate-950/80 border border-slate-800 rounded-2xl space-y-3">
                <h4 className="text-xs font-bold text-purple-400 uppercase tracking-wider">
                  Applicant Student Details
                </h4>
                <div className="grid grid-cols-2 gap-3 text-xs">
                  <div>
                    <span className="text-slate-500 block">Student Name</span>
                    <span className="text-slate-200 font-semibold">
                      {studentProfile?.firstName ? `${studentProfile.firstName} ${studentProfile.lastName || ''}` : user?.firstName ? `${user.firstName} ${user.lastName || ''}` : 'Authenticated Student'}
                    </span>
                  </div>
                  <div>
                    <span className="text-slate-500 block">Student Email</span>
                    <span className="text-slate-200 font-semibold">{studentProfile?.email || user?.email || 'N/A'}</span>
                  </div>
                  <div>
                    <span className="text-slate-500 block">Student ID</span>
                    <span className="text-slate-200 font-semibold">{studentProfile?.studentId || 'N/A'}</span>
                  </div>
                  <div>
                    <span className="text-slate-500 block">Department</span>
                    <span className="text-slate-200 font-semibold">{studentProfile?.department || 'N/A'}</span>
                  </div>
                  <div>
                    <span className="text-slate-500 block">Course</span>
                    <span className="text-slate-200 font-semibold">{studentProfile?.course || 'N/A'}</span>
                  </div>
                  <div>
                    <span className="text-slate-500 block">Academic Year / Sem</span>
                    <span className="text-slate-200 font-semibold">
                      {studentProfile ? `Year ${studentProfile.year}, Sem ${studentProfile.semester}` : 'N/A'}
                    </span>
                  </div>
                </div>
              </div>

              <p className="text-xs text-slate-400 leading-relaxed">
                By clicking <strong className="text-purple-400">Submit Application</strong>, your membership request will be registered in the database and submitted to club leadership.
              </p>

              <div className="flex justify-end space-x-3 pt-4 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setApplicationModalClub(null)}
                  className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-semibold rounded-xl"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={joiningId === applicationModalClub.id}
                  className="px-5 py-2.5 bg-purple-500 hover:bg-purple-400 text-slate-950 rounded-xl text-xs font-bold transition-all shadow-md shadow-purple-500/20"
                >
                  {joiningId === applicationModalClub.id ? 'Submitting Application...' : 'Submit Application'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default ClubsPage;
