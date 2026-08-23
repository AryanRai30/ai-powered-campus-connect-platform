import api from './api';
import { StudentProfileRequest, StudentProfileResponse } from '../types/studentProfile.types';

/**
 * Service handling Student Profile API communication.
 */
export const studentProfileService = {
  /**
   * Fetch current authenticated user's student profile.
   */
  async getProfile(): Promise<StudentProfileResponse> {
    const response = await api.get<StudentProfileResponse>('/student/profile');
    return response.data;
  },

  /**
   * Create a new student profile for current authenticated user.
   */
  async createProfile(data: StudentProfileRequest): Promise<StudentProfileResponse> {
    const response = await api.post<StudentProfileResponse>('/student/profile', data);
    return response.data;
  },

  /**
   * Update existing student profile for current authenticated user.
   */
  async updateProfile(data: StudentProfileRequest): Promise<StudentProfileResponse> {
    const response = await api.put<StudentProfileResponse>('/student/profile', data);
    return response.data;
  },
};

export default studentProfileService;
