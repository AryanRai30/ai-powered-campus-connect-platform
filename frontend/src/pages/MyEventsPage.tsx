import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { EventItem } from '../types/campus.types';
import { fetchMyEvents } from '../services/campusService';

export const MyEventsPage: React.FC = () => {
  const [myEvents, setMyEvents] = useState<EventItem[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const loadMyEvents = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchMyEvents();
      setMyEvents(data);
    } catch (err: any) {
      console.error('Failed to load my registered events', err);
      setError(err.response?.data?.message || 'Failed to load your registered events.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadMyEvents();
  }, []);

  return (
    <div className="space-y-8">
      {/* Header Banner */}
      <div className="relative p-8 rounded-3xl bg-gradient-to-r from-slate-900 via-slate-900 to-emerald-950/40 border border-slate-800 overflow-hidden shadow-2xl flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div className="relative z-10 max-w-2xl space-y-3">
          <div className="inline-flex items-center space-x-2 px-3 py-1 bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs font-semibold rounded-full">
            <span>⭐ My Event Registrations</span>
          </div>
          <h1 className="text-3xl sm:text-4xl font-extrabold text-white tracking-tight">
            My Registered Events
          </h1>
          <p className="text-slate-400 text-sm leading-relaxed">
            Review your confirmed campus event registrations, venues, and schedules.
          </p>
        </div>

        <div className="relative z-10 self-start md:self-auto">
          <Link
            to="/events"
            className="px-5 py-3 bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 rounded-xl text-xs font-semibold transition-colors flex items-center space-x-2 whitespace-nowrap"
          >
            <span>Browse All Events</span>
            <span>→</span>
          </Link>
        </div>
      </div>

      {/* Main Grid View */}
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
          <h3 className="text-lg font-semibold text-slate-200">Unable to load registered events</h3>
          <p className="text-xs text-slate-400 max-w-md mx-auto">{error}</p>
          <button
            onClick={() => loadMyEvents()}
            className="px-4 py-2 bg-emerald-500 text-slate-950 rounded-xl text-xs font-semibold hover:bg-emerald-400 transition-colors"
          >
            Retry Loading
          </button>
        </div>
      ) : myEvents.length === 0 ? (
        <div className="p-12 bg-slate-900/50 border border-slate-800 rounded-3xl text-center space-y-4">
          <div className="text-4xl">📅</div>
          <h3 className="text-lg font-semibold text-slate-300">No event registrations found</h3>
          <p className="text-xs text-slate-500 max-w-sm mx-auto">
            You have not registered for any campus events yet. Explore upcoming hackathons, tech talks, and workshops!
          </p>
          <Link
            to="/events"
            className="inline-block px-5 py-2.5 bg-emerald-500 hover:bg-emerald-400 text-slate-950 rounded-xl text-xs font-bold transition-all shadow-lg shadow-emerald-500/20"
          >
            Explore Campus Events
          </Link>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {myEvents.map((evt) => (
            <div
              key={evt.id}
              className="p-6 bg-slate-900/90 border border-slate-800 rounded-2xl flex flex-col justify-between space-y-5 shadow-xl"
            >
              <div className="space-y-3">
                <div className="flex items-center justify-between gap-2">
                  <span className="px-3 py-1 bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs font-semibold rounded-lg">
                    {evt.category || 'General'}
                  </span>
                  <span className="px-2.5 py-1 bg-emerald-500/20 text-emerald-300 border border-emerald-500/30 text-xs font-bold rounded-lg flex items-center space-x-1">
                    <span>✓</span>
                    <span>Already Registered</span>
                  </span>
                </div>

                <h2 className="text-xl font-bold text-slate-100">{evt.title}</h2>

                <p className="text-xs text-slate-400 leading-relaxed">{evt.description}</p>

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
                      <span className="text-slate-400 truncate">Organized by {evt.organizer}</span>
                    </div>
                  )}
                </div>
              </div>

              <div className="pt-4 border-t border-slate-800/80 flex items-center justify-between text-xs text-slate-500">
                <span>Confirmed Attendance</span>
                <span>{evt.registrationCount} Registrations Total</span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default MyEventsPage;
