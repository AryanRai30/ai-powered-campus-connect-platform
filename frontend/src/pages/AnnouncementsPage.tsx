import React, { useEffect, useState } from 'react';
import { AnnouncementItem } from '../types/campus.types';
import { fetchAnnouncements } from '../services/campusService';

const CATEGORIES = ['All', 'Academic', 'General', 'Urgent', 'Exam', 'Event'];

export const AnnouncementsPage: React.FC = () => {
  const [announcements, setAnnouncements] = useState<AnnouncementItem[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<string>('All');
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedAnnouncement, setSelectedAnnouncement] = useState<AnnouncementItem | null>(null);

  const loadAnnouncements = async (category: string = selectedCategory, search: string = searchQuery) => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchAnnouncements(category, search);
      setAnnouncements(data);
    } catch (err: any) {
      console.error('Failed to load announcements', err);
      setError(err.response?.data?.message || 'Failed to load announcements. Please check server connectivity.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAnnouncements(selectedCategory, searchQuery);
  }, [selectedCategory]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    loadAnnouncements(selectedCategory, searchQuery);
  };

  const formatDate = (dateString: string) => {
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return dateString;
    }
  };

  const getCategoryBadgeClass = (category?: string) => {
    switch (category?.toLowerCase()) {
      case 'urgent':
        return 'bg-red-500/10 text-red-400 border-red-500/30';
      case 'academic':
        return 'bg-blue-500/10 text-blue-400 border-blue-500/30';
      case 'exam':
        return 'bg-purple-500/10 text-purple-400 border-purple-500/30';
      default:
        return 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30';
    }
  };

  return (
    <div className="space-y-8">
      {/* Header Banner */}
      <div className="relative p-8 rounded-3xl bg-gradient-to-r from-slate-900 via-slate-900 to-indigo-950/40 border border-slate-800 overflow-hidden shadow-2xl">
        <div className="relative z-10 max-w-2xl space-y-3">
          <div className="inline-flex items-center space-x-2 px-3 py-1 bg-indigo-500/10 border border-indigo-500/20 text-indigo-400 text-xs font-semibold rounded-full">
            <span>📢 Campus Circulars & Bulletins</span>
          </div>
          <h1 className="text-3xl sm:text-4xl font-extrabold text-white tracking-tight">
            Official Announcements
          </h1>
          <p className="text-slate-400 text-sm leading-relaxed">
            Stay up to date with official administrative notices, examination timetables, library notices, and urgent campus alerts.
          </p>
        </div>
      </div>

      {/* Filter and Search */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        {/* Category Pills */}
        <div className="flex items-center space-x-2 overflow-x-auto pb-2 md:pb-0 scrollbar-none">
          {CATEGORIES.map((cat) => (
            <button
              key={cat}
              onClick={() => setSelectedCategory(cat)}
              className={`px-4 py-2 text-xs font-semibold rounded-xl border transition-all whitespace-nowrap ${
                selectedCategory === cat
                  ? 'bg-indigo-500 text-slate-950 border-indigo-500 font-bold shadow-lg shadow-indigo-500/20'
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
              placeholder="Search announcements..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full sm:w-64 px-4 py-2.5 text-xs bg-slate-900 border border-slate-800 rounded-xl text-slate-200 focus:outline-none focus:border-indigo-500 transition-colors"
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

      {/* Announcements List */}
      {loading ? (
        <div className="space-y-4">
          {[1, 2, 3].map((n) => (
            <div key={n} className="p-6 bg-slate-900/60 border border-slate-800 rounded-2xl animate-pulse space-y-3">
              <div className="h-4 bg-slate-800 rounded w-1/4"></div>
              <div className="h-6 bg-slate-800 rounded w-1/2"></div>
              <div className="h-12 bg-slate-800/50 rounded w-full"></div>
            </div>
          ))}
        </div>
      ) : error ? (
        <div className="p-8 bg-slate-900 border border-slate-800 rounded-2xl text-center space-y-4">
          <div className="w-12 h-12 rounded-full bg-red-500/10 border border-red-500/20 text-red-400 flex items-center justify-center mx-auto text-xl font-bold">
            ⚠️
          </div>
          <h3 className="text-lg font-semibold text-slate-200">Unable to load announcements</h3>
          <p className="text-xs text-slate-400 max-w-md mx-auto">{error}</p>
          <button
            onClick={() => loadAnnouncements()}
            className="px-4 py-2 bg-indigo-500 text-slate-950 rounded-xl text-xs font-semibold hover:bg-indigo-400 transition-colors"
          >
            Retry Loading
          </button>
        </div>
      ) : announcements.length === 0 ? (
        <div className="p-12 bg-slate-900/50 border border-slate-800 rounded-3xl text-center space-y-3">
          <div className="text-4xl">📭</div>
          <h3 className="text-lg font-semibold text-slate-300">No announcements found</h3>
          <p className="text-xs text-slate-500 max-w-sm mx-auto">
            There are currently no announcements under this category.
          </p>
          {(selectedCategory !== 'All' || searchQuery) && (
            <button
              onClick={() => {
                setSelectedCategory('All');
                setSearchQuery('');
                loadAnnouncements('All', '');
              }}
              className="mt-2 text-xs font-semibold text-indigo-400 hover:underline"
            >
              Clear filters
            </button>
          )}
        </div>
      ) : (
        <div className="space-y-4">
          {announcements.map((anc) => (
            <div
              key={anc.id}
              className="p-6 bg-slate-900/90 border border-slate-800 hover:border-slate-700 rounded-2xl transition-all hover:shadow-xl space-y-4 group"
            >
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                <div className="flex items-center space-x-3">
                  <span className={`px-3 py-1 text-xs font-bold rounded-lg border ${getCategoryBadgeClass(anc.category)}`}>
                    {anc.category || 'Notice'}
                  </span>
                  <span className="text-xs text-slate-500">
                    Published: {formatDate(anc.publishedAt)}
                  </span>
                </div>
              </div>

              <div className="space-y-2">
                <h2
                  onClick={() => setSelectedAnnouncement(anc)}
                  className="text-xl font-bold text-slate-100 group-hover:text-indigo-400 cursor-pointer transition-colors"
                >
                  {anc.title}
                </h2>
                <p className="text-sm text-slate-300 line-clamp-3 leading-relaxed">
                  {anc.content}
                </p>
              </div>

              <div className="pt-2 flex justify-between items-center border-t border-slate-800/80 text-xs">
                <button
                  onClick={() => setSelectedAnnouncement(anc)}
                  className="font-semibold text-indigo-400 hover:text-indigo-300 transition-colors"
                >
                  Read Full Notice →
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Announcement Detail Modal */}
      {selectedAnnouncement && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl max-w-xl w-full p-6 sm:p-8 space-y-6 shadow-2xl relative">
            <div className="flex items-start justify-between">
              <span className={`px-3 py-1 text-xs font-bold rounded-lg border ${getCategoryBadgeClass(selectedAnnouncement.category)}`}>
                {selectedAnnouncement.category || 'General'}
              </span>
              <button
                onClick={() => setSelectedAnnouncement(null)}
                className="w-8 h-8 rounded-full bg-slate-800 text-slate-400 hover:text-white flex items-center justify-center font-bold"
              >
                ✕
              </button>
            </div>

            <div className="space-y-2">
              <h2 className="text-2xl font-bold text-white">{selectedAnnouncement.title}</h2>
              <p className="text-xs text-slate-500">
                Published on {formatDate(selectedAnnouncement.publishedAt)}
              </p>
            </div>

            <div className="p-4 bg-slate-950 border border-slate-800 rounded-2xl space-y-3">
              <p className="text-sm text-slate-200 whitespace-pre-line leading-relaxed">
                {selectedAnnouncement.content}
              </p>
            </div>

            <div className="flex justify-end pt-2">
              <button
                onClick={() => setSelectedAnnouncement(null)}
                className="px-5 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold rounded-xl"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AnnouncementsPage;
