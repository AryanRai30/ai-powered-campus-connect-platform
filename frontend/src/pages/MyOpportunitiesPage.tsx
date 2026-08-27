import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { OpportunityItem } from '../types/campus.types';
import { fetchMyBookmarks, fetchMyApplications } from '../services/campusService';

export const MyOpportunitiesPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'bookmarks' | 'applications'>('bookmarks');
  const [bookmarks, setBookmarks] = useState<OpportunityItem[]>([]);
  const [applications, setApplications] = useState<OpportunityItem[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [bmData, appData] = await Promise.all([
        fetchMyBookmarks(),
        fetchMyApplications(),
      ]);
      setBookmarks(bmData);
      setApplications(appData);
    } catch (err: any) {
      console.error('Failed to load my opportunities', err);
      setError(err.response?.data?.message || 'Failed to load your personal opportunity records.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const isValidExternalUrl = (url?: string): boolean => {
    if (!url || !url.trim()) return false;
    const lower = url.trim().toLowerCase();
    if (
      lower.includes('example.com') ||
      lower.includes('example.org') ||
      lower.includes('example.net') ||
      lower.includes('placeholder')
    ) {
      return false;
    }
    return lower.startsWith('http://') || lower.startsWith('https://');
  };

  const handleOpenLink = (url?: string) => {
    if (!url || !isValidExternalUrl(url)) return;
    window.open(url, '_blank', 'noopener,noreferrer');
  };

  const getStatusBadgeClass = (status?: string) => {
    switch (status?.toUpperCase()) {
      case 'SELECTED':
        return 'bg-emerald-500/20 text-emerald-300 border-emerald-500/30';
      case 'INTERVIEW':
        return 'bg-amber-500/20 text-amber-300 border-amber-500/30';
      case 'REJECTED':
        return 'bg-red-500/20 text-red-300 border-red-500/30';
      default:
        return 'bg-blue-500/20 text-blue-300 border-blue-500/30';
    }
  };

  return (
    <div className="space-y-8">
      {/* Header Banner */}
      <div className="relative p-8 rounded-3xl bg-gradient-to-r from-slate-900 via-slate-900 to-amber-950/40 border border-slate-800 overflow-hidden shadow-2xl flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div className="relative z-10 max-w-2xl space-y-3">
          <div className="inline-flex items-center space-x-2 px-3 py-1 bg-amber-500/10 border border-amber-500/20 text-amber-400 text-xs font-semibold rounded-full">
            <span>⭐ My Personal Dashboard</span>
          </div>
          <h1 className="text-3xl sm:text-4xl font-extrabold text-white tracking-tight">
            My Opportunities
          </h1>
          <p className="text-slate-400 text-sm leading-relaxed">
            Track your bookmarked career listings and monitor application submission statuses in one place.
          </p>
        </div>

        <div className="relative z-10 self-start md:self-auto">
          <Link
            to="/opportunities"
            className="px-5 py-3 bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 rounded-xl text-xs font-semibold transition-colors flex items-center space-x-2 whitespace-nowrap"
          >
            <span>Explore Opportunities</span>
            <span>→</span>
          </Link>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-slate-800 space-x-4">
        <button
          onClick={() => setActiveTab('bookmarks')}
          className={`pb-3 text-sm font-semibold transition-colors relative ${
            activeTab === 'bookmarks'
              ? 'text-amber-400 border-b-2 border-amber-400'
              : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <span>Saved Opportunities</span>
          <span className="ml-2 px-2 py-0.5 bg-slate-800 text-slate-300 text-xs rounded-full">
            {bookmarks.length}
          </span>
        </button>

        <button
          onClick={() => setActiveTab('applications')}
          className={`pb-3 text-sm font-semibold transition-colors relative ${
            activeTab === 'applications'
              ? 'text-amber-400 border-b-2 border-amber-400'
              : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <span>Applied Opportunities</span>
          <span className="ml-2 px-2 py-0.5 bg-slate-800 text-slate-300 text-xs rounded-full">
            {applications.length}
          </span>
        </button>
      </div>

      {/* Tab Content */}
      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {[1, 2].map((n) => (
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
          <h3 className="text-lg font-semibold text-slate-200">Unable to load records</h3>
          <p className="text-xs text-slate-400 max-w-md mx-auto">{error}</p>
          <button
            onClick={() => loadData()}
            className="px-4 py-2 bg-amber-500 text-slate-950 rounded-xl text-xs font-semibold hover:bg-amber-400 transition-colors"
          >
            Retry Loading
          </button>
        </div>
      ) : activeTab === 'bookmarks' ? (
        bookmarks.length === 0 ? (
          <div className="p-12 bg-slate-900/50 border border-slate-800 rounded-3xl text-center space-y-4">
            <div className="text-4xl">⭐</div>
            <h3 className="text-lg font-semibold text-slate-300">No saved opportunities yet</h3>
            <p className="text-xs text-slate-500 max-w-sm mx-auto">
              Bookmark interesting internships, competitions, or workshops to save them for later review.
            </p>
            <Link
              to="/opportunities"
              className="inline-block px-5 py-2.5 bg-amber-500 hover:bg-amber-400 text-slate-950 rounded-xl text-xs font-bold transition-all shadow-lg shadow-amber-500/20"
            >
              Browse Opportunities
            </Link>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {bookmarks.map((opp) => (
              <div
                key={opp.id}
                className="p-6 bg-slate-900/90 border border-slate-800 rounded-2xl flex flex-col justify-between space-y-4 shadow-xl"
              >
                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <span className="px-3 py-1 bg-amber-500/10 border border-amber-500/20 text-amber-400 text-xs font-semibold rounded-lg">
                      {opp.opportunityType || 'OPPORTUNITY'}
                    </span>
                    <span className="text-xs text-amber-400 font-semibold">⭐ Saved</span>
                  </div>

                  <div>
                    <h2 className="text-lg font-bold text-slate-100">{opp.title}</h2>
                    <p className="text-xs text-slate-400">{opp.organization}</p>
                  </div>

                  <p className="text-xs text-slate-400 line-clamp-2 leading-relaxed">{opp.description}</p>

                  <div className="text-xs text-slate-400 pt-1 flex justify-between">
                    <span>📍 {opp.location || 'Remote'}</span>
                    <span>⏳ Deadline: {opp.deadline || 'Open'}</span>
                  </div>
                </div>

                <div className="pt-4 border-t border-slate-800/80 flex items-center justify-between">
                  <Link
                    to="/opportunities"
                    className="text-xs font-semibold text-slate-400 hover:text-slate-200"
                  >
                    View in Catalog →
                  </Link>

                  {isValidExternalUrl(opp.applicationUrl) ? (
                    <button
                      onClick={() => handleOpenLink(opp.applicationUrl)}
                      className="px-4 py-2 bg-amber-500 hover:bg-amber-400 text-slate-950 rounded-xl text-xs font-bold transition-all flex items-center space-x-1"
                    >
                      <span>Open Application</span>
                      <span>↗</span>
                    </button>
                  ) : (
                    <span className="px-3 py-1 bg-slate-800 text-slate-500 border border-slate-700 text-xs font-semibold rounded-xl italic">
                      Link unavailable
                    </span>
                  )}
                </div>
              </div>
            ))}
          </div>
        )
      ) : applications.length === 0 ? (
        <div className="p-12 bg-slate-900/50 border border-slate-800 rounded-3xl text-center space-y-4">
          <div className="text-4xl">📝</div>
          <h3 className="text-lg font-semibold text-slate-300">No tracked applications yet</h3>
          <p className="text-xs text-slate-500 max-w-sm mx-auto">
            When you apply for opportunities, they will automatically be recorded here for tracking.
          </p>
          <Link
            to="/opportunities"
            className="inline-block px-5 py-2.5 bg-amber-500 hover:bg-amber-400 text-slate-950 rounded-xl text-xs font-bold transition-all shadow-lg shadow-amber-500/20"
          >
            Browse Opportunities
          </Link>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {applications.map((opp) => (
            <div
              key={opp.id}
              className="p-6 bg-slate-900/90 border border-slate-800 rounded-2xl flex flex-col justify-between space-y-4 shadow-xl"
            >
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <span className="px-3 py-1 bg-amber-500/10 border border-amber-500/20 text-amber-400 text-xs font-semibold rounded-lg">
                    {opp.opportunityType || 'OPPORTUNITY'}
                  </span>
                  <span className={`px-2.5 py-1 text-xs font-bold rounded-lg border ${getStatusBadgeClass(opp.applicationStatus)}`}>
                    Status: {opp.applicationStatus || 'APPLIED'}
                  </span>
                </div>

                <div>
                  <h2 className="text-lg font-bold text-slate-100">{opp.title}</h2>
                  <p className="text-xs text-slate-400">{opp.organization}</p>
                </div>

                <p className="text-xs text-slate-400 line-clamp-2 leading-relaxed">{opp.description}</p>

                <div className="text-xs text-slate-400 pt-1 flex justify-between">
                  <span>📍 {opp.location || 'Remote'}</span>
                  <span>⏳ Deadline: {opp.deadline || 'Open'}</span>
                </div>
              </div>

              <div className="pt-4 border-t border-slate-800/80 flex items-center justify-between">
                <span className="text-xs text-emerald-400 font-semibold">✓ Tracked Application</span>

                {isValidExternalUrl(opp.applicationUrl) ? (
                  <button
                    onClick={() => handleOpenLink(opp.applicationUrl)}
                    className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 rounded-xl text-xs font-semibold transition-all flex items-center space-x-1"
                  >
                    <span>Re-open Link</span>
                    <span>↗</span>
                  </button>
                ) : (
                  <span className="px-3 py-1 bg-slate-800 text-slate-500 border border-slate-700 text-xs font-semibold rounded-xl italic">
                    Link unavailable
                  </span>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default MyOpportunitiesPage;
