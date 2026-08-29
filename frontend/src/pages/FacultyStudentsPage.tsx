import React, { useState, useEffect } from 'react';
import { getFacultyStudents } from '../services/facultyService';
import { FacultyStudent } from '../types/faculty.types';

export const FacultyStudentsPage: React.FC = () => {
  const [students, setStudents] = useState<FacultyStudent[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Filters
  const [search, setSearch] = useState<string>('');
  const [department, setDepartment] = useState<string>('');
  const [course, setCourse] = useState<string>('');
  const [year, setYear] = useState<string>('');
  const [semester, setSemester] = useState<string>('');

  const fetchStudents = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await getFacultyStudents({
        search,
        department,
        course,
        year,
        semester,
      });
      setStudents(data);
    } catch (err: any) {
      console.error('Failed to load students list:', err);
      setError('Failed to fetch student directory.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const handler = setTimeout(() => {
      fetchStudents();
    }, 300);
    return () => clearTimeout(handler);
  }, [search, department, course, year, semester]);

  const clearFilters = () => {
    setSearch('');
    setDepartment('');
    setCourse('');
    setYear('');
    setSemester('');
  };

  return (
    <div className="space-y-8 max-w-7xl mx-auto">
      {/* Header */}
      <div className="border-b border-slate-800 pb-5">
        <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-100 flex items-center space-x-3">
          <span>👥 Student Overview</span>
        </h1>
        <p className="text-slate-400 text-sm mt-1">
          Supervise registered students relevant to your department and academic domain.
        </p>
      </div>

      {/* Filters Bar */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-4 space-y-4">
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-5 gap-3">
          <div className="sm:col-span-2">
            <label className="block text-[11px] text-slate-400 font-semibold uppercase mb-1">Search</label>
            <input
              type="text"
              placeholder="Name, email, or Student ID..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-emerald-500"
            />
          </div>

          <div>
            <label className="block text-[11px] text-slate-400 font-semibold uppercase mb-1">Department</label>
            <input
              type="text"
              placeholder="e.g. CSE"
              value={department}
              onChange={(e) => setDepartment(e.target.value)}
              className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-emerald-500"
            />
          </div>

          <div>
            <label className="block text-[11px] text-slate-400 font-semibold uppercase mb-1">Course</label>
            <input
              type="text"
              placeholder="e.g. B.Tech"
              value={course}
              onChange={(e) => setCourse(e.target.value)}
              className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-emerald-500"
            />
          </div>

          <div className="flex items-end space-x-2">
            <div className="w-1/2">
              <label className="block text-[11px] text-slate-400 font-semibold uppercase mb-1">Year</label>
              <input
                type="text"
                placeholder="Year"
                value={year}
                onChange={(e) => setYear(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded-lg px-2.5 py-2 text-xs text-slate-200 focus:outline-none focus:border-emerald-500"
              />
            </div>
            <div className="w-1/2">
              <label className="block text-[11px] text-slate-400 font-semibold uppercase mb-1">Sem</label>
              <input
                type="text"
                placeholder="Sem"
                value={semester}
                onChange={(e) => setSemester(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded-lg px-2.5 py-2 text-xs text-slate-200 focus:outline-none focus:border-emerald-500"
              />
            </div>
          </div>
        </div>

        {(search || department || course || year || semester) && (
          <div className="flex justify-end pt-1">
            <button
              onClick={clearFilters}
              className="text-xs text-slate-400 hover:text-slate-200 underline"
            >
              Clear all filters
            </button>
          </div>
        )}
      </div>

      {error && (
        <div className="p-4 bg-red-500/10 border border-red-500/30 rounded-xl text-red-400 text-sm">
          {error}
        </div>
      )}

      {/* Table */}
      {loading ? (
        <div className="flex items-center justify-center py-16">
          <div className="w-8 h-8 border-4 border-emerald-500/20 border-t-emerald-500 rounded-full animate-spin"></div>
        </div>
      ) : students.length === 0 ? (
        <div className="bg-slate-900/50 border border-slate-800 rounded-2xl p-12 text-center">
          <div className="text-4xl mb-3">🎓</div>
          <h3 className="text-lg font-bold text-slate-300">No students found matching the selected criteria.</h3>
          <p className="text-slate-500 text-sm mt-1">
            Try clearing or adjusting your search filters.
          </p>
        </div>
      ) : (
        <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden shadow-lg">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm border-collapse">
              <thead>
                <tr className="bg-slate-950 border-b border-slate-800 text-xs font-semibold text-slate-400 uppercase tracking-wider">
                  <th className="px-6 py-3.5">Student ID</th>
                  <th className="px-6 py-3.5">Student Name</th>
                  <th className="px-6 py-3.5">Email</th>
                  <th className="px-6 py-3.5">Department</th>
                  <th className="px-6 py-3.5">Course</th>
                  <th className="px-6 py-3.5">Year / Sem</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800 text-slate-300">
                {students.map((st) => (
                  <tr key={st.id} className="hover:bg-slate-800/50 transition-colors">
                    <td className="px-6 py-4 font-mono text-xs font-semibold text-emerald-400">
                      {st.studentId || 'N/A'}
                    </td>
                    <td className="px-6 py-4 font-semibold text-slate-100">
                      {st.firstName} {st.lastName}
                    </td>
                    <td className="px-6 py-4 text-xs text-slate-400">{st.email}</td>
                    <td className="px-6 py-4">{st.department || 'N/A'}</td>
                    <td className="px-6 py-4">{st.course || 'N/A'}</td>
                    <td className="px-6 py-4 text-xs text-slate-400">
                      {st.year ? `Yr ${st.year}` : ''} {st.semester ? `(Sem ${st.semester})` : ''}
                      {!st.year && !st.semester && 'N/A'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="px-6 py-3 bg-slate-950 border-t border-slate-800 text-xs text-slate-400 text-right">
            Total Students: <span className="font-bold text-slate-200">{students.length}</span>
          </div>
        </div>
      )}
    </div>
  );
};

export default FacultyStudentsPage;
