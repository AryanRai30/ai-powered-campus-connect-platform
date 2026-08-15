import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

interface ProtectedRouteProps {
  children: React.ReactElement;
  allowedRoles?: string[];
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, allowedRoles }) => {
  const { isAuthenticated, loading, user } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="flex flex-col items-center space-y-4">
          <div className="w-10 h-10 border-4 border-emerald-500/20 border-t-emerald-500 rounded-full animate-spin"></div>
          <p className="text-slate-400 text-sm">Verifying authentication session...</p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (allowedRoles && allowedRoles.length > 0) {
    const hasRequiredRole = allowedRoles.some((role) => user?.roles?.includes(role));
    if (!hasRequiredRole) {
      return (
        <div className="max-w-md mx-auto my-12 p-6 bg-slate-900 border border-red-500/30 rounded-xl text-center shadow-lg">
          <div className="w-12 h-12 rounded-full bg-red-500/10 text-red-400 flex items-center justify-center mx-auto mb-4 font-bold text-xl">
            !
          </div>
          <h2 className="text-xl font-bold text-slate-100 mb-2">Access Denied (403)</h2>
          <p className="text-slate-400 text-sm mb-4">
            You do not have the required permissions ({allowedRoles.join(', ')}) to access this page.
          </p>
          <a
            href="/dashboard"
            className="inline-block px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-lg text-sm transition-colors"
          >
            Back to Dashboard
          </a>
        </div>
      );
    }
  }

  return children;
};

export default ProtectedRoute;
