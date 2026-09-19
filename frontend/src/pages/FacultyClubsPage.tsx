import React, { useState, useEffect } from 'react';
import {
  getFacultyClubs,
  createFacultyClub,
  updateFacultyClub,
  deleteFacultyClub,
  publishFacultyClub,
  unpublishFacultyClub,
  getClubMembers,
} from '../services/facultyService';
import { FacultyClub, FacultyClubRequest, ClubMemberItem } from '../types/faculty.types';

export const FacultyClubsPage: React.FC = () => {
  const [clubs, setClubs] = useState<FacultyClub[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'PUBLISHED' | 'DRAFT'>('ALL');

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [editingClub, setEditingClub] = useState<FacultyClub | null>(null);

  // Members Drawer State
  const [membersModalClub, setMembersModalClub] = useState<FacultyClub | null>(null);
  const [members, setMembers] = useState<ClubMemberItem[]>([]);
  const [membersLoading, setMembersLoading] = useState<boolean>(false);

  // Form State
  const [formData, setFormData] = useState<FacultyClubRequest>({
    name: '',
    description: '',
    category: 'Technical',
    presidentName: '',
    meetingDay: '',
    meetingTime: '',
    meetingVenue: '',
    department: '',
    published: false,
  });

  const [formSubmitting, setFormSubmitting] = useState<boolean>(false);
  const [deleteConfirmId, setDeleteConfirmId] = useState<number | null>(null);

  const fetchClubs = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await getFacultyClubs();
      setClubs(data);
    } catch (err: any) {
      console.error('Failed to load clubs:', err);
      setError('Failed to fetch faculty managed clubs.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchClubs();
  }, []);

  const openCreateModal = () => {
    setEditingClub(null);
    setFormData({
      name: '',
      description: '',
      category: 'Technical',
      presidentName: '',
      meetingDay: '',
      meetingTime: '',
      meetingVenue: '',
      department: '',
      published: false,
    });
    setIsModalOpen(true);
  };

  const openEditModal = (club: FacultyClub) => {
    setEditingClub(club);
    setFormData({
      name: club.name,
      description: club.description,
      category: club.category || 'Technical',
      presidentName: club.presidentName || '',
      meetingDay: club.meetingDay || '',
      meetingTime: club.meetingTime || '',
      meetingVenue: club.meetingVenue || '',
      department: club.department || '',
      published: club.published,
    });
    setIsModalOpen(true);
  };

  const openMembersModal = async (club: FacultyClub) => {
    setMembersModalClub(club);
    try {
      setMembersLoading(true);
      const list = await getClubMembers(club.id);
      setMembers(list);
    } catch (err: any) {
      alert('Failed to fetch club membership list.');
    } finally {
      setMembersLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setFormSubmitting(true);
      if (editingClub) {
        await updateFacultyClub(editingClub.id, formData);
      } else {
        await createFacultyClub(formData);
      }
      setIsModalOpen(false);
      fetchClubs();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to save club.');
    } finally {
      setFormSubmitting(false);
    }
  };

  const handleTogglePublish = async (club: FacultyClub) => {
    try {
      if (club.published) {
        await unpublishFacultyClub(club.id);
      } else {
        await publishFacultyClub(club.id);
      }
      fetchClubs();
    } catch (err: any) {
      alert('Failed to update publication status.');
    }
  };

  const handleDelete = async (id: number) => {
    if (!id) return;
    try {
      await deleteFacultyClub(id);
      setDeleteConfirmId(null);
      await fetchClubs();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to delete club.');
    }
  };

  const filteredClubs = clubs.filter((c) => {
    const matchesSearch =
      c.name.toLowerCase().includes(search.toLowerCase()) ||
      c.description.toLowerCase().includes(search.toLowerCase());

    const matchesStatus =
      statusFilter === 'ALL' ||
      (statusFilter === 'PUBLISHED' && c.published) ||
      (statusFilter === 'DRAFT' && !c.published);

    return matchesSearch && matchesStatus;
  });

  return (
    <div className="space-y-8 max-w-7xl mx-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-800 pb-5">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-100 flex items-center space-x-3">
            <span>🤝 Campus Clubs Management</span>
          </h1>
          <p className="text-slate-400 text-sm mt-1">
            Establish, manage, and supervise campus student clubs, societies, and member rolls.
          </p>
        </div>
        <button
          onClick={openCreateModal}
          className="px-4 py-2.5 bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-semibold rounded-xl text-sm transition-all shadow-md shadow-emerald-500/20 flex items-center justify-center space-x-2"
        >
          <span>+ Create Club</span>
        </button>
      </div>

      {/* Filter Bar */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-4 flex flex-col sm:flex-row gap-4 justify-between items-center">
        <div className="relative w-full sm:w-80">
          <input
            type="text"
            placeholder="Search clubs by name or description..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3.5 py-2 text-sm text-slate-200 focus:outline-none focus:border-emerald-500"
          />
        </div>

        <div className="flex items-center space-x-2 w-full sm:w-auto">
          <span className="text-xs text-slate-400 uppercase font-semibold">Status:</span>
          {(['ALL', 'PUBLISHED', 'DRAFT'] as const).map((st) => (
            <button
              key={st}
              onClick={() => setStatusFilter(st)}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-colors ${
                statusFilter === st
                  ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
                  : 'bg-slate-950 text-slate-400 hover:bg-slate-800 border border-slate-800'
              }`}
            >
              {st}
            </button>
          ))}
        </div>
      </div>

      {error && (
        <div className="p-4 bg-red-500/10 border border-red-500/30 rounded-xl text-red-400 text-sm">
          {error}
        </div>
      )}

      {/* Grid */}
      {loading ? (
        <div className="flex items-center justify-center py-16">
          <div className="w-8 h-8 border-4 border-emerald-500/20 border-t-emerald-500 rounded-full animate-spin"></div>
        </div>
      ) : filteredClubs.length === 0 ? (
        <div className="bg-slate-900/50 border border-slate-800 rounded-2xl p-12 text-center">
          <div className="text-4xl mb-3">🤝</div>
          <h3 className="text-lg font-bold text-slate-300">No clubs managed yet.</h3>
          <p className="text-slate-500 text-sm mt-1">
            Click "+ Create Club" to register a new campus club or society.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredClubs.map((club) => (
            <div
              key={club.id}
              className="bg-slate-900 border border-slate-800 rounded-xl p-6 flex flex-col justify-between hover:border-slate-700 transition-all shadow-lg"
            >
              <div>
                <div className="flex items-center justify-between mb-3">
                  <span className="text-xs font-semibold px-2.5 py-1 bg-slate-800 text-slate-300 rounded-md border border-slate-700">
                    {club.category || 'General'}
                  </span>
                  <span
                    className={`text-xs font-bold px-2.5 py-1 rounded-full border ${
                      club.published
                        ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30'
                        : 'bg-amber-500/10 text-amber-400 border-amber-500/30'
                    }`}
                  >
                    {club.published ? 'PUBLISHED' : 'DRAFT'}
                  </span>
                </div>

                <h3 className="text-lg font-bold text-slate-100 mb-1">{club.name}</h3>
                {club.presidentName && (
                  <p className="text-xs text-slate-400 mb-2">👤 Leader: {club.presidentName}</p>
                )}
                <p className="text-slate-400 text-sm mb-4 line-clamp-3">{club.description}</p>

                <div className="bg-slate-950/60 border border-slate-800/80 rounded-lg p-3 mb-4 flex items-center justify-between">
                  <div>
                    <div className="text-[11px] text-slate-400 font-semibold uppercase">Members</div>
                    <div className="text-base font-bold text-emerald-400">{club.memberCount} Joined</div>
                  </div>
                  <button
                    onClick={() => openMembersModal(club)}
                    className="text-xs font-semibold px-3 py-1.5 bg-emerald-500/10 text-emerald-400 hover:bg-emerald-500/20 border border-emerald-500/30 rounded-lg transition-colors"
                  >
                    Member List
                  </button>
                </div>
              </div>

              <div className="pt-4 border-t border-slate-800 flex items-center justify-between gap-2">
                <button
                  onClick={() => handleTogglePublish(club)}
                  className={`text-xs font-semibold px-3 py-1.5 rounded-lg border transition-colors ${
                    club.published
                      ? 'bg-amber-500/10 text-amber-400 border-amber-500/30 hover:bg-amber-500/20'
                      : 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30 hover:bg-emerald-500/20'
                  }`}
                >
                  {club.published ? 'Unpublish' : 'Publish'}
                </button>

                <div className="flex items-center space-x-2">
                  <button
                    onClick={() => openEditModal(club)}
                    className="text-xs font-semibold px-3 py-1.5 bg-slate-800 text-slate-300 rounded-lg hover:bg-slate-700 border border-slate-700 transition-colors"
                  >
                    Edit
                  </button>
                  <button
                    onClick={() => setDeleteConfirmId(club.id)}
                    className="text-xs font-semibold px-3 py-1.5 bg-red-500/10 text-red-400 rounded-lg hover:bg-red-500/20 border border-red-500/20 transition-colors"
                  >
                    Delete
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-2xl overflow-hidden shadow-2xl">
            <div className="px-6 py-4 border-b border-slate-800 flex justify-between items-center">
              <h3 className="text-lg font-bold text-slate-100">
                {editingClub ? 'Edit Club' : 'Create Club'}
              </h3>
              <button
                onClick={() => setIsModalOpen(false)}
                className="text-slate-400 hover:text-slate-200 text-xl font-bold"
              >
                &times;
              </button>
            </div>

            <form onSubmit={handleSubmit} className="p-6 space-y-4 max-h-[80vh] overflow-y-auto">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                    Club Name *
                  </label>
                  <input
                    type="text"
                    required
                    value={formData.name}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-emerald-500"
                    placeholder="e.g. Robotics & AI Club"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                    Category
                  </label>
                  <input
                    type="text"
                    value={formData.category}
                    onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-emerald-500"
                    placeholder="Technical / Cultural / Sports"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                  Description *
                </label>
                <textarea
                  required
                  rows={3}
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-emerald-500"
                  placeholder="Club objectives and activities..."
                />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                    President / Student Leader
                  </label>
                  <input
                    type="text"
                    value={formData.presidentName}
                    onChange={(e) => setFormData({ ...formData, presidentName: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-emerald-500"
                    placeholder="Student Leader Name"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                    Meeting Day & Time
                  </label>
                  <input
                    type="text"
                    value={formData.meetingDay}
                    onChange={(e) => setFormData({ ...formData, meetingDay: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-emerald-500"
                    placeholder="Every Friday 4:00 PM"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                    Meeting Venue
                  </label>
                  <input
                    type="text"
                    value={formData.meetingVenue}
                    onChange={(e) => setFormData({ ...formData, meetingVenue: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-emerald-500"
                    placeholder="Lab 4 / Seminar Hall"
                  />
                </div>
              </div>

              <div className="flex items-center space-x-2 pt-2">
                <input
                  type="checkbox"
                  id="clubPublishCheckbox"
                  checked={formData.published}
                  onChange={(e) => setFormData({ ...formData, published: e.target.checked })}
                  className="w-4 h-4 text-emerald-500 rounded bg-slate-950 border-slate-800"
                />
                <label htmlFor="clubPublishCheckbox" className="text-xs font-semibold text-slate-300">
                  Publish immediately (Visible on student club portal)
                </label>
              </div>

              <div className="flex items-center justify-end space-x-3 pt-4 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="px-4 py-2 bg-slate-800 text-slate-300 rounded-lg text-sm hover:bg-slate-700"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={formSubmitting}
                  className="px-4 py-2 bg-emerald-500 text-slate-950 font-semibold rounded-lg text-sm hover:bg-emerald-400"
                >
                  {formSubmitting ? 'Saving...' : editingClub ? 'Update Club' : 'Create Club'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Members Modal */}
      {membersModalClub && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-xl overflow-hidden shadow-2xl">
            <div className="px-6 py-4 border-b border-slate-800 flex justify-between items-center">
              <div>
                <h3 className="text-lg font-bold text-slate-100">Club Members</h3>
                <p className="text-xs text-slate-400">{membersModalClub.name}</p>
              </div>
              <button
                onClick={() => setMembersModalClub(null)}
                className="text-slate-400 hover:text-slate-200 text-xl font-bold"
              >
                &times;
              </button>
            </div>

            <div className="p-6 max-h-[60vh] overflow-y-auto">
              {membersLoading ? (
                <div className="flex justify-center py-8">
                  <div className="w-6 h-6 border-2 border-emerald-500/20 border-t-emerald-500 rounded-full animate-spin"></div>
                </div>
              ) : members.length === 0 ? (
                <p className="text-center text-slate-400 text-sm py-6">
                  No students have joined this club yet.
                </p>
              ) : (
                <div className="divide-y divide-slate-800">
                  {members.map((mem) => (
                    <div key={mem.membershipId} className="py-3 flex justify-between items-center text-sm">
                      <div>
                        <div className="font-semibold text-slate-200">
                          {mem.firstName} {mem.lastName}
                        </div>
                        <div className="text-xs text-slate-400">{mem.email}</div>
                        {mem.studentIdCode && (
                          <div className="text-[11px] text-purple-400 font-mono mt-0.5">ID: {mem.studentIdCode}</div>
                        )}
                      </div>
                      <div className="text-right text-xs text-slate-400">
                        <div>{mem.department || 'N/A'} {mem.course ? `(${mem.course})` : ''}</div>
                        <div>{mem.year ? `Yr ${mem.year}` : ''} {mem.semester ? `Sem ${mem.semester}` : ''}</div>
                        <div className="text-[10px] text-slate-500 mt-0.5">
                          Joined {new Date(mem.joinedAt).toLocaleString()}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div className="px-6 py-3 border-t border-slate-800 text-right">
              <button
                onClick={() => setMembersModalClub(null)}
                className="px-4 py-2 bg-slate-800 text-slate-300 rounded-lg text-sm"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Delete Modal */}
      {deleteConfirmId !== null && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 max-w-sm w-full text-center">
            <h3 className="text-lg font-bold text-slate-100 mb-2">Delete Club?</h3>
            <p className="text-slate-400 text-sm mb-6">
              Are you sure you want to delete this club?
            </p>
            <div className="flex items-center justify-center space-x-3">
              <button
                onClick={() => setDeleteConfirmId(null)}
                className="px-4 py-2 bg-slate-800 text-slate-300 rounded-lg text-sm"
              >
                Cancel
              </button>
              <button
                onClick={() => handleDelete(deleteConfirmId)}
                className="px-4 py-2 bg-red-500 text-white font-semibold rounded-lg text-sm hover:bg-red-600"
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default FacultyClubsPage;
