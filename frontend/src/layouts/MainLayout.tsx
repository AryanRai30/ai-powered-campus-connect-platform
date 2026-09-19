import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { Sidebar } from '../components/layout/Sidebar';
import { TopHeader } from '../components/layout/TopHeader';
import { XIcon } from '../components/common/Icons';

interface MainLayoutProps {
  children: React.ReactNode;
}

export const MainLayout: React.FC<MainLayoutProps> = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();
  const [collapsed, setCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);

  // If loading, show a clean initial loader screen
  if (loading) {
    return (
      <div className="min-h-screen bg-slate-50 flex items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <div className="w-12 h-12 rounded-2xl bg-gradient-to-tr from-brand-600 to-indigo-600 flex items-center justify-center text-white font-extrabold text-xl shadow-md animate-pulse">
            CC
          </div>
          <p className="text-xs font-semibold text-slate-400 tracking-wider uppercase">Loading Campus Connect...</p>
        </div>
      </div>
    );
  }

  // Unauthenticated layout (e.g., login or register pages wrapper)
  if (!isAuthenticated) {
    return (
      <div className="min-h-screen bg-slate-50 text-slate-900 flex flex-col justify-between">
        <main className="flex-grow">{children}</main>
        <footer className="py-4 border-t border-slate-200 bg-white text-center text-xs text-slate-400 font-medium">
          Campus Connect Platform &copy; {new Date().getFullYear()} — Academic Management System
        </footer>
      </div>
    );
  }

  // Authenticated Dashboard Layout with Sidebar & Header
  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 flex flex-col md:flex-row antialiased">
      {/* Desktop Sticky Sidebar */}
      <div className="hidden md:block sticky top-0 h-screen z-40 shrink-0">
        <Sidebar
          collapsed={collapsed}
          onToggleCollapse={() => setCollapsed(!collapsed)}
        />
      </div>

      {/* Mobile Slide-out Navigation Drawer Overlay */}
      {mobileOpen && (
        <div className="fixed inset-0 z-50 md:hidden">
          <div
            className="fixed inset-0 bg-slate-900/40 backdrop-blur-xs transition-opacity animate-fade-in"
            onClick={() => setMobileOpen(false)}
          />
          <div className="fixed inset-y-0 left-0 w-72 bg-white shadow-2xl z-10 flex flex-col animate-slide-up">
            <div className="p-4 flex items-center justify-between border-b border-slate-100">
              <span className="font-extrabold text-slate-900 text-lg">Menu</span>
              <button
                onClick={() => setMobileOpen(false)}
                className="p-1.5 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-lg"
              >
                <XIcon size={20} />
              </button>
            </div>
            <div className="flex-grow overflow-y-auto">
              <Sidebar
                collapsed={false}
                onToggleCollapse={() => {}}
                onMobileClose={() => setMobileOpen(false)}
              />
            </div>
          </div>
        </div>
      )}

      {/* Main Content Workspace */}
      <div className="flex-grow flex flex-col min-w-0">
        <TopHeader onMobileOpen={() => setMobileOpen(true)} />

        <main className="flex-grow p-4 md:p-8 max-w-7xl w-full mx-auto animate-fade-in">
          {children}
        </main>

        <footer className="py-6 px-4 md:px-8 border-t border-slate-200/80 bg-white text-center text-xs text-slate-400 font-medium">
          Campus Connect Platform &copy; {new Date().getFullYear()} — Academic Architecture Foundation
        </footer>
      </div>
    </div>
  );
};

export default MainLayout;
