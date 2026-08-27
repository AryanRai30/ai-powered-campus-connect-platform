import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

export const DashboardPage: React.FC = () => {
  const { user, logout } = useAuth();
  const [apiResponse, setApiResponse] = useState<string | null>(null);
  const [apiStatus, setApiStatus] = useState<number | null>(null);
  const [loadingEndpoint, setLoadingEndpoint] = useState<string | null>(null);

  const testEndpoint = async (endpoint: string) => {
    setLoadingEndpoint(endpoint);
    setApiResponse(null);
    setApiStatus(null);

    try {
      const res = await api.get(endpoint);
      setApiStatus(res.status);
      setApiResponse(JSON.stringify(res.data, null, 2));
    } catch (err: any) {
      if (err.response) {
        setApiStatus(err.response.status);
        setApiResponse(JSON.stringify(err.response.data, null, 2));
      } else {
        setApiStatus(500);
        setApiResponse(err.message || 'Network error');
      }
    } finally {
      setLoadingEndpoint(null);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-8">
      {/* Session Header Card */}
      <div className="p-6 bg-slate-900 border border-slate-800 rounded-2xl shadow-xl flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div>
          <div className="flex items-center space-x-3 mb-2">
            <span className="px-3 py-1 text-xs font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 rounded-full">
              Session Active
            </span>
            <span className="text-xs text-slate-500">User ID: #{user?.id}</span>
          </div>
          <h1 className="text-2xl font-bold text-slate-100">
            Welcome, {user?.firstName} {user?.lastName}!
          </h1>
          <p className="text-slate-400 text-sm">{user?.email}</p>
        </div>

        <div className="flex items-center space-x-3 self-start md:self-auto">
          <Link
            to="/student-profile"
            className="px-5 py-2.5 bg-emerald-500 hover:bg-emerald-400 text-slate-950 rounded-xl text-sm font-semibold transition-colors shadow-lg shadow-emerald-500/20 flex items-center space-x-2"
          >
            <span>Student Profile</span>
            <span>→</span>
          </Link>

          <button
            onClick={logout}
            className="px-5 py-2.5 bg-red-500/10 hover:bg-red-500/20 text-red-400 border border-red-500/20 rounded-xl text-sm font-semibold transition-colors"
          >
            Sign Out
          </button>
        </div>
      </div>

      {/* Campus Features Quick Access Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4">
        <Link
          to="/events"
          className="p-5 bg-slate-900 border border-slate-800 hover:border-emerald-500/50 rounded-2xl space-y-3 transition-all hover:shadow-xl group"
        >
          <div className="flex items-center justify-between">
            <div className="w-9 h-9 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 flex items-center justify-center text-base font-bold">
              📅
            </div>
            <span className="text-xs font-semibold text-emerald-400 group-hover:translate-x-1 transition-transform">
              →
            </span>
          </div>
          <h2 className="text-base font-bold text-slate-100 group-hover:text-emerald-400 transition-colors">
            Events
          </h2>
          <p className="text-xs text-slate-400 leading-relaxed line-clamp-2">
            Browse workshops, hackathons & register.
          </p>
        </Link>

        <Link
          to="/announcements"
          className="p-5 bg-slate-900 border border-slate-800 hover:border-indigo-500/50 rounded-2xl space-y-3 transition-all hover:shadow-xl group"
        >
          <div className="flex items-center justify-between">
            <div className="w-9 h-9 rounded-xl bg-indigo-500/10 border border-indigo-500/20 text-indigo-400 flex items-center justify-center text-base font-bold">
              📢
            </div>
            <span className="text-xs font-semibold text-indigo-400 group-hover:translate-x-1 transition-transform">
              →
            </span>
          </div>
          <h2 className="text-base font-bold text-slate-100 group-hover:text-indigo-400 transition-colors">
            Bulletins
          </h2>
          <p className="text-xs text-slate-400 leading-relaxed line-clamp-2">
            Academic notices, circulars & exam timetables.
          </p>
        </Link>

        <Link
          to="/clubs"
          className="p-5 bg-slate-900 border border-slate-800 hover:border-purple-500/50 rounded-2xl space-y-3 transition-all hover:shadow-xl group"
        >
          <div className="flex items-center justify-between">
            <div className="w-9 h-9 rounded-xl bg-purple-500/10 border border-purple-500/20 text-purple-400 flex items-center justify-center text-base font-bold">
              🤝
            </div>
            <span className="text-xs font-semibold text-purple-400 group-hover:translate-x-1 transition-transform">
              →
            </span>
          </div>
          <h2 className="text-base font-bold text-slate-100 group-hover:text-purple-400 transition-colors">
            Clubs
          </h2>
          <p className="text-xs text-slate-400 leading-relaxed line-clamp-2">
            Join tech, arts, sports & entrepreneur societies.
          </p>
        </Link>

        <Link
          to="/resources"
          className="p-5 bg-slate-900 border border-slate-800 hover:border-cyan-500/50 rounded-2xl space-y-3 transition-all hover:shadow-xl group"
        >
          <div className="flex items-center justify-between">
            <div className="w-9 h-9 rounded-xl bg-cyan-500/10 border border-cyan-500/20 text-cyan-400 flex items-center justify-center text-base font-bold">
              📚
            </div>
            <span className="text-xs font-semibold text-cyan-400 group-hover:translate-x-1 transition-transform">
              →
            </span>
          </div>
          <h2 className="text-base font-bold text-slate-100 group-hover:text-cyan-400 transition-colors">
            Resources
          </h2>
          <p className="text-xs text-slate-400 leading-relaxed line-clamp-2">
            Lecture notes, PDFs, videos & study material.
          </p>
        </Link>
      </div>

      {/* User Information & Roles Card */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="p-6 bg-slate-900 border border-slate-800 rounded-2xl space-y-4">
          <h2 className="text-lg font-semibold text-slate-200 border-b border-slate-800 pb-3">
            Profile Details
          </h2>
          <div className="space-y-3 text-sm">
            <div className="flex justify-between py-1 border-b border-slate-800/50">
              <span className="text-slate-400">First Name</span>
              <span className="text-slate-200 font-medium">{user?.firstName}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-800/50">
              <span className="text-slate-400">Last Name</span>
              <span className="text-slate-200 font-medium">{user?.lastName}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-800/50">
              <span className="text-slate-400">Email</span>
              <span className="text-slate-200 font-medium">{user?.email}</span>
            </div>
          </div>
        </div>

        <div className="p-6 bg-slate-900 border border-slate-800 rounded-2xl space-y-4">
          <h2 className="text-lg font-semibold text-slate-200 border-b border-slate-800 pb-3">
            Assigned System Roles
          </h2>
          <div className="flex flex-wrap gap-2 pt-2">
            {user?.roles?.map((role) => (
              <span
                key={role}
                className="px-3 py-1.5 bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 rounded-lg font-semibold text-xs tracking-wide"
              >
                ROLE_{role}
              </span>
            ))}
          </div>
          <p className="text-xs text-slate-500 pt-2">
            Role authorities determine API access across the Campus Connect Platform backend.
          </p>
        </div>
      </div>

      {/* Backend API Tester Card */}
      <div className="p-6 bg-slate-900 border border-slate-800 rounded-2xl space-y-6">
        <div>
          <h2 className="text-lg font-semibold text-slate-200">
            Protected Backend API Verification
          </h2>
          <p className="text-slate-400 text-xs mt-1">
            Test real-time stateless JWT authorization against Spring Boot security endpoints.
          </p>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-6 gap-3">
          <button
            onClick={() => testEndpoint('/protected/test')}
            disabled={!!loadingEndpoint}
            className="px-3 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold rounded-xl border border-slate-700 transition-colors disabled:opacity-50"
          >
            {loadingEndpoint === '/protected/test' ? 'Testing...' : 'Test Auth'}
          </button>

          <button
            onClick={() => testEndpoint('/events')}
            disabled={!!loadingEndpoint}
            className="px-3 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold rounded-xl border border-slate-700 transition-colors disabled:opacity-50"
          >
            {loadingEndpoint === '/events' ? 'Testing...' : 'Test Events'}
          </button>

          <button
            onClick={() => testEndpoint('/announcements')}
            disabled={!!loadingEndpoint}
            className="px-3 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold rounded-xl border border-slate-700 transition-colors disabled:opacity-50"
          >
            {loadingEndpoint === '/announcements' ? 'Testing...' : 'Test Notices'}
          </button>

          <button
            onClick={() => testEndpoint('/clubs')}
            disabled={!!loadingEndpoint}
            className="px-3 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold rounded-xl border border-slate-700 transition-colors disabled:opacity-50"
          >
            {loadingEndpoint === '/clubs' ? 'Testing...' : 'Test Clubs'}
          </button>

          <button
            onClick={() => testEndpoint('/resources')}
            disabled={!!loadingEndpoint}
            className="px-3 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold rounded-xl border border-slate-700 transition-colors disabled:opacity-50"
          >
            {loadingEndpoint === '/resources' ? 'Testing...' : 'Test Resources'}
          </button>

          <button
            onClick={() => testEndpoint('/protected/admin')}
            disabled={!!loadingEndpoint}
            className="px-3 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold rounded-xl border border-slate-700 transition-colors disabled:opacity-50"
          >
            {loadingEndpoint === '/protected/admin' ? 'Testing...' : 'Test Admin'}
          </button>
        </div>

        {apiResponse && (
          <div className="p-4 bg-slate-950 border border-slate-800 rounded-xl space-y-2">
            <div className="flex items-center justify-between text-xs">
              <span className="font-semibold text-slate-400">Response Status:</span>
              <span
                className={`font-bold px-2 py-0.5 rounded ${
                  apiStatus === 200
                    ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                    : 'bg-red-500/10 text-red-400 border border-red-500/20'
                }`}
              >
                HTTP {apiStatus}
              </span>
            </div>
            <pre className="p-3 bg-slate-900 rounded-lg text-emerald-400 text-xs font-mono overflow-x-auto">
              {apiResponse}
            </pre>
          </div>
        )}
      </div>
    </div>
  );
};

export default DashboardPage;
