import React, { useState, useEffect } from 'react';
import {
  getFacultyOpportunities,
  createFacultyOpportunity,
  updateFacultyOpportunity,
  deleteFacultyOpportunity,
  publishFacultyOpportunity,
  unpublishFacultyOpportunity,
  getOpportunityApplications,
} from '../services/facultyService';
import { FacultyOpportunity, FacultyOpportunityRequest, OpportunityApplicationItem } from '../types/faculty.types';

export const FacultyOpportunitiesPage: React.FC = () => {
  const [opportunities, setOpportunities] = useState<FacultyOpportunity[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'PUBLISHED' | 'DRAFT'>('ALL');

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [editingOpportunity, setEditingOpportunity] = useState<FacultyOpportunity | null>(null);

  // Applications Drawer State
  const [appModalOpp, setAppModalOpp] = useState<FacultyOpportunity | null>(null);
  const [applications, setApplications] = useState<OpportunityApplicationItem[]>([]);
  const [applicationsLoading, setApplicationsLoading] = useState<boolean>(false);

  // Form State
  const [formData, setFormData] = useState<FacultyOpportunityRequest>({
    title: '',
    description: '',
    organization: '',
    opportunityType: 'INTERNSHIP',
    location: '',
    skills: '',
    deadline: '',
    applicationUrl: '',
    eligibility: '',
    targetDepartment: '',
    targetCourse: '',
    targetYear: undefined,
    targetSemester: undefined,
    published: false,
  });

  const [formSubmitting, setFormSubmitting] = useState<boolean>(false);
  const [deleteConfirmId, setDeleteConfirmId] = useState<number | null>(null);

  const fetchOpportunities = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await getFacultyOpportunities();
      setOpportunities(data);
    } catch (err: any) {
      console.error('Failed to load opportunities:', err);
      setError('Failed to fetch faculty opportunities.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOpportunities();
  }, []);

  const openCreateModal = () => {
    setEditingOpportunity(null);
    setFormData({
      title: '',
      description: '',
      organization: '',
      opportunityType: 'INTERNSHIP',
      location: '',
      skills: '',
      deadline: '',
      applicationUrl: '',
      eligibility: '',
      targetDepartment: '',
      targetCourse: '',
      targetYear: undefined,
      targetSemester: undefined,
      published: false,
    });
    setIsModalOpen(true);
  };

  const openEditModal = (opp: FacultyOpportunity) => {
    setEditingOpportunity(opp);
    setFormData({
      title: opp.title,
      description: opp.description,
      organization: opp.organization,
      opportunityType: opp.opportunityType || 'INTERNSHIP',
      location: opp.location || '',
      skills: opp.skills || '',
      deadline: opp.deadline || '',
      applicationUrl: opp.applicationUrl || '',
      eligibility: opp.eligibility || '',
      targetDepartment: opp.targetDepartment || '',
      targetCourse: opp.targetCourse || '',
      targetYear: opp.targetYear || undefined,
      targetSemester: opp.targetSemester || undefined,
      published: opp.published,
    });
    setIsModalOpen(true);
  };

  const openApplicationsModal = async (opp: FacultyOpportunity) => {
    setAppModalOpp(opp);
    try {
      setApplicationsLoading(true);
      const list = await getOpportunityApplications(opp.id);
      setApplications(list);
    } catch (err: any) {
      alert('Failed to fetch applications for this opportunity.');
    } finally {
      setApplicationsLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setFormSubmitting(true);
      if (editingOpportunity) {
        await updateFacultyOpportunity(editingOpportunity.id, formData);
      } else {
        await createFacultyOpportunity(formData);
      }
      setIsModalOpen(false);
      fetchOpportunities();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to save opportunity.');
    } finally {
      setFormSubmitting(false);
    }
  };

  const handleTogglePublish = async (opp: FacultyOpportunity) => {
    try {
      if (opp.published) {
        await unpublishFacultyOpportunity(opp.id);
      } else {
        await publishFacultyOpportunity(opp.id);
      }
      fetchOpportunities();
    } catch (err: any) {
      alert('Failed to update publication status.');
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteFacultyOpportunity(id);
      setDeleteConfirmId(null);
      fetchOpportunities();
    } catch (err: any) {
      alert('Failed to delete opportunity.');
    }
  };

  const filteredOpportunities = opportunities.filter((o) => {
    const matchesSearch =
      o.title.toLowerCase().includes(search.toLowerCase()) ||
      o.organization.toLowerCase().includes(search.toLowerCase()) ||
      o.description.toLowerCase().includes(search.toLowerCase());

    const matchesStatus =
      statusFilter === 'ALL' ||
      (statusFilter === 'PUBLISHED' && o.published) ||
      (statusFilter === 'DRAFT' && !o.published);

    return matchesSearch && matchesStatus;
  });

  return (
    <div className="space-y-8 max-w-7xl mx-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-800 pb-5">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-100 flex items-center space-x-3">
            <span>💼 Student Opportunities Management</span>
          </h1>
          <p className="text-slate-400 text-sm mt-1">
            Post and manage internships, job openings, scholarships, competitions, and workshops for students.
          </p>
        </div>
        <button
          onClick={openCreateModal}
          className="px-4 py-2.5 bg-amber-500 hover:bg-amber-400 text-slate-950 font-semibold rounded-xl text-sm transition-all shadow-md shadow-amber-500/20 flex items-center justify-center space-x-2"
        >
          <span>+ Create Opportunity</span>
        </button>
      </div>

      {/* Filter Bar */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-4 flex flex-col sm:flex-row gap-4 justify-between items-center">
        <div className="relative w-full sm:w-80">
          <input
            type="text"
            placeholder="Search opportunities by title or company..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3.5 py-2 text-sm text-slate-200 focus:outline-none focus:border-amber-500"
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
                  ? 'bg-amber-500/20 text-amber-400 border border-amber-500/30'
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
          <div className="w-8 h-8 border-4 border-amber-500/20 border-t-amber-500 rounded-full animate-spin"></div>
        </div>
      ) : filteredOpportunities.length === 0 ? (
        <div className="bg-slate-900/50 border border-slate-800 rounded-2xl p-12 text-center">
          <div className="text-4xl mb-3">💼</div>
          <h3 className="text-lg font-bold text-slate-300">No opportunities created yet.</h3>
          <p className="text-slate-500 text-sm mt-1">
            Click "+ Create Opportunity" to publish career/internship opportunities for students.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredOpportunities.map((opp) => (
            <div
              key={opp.id}
              className="bg-slate-900 border border-slate-800 rounded-xl p-6 flex flex-col justify-between hover:border-slate-700 transition-all shadow-lg"
            >
              <div>
                <div className="flex items-center justify-between mb-3">
                  <span className="text-xs font-semibold px-2.5 py-1 bg-amber-500/10 text-amber-400 rounded-md border border-amber-500/20 uppercase">
                    {opp.opportunityType || 'INTERNSHIP'}
                  </span>
                  <span
                    className={`text-xs font-bold px-2.5 py-1 rounded-full border ${
                      opp.published
                        ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30'
                        : 'bg-amber-500/10 text-amber-400 border-amber-500/30'
                    }`}
                  >
                    {opp.published ? 'PUBLISHED' : 'DRAFT'}
                  </span>
                </div>

                <h3 className="text-lg font-bold text-slate-100 mb-1">{opp.title}</h3>
                <p className="text-amber-400 text-xs font-semibold mb-2">🏢 {opp.organization}</p>
                <p className="text-slate-400 text-xs mb-3 flex items-center space-x-2">
                  <span>📍 {opp.location || 'Remote'}</span>
                  {opp.deadline && <span>• ⏳ Deadline: {opp.deadline}</span>}
                </p>

                <p className="text-slate-400 text-sm mb-4 line-clamp-3">{opp.description}</p>

                <div className="bg-slate-950/60 border border-slate-800/80 rounded-lg p-3 mb-4 flex items-center justify-between">
                  <div>
                    <div className="text-[11px] text-slate-400 font-semibold uppercase">Applications</div>
                    <div className="text-xs font-semibold text-amber-400">Tracked Applications</div>
                  </div>
                  <button
                    onClick={() => openApplicationsModal(opp)}
                    className="text-xs font-semibold px-3 py-1.5 bg-amber-500/10 text-amber-400 hover:bg-amber-500/20 border border-amber-500/30 rounded-lg transition-colors"
                  >
                    View Applications
                  </button>
                </div>
              </div>

              <div className="pt-4 border-t border-slate-800 flex items-center justify-between gap-2">
                <button
                  onClick={() => handleTogglePublish(opp)}
                  className={`text-xs font-semibold px-3 py-1.5 rounded-lg border transition-colors ${
                    opp.published
                      ? 'bg-amber-500/10 text-amber-400 border-amber-500/30 hover:bg-amber-500/20'
                      : 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30 hover:bg-emerald-500/20'
                  }`}
                >
                  {opp.published ? 'Unpublish' : 'Publish'}
                </button>

                <div className="flex items-center space-x-2">
                  <button
                    onClick={() => openEditModal(opp)}
                    className="text-xs font-semibold px-3 py-1.5 bg-slate-800 text-slate-300 rounded-lg hover:bg-slate-700 border border-slate-700 transition-colors"
                  >
                    Edit
                  </button>
                  <button
                    onClick={() => setDeleteConfirmId(opp.id)}
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

      {/* Applications Modal */}
      {appModalOpp && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-xl overflow-hidden shadow-2xl">
            <div className="px-6 py-4 border-b border-slate-800 flex justify-between items-center">
              <div>
                <h3 className="text-lg font-bold text-slate-100">Opportunity Applications</h3>
                <p className="text-xs text-slate-400">{appModalOpp.title} • {appModalOpp.organization}</p>
              </div>
              <button
                onClick={() => setAppModalOpp(null)}
                className="text-slate-400 hover:text-slate-200 text-xl font-bold"
              >
                &times;
              </button>
            </div>

            <div className="p-6 max-h-[60vh] overflow-y-auto">
              {applicationsLoading ? (
                <div className="flex justify-center py-8">
                  <div className="w-6 h-6 border-2 border-amber-500/20 border-t-amber-500 rounded-full animate-spin"></div>
                </div>
              ) : applications.length === 0 ? (
                <p className="text-center text-slate-400 text-sm py-6">
                  No applications yet.
                </p>
              ) : (
                <div className="divide-y divide-slate-800">
                  {applications.map((app) => (
                    <div key={app.applicationId} className="py-3 flex justify-between items-center text-sm">
                      <div>
                        <div className="font-semibold text-slate-200">
                          {app.firstName} {app.lastName}
                        </div>
                        <div className="text-xs text-slate-400">{app.email}</div>
                      </div>
                      <div className="text-right text-xs text-slate-400">
                        <div className="font-semibold text-amber-400 uppercase text-[11px] mb-0.5">
                          {app.applicationStatus || 'APPLIED'}
                        </div>
                        <div>{app.department ? `${app.department} (${app.course})` : 'N/A'}</div>
                        <div className="text-[10px] text-slate-500">
                          Applied {new Date(app.appliedAt).toLocaleDateString()}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div className="px-6 py-3 border-t border-slate-800 text-right">
              <button
                onClick={() => setAppModalOpp(null)}
                className="px-4 py-2 bg-slate-800 text-slate-300 rounded-lg text-sm"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Create / Edit Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-2xl overflow-hidden shadow-2xl">
            <div className="px-6 py-4 border-b border-slate-800 flex justify-between items-center">
              <h3 className="text-lg font-bold text-slate-100">
                {editingOpportunity ? 'Edit Opportunity' : 'Create Opportunity'}
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
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-amber-500"
                    placeholder="e.g. Software Engineering Summer Internship"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                    Organization / Company *
                  </label>
                  <input
                    type="text"
                    required
                    value={formData.organization}
                    onChange={(e) => setFormData({ ...formData, organization: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-amber-500"
                    placeholder="e.g. Google / Microsoft / Campus R&D"
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
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-amber-500"
                  placeholder="Detailed role description and responsibilities..."
                />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                    Opportunity Type
                  </label>
                  <select
                    value={formData.opportunityType}
                    onChange={(e) => setFormData({ ...formData, opportunityType: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-amber-500"
                  >
                    <option value="INTERNSHIP">Internship</option>
                    <option value="JOB">Job Listing</option>
                    <option value="SCHOLARSHIP">Scholarship</option>
                    <option value="COMPETITION">Competition</option>
                    <option value="WORKSHOP">Workshop</option>
                    <option value="OTHER">Other</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                    Location
                  </label>
                  <input
                    type="text"
                    value={formData.location}
                    onChange={(e) => setFormData({ ...formData, location: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-amber-500"
                    placeholder="Remote / On-site / City"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                    Deadline
                  </label>
                  <input
                    type="date"
                    value={formData.deadline}
                    onChange={(e) => setFormData({ ...formData, deadline: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-amber-500"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                    Application URL
                  </label>
                  <input
                    type="url"
                    value={formData.applicationUrl}
                    onChange={(e) => setFormData({ ...formData, applicationUrl: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-amber-500"
                    placeholder="https://..."
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                    Required Skills
                  </label>
                  <input
                    type="text"
                    value={formData.skills}
                    onChange={(e) => setFormData({ ...formData, skills: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-amber-500"
                    placeholder="Java, React, SQL"
                  />
                </div>
              </div>

              {/* Targeting */}
              <div className="border-t border-slate-800 pt-4">
                <h4 className="text-xs font-bold text-amber-400 uppercase tracking-wider mb-3">
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
                  id="oppPublishCheckbox"
                  checked={formData.published}
                  onChange={(e) => setFormData({ ...formData, published: e.target.checked })}
                  className="w-4 h-4 text-amber-500 rounded bg-slate-950 border-slate-800"
                />
                <label htmlFor="oppPublishCheckbox" className="text-xs font-semibold text-slate-300">
                  Publish immediately
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
                  className="px-4 py-2 bg-amber-500 text-slate-950 font-semibold rounded-lg text-sm hover:bg-amber-400"
                >
                  {formSubmitting ? 'Saving...' : editingOpportunity ? 'Update Opportunity' : 'Create Opportunity'}
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
            <h3 className="text-lg font-bold text-slate-100 mb-2">Delete Opportunity?</h3>
            <p className="text-slate-400 text-sm mb-6">
              Are you sure you want to delete this opportunity?
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

export default FacultyOpportunitiesPage;
