import React from 'react';

interface MainLayoutProps {
  children: React.ReactNode;
}

export const MainLayout: React.FC<MainLayoutProps> = ({ children }) => {
  return (
    <div className="min-h-screen flex flex-col bg-slate-950 text-slate-100">
      {/* App Header */}
      <header className="border-b border-slate-800 bg-slate-900/50 backdrop-blur sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="w-8 h-8 rounded-lg bg-emerald-500 flex items-center justify-center font-bold text-slate-950">
              CC
            </div>
            <span className="font-semibold text-lg tracking-tight text-white">
              Campus Connect
            </span>
          </div>
          <span className="px-3 py-1 text-xs font-medium bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 rounded-full">
            Phase 3: Setup Complete
          </span>
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
