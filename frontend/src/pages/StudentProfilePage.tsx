import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { studentProfileService } from '../services/studentProfileService';
import { StudentProfileRequest, StudentProfileResponse } from '../types/studentProfile.types';

const YEAR_OPTIONS = [
  '1st Year',
  '2nd Year',
  '3rd Year',
  '4th Year',
  '5th Year',
  'Postgraduate',
];

const SEMESTER_OPTIONS = [
  '1st Semester',
  '2nd Semester',
  '3rd Semester',
  '4th Semester',
  '5th Semester',
  '6th Semester',
  '7th Semester',
  '8th Semester',
];

export const StudentProfilePage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [loading, setLoading] = useState<boolean>(true);
  const [saving, setSaving] = useState<boolean>(false);
  const [hasProfile, setHasProfile] = useState<boolean>(false);
  const [profileData, setProfileData] = useState<StudentProfileResponse | null>(null);

  const [formData, setFormData] = useState<StudentProfileRequest>({
    studentId: '',
    course: '',
    department: '',
    year: '1st Year',
    semester: '1st Semester',
    skills: '',
    interests: '',
    bio: '',
  });

  const [formErrors, setFormErrors] = useState<Record<string, string>>({});
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    setLoading(true);
    setErrorMessage(null);
    try {
      const data = await studentProfileService.getProfile();
      setProfileData(data);
      setHasProfile(true);
      setFormData({
        studentId: data.studentId || '',
        course: data.course || '',
        department: data.department || '',
        year: data.year || '1st Year',
        semester: data.semester || '1st Semester',
        skills: data.skills || '',
        interests: data.interests || '',
        bio: data.bio || '',
      });
    } catch (err: any) {
      if (err.response?.status === 404) {
        setHasProfile(false);
      } else {
        setErrorMessage(
          err.response?.data?.message || 'Failed to load profile details. Please try again.'
        );
      }
    } finally {
      setLoading(false);
    }
  };

  const validateForm = (): boolean => {
    const errors: Record<string, string> = {};

    if (!formData.studentId.trim()) {
      errors.studentId = 'Student ID is required';
    }
    if (!formData.course.trim()) {
      errors.course = 'Course is required';
    }
    if (!formData.department.trim()) {
      errors.department = 'Department is required';
    }
    if (!formData.year) {
      errors.year = 'Year is required';
    }
    if (!formData.semester) {
      errors.semester = 'Semester is required';
    }

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (formErrors[name]) {
      setFormErrors((prev) => ({ ...prev, [name]: '' }));
    }
    setSuccessMessage(null);
    setErrorMessage(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) return;

    setSaving(true);
    setSuccessMessage(null);
    setErrorMessage(null);

    try {
      let response: StudentProfileResponse;
      if (hasProfile) {
        response = await studentProfileService.updateProfile(formData);
        setSuccessMessage('Student profile updated successfully!');
      } else {
        response = await studentProfileService.createProfile(formData);
        setHasProfile(true);
        setSuccessMessage('Student profile created successfully!');
      }
      setProfileData(response);
    } catch (err: any) {
      if (err.response?.status === 409) {
        setErrorMessage(
          err.response?.data?.message || 'Conflict: Student ID is already registered or profile exists.'
        );
      } else if (err.response?.status === 400) {
        setErrorMessage(
          err.response?.data?.message || 'Validation error: Please check inputs.'
        );
      } else {
        setErrorMessage(
          err.response?.data?.message || 'Failed to save student profile. Please try again.'
        );
      }
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] space-y-4">
        <div className="w-10 h-10 border-4 border-emerald-500 border-t-transparent rounded-full animate-spin"></div>
        <p className="text-slate-400 text-sm">Loading Student Profile...</p>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto space-y-8">
      {/* Header Banner */}
      <div className="p-6 bg-slate-900 border border-slate-800 rounded-2xl shadow-xl flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div>
          <div className="flex items-center space-x-3 mb-2">
            <span
              className={`px-3 py-1 text-xs font-semibold rounded-full border ${
                hasProfile
                  ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
                  : 'bg-amber-500/10 text-amber-400 border-amber-500/20'
              }`}
            >
              {hasProfile ? 'Profile Configured' : 'Profile Uncreated'}
            </span>
            <span className="text-xs text-slate-500">
              Account: {user?.firstName} {user?.lastName}
            </span>
          </div>
          <h1 className="text-2xl font-bold text-slate-100">Student Profile Management</h1>
          <p className="text-slate-400 text-sm mt-1">
            Maintain your academic details, course information, and skills for the Campus Connect Platform.
          </p>
        </div>

        <button
          onClick={() => navigate('/dashboard')}
          className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 rounded-xl text-xs font-semibold transition-colors self-start md:self-auto"
        >
          Back to Dashboard
        </button>
      </div>

      {/* Read-Only Account Information Card */}
      <div className="p-6 bg-slate-900 border border-slate-800 rounded-2xl space-y-4">
        <h2 className="text-md font-semibold text-slate-200 border-b border-slate-800 pb-3 flex items-center justify-between">
          <span>Authenticated Account Details</span>
          <span className="text-xs font-normal text-slate-500">(Read-Only)</span>
        </h2>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 text-sm">
          <div className="p-3 bg-slate-950/60 border border-slate-800/80 rounded-xl">
            <p className="text-xs text-slate-500">First Name</p>
            <p className="text-slate-200 font-medium mt-0.5">{user?.firstName || profileData?.firstName || 'N/A'}</p>
          </div>
          <div className="p-3 bg-slate-950/60 border border-slate-800/80 rounded-xl">
            <p className="text-xs text-slate-500">Last Name</p>
            <p className="text-slate-200 font-medium mt-0.5">{user?.lastName || profileData?.lastName || 'N/A'}</p>
          </div>
          <div className="p-3 bg-slate-950/60 border border-slate-800/80 rounded-xl">
            <p className="text-xs text-slate-500">Email Address</p>
            <p className="text-slate-200 font-medium mt-0.5 truncate">{user?.email || profileData?.email || 'N/A'}</p>
          </div>
        </div>
      </div>

      {/* Feedback Banners */}
      {successMessage && (
        <div className="p-4 bg-emerald-500/10 border border-emerald-500/20 rounded-xl text-emerald-400 text-sm flex items-center space-x-3">
          <span className="font-bold">✓ Success:</span>
          <span>{successMessage}</span>
        </div>
      )}

      {errorMessage && (
        <div className="p-4 bg-red-500/10 border border-red-500/20 rounded-xl text-red-400 text-sm flex items-center space-x-3">
          <span className="font-bold">⚠ Error:</span>
          <span>{errorMessage}</span>
        </div>
      )}

      {/* Profile Form */}
      <form onSubmit={handleSubmit} className="p-6 bg-slate-900 border border-slate-800 rounded-2xl space-y-6">
        <div className="flex items-center justify-between border-b border-slate-800 pb-4">
          <h2 className="text-lg font-semibold text-slate-100">Academic & Personal Details</h2>
          {profileData?.createdAt && (
            <span className="text-xs text-slate-500">
              Created: {new Date(profileData.createdAt).toLocaleDateString()}
            </span>
          )}
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Student ID */}
          <div className="space-y-1.5">
            <label className="block text-xs font-semibold text-slate-300">
              Student ID <span className="text-emerald-400">*</span>
            </label>
            <input
              type="text"
              name="studentId"
              value={formData.studentId}
              onChange={handleChange}
              placeholder="e.g. STU10024"
              className={`w-full px-4 py-2.5 bg-slate-950 border ${
                formErrors.studentId ? 'border-red-500' : 'border-slate-800'
              } rounded-xl text-sm text-slate-100 focus:outline-none focus:border-emerald-500 transition-colors`}
            />
            {formErrors.studentId && (
              <p className="text-xs text-red-400 mt-1">{formErrors.studentId}</p>
            )}
          </div>

          {/* Course */}
          <div className="space-y-1.5">
            <label className="block text-xs font-semibold text-slate-300">
              Course / Degree Program <span className="text-emerald-400">*</span>
            </label>
            <input
              type="text"
              name="course"
              value={formData.course}
              onChange={handleChange}
              placeholder="e.g. B.Tech Computer Science"
              className={`w-full px-4 py-2.5 bg-slate-950 border ${
                formErrors.course ? 'border-red-500' : 'border-slate-800'
              } rounded-xl text-sm text-slate-100 focus:outline-none focus:border-emerald-500 transition-colors`}
            />
            {formErrors.course && (
              <p className="text-xs text-red-400 mt-1">{formErrors.course}</p>
            )}
          </div>

          {/* Department */}
          <div className="space-y-1.5">
            <label className="block text-xs font-semibold text-slate-300">
              Department <span className="text-emerald-400">*</span>
            </label>
            <input
              type="text"
              name="department"
              value={formData.department}
              onChange={handleChange}
              placeholder="e.g. Department of Engineering"
              className={`w-full px-4 py-2.5 bg-slate-950 border ${
                formErrors.department ? 'border-red-500' : 'border-slate-800'
              } rounded-xl text-sm text-slate-100 focus:outline-none focus:border-emerald-500 transition-colors`}
            />
            {formErrors.department && (
              <p className="text-xs text-red-400 mt-1">{formErrors.department}</p>
            )}
          </div>

          {/* Year */}
          <div className="space-y-1.5">
            <label className="block text-xs font-semibold text-slate-300">
              Academic Year <span className="text-emerald-400">*</span>
            </label>
            <select
              name="year"
              value={formData.year}
              onChange={handleChange}
              className="w-full px-4 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-slate-100 focus:outline-none focus:border-emerald-500 transition-colors"
            >
              {YEAR_OPTIONS.map((opt) => (
                <option key={opt} value={opt}>
                  {opt}
                </option>
              ))}
            </select>
            {formErrors.year && (
              <p className="text-xs text-red-400 mt-1">{formErrors.year}</p>
            )}
          </div>

          {/* Semester */}
          <div className="space-y-1.5">
            <label className="block text-xs font-semibold text-slate-300">
              Current Semester <span className="text-emerald-400">*</span>
            </label>
            <select
              name="semester"
              value={formData.semester}
              onChange={handleChange}
              className="w-full px-4 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-slate-100 focus:outline-none focus:border-emerald-500 transition-colors"
            >
              {SEMESTER_OPTIONS.map((opt) => (
                <option key={opt} value={opt}>
                  {opt}
                </option>
              ))}
            </select>
            {formErrors.semester && (
              <p className="text-xs text-red-400 mt-1">{formErrors.semester}</p>
            )}
          </div>

          {/* Skills */}
          <div className="space-y-1.5">
            <label className="block text-xs font-semibold text-slate-300">
              Technical / Soft Skills <span className="text-slate-500">(Optional)</span>
            </label>
            <input
              type="text"
              name="skills"
              value={formData.skills}
              onChange={handleChange}
              placeholder="e.g. Java, React, SQL, Problem Solving"
              className="w-full px-4 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-slate-100 focus:outline-none focus:border-emerald-500 transition-colors"
            />
          </div>
        </div>

        {/* Interests */}
        <div className="space-y-1.5">
          <label className="block text-xs font-semibold text-slate-300">
            Academic & Extracurricular Interests <span className="text-slate-500">(Optional)</span>
          </label>
          <input
            type="text"
            name="interests"
            value={formData.interests}
            onChange={handleChange}
            placeholder="e.g. Artificial Intelligence, Robotics, Hackathons"
            className="w-full px-4 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-slate-100 focus:outline-none focus:border-emerald-500 transition-colors"
          />
        </div>

        {/* Bio */}
        <div className="space-y-1.5">
          <label className="block text-xs font-semibold text-slate-300">
            Personal Bio / Overview <span className="text-slate-500">(Optional)</span>
          </label>
          <textarea
            name="bio"
            rows={4}
            value={formData.bio}
            onChange={handleChange}
            placeholder="Share a brief introduction about your goals, aspirations, or campus involvement..."
            className="w-full px-4 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-sm text-slate-100 focus:outline-none focus:border-emerald-500 transition-colors resize-y"
          />
        </div>

        {/* Form Actions */}
        <div className="pt-4 border-t border-slate-800 flex justify-end space-x-4">
          <button
            type="button"
            onClick={() => navigate('/dashboard')}
            className="px-5 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl text-sm font-semibold transition-colors"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={saving}
            className="px-6 py-2.5 bg-emerald-500 hover:bg-emerald-400 text-slate-950 rounded-xl text-sm font-semibold transition-colors shadow-lg shadow-emerald-500/20 disabled:opacity-50 flex items-center space-x-2"
          >
            {saving ? (
              <>
                <div className="w-4 h-4 border-2 border-slate-950 border-t-transparent rounded-full animate-spin"></div>
                <span>Saving...</span>
              </>
            ) : (
              <span>{hasProfile ? 'Update Profile' : 'Create Profile'}</span>
            )}
          </button>
        </div>
      </form>
    </div>
  );
};

export default StudentProfilePage;
