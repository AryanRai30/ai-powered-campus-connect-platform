import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import {
  searchCampusKnowledge,
  CampusSearchResultItem,
} from '../services/campusSearchService';
import {
  SearchIcon,
  SparklesIcon,
  BookOpenIcon,
  MegaphoneIcon,
  CalendarIcon,
  BriefcaseIcon,
  UsersIcon,
  AlertCircleIcon,
  ArrowRightIcon,
  ClockIcon,
  MapPinIcon,
  TagIcon,
  FilterIcon,
} from '../components/common/Icons';

const PRESET_SEARCHES = [
  'programming clubs',
  'academic resources for students',
  'backend development internships',
  'upcoming campus events',
  'student opportunities',
];

const CATEGORY_TABS = [
  { key: 'ALL', label: 'All Categories' },
  { key: 'ANNOUNCEMENT', label: 'Bulletins', icon: MegaphoneIcon },
  { key: 'EVENT', label: 'Events', icon: CalendarIcon },
  { key: 'ACADEMIC_RESOURCE', label: 'Resources', icon: BookOpenIcon },
  { key: 'CLUB', label: 'Clubs', icon: UsersIcon },
  { key: 'OPPORTUNITY', label: 'Opportunities', icon: BriefcaseIcon },
];

export const CampusSearchPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();

  const initialQuery = searchParams.get('q') || '';
  const initialCategory = searchParams.get('category') || 'ALL';

  const [query, setQuery] = useState(initialQuery);
  const [activeCategory, setActiveCategory] = useState(initialCategory);
  const [results, setResults] = useState<CampusSearchResultItem[]>([]);
  const [totalResults, setTotalResults] = useState(0);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const executeSearch = async (searchQuery: string, category: string) => {
    const q = searchQuery.trim();
    if (!q) return;

    setLoading(true);
    setError(null);
    setSearched(true);

    try {
      const res = await searchCampusKnowledge(q, category, 20);
      setResults(res.results || []);
      setTotalResults(res.totalResults || 0);
    } catch (err: any) {
      setError(
        err?.response?.data?.message ||
          'Failed to perform semantic search. Please check your network connection and try again.'
      );
      setResults([]);
      setTotalResults(0);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (initialQuery) {
      executeSearch(initialQuery, initialCategory);
    }
  }, []);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!query.trim()) return;
    setSearchParams({ q: query.trim(), category: activeCategory });
    executeSearch(query.trim(), activeCategory);
  };

  const handleCategoryChange = (categoryKey: string) => {
    setActiveCategory(categoryKey);
    if (query.trim()) {
      setSearchParams({ q: query.trim(), category: categoryKey });
      executeSearch(query.trim(), categoryKey);
    }
  };

  const handlePresetClick = (presetQuery: string) => {
    setQuery(presetQuery);
    setSearchParams({ q: presetQuery, category: activeCategory });
    executeSearch(presetQuery, activeCategory);
  };

  const getSourceBadgeStyle = (type?: string) => {
    switch (type?.toUpperCase()) {
      case 'ANNOUNCEMENT':
        return 'bg-amber-50 text-amber-700 border-amber-200';
      case 'EVENT':
        return 'bg-indigo-50 text-indigo-700 border-indigo-200';
      case 'ACADEMIC_RESOURCE':
        return 'bg-emerald-50 text-emerald-700 border-emerald-200';
      case 'CLUB':
        return 'bg-purple-50 text-purple-700 border-purple-200';
      case 'OPPORTUNITY':
        return 'bg-blue-50 text-blue-700 border-blue-200';
      default:
        return 'bg-slate-100 text-slate-700 border-slate-200';
    }
  };

  const getSourceIcon = (type?: string) => {
    switch (type?.toUpperCase()) {
      case 'ANNOUNCEMENT':
        return <MegaphoneIcon size={14} className="shrink-0" />;
      case 'EVENT':
        return <CalendarIcon size={14} className="shrink-0" />;
      case 'ACADEMIC_RESOURCE':
        return <BookOpenIcon size={14} className="shrink-0" />;
      case 'CLUB':
        return <UsersIcon size={14} className="shrink-0" />;
      case 'OPPORTUNITY':
        return <BriefcaseIcon size={14} className="shrink-0" />;
      default:
        return <TagIcon size={14} className="shrink-0" />;
    }
  };

  const getNavigationRoute = (item: CampusSearchResultItem) => {
    const type = item.sourceType?.toUpperCase();
    switch (type) {
      case 'ANNOUNCEMENT':
        return '/announcements';
      case 'EVENT':
        return '/events';
      case 'ACADEMIC_RESOURCE':
        return '/resources';
      case 'CLUB':
        return '/clubs';
      case 'OPPORTUNITY':
        return '/opportunities';
      default:
        return '/dashboard';
    }
  };

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      {/* Search Header Banner */}
      <div className="bg-gradient-to-r from-brand-700 via-indigo-700 to-brand-900 rounded-3xl p-6 md:p-8 text-white shadow-xl relative overflow-hidden">
        <div className="absolute top-0 right-0 -mt-8 -mr-8 w-64 h-64 bg-white/5 rounded-full blur-2xl pointer-events-none" />
        <div className="relative z-10 space-y-4 max-w-2xl">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white/10 backdrop-blur-md border border-white/20 text-xs font-semibold text-brand-100">
            <SparklesIcon size={14} className="text-amber-300 animate-pulse" />
            <span>AI Vector Embedding Search Engine</span>
          </div>

          <h1 className="text-2xl md:text-3xl font-black tracking-tight leading-tight">
            Semantic Campus Search
          </h1>
          <p className="text-sm text-brand-100 font-medium">
            Search campus announcements, events, study resources, clubs, and career opportunities using natural language.
          </p>

          {/* Search Input Bar */}
          <form onSubmit={handleSearchSubmit} className="pt-2">
            <div className="flex flex-col sm:flex-row items-center gap-2 bg-white/10 backdrop-blur-md p-2 rounded-2xl border border-white/20 shadow-inner">
              <div className="relative flex-1 w-full flex items-center">
                <SearchIcon size={20} className="absolute left-3.5 text-brand-200" />
                <input
                  type="text"
                  value={query}
                  onChange={(e) => setQuery(e.target.value)}
                  placeholder="e.g. 'programming clubs', 'upcoming events', 'internships'..."
                  className="w-full pl-11 pr-4 py-3 bg-white/90 focus:bg-white text-slate-900 placeholder:text-slate-400 rounded-xl text-sm font-medium focus:outline-none focus:ring-2 focus:ring-amber-400 transition-all"
                />
              </div>
              <button
                type="submit"
                disabled={loading || !query.trim()}
                className="w-full sm:w-auto px-6 py-3 bg-amber-400 hover:bg-amber-300 text-slate-900 font-black text-sm rounded-xl shadow-md disabled:opacity-50 disabled:cursor-not-allowed transition-all flex items-center justify-center gap-2 shrink-0"
              >
                {loading ? (
                  <>
                    <span className="w-4 h-4 border-2 border-slate-900/30 border-t-slate-900 rounded-full animate-spin" />
                    <span>Searching...</span>
                  </>
                ) : (
                  <>
                    <span>Search</span>
                    <SearchIcon size={18} />
                  </>
                )}
              </button>
            </div>
          </form>

          {/* Natural Language Preset Search Chips */}
          <div className="flex flex-wrap items-center gap-2 pt-1">
            <span className="text-xs font-semibold text-brand-200">Try searching:</span>
            {PRESET_SEARCHES.map((preset) => (
              <button
                key={preset}
                type="button"
                onClick={() => handlePresetClick(preset)}
                className="text-xs bg-white/15 hover:bg-white/25 text-white px-3 py-1 rounded-full transition-all border border-white/10 font-medium"
              >
                {preset}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Category Filter Tabs */}
      <div className="bg-white rounded-2xl p-3 border border-slate-200/80 shadow-subtle flex items-center gap-2 overflow-x-auto">
        <FilterIcon size={18} className="text-slate-400 ml-2 shrink-0" />
        <span className="text-xs font-bold text-slate-400 uppercase tracking-wider shrink-0 mr-1">Filter:</span>
        {CATEGORY_TABS.map((tab) => {
          const Icon = tab.icon;
          const isActive = activeCategory === tab.key;
          return (
            <button
              key={tab.key}
              onClick={() => handleCategoryChange(tab.key)}
              className={`flex items-center gap-2 px-3.5 py-2 rounded-xl text-xs font-bold transition-all shrink-0 ${
                isActive
                  ? 'bg-brand-600 text-white shadow-sm'
                  : 'bg-slate-50 text-slate-600 hover:bg-slate-100 hover:text-slate-900 border border-slate-200/60'
              }`}
            >
              {Icon && <Icon size={14} />}
              <span>{tab.label}</span>
            </button>
          );
        })}
      </div>

      {/* Error Alert State */}
      {error && (
        <div className="p-4 bg-rose-50 border border-rose-200 text-rose-800 rounded-2xl flex items-start gap-3 shadow-xs animate-fade-in">
          <AlertCircleIcon size={20} className="text-rose-600 shrink-0 mt-0.5" />
          <div className="flex-1">
            <h4 className="font-bold text-sm">Search Error</h4>
            <p className="text-xs text-rose-700 mt-1">{error}</p>
          </div>
        </div>
      )}

      {/* Loading Skeleton State */}
      {loading && (
        <div className="space-y-4">
          {[1, 2, 3].map((n) => (
            <div
              key={n}
              className="bg-white rounded-2xl p-6 border border-slate-200/80 shadow-subtle animate-pulse space-y-3"
            >
              <div className="flex items-center justify-between">
                <div className="h-5 bg-slate-200 rounded w-1/3" />
                <div className="h-5 bg-slate-200 rounded-full w-20" />
              </div>
              <div className="h-4 bg-slate-150 rounded w-full" />
              <div className="h-4 bg-slate-150 rounded w-3/4" />
            </div>
          ))}
        </div>
      )}

      {/* Results Header */}
      {!loading && searched && !error && (
        <div className="flex items-center justify-between px-1">
          <p className="text-xs font-bold text-slate-500 uppercase tracking-wider">
            Found <span className="text-brand-600 font-extrabold">{totalResults}</span> ranked results for &ldquo;
            <span className="text-slate-900">{query}</span>&rdquo;
          </p>
        </div>
      )}

      {/* Empty Results State */}
      {!loading && searched && results.length === 0 && !error && (
        <div className="bg-white rounded-2xl p-12 border border-slate-200/80 shadow-subtle text-center space-y-4">
          <div className="w-16 h-16 rounded-2xl bg-amber-50 text-amber-600 flex items-center justify-center mx-auto shadow-inner">
            <SearchIcon size={32} />
          </div>
          <div className="max-w-md mx-auto space-y-1">
            <h3 className="text-lg font-bold text-slate-900">No matching campus records found</h3>
            <p className="text-xs text-slate-500 font-medium">
              We couldn&apos;t find any authorized announcements, events, academic resources, clubs, or opportunities matching &ldquo;{query}&rdquo;.
            </p>
          </div>
          <div className="pt-2 flex flex-wrap justify-center gap-2">
            <span className="text-xs text-slate-400 self-center">Try asking about:</span>
            {PRESET_SEARCHES.map((preset) => (
              <button
                key={preset}
                onClick={() => handlePresetClick(preset)}
                className="text-xs bg-slate-100 hover:bg-brand-50 hover:text-brand-600 text-slate-700 px-3 py-1.5 rounded-xl border border-slate-200 font-medium transition-all"
              >
                {preset}
              </button>
            ))}
          </div>
        </div>
      )}

      {/* Results List Cards */}
      {!loading && results.length > 0 && (
        <div className="space-y-4">
          {results.map((item) => {
            const navRoute = getNavigationRoute(item);
            const scorePct = Math.round(item.score * 100);

            return (
              <div
                key={item.id}
                className="bg-white rounded-2xl p-6 border border-slate-200/80 shadow-subtle hover:shadow-md transition-all duration-200 space-y-3 group"
              >
                {/* Card Top Row: Type & Similarity Score */}
                <div className="flex items-center justify-between gap-3">
                  <div className="flex items-center gap-2">
                    <span
                      className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold border uppercase tracking-wider ${getSourceBadgeStyle(
                        item.sourceType
                      )}`}
                    >
                      {getSourceIcon(item.sourceType)}
                      <span>{item.sourceType?.replace('_', ' ')}</span>
                    </span>

                    {item.category && item.category !== item.sourceType && (
                      <span className="px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-slate-100 text-slate-600 border border-slate-200">
                        {item.category}
                      </span>
                    )}
                  </div>

                  {/* Similarity Score Pill */}
                  <div className="flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-50 border border-emerald-200 text-emerald-700 text-xs font-extrabold" title="Semantic Vector Similarity Match Score">
                    <SparklesIcon size={13} className="text-emerald-600" />
                    <span>{scorePct > 100 ? 100 : scorePct}% Match</span>
                  </div>
                </div>

                {/* Title */}
                <h3 className="text-lg font-bold text-slate-900 group-hover:text-brand-600 transition-colors leading-snug">
                  {item.title}
                </h3>

                {/* Content Preview */}
                <p className="text-xs text-slate-600 leading-relaxed font-normal whitespace-pre-line line-clamp-4 bg-slate-50/70 p-3.5 rounded-xl border border-slate-100">
                  {item.content}
                </p>

                {/* Metadata Tags & Action Button */}
                <div className="pt-2 flex flex-wrap items-center justify-between gap-3 border-t border-slate-100">
                  <div className="flex flex-wrap items-center gap-3 text-xs text-slate-500 font-medium">
                    {item.metadata?.targetDepartment && (
                      <span className="inline-flex items-center gap-1">
                        <TagIcon size={12} className="text-slate-400" />
                        <span>Dept: {item.metadata.targetDepartment}</span>
                      </span>
                    )}
                    {item.metadata?.venue && (
                      <span className="inline-flex items-center gap-1">
                        <MapPinIcon size={12} className="text-slate-400" />
                        <span>{item.metadata.venue}</span>
                      </span>
                    )}
                    {item.metadata?.eventDate && (
                      <span className="inline-flex items-center gap-1">
                        <ClockIcon size={12} className="text-slate-400" />
                        <span>{item.metadata.eventDate}</span>
                      </span>
                    )}
                  </div>

                  <button
                    onClick={() => navigate(navRoute)}
                    className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-bold text-brand-600 hover:bg-brand-50 border border-brand-200 transition-colors ml-auto"
                  >
                    <span>View in Portal</span>
                    <ArrowRightIcon size={14} />
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Initial Landing State before searching */}
      {!searched && !loading && (
        <div className="bg-white rounded-2xl p-10 border border-slate-200/80 shadow-subtle text-center space-y-4">
          <div className="w-16 h-16 rounded-2xl bg-gradient-to-tr from-brand-600 to-indigo-600 text-white flex items-center justify-center mx-auto shadow-md">
            <SparklesIcon size={30} />
          </div>
          <div className="max-w-md mx-auto space-y-1">
            <h3 className="text-lg font-extrabold text-slate-900">Discover Campus Intelligence</h3>
            <p className="text-xs text-slate-500 font-medium leading-relaxed">
              Type a natural-language search query above or choose a suggestion chip to instantly retrieve vector-ranked announcements, events, study materials, clubs, and opportunities.
            </p>
          </div>
        </div>
      )}
    </div>
  );
};

export default CampusSearchPage;
