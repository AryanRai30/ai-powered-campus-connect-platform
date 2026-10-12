import api from './api';
import {
  StudentActivityAnalytics,
  FacultyActivityAnalytics,
  AdminPlatformAnalytics,
} from '../types/analytics.types';

export const getStudentAnalytics = async (): Promise<StudentActivityAnalytics> => {
  const response = await api.get<StudentActivityAnalytics>('/analytics/student');
  return response.data;
};

export const getFacultyAnalytics = async (): Promise<FacultyActivityAnalytics> => {
  const response = await api.get<FacultyActivityAnalytics>('/analytics/faculty');
  return response.data;
};

export const getAdminAnalytics = async (): Promise<AdminPlatformAnalytics> => {
  const response = await api.get<AdminPlatformAnalytics>('/analytics/admin');
  return response.data;
};
