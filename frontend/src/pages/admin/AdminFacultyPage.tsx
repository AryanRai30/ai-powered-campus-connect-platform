import React, { useEffect, useState } from 'react';
import {
  getFacultyList,
  getFacultyById,
  createFaculty,
  updateFaculty,
  updateFacultyStatus,
} from '../../services/adminService';
import {
  AdminFacultyResponse,
  AdminFacultyDetailResponse,
  CreateFacultyRequest,
  UpdateFacultyRequest,
} from '../../types/admin.types';

export const AdminFacultyPage: React.FC = () => {
  const [facultyList, setFacultyList] = useState<AdminFacultyResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  // Filter & Search states
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<string>('all');

  // Modals state
  const [isCreateOpen, setIsCreateOpen] = useState<boolean>(false);
  const [isEditOpen, setIsEditOpen] = useState<boolean>(false);
  const [isDetailOpen, setIsDetailOpen] = useState<boolean>(false);
  const [statusConfirmUser, setStatusConfirmUser] = useState<AdminFacultyResponse | null>(null);

  // Active items state
  const [selectedDetail, setSelectedDetail] = useState<AdminFacultyDetailResponse | null>(null);
  const [detailLoading, setDetailLoading] = useState<boolean>(false);
  const [selectedEditUser, setSelectedEditUser] = useState<AdminFacultyResponse | null>(null);

  // Form states
  const [createForm, setCreateForm] = useState<CreateFacultyRequest>({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    phone: '',
  });

  const [editForm, setEditForm] = useState<UpdateFacultyRequest>({
    firstName: '',
    lastName: '',
    phone: '',
  });

  const [actionLoading, setActionLoading] = useState<boolean>(false);
  const [formError, setFormError] = useState<string | null>(null);

  const loadFaculty = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getFacultyList(statusFilter, searchTerm);
      setFacultyList(data);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to load faculty accounts list.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadFaculty();
  }, [statusFilter]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    loadFaculty();
  };

  const handleOpenDetails = async (id: number) => {
    setIsDetailOpen(true);
    setDetailLoading(true);
    setSelectedDetail(null);
    try {
      const detailData = await getFacultyById(id);
      setSelectedDetail(detailData);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to fetch faculty account details.');
    } finally {
      setDetailLoading(false);
    }
  };

  const handleOpenEdit = (faculty: AdminFacultyResponse) => {
    setSelectedEditUser(faculty);
    setEditForm({
      firstName: faculty.firstName,
      lastName: faculty.lastName,
      phone: faculty.phone || '',
    });
    setFormError(null);
    setIsEditOpen(true);
  };

  const handleCreateSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);
    setActionLoading(true);

    try {
      await createFaculty(createForm);
      setSuccessMessage(`Faculty account for ${createForm.firstName} ${createForm.lastName} created successfully.`);
      setIsCreateOpen(false);
      setCreateForm({ firstName: '', lastName: '', email: '', password: '', phone: '' });
      loadFaculty();
    } catch (err: any) {
      setFormError(err?.response?.data?.message || 'Failed to create Faculty account. Please verify input details.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleEditSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedEditUser) return;

    setFormError(null);
    setActionLoading(true);

    try {
      await updateFaculty(selectedEditUser.id, editForm);
      setSuccessMessage(`Faculty account for ${editForm.firstName} ${editForm.lastName} updated successfully.`);
      setIsEditOpen(false);
      loadFaculty();
    } catch (err: any) {
      setFormError(err?.response?.data?.message || 'Failed to update Faculty account.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleConfirmStatusToggle = async () => {
    if (!statusConfirmUser) return;
    setActionLoading(true);
    const newStatus = !statusConfirmUser.active;

    try {
      await updateFacultyStatus(statusConfirmUser.id, newStatus);
      setSuccessMessage(
        `Faculty account "${statusConfirmUser.firstName} ${statusConfirmUser.lastName}" has been ${
          newStatus ? 'activated' : 'deactivated'
        }.`
      );
      setStatusConfirmUser(null);
      loadFaculty();
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to update account status.');
    } finally {
      setActionLoading(false);
    }
  };

  return (
    <div className="space-y-8">
      {/* Page Header */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-6 sm:p-8 backdrop-blur shadow-xl relative overflow-hidden">
        <div className="absolute top-0 right-0 -mt-8 -mr-8 w-64 h-64 bg-cyan-500/5 rounded-full blur-3xl pointer-events-none"></div>
        <div className="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div>
            <div className="flex items-center space-x-3 mb-2">
              <span className="text-2xl">👨‍🏫</span>
              <span className="px-3 py-1 bg-cyan-500/10 text-cyan-400 border border-cyan-500/20 rounded-full text-xs font-semibold tracking-wider uppercase">
                Faculty Administration
              </span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-bold text-white tracking-tight">
              Faculty Account Management
            </h1>
            <p className="text-slate-400 text-sm mt-1 max-w-2xl">
              Create and manage academic faculty user accounts, update profile credentials, monitor managed campus content, and enforce access status.
            </p>
          </div>
          <div className="flex items-center space-x-3">
            <button
              onClick={() => {
                setFormError(null);
                setCreateForm({ firstName: '', lastName: '', email: '', password: '', phone: '' });
                setIsCreateOpen(true);
              }}
              className="px-4 py-2.5 bg-emerald-500 hover:bg-emerald-400 text-slate-950 text-xs font-bold rounded-xl transition-all shadow-lg shadow-emerald-500/20 flex items-center space-x-2"
            >
              <span>➕</span>
              <span>Create Faculty Member</span>
            </button>
          </div>
        </div>
      </div>

      {/* Notifications */}
      {successMessage && (
        <div className="bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 rounded-xl p-4 text-sm flex items-center justify-between shadow-md">
          <div className="flex items-center space-x-2">
            <span>✅</span>
            <span>{successMessage}</span>
          </div>
          <button
            onClick={() => setSuccessMessage(null)}
            className="text-emerald-400 hover:text-emerald-200 text-xs font-bold"
          >
            ✕
          </button>
        </div>
      )}

      {error && (
        <div className="bg-red-500/10 border border-red-500/30 text-red-300 rounded-xl p-4 text-sm flex items-center justify-between shadow-md">
          <div className="flex items-center space-x-2">
            <span>⚠️</span>
            <span>{error}</span>
          </div>
          <button onClick={() => setError(null)} className="text-red-400 hover:text-red-200 text-xs font-bold">
            ✕
          </button>
        </div>
      )}

      {/* Action Bar (Search & Filter) */}
      <div className="bg-slate-900/40 border border-slate-800 rounded-2xl p-4 flex flex-col sm:flex-row items-center justify-between gap-4">
        <form onSubmit={handleSearchSubmit} className="flex-1 w-full flex items-center space-x-2">
          <div className="relative flex-1">
            <input
              type="text"
              placeholder="Search faculty by name or email..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2 text-sm text-slate-100 placeholder-slate-500 focus:outline-none focus:border-cyan-500/50"
            />
          </div>
          <button
            type="submit"
            className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold rounded-xl border border-slate-700 transition-colors"
          >
            Search
          </button>
        </form>

        <div className="flex items-center space-x-3 w-full sm:w-auto justify-end">
          <label className="text-xs text-slate-400 font-medium">Status:</label>
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-500/50"
          >
            <option value="all">All Accounts</option>
            <option value="active">Active Only</option>
            <option value="inactive">Inactive Only</option>
          </select>
        </div>
      </div>

      {/* Faculty Table / List */}
      <div className="bg-slate-900/40 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
        {loading ? (
          <div className="p-8 space-y-4">
            {Array.from({ length: 4 }).map((_, idx) => (
              <div key={idx} className="h-14 bg-slate-800/40 rounded-xl animate-pulse"></div>
            ))}
          </div>
        ) : facultyList.length === 0 ? (
          <div className="p-12 text-center">
            <div className="w-12 h-12 rounded-full bg-slate-800 text-slate-500 flex items-center justify-center mx-auto mb-3 text-xl">
              👨‍🏫
            </div>
            <h3 className="text-slate-200 font-semibold text-base mb-1">No Faculty Accounts Found</h3>
            <p className="text-slate-500 text-xs max-w-sm mx-auto">
              There are currently no faculty accounts matching the selected search query or filter criteria.
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-slate-800 bg-slate-950/60 text-slate-400 text-xs font-semibold uppercase tracking-wider">
                  <th className="py-3.5 px-6">Faculty Name</th>
                  <th className="py-3.5 px-6">Email Address</th>
                  <th className="py-3.5 px-6">Phone</th>
                  <th className="py-3.5 px-6">Status</th>
                  <th className="py-3.5 px-6">Created Date</th>
                  <th className="py-3.5 px-6 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 text-sm">
                {facultyList.map((faculty) => (
                  <tr key={faculty.id} className="hover:bg-slate-800/30 transition-colors">
                    <td className="py-4 px-6 font-semibold text-white">
                      {faculty.firstName} {faculty.lastName}
                    </td>
                    <td className="py-4 px-6 text-slate-300 font-mono text-xs">{faculty.email}</td>
                    <td className="py-4 px-6 text-slate-400 text-xs">{faculty.phone || '—'}</td>
                    <td className="py-4 px-6">
                      <span
                        className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold border ${
                          faculty.active
                            ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
                            : 'bg-red-500/10 text-red-400 border-red-500/20'
                        }`}
                      >
                        <span
                          className={`w-1.5 h-1.5 rounded-full mr-1.5 ${
                            faculty.active ? 'bg-emerald-400' : 'bg-red-400'
                          }`}
                        ></span>
                        {faculty.active ? 'ACTIVE' : 'INACTIVE'}
                      </span>
                    </td>
                    <td className="py-4 px-6 text-slate-400 text-xs">
                      {faculty.createdAt ? new Date(faculty.createdAt).toLocaleDateString() : '—'}
                    </td>
                    <td className="py-4 px-6 text-right space-x-2">
                      <button
                        onClick={() => handleOpenDetails(faculty.id)}
                        className="px-2.5 py-1.5 bg-cyan-500/10 hover:bg-cyan-500/20 text-cyan-400 rounded-lg border border-cyan-500/20 text-xs font-medium transition-colors"
                      >
                        Details
                      </button>
                      <button
                        onClick={() => handleOpenEdit(faculty)}
                        className="px-2.5 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-lg border border-slate-700 text-xs font-medium transition-colors"
                      >
                        Edit
                      </button>
                      <button
                        onClick={() => setStatusConfirmUser(faculty)}
                        className={`px-2.5 py-1.5 rounded-lg border text-xs font-medium transition-colors ${
                          faculty.active
                            ? 'bg-red-500/10 hover:bg-red-500/20 text-red-400 border-red-500/20'
                            : 'bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-400 border-emerald-500/20'
                        }`}
                      >
                        {faculty.active ? 'Deactivate' : 'Activate'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* CREATE FACULTY MODAL */}
      {isCreateOpen && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-lg w-full p-6 shadow-2xl space-y-5 relative">
            <div className="flex justify-between items-center border-b border-slate-800 pb-4">
              <h3 className="text-lg font-bold text-white flex items-center space-x-2">
                <span>➕</span>
                <span>Create New Faculty Account</span>
              </h3>
              <button
                onClick={() => setIsCreateOpen(false)}
                className="text-slate-400 hover:text-white text-sm font-bold"
              >
                ✕
              </button>
            </div>

            {formError && (
              <div className="bg-red-500/10 border border-red-500/30 text-red-300 rounded-xl p-3 text-xs">
                {formError}
              </div>
            )}

            <form onSubmit={handleCreateSubmit} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">First Name *</label>
                  <input
                    type="text"
                    required
                    value={createForm.firstName}
                    onChange={(e) => setCreateForm({ ...createForm, firstName: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2 text-sm text-white focus:outline-none focus:border-cyan-500"
                    placeholder="e.g. Robert"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Last Name *</label>
                  <input
                    type="text"
                    required
                    value={createForm.lastName}
                    onChange={(e) => setCreateForm({ ...createForm, lastName: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2 text-sm text-white focus:outline-none focus:border-cyan-500"
                    placeholder="e.g. Smith"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Email Address *</label>
                <input
                  type="email"
                  required
                  value={createForm.email}
                  onChange={(e) => setCreateForm({ ...createForm, email: e.target.value })}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2 text-sm text-white focus:outline-none focus:border-cyan-500"
                  placeholder="faculty.name@campusconnect.edu"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Password *</label>
                <input
                  type="password"
                  required
                  minLength={6}
                  value={createForm.password}
                  onChange={(e) => setCreateForm({ ...createForm, password: e.target.value })}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2 text-sm text-white focus:outline-none focus:border-cyan-500"
                  placeholder="••••••••"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Phone Number (Optional)</label>
                <input
                  type="text"
                  value={createForm.phone || ''}
                  onChange={(e) => setCreateForm({ ...createForm, phone: e.target.value })}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2 text-sm text-white focus:outline-none focus:border-cyan-500"
                  placeholder="555-0199"
                />
              </div>

              <div className="flex justify-end space-x-3 pt-3 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setIsCreateOpen(false)}
                  className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-semibold rounded-xl"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={actionLoading}
                  className="px-5 py-2 bg-emerald-500 hover:bg-emerald-400 text-slate-950 text-xs font-bold rounded-xl shadow-md transition-all disabled:opacity-50"
                >
                  {actionLoading ? 'Creating Account...' : 'Create Account'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* EDIT FACULTY MODAL */}
      {isEditOpen && selectedEditUser && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-lg w-full p-6 shadow-2xl space-y-5 relative">
            <div className="flex justify-between items-center border-b border-slate-800 pb-4">
              <h3 className="text-lg font-bold text-white flex items-center space-x-2">
                <span>✏️</span>
                <span>Edit Faculty Account</span>
              </h3>
              <button
                onClick={() => setIsEditOpen(false)}
                className="text-slate-400 hover:text-white text-sm font-bold"
              >
                ✕
              </button>
            </div>

            {formError && (
              <div className="bg-red-500/10 border border-red-500/30 text-red-300 rounded-xl p-3 text-xs">
                {formError}
              </div>
            )}

            <form onSubmit={handleEditSubmit} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">First Name *</label>
                  <input
                    type="text"
                    required
                    value={editForm.firstName}
                    onChange={(e) => setEditForm({ ...editForm, firstName: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2 text-sm text-white focus:outline-none focus:border-cyan-500"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Last Name *</label>
                  <input
                    type="text"
                    required
                    value={editForm.lastName}
                    onChange={(e) => setEditForm({ ...editForm, lastName: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2 text-sm text-white focus:outline-none focus:border-cyan-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-500 mb-1">Email Address (Read-only)</label>
                <input
                  type="text"
                  disabled
                  value={selectedEditUser.email}
                  className="w-full bg-slate-950/50 border border-slate-800/50 rounded-xl px-3.5 py-2 text-sm text-slate-500 cursor-not-allowed"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Phone Number</label>
                <input
                  type="text"
                  value={editForm.phone || ''}
                  onChange={(e) => setEditForm({ ...editForm, phone: e.target.value })}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2 text-sm text-white focus:outline-none focus:border-cyan-500"
                />
              </div>

              <div className="flex justify-end space-x-3 pt-3 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setIsEditOpen(false)}
                  className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-semibold rounded-xl"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={actionLoading}
                  className="px-5 py-2 bg-cyan-500 hover:bg-cyan-400 text-slate-950 text-xs font-bold rounded-xl shadow-md transition-all disabled:opacity-50"
                >
                  {actionLoading ? 'Saving...' : 'Save Changes'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* FACULTY DETAILS MODAL */}
      {isDetailOpen && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-xl w-full p-6 shadow-2xl space-y-6 relative">
            <div className="flex justify-between items-center border-b border-slate-800 pb-4">
              <h3 className="text-lg font-bold text-white flex items-center space-x-2">
                <span>🎓</span>
                <span>Faculty Profile & Content Metrics</span>
              </h3>
              <button
                onClick={() => setIsDetailOpen(false)}
                className="text-slate-400 hover:text-white text-sm font-bold"
              >
                ✕
              </button>
            </div>

            {detailLoading ? (
              <div className="py-8 text-center space-y-3">
                <div className="w-8 h-8 border-2 border-cyan-500/30 border-t-cyan-500 rounded-full animate-spin mx-auto"></div>
                <p className="text-xs text-slate-400">Loading faculty details and content stats...</p>
              </div>
            ) : selectedDetail ? (
              <div className="space-y-6">
                <div className="bg-slate-950/60 border border-slate-800/80 rounded-xl p-4 flex items-center justify-between">
                  <div>
                    <h4 className="text-base font-bold text-white">
                      {selectedDetail.firstName} {selectedDetail.lastName}
                    </h4>
                    <p className="text-xs text-slate-400 font-mono">{selectedDetail.email}</p>
                    <p className="text-xs text-slate-500 mt-1">Phone: {selectedDetail.phone || '—'}</p>
                  </div>
                  <span
                    className={`px-3 py-1 rounded-full text-xs font-bold border ${
                      selectedDetail.active
                        ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
                        : 'bg-red-500/10 text-red-400 border-red-500/20'
                    }`}
                  >
                    {selectedDetail.active ? 'ACTIVE' : 'INACTIVE'}
                  </span>
                </div>

                <div>
                  <h5 className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-3">
                    Managed Content Metrics (Real Database Records)
                  </h5>
                  <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
                    <div className="bg-slate-950 border border-slate-800 rounded-xl p-3 text-center">
                      <div className="text-xs text-slate-400">Resources</div>
                      <div className="text-xl font-bold text-teal-400 mt-1">{selectedDetail.resourceCount}</div>
                    </div>
                    <div className="bg-slate-950 border border-slate-800 rounded-xl p-3 text-center">
                      <div className="text-xs text-slate-400">Bulletins</div>
                      <div className="text-xl font-bold text-rose-400 mt-1">{selectedDetail.announcementCount}</div>
                    </div>
                    <div className="bg-slate-950 border border-slate-800 rounded-xl p-3 text-center">
                      <div className="text-xs text-slate-400">Events</div>
                      <div className="text-xl font-bold text-indigo-400 mt-1">{selectedDetail.eventCount}</div>
                    </div>
                    <div className="bg-slate-950 border border-slate-800 rounded-xl p-3 text-center">
                      <div className="text-xs text-slate-400">Clubs</div>
                      <div className="text-xl font-bold text-purple-400 mt-1">{selectedDetail.clubCount}</div>
                    </div>
                    <div className="bg-slate-950 border border-slate-800 rounded-xl p-3 text-center">
                      <div className="text-xs text-slate-400">Opportunities</div>
                      <div className="text-xl font-bold text-amber-400 mt-1">{selectedDetail.opportunityCount}</div>
                    </div>
                  </div>
                </div>

                <div className="flex justify-end pt-3 border-t border-slate-800">
                  <button
                    onClick={() => setIsDetailOpen(false)}
                    className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-semibold rounded-xl"
                  >
                    Close
                  </button>
                </div>
              </div>
            ) : null}
          </div>
        </div>
      )}

      {/* STATUS CONFIRMATION MODAL */}
      {statusConfirmUser && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-md w-full p-6 shadow-2xl space-y-4 text-center">
            <div className="w-12 h-12 rounded-full bg-amber-500/10 text-amber-400 flex items-center justify-center mx-auto text-xl font-bold">
              ⚠️
            </div>
            <h3 className="text-lg font-bold text-white">
              {statusConfirmUser.active ? 'Deactivate Faculty Account?' : 'Activate Faculty Account?'}
            </h3>
            <p className="text-slate-400 text-xs leading-relaxed">
              Are you sure you want to {statusConfirmUser.active ? 'deactivate' : 'activate'} the account for{' '}
              <strong className="text-slate-200">
                {statusConfirmUser.firstName} {statusConfirmUser.lastName}
              </strong>
              ? {statusConfirmUser.active ? 'This will prevent the user from logging in or managing faculty content.' : 'This will restore full login and faculty portal permissions.'}
            </p>

            <div className="flex justify-center space-x-3 pt-3 border-t border-slate-800">
              <button
                onClick={() => setStatusConfirmUser(null)}
                className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-semibold rounded-xl"
              >
                Cancel
              </button>
              <button
                onClick={handleConfirmStatusToggle}
                disabled={actionLoading}
                className={`px-5 py-2 text-xs font-bold rounded-xl shadow-md transition-all disabled:opacity-50 ${
                  statusConfirmUser.active
                    ? 'bg-red-500 hover:bg-red-400 text-white shadow-red-500/20'
                    : 'bg-emerald-500 hover:bg-emerald-400 text-slate-950 shadow-emerald-500/20'
                }`}
              >
                {actionLoading ? 'Processing...' : statusConfirmUser.active ? 'Deactivate Account' : 'Activate Account'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminFacultyPage;
