import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getDefaultDashboardForRoles, isPathAllowedForRoles } from '../utils/navigationUtils';
import {
  MailIcon,
  LockIcon,
  EyeIcon,
  EyeOffIcon,
  SparklesIcon,
  GraduationCapIcon,
  BookOpenIcon,
  UsersIcon,
  AlertCircleIcon,
} from '../components/common/Icons';

export const LoginPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { login, isAuthenticated, loading, user } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const getDestinationPath = (targetPath?: string, userRoles?: string[]): string => {
    const defaultPath = getDefaultDashboardForRoles(userRoles);
    if (!targetPath || targetPath === '/login' || targetPath === '/dashboard') {
      return defaultPath;
    }
    if (!isPathAllowedForRoles(targetPath, userRoles)) {
      return defaultPath;
    }
    return targetPath;
  };

  useEffect(() => {
    if (!loading && isAuthenticated && user) {
      const targetPath = (location.state as any)?.from?.pathname;
      const dest = getDestinationPath(targetPath, user.roles);
      navigate(dest, { replace: true });
    }
  }, [isAuthenticated, loading, navigate, location.state, user]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    if (!email.trim() || !password.trim()) {
      setErrorMessage('Please provide both email and password.');
      return;
    }

    try {
      setIsSubmitting(true);
      const res = await login({ email: email.trim(), password });
      const targetPath = (location.state as any)?.from?.pathname;
      const dest = getDestinationPath(targetPath, res.roles);
      navigate(dest, { replace: true });
    } catch (err: any) {
      if (err.response?.status === 401) {
        setErrorMessage('Invalid email or password. Please check your credentials.');
      } else if (err.response?.data?.message) {
        setErrorMessage(err.response.data.message);
      } else {
        setErrorMessage('Failed to connect to backend server. Please make sure backend is running.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4 sm:p-6 lg:p-8">
      <div className="w-full max-w-5xl bg-white rounded-3xl border border-slate-200/80 shadow-2xl overflow-hidden grid grid-cols-1 lg:grid-cols-12 min-h-[640px] animate-fade-in">
        
        {/* Left Branding Hero Banner (Hidden on small screens) */}
        <div className="lg:col-span-5 bg-gradient-to-br from-brand-600 via-brand-700 to-indigo-800 p-8 sm:p-12 text-white flex flex-col justify-between relative overflow-hidden">
          {/* Subtle Background Decorative Vectors */}
          <div className="absolute -top-16 -left-16 w-64 h-64 bg-white/10 rounded-full blur-2xl pointer-events-none" />
          <div className="absolute -bottom-20 -right-20 w-80 h-80 bg-indigo-500/20 rounded-full blur-3xl pointer-events-none" />

          {/* Top Brand Logo Header */}
          <div className="relative z-10">
            <div className="inline-flex items-center gap-2.5 px-3.5 py-1.5 bg-white/10 backdrop-blur-md border border-white/20 rounded-full text-xs font-bold uppercase tracking-wider mb-6">
              <SparklesIcon size={14} className="text-cyan-300" />
              <span>Campus Connect Platform</span>
            </div>
            <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight leading-tight text-white">
              Connect. Learn. Participate.
            </h1>
            <p className="mt-3 text-blue-100 text-sm font-medium leading-relaxed">
              Your unified SaaS portal for academic resources, campus events, career opportunities, and club communities.
            </p>
          </div>

          {/* Key Value Highlights */}
          <div className="relative z-10 space-y-4 my-8">
            <div className="flex items-center gap-3 bg-white/10 backdrop-blur-xs p-3 rounded-xl border border-white/10">
              <div className="p-2 rounded-lg bg-white/20 text-white shrink-0">
                <BookOpenIcon size={18} />
              </div>
              <div className="text-xs">
                <p className="font-bold text-white">Academic Resources</p>
                <p className="text-blue-100">Access course materials & notes</p>
              </div>
            </div>

            <div className="flex items-center gap-3 bg-white/10 backdrop-blur-xs p-3 rounded-xl border border-white/10">
              <div className="p-2 rounded-lg bg-white/20 text-white shrink-0">
                <UsersIcon size={18} />
              </div>
              <div className="text-xs">
                <p className="font-bold text-white">Clubs & Organizations</p>
                <p className="text-blue-100">Join vibrant student communities</p>
              </div>
            </div>

            <div className="flex items-center gap-3 bg-white/10 backdrop-blur-xs p-3 rounded-xl border border-white/10">
              <div className="p-2 rounded-lg bg-white/20 text-white shrink-0">
                <GraduationCapIcon size={18} />
              </div>
              <div className="text-xs">
                <p className="font-bold text-white">Career Opportunities</p>
                <p className="text-blue-100">Internships & placement drives</p>
              </div>
            </div>
          </div>

          {/* Bottom Footer Note */}
          <div className="relative z-10 text-xs text-blue-200 font-medium">
            Academic Architecture Foundation &copy; {new Date().getFullYear()}
          </div>
        </div>

        {/* Right Sign-in Form Column */}
        <div className="lg:col-span-7 p-6 sm:p-10 lg:p-12 flex flex-col justify-center">
          <div className="max-w-md mx-auto w-full">
            {/* Header */}
            <div className="mb-8 text-center sm:text-left">
              <div className="w-12 h-12 rounded-2xl bg-brand-50 text-brand-600 border border-brand-100 flex items-center justify-center font-extrabold text-xl mb-4 sm:mx-0 mx-auto shadow-xs">
                CC
              </div>
              <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Welcome Back</h2>
              <p className="text-slate-500 text-sm mt-1">
                Please enter your credentials to access your account
              </p>
            </div>

            {/* Error Message Alert */}
            {errorMessage && (
              <div className="mb-6 p-4 bg-rose-50 border border-rose-200/80 rounded-2xl text-rose-700 text-sm flex items-start gap-3 animate-slide-up">
                <AlertCircleIcon size={20} className="shrink-0 text-rose-600 mt-0.5" />
                <span className="font-medium leading-snug">{errorMessage}</span>
              </div>
            )}

            {/* Form */}
            <form onSubmit={handleSubmit} className="space-y-5">
              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-2">
                  Email Address
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                    <MailIcon size={18} />
                  </div>
                  <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="student@campusconnect.edu"
                    required
                    className="w-full pl-10 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-slate-900 text-sm placeholder-slate-400 focus:bg-white focus:outline-none focus:border-brand-600 focus:ring-2 focus:ring-brand-500/20 transition-all font-medium"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-2">
                  Password
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                    <LockIcon size={18} />
                  </div>
                  <input
                    type={showPassword ? 'text' : 'password'}
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="••••••••"
                    required
                    className="w-full pl-10 pr-10 py-3 bg-slate-50 border border-slate-200 rounded-xl text-slate-900 text-sm placeholder-slate-400 focus:bg-white focus:outline-none focus:border-brand-600 focus:ring-2 focus:ring-brand-500/20 transition-all font-medium"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute inset-y-0 right-0 pr-3.5 flex items-center text-slate-400 hover:text-slate-600 transition-colors"
                    aria-label={showPassword ? 'Hide password' : 'Show password'}
                  >
                    {showPassword ? <EyeOffIcon size={18} /> : <EyeIcon size={18} />}
                  </button>
                </div>
              </div>

              {/* Submit Button */}
              <button
                type="submit"
                disabled={isSubmitting}
                className="w-full py-3.5 bg-brand-600 hover:bg-brand-700 active:bg-brand-800 text-white font-bold text-sm rounded-xl transition-all shadow-md hover:shadow-lg disabled:opacity-50 flex items-center justify-center gap-2 cursor-pointer active:scale-98"
              >
                {isSubmitting ? (
                  <>
                    <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                    <span>Authenticating...</span>
                  </>
                ) : (
                  <span>Sign In to Portal</span>
                )}
              </button>
            </form>

            {/* Registration Footer */}
            <div className="mt-8 text-center text-xs text-slate-500 border-t border-slate-100 pt-6">
              Don't have a student account yet?{' '}
              <Link to="/register" className="text-brand-600 hover:text-brand-700 font-bold underline transition-colors">
                Register as Student
              </Link>
            </div>
          </div>
        </div>

      </div>
    </div>
  );
};

export default LoginPage;
