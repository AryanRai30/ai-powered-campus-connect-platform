import React, { useEffect, useState } from 'react';
import { AcademicResourceItem } from '../types/campus.types';
import { fetchAcademicResources } from '../services/campusService';

const RESOURCE_TYPES = ['All', 'NOTES', 'PDF', 'VIDEO', 'WEBSITE', 'OTHER'];
const CATEGORIES = ['All', 'Computer Science', 'Software Engineering', 'Data Science'];

export const ResourcesPage: React.FC = () => {
  const [resources, setResources] = useState<AcademicResourceItem[]>([]);
  const [selectedType, setSelectedType] = useState<string>('All');
  const [selectedCategory, setSelectedCategory] = useState<string>('All');
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedResource, setSelectedResource] = useState<AcademicResourceItem | null>(null);

  const loadResources = async (
    category: string = selectedCategory,
    type: string = selectedType,
    search: string = searchQuery
  ) => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchAcademicResources(category, undefined, type, search);
      setResources(data);
    } catch (err: any) {
      console.error('Failed to load academic resources', err);
      setError(err.response?.data?.message || 'Failed to load academic resources. Please check server connectivity.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadResources(selectedCategory, selectedType, searchQuery);
  }, [selectedCategory, selectedType]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    loadResources(selectedCategory, selectedType, searchQuery);
  };

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

  const getTypeBadgeClass = (type?: string) => {
    switch (type?.toUpperCase()) {
      case 'PDF':
        return 'bg-red-500/10 text-red-400 border-red-500/30';
      case 'VIDEO':
        return 'bg-amber-500/10 text-amber-400 border-amber-500/30';
      case 'WEBSITE':
        return 'bg-blue-500/10 text-blue-400 border-blue-500/30';
      case 'NOTES':
        return 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30';
      default:
        return 'bg-purple-500/10 text-purple-400 border-purple-500/30';
    }
  };

  return (
    <div className="space-y-8">
      {/* Header Banner */}
      <div className="relative p-8 rounded-3xl bg-gradient-to-r from-slate-900 via-slate-900 to-cyan-950/40 border border-slate-800 overflow-hidden shadow-2xl">
        <div className="relative z-10 max-w-2xl space-y-3">
          <div className="inline-flex items-center space-x-2 px-3 py-1 bg-cyan-500/10 border border-cyan-500/20 text-cyan-400 text-xs font-semibold rounded-full">
            <span>📚 Academic Support & Study Materials</span>
          </div>
          <h1 className="text-3xl sm:text-4xl font-extrabold text-white tracking-tight">
            Academic Resources
          </h1>
          <p className="text-slate-400 text-sm leading-relaxed">
            Access curated course lecture notes, reference PDFs, video tutorials, and technical handbooks across your subjects.
          </p>
        </div>
      </div>

      {/* Controls: Search, Type Pills, and Category Dropdown */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        {/* Type Pills */}
        <div className="flex items-center space-x-2 overflow-x-auto pb-2 md:pb-0 scrollbar-none">
          {RESOURCE_TYPES.map((type) => (
            <button
              key={type}
              onClick={() => setSelectedType(type)}
              className={`px-4 py-2 text-xs font-semibold rounded-xl border transition-all whitespace-nowrap ${
                selectedType === type
                  ? 'bg-cyan-500 text-slate-950 border-cyan-500 font-bold shadow-lg shadow-cyan-500/20'
                  : 'bg-slate-900 text-slate-400 border-slate-800 hover:bg-slate-800 hover:text-slate-200'
              }`}
            >
              {type}
            </button>
          ))}
        </div>

        {/* Filter Dropdown & Search Form */}
        <div className="flex flex-wrap items-center gap-3">
          <select
            value={selectedCategory}
            onChange={(e) => setSelectedCategory(e.target.value)}
            className="px-3 py-2.5 text-xs bg-slate-900 border border-slate-800 rounded-xl text-slate-200 focus:outline-none focus:border-cyan-500 transition-colors"
          >
            {CATEGORIES.map((cat) => (
              <option key={cat} value={cat}>
                {cat === 'All' ? 'All Categories' : cat}
              </option>
            ))}
          </select>

          <form onSubmit={handleSearchSubmit} className="flex items-center space-x-2 flex-grow sm:flex-grow-0">
            <input
              type="text"
              placeholder="Search topic, subject, or title..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full sm:w-56 px-4 py-2.5 text-xs bg-slate-900 border border-slate-800 rounded-xl text-slate-200 focus:outline-none focus:border-cyan-500 transition-colors"
            />
            <button
              type="submit"
              className="px-4 py-2.5 text-xs font-semibold bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 rounded-xl transition-colors"
            >
              Search
            </button>
          </form>
        </div>
      </div>

      {/* Main Grid View */}
      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[1, 2, 3, 4, 5, 6].map((n) => (
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
          <h3 className="text-lg font-semibold text-slate-200">Unable to load academic resources</h3>
          <p className="text-xs text-slate-400 max-w-md mx-auto">{error}</p>
          <button
            onClick={() => loadResources()}
            className="px-4 py-2 bg-cyan-500 text-slate-950 rounded-xl text-xs font-semibold hover:bg-cyan-400 transition-colors"
          >
            Retry Loading
          </button>
        </div>
      ) : resources.length === 0 ? (
        <div className="p-12 bg-slate-900/50 border border-slate-800 rounded-3xl text-center space-y-3">
          <div className="text-4xl">📚</div>
          <h3 className="text-lg font-semibold text-slate-300">No academic resources available yet</h3>
          <p className="text-xs text-slate-500 max-w-md mx-auto">
            Faculty resources will appear here when they are published.
          </p>
          {(selectedCategory !== 'All' || selectedType !== 'All' || searchQuery) && (
            <button
              onClick={() => {
                setSelectedCategory('All');
                setSelectedType('All');
                setSearchQuery('');
                loadResources('All', 'All', '');
              }}
              className="mt-2 text-xs font-semibold text-cyan-400 hover:underline"
            >
              Clear filters
            </button>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {resources.map((res) => (
            <div
              key={res.id}
              className="p-6 bg-slate-900/90 border border-slate-800 hover:border-slate-700 rounded-2xl flex flex-col justify-between space-y-5 transition-all hover:shadow-xl group"
            >
              <div className="space-y-3">
                <div className="flex items-center justify-between gap-2">
                  <span className={`px-2.5 py-1 text-xs font-bold rounded-lg border ${getTypeBadgeClass(res.resourceType)}`}>
                    {res.resourceType || 'RESOURCE'}
                  </span>
                  <span className="px-2.5 py-1 bg-slate-800 text-slate-400 border border-slate-700 text-xs font-medium rounded-lg truncate max-w-[140px]">
                    {res.subject}
                  </span>
                </div>

                <h2 className="text-lg font-bold text-slate-100 group-hover:text-cyan-400 transition-colors line-clamp-2">
                  {res.title}
                </h2>

                <p className="text-xs text-slate-400 line-clamp-3 leading-relaxed">
                  {res.description}
                </p>

                {res.category && (
                  <div className="text-xs text-slate-500 pt-1">
                    Category: <span className="text-slate-300 font-medium">{res.category}</span>
                  </div>
                )}
              </div>

              {/* Action buttons footer */}
              <div className="pt-4 border-t border-slate-800/80 flex items-center justify-between gap-2">
                <button
                  onClick={() => setSelectedResource(res)}
                  className="text-xs font-semibold text-slate-400 hover:text-slate-200 transition-colors"
                >
                  View Details →
                </button>

                {isValidExternalUrl(res.resourceUrl) ? (
                  <button
                    onClick={() => handleOpenLink(res.resourceUrl)}
                    className="px-3.5 py-2 bg-cyan-500 hover:bg-cyan-400 text-slate-950 rounded-xl text-xs font-bold transition-all shadow-md shadow-cyan-500/20 flex items-center space-x-1"
                  >
                    <span>Open Resource</span>
                    <span>↗</span>
                  </button>
                ) : (
                  <span className="px-3 py-1.5 bg-slate-800/60 text-slate-500 border border-slate-800 text-xs rounded-xl font-medium italic">
                    Link unavailable
                  </span>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Resource Details Modal */}
      {selectedResource && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl max-w-xl w-full p-6 sm:p-8 space-y-6 shadow-2xl relative">
            <div className="flex items-start justify-between">
              <span className={`px-3 py-1 text-xs font-bold rounded-lg border ${getTypeBadgeClass(selectedResource.resourceType)}`}>
                {selectedResource.resourceType || 'RESOURCE'}
              </span>
              <button
                onClick={() => setSelectedResource(null)}
                className="w-8 h-8 rounded-full bg-slate-800 text-slate-400 hover:text-white flex items-center justify-center font-bold"
              >
                ✕
              </button>
            </div>

            <div className="space-y-2">
              <h2 className="text-2xl font-bold text-white">{selectedResource.title}</h2>
              <p className="text-xs text-slate-400">
                Subject: <span className="text-slate-200 font-semibold">{selectedResource.subject}</span>
              </p>
            </div>

            <div className="p-4 bg-slate-950 border border-slate-800 rounded-2xl grid grid-cols-2 gap-4 text-xs">
              <div>
                <span className="text-slate-500 block">Category</span>
                <span className="text-slate-200 font-semibold">{selectedResource.category || 'General'}</span>
              </div>
              <div>
                <span className="text-slate-500 block">Resource Format</span>
                <span className="text-slate-200 font-semibold">{selectedResource.resourceType || 'Standard'}</span>
              </div>
              <div className="col-span-2">
                <span className="text-slate-500 block">Resource Link</span>
                {isValidExternalUrl(selectedResource.resourceUrl) ? (
                  <span className="text-cyan-400 font-mono font-medium truncate block">{selectedResource.resourceUrl}</span>
                ) : (
                  <span className="text-slate-500 font-mono font-medium italic block">Link unavailable</span>
                )}
              </div>
            </div>

            <div className="space-y-2">
              <h4 className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Resource Summary</h4>
              <p className="text-sm text-slate-300 whitespace-pre-line leading-relaxed">
                {selectedResource.description}
              </p>
            </div>

            <div className="flex justify-end space-x-3 pt-4 border-t border-slate-800">
              <button
                onClick={() => setSelectedResource(null)}
                className="px-5 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold rounded-xl"
              >
                Close
              </button>

              {isValidExternalUrl(selectedResource.resourceUrl) ? (
                <button
                  onClick={() => handleOpenLink(selectedResource.resourceUrl)}
                  className="px-5 py-2.5 bg-cyan-500 hover:bg-cyan-400 text-slate-950 rounded-xl text-xs font-bold transition-all flex items-center space-x-1"
                >
                  <span>Open Resource</span>
                  <span>↗</span>
                </button>
              ) : (
                <button
                  disabled
                  className="px-5 py-2.5 bg-slate-800 text-slate-500 border border-slate-700 text-xs font-semibold rounded-xl cursor-not-allowed italic"
                >
                  Link unavailable
                </button>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ResourcesPage;
