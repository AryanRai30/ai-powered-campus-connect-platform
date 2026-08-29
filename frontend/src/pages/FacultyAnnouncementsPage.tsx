import React, { useState, useEffect } from 'react';
import {
  getFacultyAnnouncements,
  createFacultyAnnouncement,
  updateFacultyAnnouncement,
  deleteFacultyAnnouncement,
  publishFacultyAnnouncement,
  unpublishFacultyAnnouncement,
} from '../services/facultyService';
import { FacultyAnnouncement, FacultyAnnouncementRequest } from '../types/faculty.types';

export const FacultyAnnouncementsPage: React.FC = () => {
  const [announcements, setAnnouncements] = useState<FacultyAnnouncement[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'PUBLISHED' | 'DRAFT'>('ALL');

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [editingAnnouncement, setEditingAnnouncement] = useState<FacultyAnnouncement | null>(null);

  // Form State
  const [formData, setFormData] = useState<FacultyAnnouncementRequest>({
    title: '',
    content: '',
    category: 'General',
    targetDepartment: '',
    targetCourse: '',
    targetYear: undefined,
    targetSemester: undefined,
    published: false,
  });

  const [formSubmitting, setFormSubmitting] = useState<boolean>(false);
  const [deleteConfirmId, setDeleteConfirmId] = useState<number | null>(null);

  const fetchAnnouncements = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await getFacultyAnnouncements();
      setAnnouncements(data);
    } catch (err: any) {
      console.error('Failed to load announcements:', err);
      setError('Failed to fetch faculty announcements.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAnnouncements();
  }, []);

  const openCreateModal = () => {
    setEditingAnnouncement(null);
    setFormData({
      title: '',
      content: '',
      category: 'General',
      targetDepartment: '',
      targetCourse: '',
      targetYear: undefined,
      targetSemester: undefined,
      published: false,
    });
    setIsModalOpen(true);
  };

  const openEditModal = (announcement: FacultyAnnouncement) => {
    setEditingAnnouncement(announcement);
    setFormData({
      title: announcement.title,
      content: announcement.content,
      category: announcement.category || 'General',
      targetDepartment: announcement.targetDepartment || '',
      targetCourse: announcement.targetCourse || '',
      targetYear: announcement.targetYear || undefined,
      targetSemester: announcement.targetSemester || undefined,
      published: announcement.published,
    });
    setIsModalOpen(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setFormSubmitting(true);
      if (editingAnnouncement) {
        await updateFacultyAnnouncement(editingAnnouncement.id, formData);
      } else {
        await createFacultyAnnouncement(formData);
      }
      setIsModalOpen(false);
      fetchAnnouncements();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to save announcement.');
    } finally {
      setFormSubmitting(false);
    }
  };

  const handleTogglePublish = async (announcement: FacultyAnnouncement) => {
    try {
      if (announcement.published) {
        await unpublishFacultyAnnouncement(announcement.id);
      } else {
        await publishFacultyAnnouncement(announcement.id);
      }
      fetchAnnouncements();
    } catch (err: any) {
      alert('Failed to update publication status.');
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteFacultyAnnouncement(id);
      setDeleteConfirmId(null);
      fetchAnnouncements();
    } catch (err: any) {
      alert('Failed to delete announcement.');
    }
  };

  const filteredAnnouncements = announcements.filter((a) => {
    const matchesSearch =
      a.title.toLowerCase().includes(search.toLowerCase()) ||
      a.content.toLowerCase().includes(search.toLowerCase());

    const matchesStatus =
      statusFilter === 'ALL' ||
      (statusFilter === 'PUBLISHED' && a.published) ||
      (statusFilter === 'DRAFT' && !a.published);

    return matchesSearch && matchesStatus;
  });

  return (
    <div className="space-y-8 max-w-7xl mx-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-800 pb-5">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-100 flex items-center space-x-3">
            <span>📢 Campus Announcements Management</span>
          </h1>
          <p className="text-slate-400 text-sm mt-1">
            Publish official academic bulletins and announcements targeted to specific student groups.
          </p>
        </div>
        <button
          onClick={openCreateModal}
          className="px-4 py-2.5 bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-semibold rounded-xl text-sm transition-all shadow-md shadow-emerald-500/20 flex items-center justify-center space-x-2"
        >
          <span>+ Create Announcement</span>
        </button>
      </div>

      {/* Search & Status Filters */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-4 flex flex-col sm:flex-row gap-4 justify-between items-center">
        <div className="relative w-full sm:w-80">
          <input
            type="text"
            placeholder="Search announcements..."
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
      ) : filteredAnnouncements.length === 0 ? (
        <div className="bg-slate-900/50 border border-slate-800 rounded-2xl p-12 text-center">
          <div className="text-4xl mb-3">📢</div>
          <h3 className="text-lg font-bold text-slate-300">No announcements created yet.</h3>
          <p className="text-slate-500 text-sm mt-1">
            Click "+ Create Announcement" to broadcast a new notice.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {filteredAnnouncements.map((a) => (
            <div
              key={a.id}
              className="bg-slate-900 border border-slate-800 rounded-xl p-6 flex flex-col justify-between hover:border-slate-700 transition-all shadow-lg"
            >
              <div>
                <div className="flex items-center justify-between mb-3">
                  <span className="text-xs font-semibold px-2.5 py-1 bg-slate-800 text-slate-300 rounded-md border border-slate-700">
                    {a.category || 'General'}
                  </span>
                  <span
                    className={`text-xs font-bold px-2.5 py-1 rounded-full border ${
                      a.published
                        ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30'
                        : 'bg-amber-500/10 text-amber-400 border-amber-500/30'
                    }`}
                  >
                    {a.published ? 'PUBLISHED' : 'DRAFT'}
                  </span>
                </div>

                <h3 className="text-lg font-bold text-slate-100 mb-2">{a.title}</h3>
                <p className="text-slate-400 text-sm mb-4 whitespace-pre-line">{a.content}</p>

                {/* Target Audience Tags */}
                <div className="flex flex-wrap gap-1.5 mb-4">
                  {a.targetDepartment && (
                    <span className="text-[11px] bg-slate-950 text-slate-400 px-2 py-0.5 rounded border border-slate-800">
                      Dept: {a.targetDepartment}
                    </span>
                  )}
                  {a.targetCourse && (
                    <span className="text-[11px] bg-slate-950 text-slate-400 px-2 py-0.5 rounded border border-slate-800">
                      Course: {a.targetCourse}
                    </span>
                  )}
                  {a.targetYear && (
                    <span className="text-[11px] bg-slate-950 text-slate-400 px-2 py-0.5 rounded border border-slate-800">
                      Yr {a.targetYear}
                    </span>
                  )}
                  {a.targetSemester && (
                    <span className="text-[11px] bg-slate-950 text-slate-400 px-2 py-0.5 rounded border border-slate-800">
                      Sem {a.targetSemester}
                    </span>
                  )}
                  {!a.targetDepartment && !a.targetCourse && !a.targetYear && !a.targetSemester && (
                    <span className="text-[11px] bg-emerald-500/10 text-emerald-400 px-2 py-0.5 rounded border border-emerald-500/20">
                      All Students
                    </span>
                  )}
                </div>
              </div>

              <div className="pt-4 border-t border-slate-800 flex items-center justify-between gap-2">
                <button
                  onClick={() => handleTogglePublish(a)}
                  className={`text-xs font-semibold px-3 py-1.5 rounded-lg border transition-colors ${
                    a.published
                      ? 'bg-amber-500/10 text-amber-400 border-amber-500/30 hover:bg-amber-500/20'
                      : 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30 hover:bg-emerald-500/20'
                  }`}
                >
                  {a.published ? 'Unpublish' : 'Publish'}
                </button>

                <div className="flex items-center space-x-2">
                  <button
                    onClick={() => openEditModal(a)}
                    className="text-xs font-semibold px-3 py-1.5 bg-slate-800 text-slate-300 rounded-lg hover:bg-slate-700 border border-slate-700 transition-colors"
                  >
                    Edit
                  </button>
                  <button
                    onClick={() => setDeleteConfirmId(a.id)}
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
                {editingAnnouncement ? 'Edit Announcement' : 'Create Announcement'}
              </h3>
              <button
                onClick={() => setIsModalOpen(false)}
                className="text-slate-400 hover:text-slate-200 text-xl font-bold"
              >
                &times;
              </button>
            </div>

            <form onSubmit={handleSubmit} className="p-6 space-y-4 max-h-[80vh] overflow-y-auto">
              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                  Title *
                </label>
                <input
                  type="text"
                  required
                  value={formData.title}
                  onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-emerald-500"
                  placeholder="e.g. Mid-Term Examination Schedule Released"
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
                  placeholder="Academic / Administrative / General"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                  Content *
                </label>
                <textarea
                  required
                  rows={4}
                  value={formData.content}
                  onChange={(e) => setFormData({ ...formData, content: e.target.value })}
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-emerald-500"
                  placeholder="Detailed announcement description..."
                />
              </div>

              {/* Targeting */}
              <div className="border-t border-slate-800 pt-4">
                <h4 className="text-xs font-bold text-emerald-400 uppercase tracking-wider mb-3">
                  Target Student Audience (Optional)
                </h4>
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                  <div>
                    <label className="block text-[11px] text-slate-400 mb-1">Department</label>
                    <input
                      type="text"
                      value={formData.targetDepartment}
                      onChange={(e) => setFormData({ ...formData, targetDepartment: e.target.value })}
                      className="w-full bg-slate-950 border border-slate-800 rounded px-2.5 py-1.5 text-xs text-slate-200"
                      placeholder="e.g. CSE"
                    />
                  </div>
                  <div>
                    <label className="block text-[11px] text-slate-400 mb-1">Course</label>
                    <input
                      type="text"
                      value={formData.targetCourse}
                      onChange={(e) => setFormData({ ...formData, targetCourse: e.target.value })}
                      className="w-full bg-slate-950 border border-slate-800 rounded px-2.5 py-1.5 text-xs text-slate-200"
                      placeholder="e.g. B.Tech"
                    />
                  </div>
                  <div>
                    <label className="block text-[11px] text-slate-400 mb-1">Year</label>
                    <input
                      type="number"
                      min={1}
                      max={6}
                      value={formData.targetYear || ''}
                      onChange={(e) => setFormData({ ...formData, targetYear: e.target.value ? parseInt(e.target.value) : undefined })}
                      className="w-full bg-slate-950 border border-slate-800 rounded px-2.5 py-1.5 text-xs text-slate-200"
                      placeholder="e.g. 3"
                    />
                  </div>
                  <div>
                    <label className="block text-[11px] text-slate-400 mb-1">Semester</label>
                    <input
                      type="number"
                      min={1}
                      max={12}
                      value={formData.targetSemester || ''}
                      onChange={(e) => setFormData({ ...formData, targetSemester: e.target.value ? parseInt(e.target.value) : undefined })}
                      className="w-full bg-slate-950 border border-slate-800 rounded px-2.5 py-1.5 text-xs text-slate-200"
                      placeholder="e.g. 5"
                    />
                  </div>
                </div>
              </div>

              <div className="flex items-center space-x-2 pt-2">
                <input
                  type="checkbox"
                  id="announcementPublishCheckbox"
                  checked={formData.published}
                  onChange={(e) => setFormData({ ...formData, published: e.target.checked })}
                  className="w-4 h-4 text-emerald-500 rounded bg-slate-950 border-slate-800"
                />
                <label htmlFor="announcementPublishCheckbox" className="text-xs font-semibold text-slate-300">
                  Publish immediately (Visible to targeted students)
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
                  {formSubmitting ? 'Saving...' : editingAnnouncement ? 'Update Announcement' : 'Create Announcement'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Delete Modal */}
      {deleteConfirmId !== null && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 max-w-sm w-full text-center">
            <h3 className="text-lg font-bold text-slate-100 mb-2">Delete Announcement?</h3>
            <p className="text-slate-400 text-sm mb-6">
              Are you sure you want to delete this announcement?
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

export default FacultyAnnouncementsPage;
