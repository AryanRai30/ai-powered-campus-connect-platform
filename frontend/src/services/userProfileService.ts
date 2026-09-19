import api from './api';
import { UserProfileResponse, UserProfileUpdateRequest } from '../types/userProfile.types';

export const userProfileService = {
  getUserProfile: async (): Promise<UserProfileResponse> => {
    const response = await api.get<UserProfileResponse>('/profile/me');
    return response.data;
  },

  updateUserProfile: async (data: UserProfileUpdateRequest): Promise<UserProfileResponse> => {
    const response = await api.put<UserProfileResponse>('/profile/me', data);
    return response.data;
  },
};

export default userProfileService;
