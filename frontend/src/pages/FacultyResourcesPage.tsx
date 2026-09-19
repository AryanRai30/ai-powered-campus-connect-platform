import React, { useState, useEffect } from 'react';
import {
  getFacultyResources,
  createFacultyResource,
  updateFacultyResource,
  deleteFacultyResource,
  publishFacultyResource,
  unpublishFacultyResource,
} from '../services/facultyService';
import { viewResourceFile, downloadResourceFile } from '../services/campusService';
import { FacultyResource, FacultyResourceRequest } from '../types/faculty.types';

const formatFileSize = (bytes?: number): string => {
  if (!bytes || bytes === 0) return '';
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
};

export const FacultyResourcesPage: React.FC = () => {
  const [resources, setResources] = useState<FacultyResource[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'PUBLISHED' | 'DRAFT'>('ALL');

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [editingResource, setEditingResource] = useState<FacultyResource | null>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);

  // Form State
  const [formData, setFormData] = useState<FacultyResourceRequest>({
    title: '',
    description: '',
    subject: '',
    category: 'General',
    resourceType: 'PDF',
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
    setSelectedFile(null);
    setFormData({
      title: '',
      description: '',
      subject: '',
      category: 'General',
      resourceType: 'PDF',
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
    setSelectedFile(null);
    setFormData({
      title: resource.title,
      description: resource.description,
      subject: resource.subject,
      category: resource.category || 'General',
      resourceType: resource.resourceType || 'PDF',
      resourceUrl: resource.resourceUrl || '',
      targetDepartment: resource.targetDepartment || '',
      targetCourse: resource.targetCourse || '',
      targetYear: resource.targetYear || undefined,
      targetSemester: resource.targetSemester || undefined,
      published: resource.published,
    });
    setIsModalOpen(true);
  };

  const ALLOWED_EXTENSIONS = ['pdf', 'ppt', 'pptx', 'doc', 'docx', 'txt', 'jpg', 'jpeg', 'png', 'webp', 'mp4', 'webm', 'mov', 'zip'];
  const DISALLOWED_EXTENSIONS = ['exe', 'bat', 'cmd', 'sh', 'ps1', 'jar', 'php', 'jsp', 'asp', 'aspx', 'py', 'js', 'vbs', 'wsf', 'com', 'scr', 'cpl', 'msi', 'htm', 'html'];
  const MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0];
      const ext = file.name.split('.').pop()?.toLowerCase() || '';

      if (DISALLOWED_EXTENSIONS.includes(ext)) {
        alert(`Executable or dangerous file types (.${ext}) are strictly prohibited.`);
        e.target.value = '';
        setSelectedFile(null);
        return;
      }

      if (!ALLOWED_EXTENSIONS.includes(ext)) {
        alert(`Unsupported file type: .${ext}. Allowed formats: PDF, PPT/PPTX, DOC/DOCX, TXT, Images (JPG, PNG, WEBP), Videos (MP4, WEBM, MOV), ZIP.`);
        e.target.value = '';
        setSelectedFile(null);
        return;
      }

      if (file.size > MAX_FILE_SIZE) {
        alert(`File size (${(file.size / (1024 * 1024)).toFixed(1)}MB) exceeds the maximum limit of 100MB.`);
        e.target.value = '';
        setSelectedFile(null);
        return;
      }

      setSelectedFile(file);

      // Auto-detect resource type from file extension
      if (['pdf'].includes(ext)) setFormData((prev) => ({ ...prev, resourceType: 'PDF' }));
      else if (['ppt', 'pptx'].includes(ext)) setFormData((prev) => ({ ...prev, resourceType: 'PPT' }));
      else if (['doc', 'docx', 'txt'].includes(ext)) setFormData((prev) => ({ ...prev, resourceType: 'DOC' }));
      else if (['mp4', 'webm', 'mov'].includes(ext)) setFormData((prev) => ({ ...prev, resourceType: 'VIDEO' }));
      else if (['jpg', 'jpeg', 'png', 'webp'].includes(ext)) setFormData((prev) => ({ ...prev, resourceType: 'IMAGE' }));
      else if (['zip'].includes(ext)) setFormData((prev) => ({ ...prev, resourceType: 'ZIP' }));
    }
  };

  const removeSelectedFile = () => {
    setSelectedFile(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setFormSubmitting(true);

      if (!editingResource && !selectedFile && !formData.resourceUrl?.trim()) {
        alert('Please select a file to upload for the academic resource.');
        setFormSubmitting(false);
        return;
      }

      if (editingResource) {
        await updateFacultyResource(editingResource.id, formData, selectedFile || undefined);
      } else {
        await createFacultyResource(formData, selectedFile || undefined);
      }

      setIsModalOpen(false);
      setSelectedFile(null);
      fetchResources();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to save academic resource file.');
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
      r.description.toLowerCase().includes(search.toLowerCase()) ||
      (r.originalFileName && r.originalFileName.toLowerCase().includes(search.toLowerCase()));

    const matchesStatus =
      statusFilter === 'ALL' ||
      (statusFilter === 'PUBLISHED' && r.published) ||
      (statusFilter === 'DRAFT' && !r.published);

    return matchesSearch && matchesStatus;
  });

  const getFormatBadge = (type?: string) => {
    switch (type?.toUpperCase()) {
      case 'PDF': return 'bg-red-500/10 text-red-400 border-red-500/30';
      case 'PPT': return 'bg-orange-500/10 text-orange-400 border-orange-500/30';
      case 'DOC': return 'bg-blue-500/10 text-blue-400 border-blue-500/30';
      case 'VIDEO': return 'bg-amber-500/10 text-amber-400 border-amber-500/30';
      case 'IMAGE': return 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30';
      case 'ZIP': return 'bg-purple-500/10 text-purple-400 border-purple-500/30';
      default: return 'bg-teal-500/10 text-teal-400 border-teal-500/30';
    }
  };

  return (
    <div className="space-y-8 max-w-7xl mx-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-800 pb-5">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-100 flex items-center space-x-3">
            <span>📚 Academic Resources & File Upload Portal</span>
          </h1>
          <p className="text-slate-400 text-sm mt-1">
            Upload PDF notes, slides, video lectures, and study documents for enrolled students.
          </p>
        </div>
        <button
          onClick={openCreateModal}
          className="px-4 py-2.5 bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-semibold rounded-xl text-sm transition-all shadow-md shadow-emerald-500/20 flex items-center justify-center space-x-2"
        >
          <span>📤 Upload Resource File</span>
        </button>
      </div>

      {/* Filter & Search Bar */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-4 flex flex-col sm:flex-row gap-4 justify-between items-center">
        <div className="relative w-full sm:w-80">
          <input
            type="text"
            placeholder="Search by title, subject, or filename..."
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
          <div className="text-4xl mb-3">📁</div>
          <h3 className="text-lg font-bold text-slate-300">No resources uploaded yet.</h3>
          <p className="text-slate-500 text-sm mt-1">
            Click "Upload Resource File" to upload course materials for your students.
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
                  <span className={`text-xs font-bold px-2.5 py-1 rounded-md border ${getFormatBadge(resource.resourceType)}`}>
                    {resource.resourceType || 'FILE'}
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

                <h3 className="text-lg font-bold text-slate-100 mb-1 line-clamp-1">
                  {resource.title}
                </h3>

                <p className="text-xs text-slate-400 font-semibold mb-2">
                  Subject: <span className="text-slate-200">{resource.subject}</span>
                </p>

                <p className="text-slate-400 text-sm mb-4 line-clamp-2">
                  {resource.description}
                </p>

                {/* File Attachment Details Box */}
                {resource.hasFile ? (
                  <div className="p-3 bg-slate-950 border border-slate-800 rounded-xl mb-4 space-y-1.5">
                    <div className="flex items-center justify-between text-xs">
                      <span className="text-slate-400 font-medium truncate max-w-[200px]" title={resource.originalFileName}>
                        📄 {resource.originalFileName}
                      </span>
                      <span className="text-emerald-400 font-mono text-[11px] font-bold">
                        {formatFileSize(resource.fileSize)}
                      </span>
                    </div>
                    <div className="flex items-center space-x-2 pt-1 border-t border-slate-900">
                      <button
                        onClick={() => viewResourceFile(resource.id)}
                        className="text-[11px] font-semibold text-emerald-400 hover:underline"
                      >
                        👁️ View File
                      </button>
                      <span className="text-slate-700">•</span>
                      <button
                        onClick={() => downloadResourceFile(resource.id, resource.originalFileName)}
                        className="text-[11px] font-semibold text-cyan-400 hover:underline"
                      >
                        ⬇️ Download
                      </button>
                    </div>
                  </div>
                ) : resource.resourceUrl ? (
                  <div className="p-3 bg-slate-950 border border-slate-800 rounded-xl mb-4 text-xs">
                    <span className="text-slate-400 block truncate">🔗 {resource.resourceUrl}</span>
                  </div>
                ) : (
                  <div className="p-3 bg-slate-950 border border-slate-800 rounded-xl mb-4 text-xs text-slate-500 italic flex items-center space-x-2">
                    <span>⚠️</span>
                    <span>No file attached</span>
                  </div>
                )}

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
                {editingResource ? 'Edit Academic Resource' : 'Upload Academic Resource File'}
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
                    placeholder="e.g. Data Structures Unit 3 Notes"
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
                  placeholder="Detailed description of the material..."
                />
              </div>

              {/* FILE UPLOAD SECTION */}
              <div className="p-4 bg-slate-950 border border-slate-800 rounded-xl space-y-3">
                <label className="block text-xs font-bold text-emerald-400 uppercase tracking-wider">
                  Select File to Upload *
                </label>
                <input
                  type="file"
                  onChange={handleFileChange}
                  accept=".pdf,.ppt,.pptx,.doc,.docx,.txt,.jpg,.jpeg,.png,.webp,.mp4,.webm,.mov,.zip"
                  className="block w-full text-xs text-slate-400 file:mr-4 file:py-2 file:px-4 file:rounded-xl file:border-0 file:text-xs file:font-semibold file:bg-emerald-500 file:text-slate-950 hover:file:bg-emerald-400 cursor-pointer"
                />

                {selectedFile && (
                  <div className="p-3 bg-slate-900 border border-emerald-500/30 rounded-lg flex items-center justify-between text-xs">
                    <div>
                      <span className="font-semibold text-slate-200 block">Selected: {selectedFile.name}</span>
                      <span className="text-slate-400 text-[11px]">Type: {selectedFile.type || 'Standard Document'}</span>
                    </div>
                    <div className="flex items-center space-x-3">
                      <span className="text-emerald-400 font-mono font-bold">{formatFileSize(selectedFile.size)}</span>
                      <button
                        type="button"
                        onClick={removeSelectedFile}
                        className="text-red-400 hover:text-red-300 font-bold px-2 py-1 bg-red-500/10 hover:bg-red-500/20 rounded border border-red-500/20 text-[11px]"
                      >
                        ✕ Remove
                      </button>
                    </div>
                  </div>
                )}

                {editingResource && editingResource.hasFile && !selectedFile && (
                  <div className="text-xs text-slate-400 italic">
                    Current file: <span className="text-slate-200 font-semibold">{editingResource.originalFileName}</span> ({formatFileSize(editingResource.fileSize)}). Select a new file above to replace it.
                  </div>
                )}

                <p className="text-[11px] text-slate-500">
                  Supported Formats: PDF, PPT/PPTX, DOC/DOCX, TXT, Images (JPG, PNG, WEBP), Videos (MP4, WEBM, MOV), ZIP. Executables (.exe, .bat, etc.) are blocked. Max file size: 100MB.
                </p>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                    Resource Category
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
                    Resource Format / Type
                  </label>
                  <select
                    value={formData.resourceType}
                    onChange={(e) => setFormData({ ...formData, resourceType: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-emerald-500"
                  >
                    <option value="PDF">PDF</option>
                    <option value="PPT">PowerPoint (PPT/PPTX)</option>
                    <option value="DOC">Word Document (DOC/DOCX)</option>
                    <option value="VIDEO">Video Lecture (MP4/WEBM)</option>
                    <option value="IMAGE">Image (JPG/PNG/WEBP)</option>
                    <option value="ZIP">ZIP Archive</option>
                    <option value="NOTES">Text / Notes</option>
                    <option value="OTHER">Other</option>
                  </select>
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
                  {formSubmitting ? 'Uploading...' : editingResource ? 'Update Resource' : 'Publish Resource'}
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
              Are you sure you want to delete this resource and its associated file? This action cannot be undone.
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
