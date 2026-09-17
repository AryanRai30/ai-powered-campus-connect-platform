import api from './api';
import {
  AdminStatsResponse,
  AdminFacultyResponse,
  AdminFacultyDetailResponse,
  CreateFacultyRequest,
  UpdateFacultyRequest,
  AdminStudentResponse,
  AdminStudentDetailResponse,
} from '../types/admin.types';

/**
 * Fetch real-time administration dashboard statistics from backend database.
 */
export const getAdminStats = async (): Promise<AdminStatsResponse> => {
  const response = await api.get<AdminStatsResponse>('/admin/stats');
  return response.data;
};

/**
 * Fetch Faculty accounts list with optional status and search parameters.
 */
export const getFacultyList = async (status?: string, search?: string): Promise<AdminFacultyResponse[]> => {
  const params = new URLSearchParams();
  if (status && status !== 'all') params.append('status', status);
  if (search && search.trim() !== '') params.append('search', search.trim());

  const response = await api.get<AdminFacultyResponse[]>(`/admin/faculty?${params.toString()}`);
  return response.data;
};

/**
 * Fetch detailed account info and real content metrics for a single Faculty account.
 */
export const getFacultyById = async (id: number): Promise<AdminFacultyDetailResponse> => {
  const response = await api.get<AdminFacultyDetailResponse>(`/admin/faculty/${id}`);
  return response.data;
};

/**
 * Create a new Faculty user account (Admin exclusive action).
 */
export const createFaculty = async (data: CreateFacultyRequest): Promise<AdminFacultyResponse> => {
  const response = await api.post<AdminFacultyResponse>('/admin/faculty', data);
  return response.data;
};

/**
 * Update Faculty user account details.
 */
export const updateFaculty = async (id: number, data: UpdateFacultyRequest): Promise<AdminFacultyResponse> => {
  const response = await api.put<AdminFacultyResponse>(`/admin/faculty/${id}`, data);
  return response.data;
};

/**
 * Toggle Faculty user account active status (ACTIVE / INACTIVE).
 */
export const updateFacultyStatus = async (id: number, active: boolean): Promise<AdminFacultyResponse> => {
  const response = await api.patch<AdminFacultyResponse>(`/admin/faculty/${id}/status`, { active });
  return response.data;
};

/**
 * Fetch Student accounts directory list with optional status, academic, and search filters.
 */
export const getStudentList = async (
  status?: string,
  dept?: string,
  course?: string,
  year?: string,
  sem?: string,
  search?: string
): Promise<AdminStudentResponse[]> => {
  const params = new URLSearchParams();
  if (status && status !== 'all') params.append('status', status);
  if (dept && dept !== 'all') params.append('dept', dept);
  if (course && course !== 'all') params.append('course', course);
  if (year && year !== 'all') params.append('year', year);
  if (sem && sem !== 'all') params.append('sem', sem);
  if (search && search.trim() !== '') params.append('search', search.trim());

  const response = await api.get<AdminStudentResponse[]>(`/admin/students?${params.toString()}`);
  return response.data;
};

/**
 * Fetch detailed account info and profile data for a single Student account.
 */
export const getStudentById = async (id: number): Promise<AdminStudentDetailResponse> => {
  const response = await api.get<AdminStudentDetailResponse>(`/admin/students/${id}`);
  return response.data;
};

/**
 * Toggle Student user account active status (ACTIVE / INACTIVE).
 */
export const updateStudentStatus = async (id: number, active: boolean): Promise<AdminStudentResponse> => {
  const response = await api.patch<AdminStudentResponse>(`/admin/students/${id}/status`, { active });
  return response.data;
};
