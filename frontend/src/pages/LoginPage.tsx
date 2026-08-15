import React, { useState } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export const LoginPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const from = (location.state as any)?.from?.pathname || '/dashboard';

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    if (!email.trim() || !password.trim()) {
      setErrorMessage('Please provide both email and password.');
      return;
    }

    try {
      setIsSubmitting(true);
      await login({ email: email.trim(), password });
      navigate(from, { replace: true });
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
    <div className="max-w-md mx-auto my-10 p-8 bg-slate-900 border border-slate-800 rounded-2xl shadow-xl">
      <div className="text-center mb-8">
        <div className="w-12 h-12 rounded-xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 flex items-center justify-center font-bold text-xl mx-auto mb-3">
          CC
        </div>
        <h1 className="text-2xl font-bold text-slate-100">Welcome Back</h1>
        <p className="text-slate-400 text-sm mt-1">Sign in to your Campus Connect account</p>
      </div>

      {errorMessage && (
        <div className="mb-6 p-4 bg-red-500/10 border border-red-500/20 rounded-xl text-red-400 text-sm">
          {errorMessage}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-5">
        <div>
          <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-2">
            Email Address
          </label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="student@example.com"
            required
            className="w-full px-4 py-3 bg-slate-950 border border-slate-800 rounded-xl text-slate-100 placeholder-slate-600 focus:outline-none focus:border-emerald-500 transition-colors"
          />
        </div>

        <div>
          <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-2">
            Password
          </label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="••••••••"
            required
            className="w-full px-4 py-3 bg-slate-950 border border-slate-800 rounded-xl text-slate-100 placeholder-slate-600 focus:outline-none focus:border-emerald-500 transition-colors"
          />
        </div>

        <button
          type="submit"
          disabled={isSubmitting}
          className="w-full py-3.5 bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-semibold rounded-xl transition-colors shadow-lg shadow-emerald-500/20 disabled:opacity-50 flex items-center justify-center space-x-2"
        >
          {isSubmitting ? (
            <>
              <div className="w-4 h-4 border-2 border-slate-950 border-t-transparent rounded-full animate-spin"></div>
              <span>Authenticating...</span>
            </>
          ) : (
            <span>Sign In</span>
          )}
        </button>
      </form>

      <div className="mt-8 text-center text-sm text-slate-400">
        Don't have an account?{' '}
        <Link to="/register" className="text-emerald-400 hover:text-emerald-300 font-medium underline">
          Register as Student
        </Link>
      </div>
    </div>
  );
};

export default LoginPage;
