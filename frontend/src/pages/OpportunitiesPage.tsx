import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { OpportunityItem } from '../types/campus.types';
import { fetchOpportunities, bookmarkOpportunity, applyForOpportunity } from '../services/campusService';

const OPPORTUNITY_TYPES = ['All', 'INTERNSHIP', 'JOB', 'SCHOLARSHIP', 'COMPETITION', 'WORKSHOP', 'OTHER'];

export const OpportunitiesPage: React.FC = () => {
  const [opportunities, setOpportunities] = useState<OpportunityItem[]>([]);
  const [selectedType, setSelectedType] = useState<string>('All');
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [actionInProgress, setActionInProgress] = useState<number | null>(null);
  const [selectedOpportunity, setSelectedOpportunity] = useState<OpportunityItem | null>(null);
  const [toastMessage, setToastMessage] = useState<{ text: string; type: 'success' | 'error' } | null>(null);

  const loadOpportunities = async (type: string = selectedType, search: string = searchQuery) => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchOpportunities(type, undefined, search);
      setOpportunities(data);
    } catch (err: any) {
      console.error('Failed to load opportunities', err);
      setError(err.response?.data?.message || 'Failed to load career opportunities. Please check server connectivity.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadOpportunities(selectedType, searchQuery);
  }, [selectedType]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    loadOpportunities(selectedType, searchQuery);
  };

  const handleBookmark = async (opportunityId: number) => {
    setActionInProgress(opportunityId);
    setToastMessage(null);
    try {
      await bookmarkOpportunity(opportunityId);
      setToastMessage({ text: 'Opportunity bookmarked successfully!', type: 'success' });
      await loadOpportunities(selectedType, searchQuery);
      if (selectedOpportunity && selectedOpportunity.id === opportunityId) {
        setSelectedOpportunity((prev) => prev ? { ...prev, bookmarked: true } : null);
      }
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Bookmark update failed.';
      setToastMessage({ text: msg, type: 'error' });
    } finally {
      setActionInProgress(null);
    }
  };

  const handleApply = async (opp: OpportunityItem) => {
    setActionInProgress(opp.id);
    setToastMessage(null);
    try {
      if (!opp.applied) {
        await applyForOpportunity(opp.id);
      }
      setToastMessage({ text: 'Application tracked in My Opportunities!', type: 'success' });
      await loadOpportunities(selectedType, searchQuery);
      if (selectedOpportunity && selectedOpportunity.id === opp.id) {
        setSelectedOpportunity((prev) => prev ? { ...prev, applied: true, applicationStatus: 'APPLIED' } : null);
      }
      if (opp.applicationUrl) {
        window.open(opp.applicationUrl, '_blank', 'noopener,noreferrer');
      }
    } catch (err: any) {
      // If already applied, still allow opening external link
      if (opp.applicationUrl) {
        window.open(opp.applicationUrl, '_blank', 'noopener,noreferrer');
      } else {
        const msg = err.response?.data?.message || 'Application tracking failed.';
        setToastMessage({ text: msg, type: 'error' });
      }
    } finally {
      setActionInProgress(null);
    }
  };

  const getTypeBadgeClass = (type?: string) => {
    switch (type?.toUpperCase()) {
      case 'INTERNSHIP':
        return 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30';
      case 'JOB':
        return 'bg-blue-500/10 text-blue-400 border-blue-500/30';
      case 'SCHOLARSHIP':
        return 'bg-amber-500/10 text-amber-400 border-amber-500/30';
      case 'COMPETITION':
        return 'bg-purple-500/10 text-purple-400 border-purple-500/30';
      case 'WORKSHOP':
        return 'bg-cyan-500/10 text-cyan-400 border-cyan-500/30';
      default:
        return 'bg-slate-800 text-slate-300 border-slate-700';
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
      <div className="relative p-8 rounded-3xl bg-gradient-to-r from-slate-900 via-slate-900 to-amber-950/40 border border-slate-800 overflow-hidden shadow-2xl flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div className="relative z-10 max-w-2xl space-y-3">
          <div className="inline-flex items-center space-x-2 px-3 py-1 bg-amber-500/10 border border-amber-500/20 text-amber-400 text-xs font-semibold rounded-full">
            <span>💼 Career Support & Opportunities</span>
          </div>
          <h1 className="text-3xl sm:text-4xl font-extrabold text-white tracking-tight">
            Student Opportunities
          </h1>
          <p className="text-slate-400 text-sm leading-relaxed">
            Discover internships, full-time roles, competitive coding hackathons, scholarships, and skill workshops. Bookmark listings and track your applications.
          </p>
        </div>

        <div className="relative z-10 self-start md:self-auto">
          <Link
            to="/my-opportunities"
            className="px-5 py-3 bg-amber-500 hover:bg-amber-400 text-slate-950 rounded-xl text-xs font-bold transition-all shadow-lg shadow-amber-500/20 flex items-center space-x-2 whitespace-nowrap"
          >
            <span>My Tracked & Saved</span>
            <span>→</span>
          </Link>
        </div>
      </div>

      {/* Controls: Search and Filter Tabs */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        {/* Type Pills */}
        <div className="flex items-center space-x-2 overflow-x-auto pb-2 md:pb-0 scrollbar-none">
          {OPPORTUNITY_TYPES.map((type) => (
            <button
              key={type}
              onClick={() => setSelectedType(type)}
              className={`px-4 py-2 text-xs font-semibold rounded-xl border transition-all whitespace-nowrap ${
                selectedType === type
                  ? 'bg-amber-500 text-slate-950 border-amber-500 font-bold shadow-lg shadow-amber-500/20'
                  : 'bg-slate-900 text-slate-400 border-slate-800 hover:bg-slate-800 hover:text-slate-200'
              }`}
            >
              {type}
            </button>
          ))}
        </div>

        {/* Search Bar */}
        <form onSubmit={handleSearchSubmit} className="flex items-center space-x-2">
          <div className="relative flex-grow">
            <input
              type="text"
              placeholder="Search opportunity, company, skills..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full sm:w-64 px-4 py-2.5 text-xs bg-slate-900 border border-slate-800 rounded-xl text-slate-200 focus:outline-none focus:border-amber-500 transition-colors"
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
          <h3 className="text-lg font-semibold text-slate-200">Unable to load opportunities</h3>
          <p className="text-xs text-slate-400 max-w-md mx-auto">{error}</p>
          <button
            onClick={() => loadOpportunities()}
            className="px-4 py-2 bg-amber-500 text-slate-950 rounded-xl text-xs font-semibold hover:bg-amber-400 transition-colors"
          >
            Retry Loading
          </button>
        </div>
      ) : opportunities.length === 0 ? (
        <div className="p-12 bg-slate-900/50 border border-slate-800 rounded-3xl text-center space-y-3">
          <div className="text-4xl">🚀</div>
          <h3 className="text-lg font-semibold text-slate-300">No opportunities found</h3>
          <p className="text-xs text-slate-500 max-w-sm mx-auto">
            No active listings match your current filters or search query. Try clearing search filters.
          </p>
          {(selectedType !== 'All' || searchQuery) && (
            <button
              onClick={() => {
                setSelectedType('All');
                setSearchQuery('');
                loadOpportunities('All', '');
              }}
              className="mt-2 text-xs font-semibold text-amber-400 hover:underline"
            >
              Clear filters
            </button>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {opportunities.map((opp) => (
            <div
              key={opp.id}
              className="p-6 bg-slate-900/90 border border-slate-800 hover:border-slate-700 rounded-2xl flex flex-col justify-between space-y-5 transition-all hover:shadow-xl group"
            >
              <div className="space-y-3">
                <div className="flex items-center justify-between gap-2">
                  <span className={`px-3 py-1 text-xs font-bold rounded-lg border ${getTypeBadgeClass(opp.opportunityType)}`}>
                    {opp.opportunityType || 'OPPORTUNITY'}
                  </span>

                  <div className="flex items-center space-x-2">
                    {opp.applied && (
                      <span className="px-2.5 py-1 bg-emerald-500/20 text-emerald-300 border border-emerald-500/30 text-xs font-bold rounded-lg flex items-center space-x-1">
                        <span>✓</span>
                        <span>{opp.applicationStatus || 'Applied'}</span>
                      </span>
                    )}

                    <button
                      onClick={() => handleBookmark(opp.id)}
                      disabled={opp.bookmarked || actionInProgress === opp.id}
                      className={`p-1.5 rounded-lg border text-xs font-semibold transition-colors ${
                        opp.bookmarked
                          ? 'bg-amber-500/20 text-amber-400 border-amber-500/30 cursor-default'
                          : 'bg-slate-800 text-slate-400 border-slate-700 hover:text-white'
                      }`}
                      title={opp.bookmarked ? 'Saved to Bookmarks' : 'Bookmark Opportunity'}
                    >
                      {opp.bookmarked ? '⭐ Saved' : '☆ Bookmark'}
                    </button>
                  </div>
                </div>

                <div>
                  <h2 className="text-xl font-bold text-slate-100 group-hover:text-amber-400 transition-colors">
                    {opp.title}
                  </h2>
                  <p className="text-xs font-semibold text-amber-400/90 mt-0.5">{opp.organization}</p>
                </div>

                <p className="text-xs text-slate-400 line-clamp-2 leading-relaxed">
                  {opp.description}
                </p>

                <div className="pt-2 grid grid-cols-2 gap-2 text-xs text-slate-300 border-t border-slate-800/80">
                  {opp.location && (
                    <div className="flex items-center space-x-2">
                      <span className="text-slate-500">📍</span>
                      <span className="truncate">{opp.location}</span>
                    </div>
                  )}
                  {opp.deadline && (
                    <div className="flex items-center space-x-2">
                      <span className="text-slate-500">⏳</span>
                      <span>Deadline: {opp.deadline}</span>
                    </div>
                  )}
                  {opp.skills && (
                    <div className="flex items-center space-x-2 col-span-2">
                      <span className="text-slate-500">🛠️</span>
                      <span className="text-slate-400 truncate">{opp.skills}</span>
                    </div>
                  )}
                </div>
              </div>

              {/* Action buttons footer */}
              <div className="pt-4 border-t border-slate-800/80 flex items-center justify-between gap-3">
                <button
                  onClick={() => setSelectedOpportunity(opp)}
                  className="text-xs font-semibold text-slate-400 hover:text-slate-200 transition-colors"
                >
                  View Details →
                </button>

                <button
                  onClick={() => handleApply(opp)}
                  disabled={actionInProgress === opp.id}
                  className={`px-4 py-2 rounded-xl text-xs font-bold transition-all shadow-md flex items-center space-x-1 ${
                    opp.applied
                      ? 'bg-slate-800 text-emerald-400 border border-emerald-500/20'
                      : 'bg-amber-500 hover:bg-amber-400 text-slate-950 shadow-amber-500/20'
                  }`}
                >
                  <span>{opp.applied ? 'Applied / Re-open Link' : 'Apply Now'}</span>
                  <span>↗</span>
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Opportunity Details Modal */}
      {selectedOpportunity && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl max-w-xl w-full p-6 sm:p-8 space-y-6 shadow-2xl relative">
            <div className="flex items-start justify-between">
              <span className={`px-3 py-1 text-xs font-bold rounded-lg border ${getTypeBadgeClass(selectedOpportunity.opportunityType)}`}>
                {selectedOpportunity.opportunityType || 'OPPORTUNITY'}
              </span>
              <button
                onClick={() => setSelectedOpportunity(null)}
                className="w-8 h-8 rounded-full bg-slate-800 text-slate-400 hover:text-white flex items-center justify-center font-bold"
              >
                ✕
              </button>
            </div>

            <div className="space-y-1">
              <h2 className="text-2xl font-bold text-white">{selectedOpportunity.title}</h2>
              <p className="text-xs text-amber-400 font-semibold">{selectedOpportunity.organization}</p>
            </div>

            <div className="p-4 bg-slate-950 border border-slate-800 rounded-2xl grid grid-cols-2 gap-4 text-xs">
              <div>
                <span className="text-slate-500 block">Location</span>
                <span className="text-slate-200 font-semibold">{selectedOpportunity.location || 'Remote'}</span>
              </div>
              <div>
                <span className="text-slate-500 block">Application Deadline</span>
                <span className="text-slate-200 font-semibold">{selectedOpportunity.deadline || 'Open'}</span>
              </div>
              <div className="col-span-2">
                <span className="text-slate-500 block">Required Skills</span>
                <span className="text-slate-200 font-semibold">{selectedOpportunity.skills || 'General Competencies'}</span>
              </div>
              <div>
                <span className="text-slate-500 block">Bookmark Status</span>
                <span className="text-slate-200 font-semibold">{selectedOpportunity.bookmarked ? 'Saved to Bookmarks' : 'Not Saved'}</span>
              </div>
              <div>
                <span className="text-slate-500 block">Application Status</span>
                <span className="text-slate-200 font-semibold">{selectedOpportunity.applied ? selectedOpportunity.applicationStatus || 'Applied' : 'Not Applied'}</span>
              </div>
            </div>

            <div className="space-y-2">
              <h4 className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Opportunity Description</h4>
              <p className="text-sm text-slate-300 whitespace-pre-line leading-relaxed">
                {selectedOpportunity.description}
              </p>
            </div>

            <div className="flex items-center justify-between pt-4 border-t border-slate-800">
              <button
                onClick={() => handleBookmark(selectedOpportunity.id)}
                disabled={selectedOpportunity.bookmarked}
                className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold rounded-xl border border-slate-700"
              >
                {selectedOpportunity.bookmarked ? '⭐ Saved' : '☆ Bookmark'}
              </button>

              <div className="flex space-x-2">
                <button
                  onClick={() => setSelectedOpportunity(null)}
                  className="px-5 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold rounded-xl"
                >
                  Close
                </button>

                <button
                  onClick={() => handleApply(selectedOpportunity)}
                  className="px-5 py-2.5 bg-amber-500 hover:bg-amber-400 text-slate-950 rounded-xl text-xs font-bold transition-all flex items-center space-x-1"
                >
                  <span>{selectedOpportunity.applied ? 'Applied / Re-open Link' : 'Apply Now'}</span>
                  <span>↗</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default OpportunitiesPage;
