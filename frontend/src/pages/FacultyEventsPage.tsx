import React, { useState, useEffect } from 'react';
import {
  getFacultyEvents,
  createFacultyEvent,
  updateFacultyEvent,
  deleteFacultyEvent,
  publishFacultyEvent,
  unpublishFacultyEvent,
  getEventRegistrations,
} from '../services/facultyService';
import { FacultyEvent, FacultyEventRequest, EventRegistrationItem } from '../types/faculty.types';

export const FacultyEventsPage: React.FC = () => {
  const [events, setEvents] = useState<FacultyEvent[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'PUBLISHED' | 'DRAFT'>('ALL');

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [editingEvent, setEditingEvent] = useState<FacultyEvent | null>(null);

  // Registrations Drawer State
  const [registrationModalEvent, setRegistrationModalEvent] = useState<FacultyEvent | null>(null);
  const [registrations, setRegistrations] = useState<EventRegistrationItem[]>([]);
  const [registrationsLoading, setRegistrationsLoading] = useState<boolean>(false);

  // Form State
  const [formData, setFormData] = useState<FacultyEventRequest>({
    title: '',
    description: '',
    eventDate: '',
    eventTime: '',
    venue: '',
    category: 'Academic',
    organizer: '',
    registrationRequired: false,
    targetDepartment: '',
    targetCourse: '',
    targetYear: undefined,
    targetSemester: undefined,
    published: false,
  });

  const [formSubmitting, setFormSubmitting] = useState<boolean>(false);
  const [deleteConfirmId, setDeleteConfirmId] = useState<number | null>(null);

  const fetchEvents = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await getFacultyEvents();
      setEvents(data);
    } catch (err: any) {
      console.error('Failed to load events:', err);
      setError('Failed to fetch faculty events.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchEvents();
  }, []);

  const openCreateModal = () => {
    setEditingEvent(null);
    setFormData({
      title: '',
      description: '',
      eventDate: '',
      eventTime: '',
      venue: '',
      category: 'Academic',
      organizer: '',
      registrationRequired: false,
      targetDepartment: '',
      targetCourse: '',
      targetYear: undefined,
      targetSemester: undefined,
      published: false,
    });
    setIsModalOpen(true);
  };

  const openEditModal = (event: FacultyEvent) => {
    setEditingEvent(event);
    setFormData({
      title: event.title,
      description: event.description,
      eventDate: event.eventDate,
      eventTime: event.eventTime || '',
      venue: event.venue,
      category: event.category || 'Academic',
      organizer: event.organizer || '',
      registrationRequired: event.registrationRequired,
      targetDepartment: event.targetDepartment || '',
      targetCourse: event.targetCourse || '',
      targetYear: event.targetYear || undefined,
      targetSemester: event.targetSemester || undefined,
      published: event.published,
    });
    setIsModalOpen(true);
  };

  const openRegistrationsModal = async (event: FacultyEvent) => {
    setRegistrationModalEvent(event);
    try {
      setRegistrationsLoading(true);
      const list = await getEventRegistrations(event.id);
      setRegistrations(list);
    } catch (err: any) {
      alert('Failed to fetch registrations.');
    } finally {
      setRegistrationsLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setFormSubmitting(true);
      if (editingEvent) {
        await updateFacultyEvent(editingEvent.id, formData);
      } else {
        await createFacultyEvent(formData);
      }
      setIsModalOpen(false);
      fetchEvents();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to save event.');
    } finally {
      setFormSubmitting(false);
    }
  };

  const handleTogglePublish = async (event: FacultyEvent) => {
    try {
      if (event.published) {
        await unpublishFacultyEvent(event.id);
      } else {
        await publishFacultyEvent(event.id);
      }
      fetchEvents();
    } catch (err: any) {
      alert('Failed to update publication status.');
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteFacultyEvent(id);
      setDeleteConfirmId(null);
      fetchEvents();
    } catch (err: any) {
      alert('Failed to delete event.');
    }
  };

  const filteredEvents = events.filter((e) => {
    const matchesSearch =
      e.title.toLowerCase().includes(search.toLowerCase()) ||
      e.venue.toLowerCase().includes(search.toLowerCase()) ||
      e.description.toLowerCase().includes(search.toLowerCase());

    const matchesStatus =
      statusFilter === 'ALL' ||
      (statusFilter === 'PUBLISHED' && e.published) ||
      (statusFilter === 'DRAFT' && !e.published);

    return matchesSearch && matchesStatus;
  });

  return (
    <div className="space-y-8 max-w-7xl mx-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-800 pb-5">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-100 flex items-center space-x-3">
            <span>📅 Campus Events Management</span>
          </h1>
          <p className="text-slate-400 text-sm mt-1">
            Create, schedule, and manage campus events and review student registrations.
          </p>
        </div>
        <button
          onClick={openCreateModal}
          className="px-4 py-2.5 bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-semibold rounded-xl text-sm transition-all shadow-md shadow-emerald-500/20 flex items-center justify-center space-x-2"
        >
          <span>+ Create Event</span>
        </button>
      </div>

      {/* Filter Bar */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-4 flex flex-col sm:flex-row gap-4 justify-between items-center">
        <div className="relative w-full sm:w-80">
          <input
            type="text"
            placeholder="Search events by title or venue..."
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
      ) : filteredEvents.length === 0 ? (
        <div className="bg-slate-900/50 border border-slate-800 rounded-2xl p-12 text-center">
          <div className="text-4xl mb-3">📅</div>
          <h3 className="text-lg font-bold text-slate-300">No events created yet.</h3>
          <p className="text-slate-500 text-sm mt-1">
            Click "+ Create Event" to schedule your first event.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredEvents.map((event) => (
            <div
              key={event.id}
              className="bg-slate-900 border border-slate-800 rounded-xl p-6 flex flex-col justify-between hover:border-slate-700 transition-all shadow-lg"
            >
              <div>
                <div className="flex items-center justify-between mb-3">
                  <span className="text-xs font-semibold px-2.5 py-1 bg-slate-800 text-slate-300 rounded-md border border-slate-700">
                    📅 {event.eventDate}
                  </span>
                  <span
                    className={`text-xs font-bold px-2.5 py-1 rounded-full border ${
                      event.published
                        ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30'
                        : 'bg-amber-500/10 text-amber-400 border-amber-500/30'
                    }`}
                  >
                    {event.published ? 'PUBLISHED' : 'DRAFT'}
                  </span>
                </div>

                <h3 className="text-lg font-bold text-slate-100 mb-1">{event.title}</h3>
                <p className="text-slate-400 text-xs mb-3 flex items-center space-x-2">
                  <span>📍 {event.venue}</span>
                  {event.eventTime && <span>• ⏰ {event.eventTime}</span>}
                </p>

                <p className="text-slate-400 text-sm mb-4 line-clamp-2">{event.description}</p>

                {/* Registration Count */}
                <div className="bg-slate-950/60 border border-slate-800/80 rounded-lg p-3 mb-4 flex items-center justify-between">
                  <div>
                    <div className="text-[11px] text-slate-400 font-semibold uppercase">Registrations</div>
                    <div className="text-base font-bold text-emerald-400">{event.registrationCount} Registered</div>
                  </div>
                  <button
                    onClick={() => openRegistrationsModal(event)}
                    className="text-xs font-semibold px-3 py-1.5 bg-emerald-500/10 text-emerald-400 hover:bg-emerald-500/20 border border-emerald-500/30 rounded-lg transition-colors"
                  >
                    View List
                  </button>
                </div>
              </div>

              <div className="pt-4 border-t border-slate-800 flex items-center justify-between gap-2">
                <button
                  onClick={() => handleTogglePublish(event)}
                  className={`text-xs font-semibold px-3 py-1.5 rounded-lg border transition-colors ${
                    event.published
                      ? 'bg-amber-500/10 text-amber-400 border-amber-500/30 hover:bg-amber-500/20'
                      : 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30 hover:bg-emerald-500/20'
                  }`}
                >
                  {event.published ? 'Unpublish' : 'Publish'}
                </button>

                <div className="flex items-center space-x-2">
                  <button
                    onClick={() => openEditModal(event)}
                    className="text-xs font-semibold px-3 py-1.5 bg-slate-800 text-slate-300 rounded-lg hover:bg-slate-700 border border-slate-700 transition-colors"
                  >
                    Edit
                  </button>
                  <button
                    onClick={() => setDeleteConfirmId(event.id)}
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

      {/* Event Create / Edit Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-2xl overflow-hidden shadow-2xl">
            <div className="px-6 py-4 border-b border-slate-800 flex justify-between items-center">
              <h3 className="text-lg font-bold text-slate-100">
                {editingEvent ? 'Edit Event' : 'Create Event'}
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
                  Event Title *
                </label>
                <input
                  type="text"
                  required
                  value={formData.title}
                  onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-emerald-500"
                  placeholder="e.g. Annual Tech Symposium 2026"
                />
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
                  placeholder="Detailed event agenda..."
                />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                    Event Date *
                  </label>
                  <input
                    type="date"
                    required
                    value={formData.eventDate}
                    onChange={(e) => setFormData({ ...formData, eventDate: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-emerald-500"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                    Time
                  </label>
                  <input
                    type="time"
                    value={formData.eventTime}
                    onChange={(e) => setFormData({ ...formData, eventTime: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-emerald-500"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                    Venue *
                  </label>
                  <input
                    type="text"
                    required
                    value={formData.venue}
                    onChange={(e) => setFormData({ ...formData, venue: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-emerald-500"
                    placeholder="Auditorium / Online"
                  />
                </div>
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
                  id="eventPublishCheckbox"
                  checked={formData.published}
                  onChange={(e) => setFormData({ ...formData, published: e.target.checked })}
                  className="w-4 h-4 text-emerald-500 rounded bg-slate-950 border-slate-800"
                />
                <label htmlFor="eventPublishCheckbox" className="text-xs font-semibold text-slate-300">
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
                  className="px-4 py-2 bg-emerald-500 text-slate-950 font-semibold rounded-lg text-sm hover:bg-emerald-400"
                >
                  {formSubmitting ? 'Saving...' : editingEvent ? 'Update Event' : 'Create Event'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Registrations List Modal */}
      {registrationModalEvent && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-xl overflow-hidden shadow-2xl">
            <div className="px-6 py-4 border-b border-slate-800 flex justify-between items-center">
              <div>
                <h3 className="text-lg font-bold text-slate-100">Registered Students</h3>
                <p className="text-xs text-slate-400">{registrationModalEvent.title}</p>
              </div>
              <button
                onClick={() => setRegistrationModalEvent(null)}
                className="text-slate-400 hover:text-slate-200 text-xl font-bold"
              >
                &times;
              </button>
            </div>

            <div className="p-6 max-h-[60vh] overflow-y-auto">
              {registrationsLoading ? (
                <div className="flex justify-center py-8">
                  <div className="w-6 h-6 border-2 border-emerald-500/20 border-t-emerald-500 rounded-full animate-spin"></div>
                </div>
              ) : registrations.length === 0 ? (
                <p className="text-center text-slate-400 text-sm py-6">
                  No students have registered for this event yet.
                </p>
              ) : (
                <div className="divide-y divide-slate-800">
                  {registrations.map((reg) => (
                    <div key={reg.registrationId} className="py-3 flex justify-between items-center text-sm">
                      <div>
                        <div className="font-semibold text-slate-200">
                          {reg.firstName} {reg.lastName}
                        </div>
                        <div className="text-xs text-slate-400">{reg.email}</div>
                      </div>
                      <div className="text-right text-xs text-slate-400">
                        <div>{reg.department ? `${reg.department} (Yr ${reg.year})` : 'N/A'}</div>
                        <div className="text-[10px] text-slate-500">
                          {new Date(reg.registeredAt).toLocaleDateString()}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div className="px-6 py-3 border-t border-slate-800 text-right">
              <button
                onClick={() => setRegistrationModalEvent(null)}
                className="px-4 py-2 bg-slate-800 text-slate-300 rounded-lg text-sm"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Delete Confirmation Modal */}
      {deleteConfirmId !== null && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 max-w-sm w-full text-center">
            <h3 className="text-lg font-bold text-slate-100 mb-2">Delete Event?</h3>
            <p className="text-slate-400 text-sm mb-6">
              Are you sure you want to delete this event?
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

export default FacultyEventsPage;
