import React, { useState, useEffect } from 'react';
import {
  getFacultyResources,
  createFacultyResource,
  updateFacultyResource,
  deleteFacultyResource,
  publishFacultyResource,
  unpublishFacultyResource,
} from '../services/facultyService';
import { FacultyResource, FacultyResourceRequest } from '../types/faculty.types';

export const FacultyResourcesPage: React.FC = () => {
  const [resources, setResources] = useState<FacultyResource[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'PUBLISHED' | 'DRAFT'>('ALL');

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [editingResource, setEditingResource] = useState<FacultyResource | null>(null);

  // Form State
  const [formData, setFormData] = useState<FacultyResourceRequest>({
    title: '',
    description: '',
    subject: '',
    category: 'General',
    resourceType: 'NOTES',
    resourceUrl: '',
    targetDepartment: '',
    targetCourse: '',
    targetYear: undefined,
    targetSemester: undefined,
    published: false,
  });

  const [formSubmitting, setFormSubmitting] = useState<boolean>(false);
  const [deleteConfirmId, setDeleteConfirmId] = useState<number | null>(null);

  const fetchResources = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await getFacultyResources();
      setResources(data);
    } catch (err: any) {
      console.error('Failed to load resources:', err);
      setError('Failed to fetch faculty academic resources.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchResources();
  }, []);

  const openCreateModal = () => {
    setEditingResource(null);
    setFormData({
      title: '',
      description: '',
      subject: '',
      category: 'General',
      resourceType: 'NOTES',
      resourceUrl: '',
      targetDepartment: '',
      targetCourse: '',
      targetYear: undefined,
      targetSemester: undefined,
      published: false,
    });
    setIsModalOpen(true);
  };

  const openEditModal = (resource: FacultyResource) => {
    setEditingResource(resource);
    setFormData({
      title: resource.title,
      description: resource.description,
      subject: resource.subject,
      category: resource.category || 'General',
      resourceType: resource.resourceType || 'NOTES',
      resourceUrl: resource.resourceUrl || '',
      targetDepartment: resource.targetDepartment || '',
      targetCourse: resource.targetCourse || '',
      targetYear: resource.targetYear || undefined,
      targetSemester: resource.targetSemester || undefined,
      published: resource.published,
    });
    setIsModalOpen(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setFormSubmitting(true);
      if (editingResource) {
        await updateFacultyResource(editingResource.id, formData);
      } else {
        await createFacultyResource(formData);
      }
      setIsModalOpen(false);
      fetchResources();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to save academic resource.');
    } finally {
      setFormSubmitting(false);
    }
  };

  const handleTogglePublish = async (resource: FacultyResource) => {
    try {
      if (resource.published) {
        await unpublishFacultyResource(resource.id);
      } else {
        await publishFacultyResource(resource.id);
      }
      fetchResources();
    } catch (err: any) {
      alert('Failed to update publication status.');
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteFacultyResource(id);
      setDeleteConfirmId(null);
      fetchResources();
    } catch (err: any) {
      alert('Failed to delete resource.');
    }
  };

  const filteredResources = resources.filter((r) => {
    const matchesSearch =
      r.title.toLowerCase().includes(search.toLowerCase()) ||
      r.subject.toLowerCase().includes(search.toLowerCase()) ||
      r.description.toLowerCase().includes(search.toLowerCase());

    const matchesStatus =
      statusFilter === 'ALL' ||
      (statusFilter === 'PUBLISHED' && r.published) ||
      (statusFilter === 'DRAFT' && !r.published);

    return matchesSearch && matchesStatus;
  });

  return (
    <div className="space-y-8 max-w-7xl mx-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-800 pb-5">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-100 flex items-center space-x-3">
            <span>📚 Academic Resources Management</span>
          </h1>
          <p className="text-slate-400 text-sm mt-1">
            Create, publish, and target course materials, notes, and study resources for students.
          </p>
        </div>
        <button
          onClick={openCreateModal}
          className="px-4 py-2.5 bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-semibold rounded-xl text-sm transition-all shadow-md shadow-emerald-500/20 flex items-center justify-center space-x-2"
        >
          <span>+ Create Resource</span>
        </button>
      </div>

      {/* Filter & Search Bar */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-4 flex flex-col sm:flex-row gap-4 justify-between items-center">
        <div className="relative w-full sm:w-80">
          <input
            type="text"
            placeholder="Search resources by title or subject..."
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

      {/* Resource Grid */}
      {loading ? (
        <div className="flex items-center justify-center py-16">
          <div className="w-8 h-8 border-4 border-emerald-500/20 border-t-emerald-500 rounded-full animate-spin"></div>
        </div>
      ) : filteredResources.length === 0 ? (
        <div className="bg-slate-900/50 border border-slate-800 rounded-2xl p-12 text-center">
          <div className="text-4xl mb-3">📚</div>
          <h3 className="text-lg font-bold text-slate-300">No resources created yet.</h3>
          <p className="text-slate-500 text-sm mt-1">
            Click "+ Create Resource" to add your first academic resource.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredResources.map((resource) => (
            <div
              key={resource.id}
              className="bg-slate-900 border border-slate-800 rounded-xl p-6 flex flex-col justify-between hover:border-slate-700 transition-all shadow-lg"
            >
              <div>
                <div className="flex items-center justify-between mb-3">
                  <span className="text-xs font-semibold px-2.5 py-1 bg-slate-800 text-slate-300 rounded-md border border-slate-700">
                    {resource.subject}
                  </span>
                  <span
                    className={`text-xs font-bold px-2.5 py-1 rounded-full border ${
                      resource.published
                        ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30'
                        : 'bg-amber-500/10 text-amber-400 border-amber-500/30'
                    }`}
                  >
                    {resource.published ? 'PUBLISHED' : 'DRAFT'}
                  </span>
                </div>

                <h3 className="text-lg font-bold text-slate-100 mb-2 line-clamp-1">
                  {resource.title}
                </h3>
                <p className="text-slate-400 text-sm mb-4 line-clamp-3">
                  {resource.description}
                </p>

                {/* Target Tags */}
                <div className="flex flex-wrap gap-1.5 mb-4">
                  {resource.targetDepartment && (
                    <span className="text-[11px] bg-slate-950 text-slate-400 px-2 py-0.5 rounded border border-slate-800">
                      Dept: {resource.targetDepartment}
                    </span>
                  )}
                  {resource.targetCourse && (
                    <span className="text-[11px] bg-slate-950 text-slate-400 px-2 py-0.5 rounded border border-slate-800">
                      Course: {resource.targetCourse}
                    </span>
                  )}
                  {resource.targetYear && (
                    <span className="text-[11px] bg-slate-950 text-slate-400 px-2 py-0.5 rounded border border-slate-800">
                      Yr {resource.targetYear}
                    </span>
                  )}
                  {resource.targetSemester && (
                    <span className="text-[11px] bg-slate-950 text-slate-400 px-2 py-0.5 rounded border border-slate-800">
                      Sem {resource.targetSemester}
                    </span>
                  )}
                  {!resource.targetDepartment && !resource.targetCourse && !resource.targetYear && !resource.targetSemester && (
                    <span className="text-[11px] bg-emerald-500/10 text-emerald-400 px-2 py-0.5 rounded border border-emerald-500/20">
                      All Students
                    </span>
                  )}
                </div>
              </div>

              <div className="pt-4 border-t border-slate-800 flex items-center justify-between gap-2">
                <button
                  onClick={() => handleTogglePublish(resource)}
                  className={`text-xs font-semibold px-3 py-1.5 rounded-lg border transition-colors ${
                    resource.published
                      ? 'bg-amber-500/10 text-amber-400 border-amber-500/30 hover:bg-amber-500/20'
                      : 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30 hover:bg-emerald-500/20'
                  }`}
                >
                  {resource.published ? 'Unpublish' : 'Publish'}
                </button>

                <div className="flex items-center space-x-2">
                  <button
                    onClick={() => openEditModal(resource)}
                    className="text-xs font-semibold px-3 py-1.5 bg-slate-800 text-slate-300 rounded-lg hover:bg-slate-700 border border-slate-700 transition-colors"
                  >
                    Edit
                  </button>
                  <button
                    onClick={() => setDeleteConfirmId(resource.id)}
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

      {/* Create / Edit Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-2xl overflow-hidden shadow-2xl">
            <div className="px-6 py-4 border-b border-slate-800 flex justify-between items-center">
              <h3 className="text-lg font-bold text-slate-100">
                {editingResource ? 'Edit Academic Resource' : 'Create Academic Resource'}
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
                    Title *
                  </label>
                  <input
                    type="text"
                    required
                    value={formData.title}
                    onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-emerald-500"
                    placeholder="e.g. Data Structures Unit 3 Lecture Notes"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                    Subject *
                  </label>
                  <input
                    type="text"
                    required
                    value={formData.subject}
                    onChange={(e) => setFormData({ ...formData, subject: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-emerald-500"
                    placeholder="e.g. Computer Science / DS"
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
                  placeholder="Detailed breakdown of the material..."
                />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                    Resource Type
                  </label>
                  <select
                    value={formData.resourceType}
                    onChange={(e) => setFormData({ ...formData, resourceType: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-emerald-500"
                  >
                    <option value="NOTES">Notes</option>
                    <option value="PDF">PDF</option>
                    <option value="VIDEO">Video Lecture</option>
                    <option value="WEBSITE">Website / Link</option>
                    <option value="OTHER">Other</option>
                  </select>
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
                    placeholder="General / Exam Prep"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                    Resource URL
                  </label>
                  <input
                    type="url"
                    value={formData.resourceUrl}
                    onChange={(e) => setFormData({ ...formData, resourceUrl: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-emerald-500"
                    placeholder="https://..."
                  />
                </div>
              </div>

              {/* Target Academic Audience Section */}
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
                  id="publishCheckbox"
                  checked={formData.published}
                  onChange={(e) => setFormData({ ...formData, published: e.target.checked })}
                  className="w-4 h-4 text-emerald-500 rounded bg-slate-950 border-slate-800"
                />
                <label htmlFor="publishCheckbox" className="text-xs font-semibold text-slate-300">
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
                  {formSubmitting ? 'Saving...' : editingResource ? 'Update Resource' : 'Create Resource'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Delete Confirmation Modal */}
      {deleteConfirmId !== null && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 max-w-sm w-full text-center">
            <h3 className="text-lg font-bold text-slate-100 mb-2">Delete Resource?</h3>
            <p className="text-slate-400 text-sm mb-6">
              Are you sure you want to delete this resource? This action cannot be undone.
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

export default FacultyResourcesPage;
