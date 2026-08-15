import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

interface MainLayoutProps {
  children: React.ReactNode;
}

export const MainLayout: React.FC<MainLayoutProps> = ({ children }) => {
  const { isAuthenticated, user, logout } = useAuth();

  return (
    <div className="min-h-screen flex flex-col bg-slate-950 text-slate-100">
      {/* App Header */}
      <header className="border-b border-slate-800 bg-slate-900/50 backdrop-blur sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <Link to="/" className="flex items-center space-x-3 hover:opacity-90 transition-opacity">
            <div className="w-8 h-8 rounded-lg bg-emerald-500 flex items-center justify-center font-bold text-slate-950">
              CC
            </div>
            <span className="font-semibold text-lg tracking-tight text-white">
              Campus Connect
            </span>
          </Link>

          <div className="flex items-center space-x-4">
            {isAuthenticated ? (
              <>
                <Link
                  to="/dashboard"
                  className="text-xs font-semibold px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-lg border border-slate-700 transition-colors"
                >
                  Dashboard ({user?.firstName})
                </Link>
                <button
                  onClick={logout}
                  className="text-xs font-semibold px-3 py-1.5 bg-red-500/10 hover:bg-red-500/20 text-red-400 rounded-lg border border-red-500/20 transition-colors"
                >
                  Logout
                </button>
              </>
            ) : (
              <>
                <Link
                  to="/login"
                  className="text-xs font-semibold px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-lg border border-slate-700 transition-colors"
                >
                  Sign In
                </Link>
                <Link
                  to="/register"
                  className="text-xs font-semibold px-3 py-1.5 bg-emerald-500 hover:bg-emerald-400 text-slate-950 rounded-lg transition-colors shadow-sm shadow-emerald-500/20"
                >
                  Register
                </Link>
              </>
            )}
          </div>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-grow max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-10">
        {children}
      </main>

      {/* App Footer */}
      <footer className="border-t border-slate-800 bg-slate-900/30 py-6">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center text-sm text-slate-500">
          Ai Powered Campus Connect Platform &copy; {new Date().getFullYear()} — Academic Architecture Foundation
        </div>
      </footer>
    </div>
  );
};

export default MainLayout;
