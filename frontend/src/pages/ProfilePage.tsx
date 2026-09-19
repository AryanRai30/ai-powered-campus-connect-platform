import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { userProfileService } from '../services/userProfileService';
import { UserProfileResponse, UserProfileUpdateRequest } from '../types/userProfile.types';
import { PageHeader } from '../components/common/PageHeader';
import { Badge } from '../components/common/Badge';
import { SkeletonLoader } from '../components/common/SkeletonLoader';
import { EmptyState } from '../components/common/EmptyState';
import {
  UserIcon,
  GraduationCapIcon,
  ShieldIcon,
  CheckCircleIcon,
  AlertCircleIcon,
  EditIcon,
  XIcon,
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

export const ProfilePage: React.FC = () => {
  const { user, updateUser } = useAuth();
  const [profile, setProfile] = useState<UserProfileResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [saving, setSaving] = useState<boolean>(false);
  const [isEditing, setIsEditing] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const [editForm, setEditForm] = useState<UserProfileUpdateRequest>({
    firstName: '',
    lastName: '',
    phone: '',
    studentId: '',
    course: '',
    department: '',
    year: '1st Year',
    semester: '1st Semester',
    skills: '',
    interests: '',
    bio: '',
  });

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    setLoading(true);
    setErrorMessage(null);
    try {
      const data = await userProfileService.getUserProfile();
      setProfile(data);
      populateForm(data);
    } catch (err: any) {
      console.error('Failed to load profile:', err);
      setErrorMessage(err?.response?.data?.message || 'Unable to load profile information.');
    } finally {
      setLoading(false);
    }
  };

  const populateForm = (data: UserProfileResponse) => {
    setEditForm({
      firstName: data.firstName || '',
      lastName: data.lastName || '',
      phone: data.phone || '',
      studentId: data.studentId || '',
      course: data.course || '',
      department: data.department || '',
      year: data.year || '1st Year',
      semester: data.semester || '1st Semester',
      skills: data.skills || '',
      interests: data.interests || '',
      bio: data.bio || '',
    });
  };

  const handleStartEdit = () => {
    if (profile) populateForm(profile);
    setSuccessMessage(null);
    setErrorMessage(null);
    setIsEditing(true);
  };

  const handleCancelEdit = () => {
    if (profile) populateForm(profile);
    setIsEditing(false);
    setErrorMessage(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editForm.firstName.trim() || !editForm.lastName.trim()) {
      setErrorMessage('First name and last name are required.');
      return;
    }

    setSaving(true);
    setErrorMessage(null);
    setSuccessMessage(null);

    try {
      const updated = await userProfileService.updateUserProfile(editForm);
      setProfile(updated);
      populateForm(updated);

      // Synchronize AuthContext state so TopHeader and Sidebar update instantly
      updateUser({
        firstName: updated.firstName,
        lastName: updated.lastName,
      });

      setIsEditing(false);
      setSuccessMessage('Profile updated successfully.');
    } catch (err: any) {
      setErrorMessage(err?.response?.data?.message || 'Failed to save profile changes.');
    } finally {
      setSaving(false);
    }
  };

  const roles = profile?.roles || user?.roles || [];
  const isAdmin = roles.some((r) => ['ADMIN', 'SUPER_ADMIN', 'CLUB_ADMIN'].includes(r));
  const isFaculty = !isAdmin && roles.includes('FACULTY');
  const isStudent = !isAdmin && !isFaculty;

  const primaryRole = isAdmin ? 'ADMIN' : isFaculty ? 'FACULTY' : 'STUDENT';
  const fullName = `${profile?.firstName || user?.firstName || ''} ${profile?.lastName || user?.lastName || ''}`.trim() || 'User';

  const initials = fullName
    .split(' ')
    .map((n) => n.charAt(0))
    .join('')
    .toUpperCase()
    .slice(0, 2) || 'U';

  return (
    <div className="max-w-4xl mx-auto space-y-8 animate-fade-in">
      <PageHeader
        title="My Profile"
        subtitle="Manage your authenticated user credentials and profile information."
      >
        {!isEditing && !loading && profile && (
          <button
            onClick={handleStartEdit}
            className="inline-flex items-center gap-2 px-4 py-2.5 bg-brand-600 hover:bg-brand-700 text-white font-bold text-sm rounded-xl transition-all shadow-subtle hover:shadow active:scale-95"
          >
            <EditIcon size={16} />
            <span>Edit Profile</span>
          </button>
        )}
        {isEditing && (
          <button
            onClick={handleCancelEdit}
            className="inline-flex items-center gap-2 px-4 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold text-sm rounded-xl transition-all"
          >
            <XIcon size={16} />
            <span>Cancel Edit</span>
          </button>
        )}
      </PageHeader>

      {/* Alert Messages */}
      {successMessage && (
        <div className="p-4 bg-emerald-50 border border-emerald-200/80 rounded-2xl text-emerald-800 text-sm font-medium flex items-center gap-3 animate-slide-up">
          <CheckCircleIcon size={20} className="text-emerald-600 shrink-0" />
          <span>{successMessage}</span>
        </div>
      )}

      {errorMessage && (
        <div className="p-4 bg-rose-50 border border-rose-200/80 rounded-2xl text-rose-800 text-sm font-medium flex items-center gap-3 animate-slide-up">
          <AlertCircleIcon size={20} className="text-rose-600 shrink-0" />
          <span>{errorMessage}</span>
        </div>
      )}

      {/* Main Profile Skeleton or Empty State */}
      {loading ? (
        <SkeletonLoader type="card" count={2} />
      ) : !profile ? (
        <EmptyState
          title="Unable to Load Profile"
          description="We could not retrieve your profile records from the server."
          icon={UserIcon}
          action={{
            label: 'Try Again',
            onClick: fetchProfile,
          }}
        />
      ) : (
        <div className="space-y-6">
          {/* Header Card */}
          <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200/80 shadow-subtle flex flex-col sm:flex-row items-center gap-6">
            <div className="w-24 h-24 rounded-3xl bg-gradient-to-tr from-brand-600 to-indigo-600 text-white font-extrabold text-3xl flex items-center justify-center shadow-md shrink-0">
              {initials}
            </div>

            <div className="space-y-2 text-center sm:text-left flex-grow">
              <div className="flex flex-wrap items-center justify-center sm:justify-start gap-2.5">
                <h2 className="text-2xl font-extrabold text-slate-900 tracking-tight">{fullName}</h2>
                <Badge
                  variant={isAdmin ? 'secondary' : isFaculty ? 'info' : 'published'}
                  size="md"
                >
                  {primaryRole}
                </Badge>
              </div>

              <p className="text-sm text-slate-500 font-medium">{profile.email}</p>

              <div className="pt-1 flex flex-wrap items-center justify-center sm:justify-start gap-3 text-xs">
                <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-emerald-50 text-emerald-700 font-semibold border border-emerald-200/80">
                  <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
                  Account Active
                </span>
                {profile.phone && (
                  <span className="text-slate-500 font-medium">📞 {profile.phone}</span>
                )}
              </div>
            </div>
          </div>

          {/* Form wrapper if editing, or static view cards */}
          {isEditing ? (
            <form onSubmit={handleSubmit} className="space-y-6">
              {/* Personal Information Edit Card */}
              <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200/80 shadow-subtle space-y-5">
                <h3 className="text-base font-bold text-slate-900 border-b border-slate-100 pb-3 flex items-center gap-2">
                  <UserIcon size={18} className="text-brand-600" />
                  <span>Personal Details</span>
                </h3>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
                  <div>
                    <label className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-2">
                      First Name *
                    </label>
                    <input
                      type="text"
                      value={editForm.firstName}
                      onChange={(e) => setEditForm({ ...editForm, firstName: e.target.value })}
                      required
                      className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-slate-900 text-sm font-medium focus:bg-white focus:outline-none focus:border-brand-600 focus:ring-2 focus:ring-brand-500/20 transition-all"
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-2">
                      Last Name *
                    </label>
                    <input
                      type="text"
                      value={editForm.lastName}
                      onChange={(e) => setEditForm({ ...editForm, lastName: e.target.value })}
                      required
                      className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-slate-900 text-sm font-medium focus:bg-white focus:outline-none focus:border-brand-600 focus:ring-2 focus:ring-brand-500/20 transition-all"
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-2">
                      Email Address (Locked)
                    </label>
                    <input
                      type="email"
                      value={profile.email}
                      disabled
                      className="w-full px-4 py-3 bg-slate-100 border border-slate-200 rounded-xl text-slate-500 text-sm font-medium cursor-not-allowed"
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-2">
                      Phone Number
                    </label>
                    <input
                      type="tel"
                      value={editForm.phone}
                      onChange={(e) => setEditForm({ ...editForm, phone: e.target.value })}
                      placeholder="+1 (555) 000-0000"
                      className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-slate-900 text-sm font-medium focus:bg-white focus:outline-none focus:border-brand-600 focus:ring-2 focus:ring-brand-500/20 transition-all"
                    />
                  </div>
                </div>
              </div>

              {/* Student Role Specific Fields Edit Card */}
              {isStudent && (
                <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200/80 shadow-subtle space-y-5">
                  <h3 className="text-base font-bold text-slate-900 border-b border-slate-100 pb-3 flex items-center gap-2">
                    <GraduationCapIcon size={18} className="text-purple-600" />
                    <span>Academic & Student Records</span>
                  </h3>

                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
                    <div>
                      <label className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-2">
                        Student Registration ID
                      </label>
                      <input
                        type="text"
                        value={editForm.studentId}
                        onChange={(e) => setEditForm({ ...editForm, studentId: e.target.value })}
                        placeholder="STU-2026-001"
                        className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-slate-900 text-sm font-medium focus:bg-white focus:outline-none focus:border-brand-600 focus:ring-2 focus:ring-brand-500/20 transition-all"
                      />
                    </div>

                    <div>
                      <label className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-2">
                        Department / Faculty
                      </label>
                      <input
                        type="text"
                        value={editForm.department}
                        onChange={(e) => setEditForm({ ...editForm, department: e.target.value })}
                        placeholder="Computer Science & Engineering"
                        className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-slate-900 text-sm font-medium focus:bg-white focus:outline-none focus:border-brand-600 focus:ring-2 focus:ring-brand-500/20 transition-all"
                      />
                    </div>

                    <div>
                      <label className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-2">
                        Course / Program
                      </label>
                      <input
                        type="text"
                        value={editForm.course}
                        onChange={(e) => setEditForm({ ...editForm, course: e.target.value })}
                        placeholder="B.Tech Computer Science"
                        className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-slate-900 text-sm font-medium focus:bg-white focus:outline-none focus:border-brand-600 focus:ring-2 focus:ring-brand-500/20 transition-all"
                      />
                    </div>

                    <div>
                      <label className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-2">
                        Academic Year
                      </label>
                      <select
                        value={editForm.year}
                        onChange={(e) => setEditForm({ ...editForm, year: e.target.value })}
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
                        value={editForm.semester}
                        onChange={(e) => setEditForm({ ...editForm, semester: e.target.value })}
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
                        Technical Skills
                      </label>
                      <input
                        type="text"
                        value={editForm.skills}
                        onChange={(e) => setEditForm({ ...editForm, skills: e.target.value })}
                        placeholder="Java, React, SQL, Python"
                        className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-slate-900 text-sm font-medium focus:bg-white focus:outline-none focus:border-brand-600 focus:ring-2 focus:ring-brand-500/20 transition-all"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-xs font-semibold uppercase tracking-wider text-slate-700 mb-2">
                      Academic Bio & Goals
                    </label>
                    <textarea
                      rows={3}
                      value={editForm.bio}
                      onChange={(e) => setEditForm({ ...editForm, bio: e.target.value })}
                      placeholder="Share a brief overview of your academic focus..."
                      className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-slate-900 text-sm font-medium focus:bg-white focus:outline-none focus:border-brand-600 focus:ring-2 focus:ring-brand-500/20 transition-all"
                    />
                  </div>
                </div>
              )}

              {/* Form Action Buttons */}
              <div className="flex items-center justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={handleCancelEdit}
                  className="px-5 py-3 bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold text-sm rounded-xl transition-all"
                >
                  Cancel
                </button>

                <button
                  type="submit"
                  disabled={saving}
                  className="px-6 py-3 bg-brand-600 hover:bg-brand-700 active:bg-brand-800 text-white font-bold text-sm rounded-xl transition-all shadow-md hover:shadow-lg disabled:opacity-50 flex items-center gap-2 cursor-pointer"
                >
                  {saving ? (
                    <>
                      <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                      <span>Saving Changes...</span>
                    </>
                  ) : (
                    <span>Save Profile Changes</span>
                  )}
                </button>
              </div>
            </form>
          ) : (
            /* View Cards */
            <div className="space-y-6">
              {/* Personal Details */}
              <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200/80 shadow-subtle space-y-4">
                <h3 className="text-base font-bold text-slate-900 border-b border-slate-100 pb-3 flex items-center gap-2">
                  <UserIcon size={18} className="text-brand-600" />
                  <span>Personal Information</span>
                </h3>

                <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 text-xs">
                  <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-200/60">
                    <span className="text-slate-400 font-medium block mb-0.5">First Name</span>
                    <span className="text-slate-900 font-bold text-sm">{profile.firstName}</span>
                  </div>

                  <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-200/60">
                    <span className="text-slate-400 font-medium block mb-0.5">Last Name</span>
                    <span className="text-slate-900 font-bold text-sm">{profile.lastName}</span>
                  </div>

                  <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-200/60">
                    <span className="text-slate-400 font-medium block mb-0.5">Email Address</span>
                    <span className="text-slate-900 font-bold text-sm truncate block">{profile.email}</span>
                  </div>
                </div>
              </div>

              {/* Role-Specific View Section */}
              {isStudent && (
                <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200/80 shadow-subtle space-y-4">
                  <h3 className="text-base font-bold text-slate-900 border-b border-slate-100 pb-3 flex items-center gap-2">
                    <GraduationCapIcon size={18} className="text-purple-600" />
                    <span>Academic Information</span>
                  </h3>

                  <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-xs">
                    <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-200/60">
                      <span className="text-slate-400 font-medium block mb-0.5">Student ID</span>
                      <span className="text-slate-900 font-bold text-sm">{profile.studentId || 'N/A'}</span>
                    </div>

                    <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-200/60">
                      <span className="text-slate-400 font-medium block mb-0.5">Department</span>
                      <span className="text-slate-900 font-bold text-sm">{profile.department || 'N/A'}</span>
                    </div>

                    <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-200/60">
                      <span className="text-slate-400 font-medium block mb-0.5">Course</span>
                      <span className="text-slate-900 font-bold text-sm">{profile.course || 'N/A'}</span>
                    </div>

                    <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-200/60">
                      <span className="text-slate-400 font-medium block mb-0.5">Year / Semester</span>
                      <span className="text-slate-900 font-bold text-sm">
                        {profile.year || 'N/A'} / {profile.semester || 'N/A'}
                      </span>
                    </div>
                  </div>

                  {profile.bio && (
                    <div className="p-4 bg-slate-50 rounded-xl border border-slate-200/60">
                      <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider block mb-1">
                        Academic Bio
                      </span>
                      <p className="text-xs text-slate-700 leading-relaxed font-medium">{profile.bio}</p>
                    </div>
                  )}

                  {profile.skills && (
                    <div className="p-4 bg-slate-50 rounded-xl border border-slate-200/60">
                      <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider block mb-2">
                        Technical Skills
                      </span>
                      <div className="flex flex-wrap gap-2">
                        {profile.skills.split(',').map((skill, idx) => (
                          <span
                            key={idx}
                            className="px-2.5 py-1 bg-white border border-slate-200 text-slate-700 text-xs font-semibold rounded-lg shadow-2xs"
                          >
                            {skill.trim()}
                          </span>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              )}

              {isFaculty && (
                <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200/80 shadow-subtle space-y-4">
                  <h3 className="text-base font-bold text-slate-900 border-b border-slate-100 pb-3 flex items-center gap-2">
                    <GraduationCapIcon size={18} className="text-blue-600" />
                    <span>Faculty Account & Supervision Clearance</span>
                  </h3>

                  <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 text-xs">
                    <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-200/60">
                      <span className="text-slate-400 font-medium block mb-0.5">Faculty Member</span>
                      <span className="text-slate-900 font-bold text-sm">{fullName}</span>
                    </div>

                    <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-200/60">
                      <span className="text-slate-400 font-medium block mb-0.5">Role Designation</span>
                      <Badge variant="info" size="sm">FACULTY</Badge>
                    </div>

                    <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-200/60">
                      <span className="text-slate-400 font-medium block mb-0.5">System Privileges</span>
                      <span className="text-emerald-600 font-bold text-xs">Content Creation & Supervision</span>
                    </div>
                  </div>
                </div>
              )}

              {isAdmin && (
                <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200/80 shadow-subtle space-y-4">
                  <h3 className="text-base font-bold text-slate-900 border-b border-slate-100 pb-3 flex items-center gap-2">
                    <ShieldIcon size={18} className="text-purple-600" />
                    <span>System Administration Profile</span>
                  </h3>

                  <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 text-xs">
                    <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-200/60">
                      <span className="text-slate-400 font-medium block mb-0.5">Administrator</span>
                      <span className="text-slate-900 font-bold text-sm">{fullName}</span>
                    </div>

                    <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-200/60">
                      <span className="text-slate-400 font-medium block mb-0.5">Access Tier</span>
                      <Badge variant="secondary" size="sm">SYSTEM ADMIN</Badge>
                    </div>

                    <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-200/60">
                      <span className="text-slate-400 font-medium block mb-0.5">Operational Scope</span>
                      <span className="text-purple-600 font-bold text-xs">Full Platform Governance</span>
                    </div>
                  </div>
                </div>
              )}

              {/* Account Security Info Card */}
              <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200/80 shadow-subtle space-y-4">
                <h3 className="text-base font-bold text-slate-900 border-b border-slate-100 pb-3 flex items-center gap-2">
                  <ShieldIcon size={18} className="text-emerald-600" />
                  <span>Account Authorization & Security</span>
                </h3>

                <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 text-xs">
                  <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-200/60">
                    <span className="text-slate-400 font-medium block mb-0.5">Account Status</span>
                    <span className="text-emerald-600 font-bold text-sm">ACTIVE</span>
                  </div>

                  <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-200/60">
                    <span className="text-slate-400 font-medium block mb-0.5">Assigned Roles</span>
                    <div className="flex flex-wrap gap-1 mt-0.5">
                      {roles.map((r, idx) => (
                        <Badge key={idx} variant="info" size="sm">
                          {r}
                        </Badge>
                      ))}
                    </div>
                  </div>

                  <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-200/60">
                    <span className="text-slate-400 font-medium block mb-0.5">Authentication Policy</span>
                    <span className="text-slate-700 font-semibold text-xs">Stateless JWT Authorization</span>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default ProfilePage;
