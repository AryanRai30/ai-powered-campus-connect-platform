import React, { useEffect, useState } from 'react';
import { EventItem } from '../types/campus.types';
import { fetchEvents, registerForEvent } from '../services/campusService';

const CATEGORIES = ['All', 'Tech Summit', 'Hackathon', 'Workshop', 'Cultural', 'Sports'];

export const EventsPage: React.FC = () => {
  const [events, setEvents] = useState<EventItem[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<string>('All');
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [registeringId, setRegisteringId] = useState<number | null>(null);
  const [selectedEvent, setSelectedEvent] = useState<EventItem | null>(null);
  const [toastMessage, setToastMessage] = useState<{ text: string; type: 'success' | 'error' } | null>(null);

  const loadEvents = async (category: string = selectedCategory, search: string = searchQuery) => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchEvents(category, search);
      setEvents(data);
    } catch (err: any) {
      console.error('Failed to load events', err);
      setError(err.response?.data?.message || 'Failed to load campus events. Please check server connectivity.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadEvents(selectedCategory, searchQuery);
  }, [selectedCategory]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    loadEvents(selectedCategory, searchQuery);
  };

  const handleRegister = async (eventId: number) => {
    setRegisteringId(eventId);
    setToastMessage(null);
    try {
      await registerForEvent(eventId);
      setToastMessage({ text: 'Successfully registered for the event!', type: 'success' });
      // Refresh event list to update counts and state
      await loadEvents(selectedCategory, searchQuery);
      if (selectedEvent && selectedEvent.id === eventId) {
        setSelectedEvent((prev) => prev ? { ...prev, registered: true, registrationCount: prev.registrationCount + 1 } : null);
      }
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Registration failed. You may already be registered.';
      setToastMessage({ text: msg, type: 'error' });
    } finally {
      setRegisteringId(null);
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
      <div className="relative p-8 rounded-3xl bg-gradient-to-r from-slate-900 via-slate-900 to-emerald-950/40 border border-slate-800 overflow-hidden shadow-2xl">
        <div className="relative z-10 max-w-2xl space-y-3">
          <div className="inline-flex items-center space-x-2 px-3 py-1 bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs font-semibold rounded-full">
            <span>📅 Campus Life & Activities</span>
          </div>
          <h1 className="text-3xl sm:text-4xl font-extrabold text-white tracking-tight">
            Campus Events & Workshops
          </h1>
          <p className="text-slate-400 text-sm leading-relaxed">
            Discover hackathons, guest lectures, cultural festivals, and technical symposiums. Register instantly with your student credentials.
          </p>
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
                  ? 'bg-emerald-500 text-slate-950 border-emerald-500 font-bold shadow-lg shadow-emerald-500/20'
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
              placeholder="Search by event, topic, or venue..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full sm:w-64 px-4 py-2.5 text-xs bg-slate-900 border border-slate-800 rounded-xl text-slate-200 focus:outline-none focus:border-emerald-500 transition-colors"
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
          <h3 className="text-lg font-semibold text-slate-200">Unable to load events</h3>
          <p className="text-xs text-slate-400 max-w-md mx-auto">{error}</p>
          <button
            onClick={() => loadEvents()}
            className="px-4 py-2 bg-emerald-500 text-slate-950 rounded-xl text-xs font-semibold hover:bg-emerald-400 transition-colors"
          >
            Retry Loading
          </button>
        </div>
      ) : events.length === 0 ? (
        <div className="p-12 bg-slate-900/50 border border-slate-800 rounded-3xl text-center space-y-3">
          <div className="text-4xl">🎪</div>
          <h3 className="text-lg font-semibold text-slate-300">No events found</h3>
          <p className="text-xs text-slate-500 max-w-sm mx-auto">
            No events match your current filter or search criteria. Try selecting another category or clearing your search query.
          </p>
          {(selectedCategory !== 'All' || searchQuery) && (
            <button
              onClick={() => {
                setSelectedCategory('All');
                setSearchQuery('');
                loadEvents('All', '');
              }}
              className="mt-2 text-xs font-semibold text-emerald-400 hover:underline"
            >
              Clear filters
            </button>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {events.map((evt) => (
            <div
              key={evt.id}
              className="p-6 bg-slate-900/90 border border-slate-800 hover:border-slate-700 rounded-2xl flex flex-col justify-between space-y-5 transition-all hover:shadow-xl group"
            >
              <div className="space-y-3">
                <div className="flex items-center justify-between gap-2">
                  <span className="px-3 py-1 bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs font-semibold rounded-lg">
                    {evt.category || 'General'}
                  </span>
                  {evt.registered ? (
                    <span className="px-2.5 py-1 bg-emerald-500/20 text-emerald-300 border border-emerald-500/30 text-xs font-bold rounded-lg flex items-center space-x-1">
                      <span>✓</span>
                      <span>Registered</span>
                    </span>
                  ) : evt.registrationRequired ? (
                    <span className="px-2.5 py-1 bg-amber-500/10 text-amber-400 border border-amber-500/20 text-xs font-semibold rounded-lg">
                      Registration Required
                    </span>
                  ) : (
                    <span className="px-2.5 py-1 bg-slate-800 text-slate-400 border border-slate-700 text-xs font-medium rounded-lg">
                      Open Access
                    </span>
                  )}
                </div>

                <h2 className="text-xl font-bold text-slate-100 group-hover:text-emerald-400 transition-colors">
                  {evt.title}
                </h2>

                <p className="text-xs text-slate-400 line-clamp-2 leading-relaxed">
                  {evt.description}
                </p>

                <div className="pt-2 grid grid-cols-2 gap-2 text-xs text-slate-300 border-t border-slate-800/80">
                  <div className="flex items-center space-x-2">
                    <span className="text-slate-500">📅</span>
                    <span>{evt.eventDate}</span>
                  </div>
                  {evt.eventTime && (
                    <div className="flex items-center space-x-2">
                      <span className="text-slate-500">⏰</span>
                      <span>{evt.eventTime}</span>
                    </div>
                  )}
                  <div className="flex items-center space-x-2 col-span-2">
                    <span className="text-slate-500">📍</span>
                    <span className="truncate">{evt.venue}</span>
                  </div>
                  {evt.organizer && (
                    <div className="flex items-center space-x-2 col-span-2">
                      <span className="text-slate-500">🏢</span>
                      <span className="text-slate-400 truncate">By {evt.organizer}</span>
                    </div>
                  )}
                </div>
              </div>

              {/* Action buttons footer */}
              <div className="pt-4 border-t border-slate-800/80 flex items-center justify-between gap-3">
                <button
                  onClick={() => setSelectedEvent(evt)}
                  className="text-xs font-semibold text-slate-400 hover:text-slate-200 transition-colors"
                >
                  View Details →
                </button>

                {evt.registrationRequired && (
                  <button
                    onClick={() => handleRegister(evt.id)}
                    disabled={evt.registered || registeringId === evt.id}
                    className={`px-4 py-2 rounded-xl text-xs font-bold transition-all shadow-md ${
                      evt.registered
                        ? 'bg-slate-800 text-emerald-400 border border-emerald-500/20 cursor-default'
                        : registeringId === evt.id
                        ? 'bg-emerald-500/50 text-slate-950 cursor-wait'
                        : 'bg-emerald-500 hover:bg-emerald-400 text-slate-950 shadow-emerald-500/20'
                    }`}
                  >
                    {evt.registered
                      ? 'Already Registered'
                      : registeringId === evt.id
                      ? 'Registering...'
                      : 'Register Now'}
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Event Details Modal */}
      {selectedEvent && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl max-w-xl w-full p-6 sm:p-8 space-y-6 shadow-2xl relative">
            <div className="flex items-start justify-between">
              <span className="px-3 py-1 bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs font-semibold rounded-lg">
                {selectedEvent.category || 'General'}
              </span>
              <button
                onClick={() => setSelectedEvent(null)}
                className="w-8 h-8 rounded-full bg-slate-800 text-slate-400 hover:text-white flex items-center justify-center font-bold"
              >
                ✕
              </button>
            </div>

            <div className="space-y-2">
              <h2 className="text-2xl font-bold text-white">{selectedEvent.title}</h2>
              <p className="text-xs text-slate-400">Organized by {selectedEvent.organizer || 'Campus Connect'}</p>
            </div>

            <div className="p-4 bg-slate-950 border border-slate-800 rounded-2xl grid grid-cols-2 gap-4 text-xs">
              <div>
                <span className="text-slate-500 block">Date</span>
                <span className="text-slate-200 font-semibold">{selectedEvent.eventDate}</span>
              </div>
              <div>
                <span className="text-slate-500 block">Time</span>
                <span className="text-slate-200 font-semibold">{selectedEvent.eventTime || 'TBA'}</span>
              </div>
              <div className="col-span-2">
                <span className="text-slate-500 block">Venue</span>
                <span className="text-slate-200 font-semibold">{selectedEvent.venue}</span>
              </div>
              <div>
                <span className="text-slate-500 block">Registrations</span>
                <span className="text-slate-200 font-semibold">{selectedEvent.registrationCount} Registered</span>
              </div>
              <div>
                <span className="text-slate-500 block">Access Mode</span>
                <span className="text-slate-200 font-semibold">
                  {selectedEvent.registrationRequired ? 'Registration Required' : 'Open Entry'}
                </span>
              </div>
            </div>

            <div className="space-y-2">
              <h4 className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Description</h4>
              <p className="text-sm text-slate-300 whitespace-pre-line leading-relaxed">
                {selectedEvent.description}
              </p>
            </div>

            <div className="flex justify-end space-x-3 pt-4 border-t border-slate-800">
              <button
                onClick={() => setSelectedEvent(null)}
                className="px-5 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold rounded-xl"
              >
                Close
              </button>

              {selectedEvent.registrationRequired && (
                <button
                  onClick={() => handleRegister(selectedEvent.id)}
                  disabled={selectedEvent.registered || registeringId === selectedEvent.id}
                  className={`px-5 py-2.5 rounded-xl text-xs font-bold transition-all ${
                    selectedEvent.registered
                      ? 'bg-slate-800 text-emerald-400 border border-emerald-500/20 cursor-default'
                      : 'bg-emerald-500 hover:bg-emerald-400 text-slate-950'
                  }`}
                >
                  {selectedEvent.registered ? 'Already Registered' : 'Register Now'}
                </button>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default EventsPage;
