import React, { useEffect, useState } from 'react';
import {
  getStudentList,
  getStudentById,
  updateStudentStatus,
} from '../../services/adminService';
import {
  AdminStudentResponse,
  AdminStudentDetailResponse,
} from '../../types/admin.types';

export const AdminStudentsPage: React.FC = () => {
  const [students, setStudents] = useState<AdminStudentResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  // Search & Filter states
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<string>('all');
  const [deptFilter, setDeptFilter] = useState<string>('all');
  const [courseFilter, setCourseFilter] = useState<string>('all');
  const [yearFilter, setYearFilter] = useState<string>('all');
  const [semFilter, setSemFilter] = useState<string>('all');

  // Modals state
  const [isDetailOpen, setIsDetailOpen] = useState<boolean>(false);
  const [selectedDetail, setSelectedDetail] = useState<AdminStudentDetailResponse | null>(null);
  const [detailLoading, setDetailLoading] = useState<boolean>(false);

  const [statusConfirmUser, setStatusConfirmUser] = useState<AdminStudentResponse | null>(null);
  const [actionLoading, setActionLoading] = useState<boolean>(false);

  const loadStudents = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getStudentList(
        statusFilter,
        deptFilter,
        courseFilter,
        yearFilter,
        semFilter,
        searchTerm
      );
      setStudents(data);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to load student directory list.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadStudents();
  }, [statusFilter, deptFilter, courseFilter, yearFilter, semFilter]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    loadStudents();
  };

  const handleOpenDetails = async (id: number) => {
    setIsDetailOpen(true);
    setDetailLoading(true);
    setSelectedDetail(null);
    try {
      const detailData = await getStudentById(id);
      setSelectedDetail(detailData);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to fetch student account details.');
    } finally {
      setDetailLoading(false);
    }
  };

  const handleConfirmStatusToggle = async () => {
    if (!statusConfirmUser) return;
    setActionLoading(true);
    const newStatus = !statusConfirmUser.active;

    try {
      await updateStudentStatus(statusConfirmUser.id, newStatus);
      setSuccessMessage(
        `Student account "${statusConfirmUser.firstName} ${statusConfirmUser.lastName}" has been ${
          newStatus ? 'activated' : 'deactivated'
        }.`
      );
      setStatusConfirmUser(null);
      loadStudents();
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to update student account status.');
    } finally {
      setActionLoading(false);
    }
  };

  // Derive unique filter options dynamically from current real student list
  const departments = Array.from(new Set(students.map((s) => s.department).filter(Boolean))) as string[];
  const courses = Array.from(new Set(students.map((s) => s.course).filter(Boolean))) as string[];
  const years = Array.from(new Set(students.map((s) => s.year).filter(Boolean))) as string[];
  const semesters = Array.from(new Set(students.map((s) => s.semester).filter(Boolean))) as string[];

  return (
    <div className="space-y-8">
      {/* Page Header */}
      <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-6 sm:p-8 backdrop-blur shadow-xl relative overflow-hidden">
        <div className="absolute top-0 right-0 -mt-8 -mr-8 w-64 h-64 bg-emerald-500/5 rounded-full blur-3xl pointer-events-none"></div>
        <div className="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div>
            <div className="flex items-center space-x-3 mb-2">
              <span className="text-2xl">🎓</span>
              <span className="px-3 py-1 bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 rounded-full text-xs font-semibold tracking-wider uppercase">
                Student Administration
              </span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-bold text-white tracking-tight">
              Student Directory & Account Management
            </h1>
            <p className="text-slate-400 text-sm mt-1 max-w-2xl">
              Inspect student enrollment records, view academic profile details, filter accounts by status or department, and manage authentication access.
            </p>
          </div>
          <div className="flex items-center space-x-3">
            <button
              onClick={loadStudents}
              disabled={loading}
              className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold rounded-xl border border-slate-700 transition-all flex items-center space-x-2 disabled:opacity-50"
            >
              <span className={loading ? 'animate-spin' : ''}>🔄</span>
              <span>{loading ? 'Refreshing...' : 'Refresh Directory'}</span>
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

      {/* Search & Filter Controls */}
      <div className="bg-slate-900/40 border border-slate-800 rounded-2xl p-4 space-y-4">
        <form onSubmit={handleSearchSubmit} className="flex items-center space-x-2">
          <div className="relative flex-1">
            <input
              type="text"
              placeholder="Search students by name, email, or Student ID..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2 text-sm text-slate-100 placeholder-slate-500 focus:outline-none focus:border-emerald-500/50"
            />
          </div>
          <button
            type="submit"
            className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold rounded-xl border border-slate-700 transition-colors"
          >
            Search
          </button>
        </form>

        <div className="grid grid-cols-2 sm:grid-cols-5 gap-3 pt-2 border-t border-slate-800/60">
          <div>
            <label className="block text-[10px] text-slate-500 font-semibold uppercase mb-1">Account Status</label>
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-1.5 text-xs text-slate-200 focus:outline-none focus:border-emerald-500/50"
            >
              <option value="all">All Statuses</option>
              <option value="active">Active Only</option>
              <option value="inactive">Inactive Only</option>
            </select>
          </div>

          <div>
            <label className="block text-[10px] text-slate-500 font-semibold uppercase mb-1">Department</label>
            <select
              value={deptFilter}
              onChange={(e) => setDeptFilter(e.target.value)}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-1.5 text-xs text-slate-200 focus:outline-none focus:border-emerald-500/50"
            >
              <option value="all">All Departments</option>
              {departments.map((dept, idx) => (
                <option key={idx} value={dept}>
                  {dept}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-[10px] text-slate-500 font-semibold uppercase mb-1">Course</label>
            <select
              value={courseFilter}
              onChange={(e) => setCourseFilter(e.target.value)}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-1.5 text-xs text-slate-200 focus:outline-none focus:border-emerald-500/50"
            >
              <option value="all">All Courses</option>
              {courses.map((crs, idx) => (
                <option key={idx} value={crs}>
                  {crs}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-[10px] text-slate-500 font-semibold uppercase mb-1">Year</label>
            <select
              value={yearFilter}
              onChange={(e) => setYearFilter(e.target.value)}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-1.5 text-xs text-slate-200 focus:outline-none focus:border-emerald-500/50"
            >
              <option value="all">All Years</option>
              {years.map((yr, idx) => (
                <option key={idx} value={yr}>
                  {yr}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-[10px] text-slate-500 font-semibold uppercase mb-1">Semester</label>
            <select
              value={semFilter}
              onChange={(e) => setSemFilter(e.target.value)}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-1.5 text-xs text-slate-200 focus:outline-none focus:border-emerald-500/50"
            >
              <option value="all">All Semesters</option>
              {semesters.map((sm, idx) => (
                <option key={idx} value={sm}>
                  {sm}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Student Data Table */}
      <div className="bg-slate-900/40 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
        {loading ? (
          <div className="p-8 space-y-4">
            {Array.from({ length: 4 }).map((_, idx) => (
              <div key={idx} className="h-14 bg-slate-800/40 rounded-xl animate-pulse"></div>
            ))}
          </div>
        ) : students.length === 0 ? (
          <div className="p-12 text-center">
            <div className="w-12 h-12 rounded-full bg-slate-800 text-slate-500 flex items-center justify-center mx-auto mb-3 text-xl">
              🎓
            </div>
            <h3 className="text-slate-200 font-semibold text-base mb-1">No Student Accounts Found</h3>
            <p className="text-slate-500 text-xs max-w-sm mx-auto">
              There are currently no student accounts in the database matching the search query or selected filters.
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-slate-800 bg-slate-950/60 text-slate-400 text-xs font-semibold uppercase tracking-wider">
                  <th className="py-3.5 px-6">Student ID</th>
                  <th className="py-3.5 px-6">Student Name</th>
                  <th className="py-3.5 px-6">Email Address</th>
                  <th className="py-3.5 px-6">Course & Dept</th>
                  <th className="py-3.5 px-6">Year & Sem</th>
                  <th className="py-3.5 px-6">Status</th>
                  <th className="py-3.5 px-6 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 text-sm">
                {students.map((student) => (
                  <tr key={student.id} className="hover:bg-slate-800/30 transition-colors">
                    <td className="py-4 px-6 font-mono text-xs text-emerald-400 font-semibold">
                      {student.studentId || 'Not Configured'}
                    </td>
                    <td className="py-4 px-6 font-semibold text-white">
                      {student.firstName} {student.lastName}
                    </td>
                    <td className="py-4 px-6 text-slate-300 font-mono text-xs">{student.email}</td>
                    <td className="py-4 px-6 text-slate-400 text-xs">
                      {student.course || student.department ? (
                        <div>
                          <div className="text-slate-200">{student.course || '—'}</div>
                          <div className="text-slate-500 text-[11px]">{student.department || '—'}</div>
                        </div>
                      ) : (
                        '—'
                      )}
                    </td>
                    <td className="py-4 px-6 text-slate-400 text-xs">
                      {student.year || student.semester ? (
                        <span>
                          {student.year || '—'} / {student.semester || '—'}
                        </span>
                      ) : (
                        '—'
                      )}
                    </td>
                    <td className="py-4 px-6">
                      <span
                        className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold border ${
                          student.active
                            ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
                            : 'bg-red-500/10 text-red-400 border-red-500/20'
                        }`}
                      >
                        <span
                          className={`w-1.5 h-1.5 rounded-full mr-1.5 ${
                            student.active ? 'bg-emerald-400' : 'bg-red-400'
                          }`}
                        ></span>
                        {student.active ? 'ACTIVE' : 'INACTIVE'}
                      </span>
                    </td>
                    <td className="py-4 px-6 text-right space-x-2">
                      <button
                        onClick={() => handleOpenDetails(student.id)}
                        className="px-2.5 py-1.5 bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-400 rounded-lg border border-emerald-500/20 text-xs font-medium transition-colors"
                      >
                        View Details
                      </button>
                      <button
                        onClick={() => setStatusConfirmUser(student)}
                        className={`px-2.5 py-1.5 rounded-lg border text-xs font-medium transition-colors ${
                          student.active
                            ? 'bg-red-500/10 hover:bg-red-500/20 text-red-400 border-red-500/20'
                            : 'bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-400 border-emerald-500/20'
                        }`}
                      >
                        {student.active ? 'Deactivate' : 'Activate'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* STUDENT DETAILS MODAL */}
      {isDetailOpen && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-xl w-full p-6 shadow-2xl space-y-6 relative">
            <div className="flex justify-between items-center border-b border-slate-800 pb-4">
              <h3 className="text-lg font-bold text-white flex items-center space-x-2">
                <span>🎓</span>
                <span>Student Account & Profile Details</span>
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
                <div className="w-8 h-8 border-2 border-emerald-500/30 border-t-emerald-500 rounded-full animate-spin mx-auto"></div>
                <p className="text-xs text-slate-400">Loading student details...</p>
              </div>
            ) : selectedDetail ? (
              <div className="space-y-6">
                <div className="bg-slate-950/60 border border-slate-800/80 rounded-xl p-4 flex items-center justify-between">
                  <div>
                    <h4 className="text-base font-bold text-white">
                      {selectedDetail.firstName} {selectedDetail.lastName}
                    </h4>
                    <p className="text-xs text-slate-400 font-mono">{selectedDetail.email}</p>
                    <p className="text-xs text-emerald-400 font-mono mt-1">
                      Student ID: {selectedDetail.studentId || 'Not Configured'}
                    </p>
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

                <div className="grid grid-cols-2 gap-4 text-xs">
                  <div className="bg-slate-950 border border-slate-800 rounded-xl p-3">
                    <span className="text-slate-500 font-semibold block uppercase text-[10px]">Department</span>
                    <span className="text-slate-200 font-medium">{selectedDetail.department || 'Not Specified'}</span>
                  </div>

                  <div className="bg-slate-950 border border-slate-800 rounded-xl p-3">
                    <span className="text-slate-500 font-semibold block uppercase text-[10px]">Course / Program</span>
                    <span className="text-slate-200 font-medium">{selectedDetail.course || 'Not Specified'}</span>
                  </div>

                  <div className="bg-slate-950 border border-slate-800 rounded-xl p-3">
                    <span className="text-slate-500 font-semibold block uppercase text-[10px]">Academic Year</span>
                    <span className="text-slate-200 font-medium">{selectedDetail.year || 'Not Specified'}</span>
                  </div>

                  <div className="bg-slate-950 border border-slate-800 rounded-xl p-3">
                    <span className="text-slate-500 font-semibold block uppercase text-[10px]">Semester</span>
                    <span className="text-slate-200 font-medium">{selectedDetail.semester || 'Not Specified'}</span>
                  </div>
                </div>

                {selectedDetail.skills || selectedDetail.interests || selectedDetail.bio ? (
                  <div className="space-y-3 bg-slate-950/40 border border-slate-800/80 rounded-xl p-4 text-xs">
                    {selectedDetail.skills && (
                      <div>
                        <span className="text-slate-400 font-semibold block mb-0.5">Skills:</span>
                        <span className="text-slate-200">{selectedDetail.skills}</span>
                      </div>
                    )}
                    {selectedDetail.interests && (
                      <div>
                        <span className="text-slate-400 font-semibold block mb-0.5">Interests:</span>
                        <span className="text-slate-200">{selectedDetail.interests}</span>
                      </div>
                    )}
                    {selectedDetail.bio && (
                      <div>
                        <span className="text-slate-400 font-semibold block mb-0.5">Bio:</span>
                        <p className="text-slate-300 italic leading-relaxed">{selectedDetail.bio}</p>
                      </div>
                    )}
                  </div>
                ) : (
                  <div className="text-xs text-slate-500 italic bg-slate-950/30 border border-slate-800/40 rounded-xl p-3 text-center">
                    Student has not populated skills, interests, or bio yet.
                  </div>
                )}

                <div className="flex justify-between items-center pt-3 border-t border-slate-800 text-xs text-slate-500">
                  <span>Registered: {selectedDetail.createdAt ? new Date(selectedDetail.createdAt).toLocaleDateString() : '—'}</span>
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
              {statusConfirmUser.active ? 'Deactivate Student Account?' : 'Activate Student Account?'}
            </h3>
            <p className="text-slate-400 text-xs leading-relaxed">
              Are you sure you want to {statusConfirmUser.active ? 'deactivate' : 'activate'} the account for{' '}
              <strong className="text-slate-200">
                {statusConfirmUser.firstName} {statusConfirmUser.lastName}
              </strong>
              ? {statusConfirmUser.active ? 'This will prevent the student from authenticating or accessing student portal services.' : 'This will restore full login and student portal permissions.'}
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

export default AdminStudentsPage;
