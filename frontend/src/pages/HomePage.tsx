import React, { useState } from 'react';
import { checkBackendHealth } from '../services/api';
import { HealthStatus } from '../types/api.types';

export const HomePage: React.FC = () => {
  const [healthInfo, setHealthInfo] = useState<HealthStatus | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const handleTestHealth = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await checkBackendHealth();
      setHealthInfo(data);
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Failed to connect to backend service';
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col items-center justify-center min-h-[60vh] text-center px-4">
      <div className="inline-flex items-center space-x-2 px-4 py-2 rounded-full bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 text-sm font-medium mb-6">
        <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
        <span>Project Setup Complete</span>
      </div>

      <h1 className="text-4xl sm:text-5xl font-extrabold text-white tracking-tight mb-4 max-w-3xl">
        Ai Powered Campus Connect Platform
      </h1>

      <p className="text-lg text-slate-400 max-w-2xl mb-8 leading-relaxed">
        A clean, scalable, multi-tenant digital campus architecture prepared for multi-phase development including AI document RAG, peer matching, and placement intelligence.
      </p>

      {/* Backend API Connection Test Card */}
      <div className="w-full max-w-md bg-slate-900 border border-slate-800 rounded-xl p-6 text-left shadow-xl">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-sm font-semibold text-slate-300 uppercase tracking-wider">
            Backend Connectivity
          </h2>
          <span className="text-xs text-slate-500">GET /api/health</span>
        </div>

        <p className="text-xs text-slate-400 mb-4">
          Click below to verify communication with the Spring Boot backend service.
        </p>

        <button
          onClick={handleTestHealth}
          disabled={loading}
          className="w-full py-2.5 px-4 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white font-medium text-sm transition-colors duration-200 disabled:opacity-50 flex items-center justify-center space-x-2"
        >
          {loading ? (
            <span>Connecting to Backend...</span>
          ) : (
            <span>Test Health Endpoint</span>
          )}
        </button>

        {healthInfo && (
          <div className="mt-4 p-3 bg-emerald-950/40 border border-emerald-800/40 rounded-lg text-xs font-mono text-emerald-300">
            <p className="font-semibold text-emerald-400 mb-1">Backend Connected</p>
            <p><span className="font-semibold text-white">Status:</span> {healthInfo.status}</p>
            <p><span className="font-semibold text-white">Application:</span> {healthInfo.application}</p>
          </div>
        )}

        {error && (
          <div className="mt-4 p-3 bg-rose-950/40 border border-rose-800/40 rounded-lg text-xs text-rose-300">
            <p className="font-semibold mb-1">Connection Note:</p>
            <p>{error}</p>
            <p className="text-[11px] text-slate-400 mt-2">
              Ensure Spring Boot backend is running at <code className="text-slate-300">http://localhost:8080</code>
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

export default HomePage;
