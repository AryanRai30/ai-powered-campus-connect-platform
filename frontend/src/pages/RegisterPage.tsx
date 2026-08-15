import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export const RegisterPage: React.FC = () => {
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [phone, setPhone] = useState('');

  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { register } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    if (!firstName.trim() || !lastName.trim() || !email.trim() || !password.trim()) {
      setErrorMessage('Please fill in all required fields.');
      return;
    }

    if (password.length < 6) {
      setErrorMessage('Password must be at least 6 characters long.');
      return;
    }

    try {
      setIsSubmitting(true);
      await register({
        firstName: firstName.trim(),
        lastName: lastName.trim(),
        email: email.trim(),
        password,
        phone: phone.trim() || undefined,
      });
      navigate('/dashboard', { replace: true });
    } catch (err: any) {
      if (err.response?.status === 409) {
        setErrorMessage('This email address is already registered. Please login or use a different email.');
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
        <h1 className="text-2xl font-bold text-slate-100">Create Account</h1>
        <p className="text-slate-400 text-sm mt-1">Register for Campus Connect Student Portal</p>
      </div>

      {errorMessage && (
        <div className="mb-6 p-4 bg-red-500/10 border border-red-500/20 rounded-xl text-red-400 text-sm">
          {errorMessage}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-2">
              First Name *
            </label>
            <input
              type="text"
              value={firstName}
              onChange={(e) => setFirstName(e.target.value)}
              placeholder="John"
              required
              className="w-full px-4 py-3 bg-slate-950 border border-slate-800 rounded-xl text-slate-100 placeholder-slate-600 focus:outline-none focus:border-emerald-500 transition-colors"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-2">
              Last Name *
            </label>
            <input
              type="text"
              value={lastName}
              onChange={(e) => setLastName(e.target.value)}
              placeholder="Doe"
              required
              className="w-full px-4 py-3 bg-slate-950 border border-slate-800 rounded-xl text-slate-100 placeholder-slate-600 focus:outline-none focus:border-emerald-500 transition-colors"
            />
          </div>
        </div>

        <div>
          <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-2">
            Email Address *
          </label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="john.doe@example.com"
            required
            className="w-full px-4 py-3 bg-slate-950 border border-slate-800 rounded-xl text-slate-100 placeholder-slate-600 focus:outline-none focus:border-emerald-500 transition-colors"
          />
        </div>

        <div>
          <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-2">
            Password *
          </label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="••••••••"
            required
            minLength={6}
            className="w-full px-4 py-3 bg-slate-950 border border-slate-800 rounded-xl text-slate-100 placeholder-slate-600 focus:outline-none focus:border-emerald-500 transition-colors"
          />
        </div>

        <div>
          <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-2">
            Phone Number (Optional)
          </label>
          <input
            type="tel"
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
            placeholder="9876543210"
            className="w-full px-4 py-3 bg-slate-950 border border-slate-800 rounded-xl text-slate-100 placeholder-slate-600 focus:outline-none focus:border-emerald-500 transition-colors"
          />
        </div>

        <div className="p-3 bg-slate-950/60 border border-slate-800/80 rounded-xl text-xs text-slate-400 flex items-center justify-between">
          <span>Assigned Role:</span>
          <span className="font-semibold text-emerald-400 bg-emerald-500/10 px-2 py-0.5 rounded border border-emerald-500/20">
            STUDENT
          </span>
        </div>

        <button
          type="submit"
          disabled={isSubmitting}
          className="w-full py-3.5 bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-semibold rounded-xl transition-colors shadow-lg shadow-emerald-500/20 disabled:opacity-50 flex items-center justify-center space-x-2 mt-2"
        >
          {isSubmitting ? (
            <>
              <div className="w-4 h-4 border-2 border-slate-950 border-t-transparent rounded-full animate-spin"></div>
              <span>Registering...</span>
            </>
          ) : (
            <span>Create Account</span>
          )}
        </button>
      </form>

      <div className="mt-6 text-center text-sm text-slate-400">
        Already have an account?{' '}
        <Link to="/login" className="text-emerald-400 hover:text-emerald-300 font-medium underline">
          Sign In
        </Link>
      </div>
    </div>
  );
};

export default RegisterPage;
