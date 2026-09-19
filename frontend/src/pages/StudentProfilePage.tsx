import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { studentProfileService } from '../services/studentProfileService';
import { StudentProfileRequest, StudentProfileResponse } from '../types/studentProfile.types';
import { PageHeader } from '../components/common/PageHeader';
import { Badge } from '../components/common/Badge';
import { SkeletonLoader } from '../components/common/SkeletonLoader';
import {
  GraduationCapIcon,
  BuildingIcon,
  CheckCircleIcon,
  AlertCircleIcon,
} from '../components/common/Icons';

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
  const fullName = `${user?.firstName || ''} ${user?.lastName || ''}`.trim() || 'Student';

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
        setErrorMessage('Failed to load profile details. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  const validate = (): boolean => {
    const errors: Record<string, string> = {};
    if (!formData.studentId?.trim()) errors.studentId = 'Student ID is required.';
    if (!formData.course?.trim()) errors.course = 'Course / Program name is required.';
    if (!formData.department?.trim()) errors.department = 'Department name is required.';
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;

    setSaving(true);
    setSuccessMessage(null);
    setErrorMessage(null);

    try {
      if (hasProfile) {
        const updated = await studentProfileService.updateProfile(formData);
        setProfileData(updated);
        setSuccessMessage('Student profile updated successfully!');
      } else {
        const created = await studentProfileService.createProfile(formData);
        setProfileData(created);
        setHasProfile(true);
        setSuccessMessage('Student profile created successfully!');
      }
    } catch (err: any) {
      setErrorMessage(err.response?.data?.message || 'Failed to save profile information.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-8 animate-fade-in">
      <PageHeader
        title="Student Profile"
        subtitle="Manage your verified academic records, department details, and skills."
      />

      {/* User Header Profile Card */}
      <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200/80 shadow-subtle flex flex-col sm:flex-row items-center gap-6">
        <div className="w-20 h-20 rounded-2xl bg-gradient-to-tr from-brand-600 to-indigo-600 text-white font-black text-2xl flex items-center justify-center shadow-md shrink-0">
          {fullName ? fullName.charAt(0).toUpperCase() : 'S'}
        </div>
        <div className="space-y-1 text-center sm:text-left flex-grow">
          <div className="flex flex-wrap items-center justify-center sm:justify-start gap-2">
            <h2 className="text-xl font-extrabold text-slate-900">{fullName}</h2>
            <Badge variant="info" size="sm">STUDENT</Badge>
          </div>
          <p className="text-xs text-slate-500 font-medium">{user?.email}</p>
          <div className="pt-2 flex flex-wrap gap-4 text-xs font-semibold text-slate-600 justify-center sm:justify-start">
            {profileData?.studentId && (
              <span className="flex items-center gap-1.5 bg-slate-100 px-2.5 py-1 rounded-lg">
                <GraduationCapIcon size={14} className="text-brand-600" />
                ID: {profileData.studentId}
              </span>
            )}
            {profileData?.department && (
              <span className="flex items-center gap-1.5 bg-slate-100 px-2.5 py-1 rounded-lg">
                <BuildingIcon size={14} className="text-purple-600" />
                {profileData.department}
              </span>
            )}
          </div>
        </div>
      </div>

      {/* Alerts */}
      {successMessage && (
        <div className="p-4 bg-emerald-50 border border-emerald-200/80 rounded-2xl text-emerald-800 text-sm font-medium flex items-center gap-3">
          <CheckCircleIcon size={20} className="text-emerald-600 shrink-0" />
          <span>{successMessage}</span>
        </div>
      )}

      {errorMessage && (
        <div className="p-4 bg-rose-50 border border-rose-200/80 rounded-2xl text-rose-800 text-sm font-medium flex items-center gap-3">
          <AlertCircleIcon size={20} className="text-rose-600 shrink-0" />
          <span>{errorMessage}</span>
        </div>
      )}

      {/* Main Profile Form Card */}
      <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200/80 shadow-subtle">
        <h3 className="text-base font-bold text-slate-900 border-b border-slate-100 pb-4 mb-6">
          Academic & Personal Information
        </h3>

        {loading ? (
          <SkeletonLoader type="table" count={2} />
        ) : (
          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-2">
                  Student Roll / Registration ID *
                </label>
                <input
                  type="text"
                  value={formData.studentId}
                  onChange={(e) => setFormData({ ...formData, studentId: e.target.value })}
                  placeholder="STU-2026-001"
                  required
                  className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-slate-900 text-sm font-medium focus:bg-white focus:outline-none focus:border-brand-600 focus:ring-2 focus:ring-brand-500/20 transition-all"
                />
                {formErrors.studentId && (
                  <p className="text-xs text-rose-500 mt-1">{formErrors.studentId}</p>
                )}
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-2">
                  Department / Faculty *
                </label>
                <input
                  type="text"
                  value={formData.department}
                  onChange={(e) => setFormData({ ...formData, department: e.target.value })}
                  placeholder="Computer Science & Engineering"
                  required
                  className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-slate-900 text-sm font-medium focus:bg-white focus:outline-none focus:border-brand-600 focus:ring-2 focus:ring-brand-500/20 transition-all"
                />
                {formErrors.department && (
                  <p className="text-xs text-rose-500 mt-1">{formErrors.department}</p>
                )}
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-2">
                  Course / Program *
                </label>
                <input
                  type="text"
                  value={formData.course}
                  onChange={(e) => setFormData({ ...formData, course: e.target.value })}
                  placeholder="B.Tech Computer Science"
                  required
                  className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-slate-900 text-sm font-medium focus:bg-white focus:outline-none focus:border-brand-600 focus:ring-2 focus:ring-brand-500/20 transition-all"
                />
                {formErrors.course && (
                  <p className="text-xs text-rose-500 mt-1">{formErrors.course}</p>
                )}
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-2">
                  Academic Year
                </label>
                <select
                  value={formData.year}
                  onChange={(e) => setFormData({ ...formData, year: e.target.value })}
                  className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-slate-900 text-sm font-medium focus:bg-white focus:outline-none focus:border-brand-600 focus:ring-2 focus:ring-brand-500/20 transition-all"
                >
                  {YEAR_OPTIONS.map((y) => (
                    <option key={y} value={y}>
                      {y}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-2">
                  Semester
                </label>
                <select
                  value={formData.semester}
                  onChange={(e) => setFormData({ ...formData, semester: e.target.value })}
                  className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-slate-900 text-sm font-medium focus:bg-white focus:outline-none focus:border-brand-600 focus:ring-2 focus:ring-brand-500/20 transition-all"
                >
                  {SEMESTER_OPTIONS.map((s) => (
                    <option key={s} value={s}>
                      {s}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-2">
                  Technical Skills (Comma Separated)
                </label>
                <input
                  type="text"
                  value={formData.skills}
                  onChange={(e) => setFormData({ ...formData, skills: e.target.value })}
                  placeholder="Java, React, Python, Machine Learning"
                  className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-slate-900 text-sm font-medium focus:bg-white focus:outline-none focus:border-brand-600 focus:ring-2 focus:ring-brand-500/20 transition-all"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-2">
                Student Bio & Goals
              </label>
              <textarea
                rows={3}
                value={formData.bio}
                onChange={(e) => setFormData({ ...formData, bio: e.target.value })}
                placeholder="Briefly describe your academic interests and career goals..."
                className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-slate-900 text-sm font-medium focus:bg-white focus:outline-none focus:border-brand-600 focus:ring-2 focus:ring-brand-500/20 transition-all"
              />
            </div>

            <div className="pt-4 border-t border-slate-100 flex justify-end">
              <button
                type="submit"
                disabled={saving}
                className="px-6 py-3 bg-brand-600 hover:bg-brand-700 active:bg-brand-800 text-white font-bold text-sm rounded-xl transition-all shadow-md hover:shadow-lg disabled:opacity-50 flex items-center gap-2 cursor-pointer active:scale-95"
              >
                {saving ? (
                  <>
                    <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                    <span>Saving...</span>
                  </>
                ) : (
                  <span>{hasProfile ? 'Update Student Profile' : 'Create Student Profile'}</span>
                )}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
};

export default StudentProfilePage;
