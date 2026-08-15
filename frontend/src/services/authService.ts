import api from './api';
import { AuthResponse, LoginRequest, RegisterRequest } from '../types/auth.types';

/**
 * Service calling backend authentication endpoints
 */
export const registerUser = async (data: RegisterRequest): Promise<AuthResponse> => {
  const response = await api.post<AuthResponse>('/auth/register', data);
  return response.data;
};

export const loginUser = async (data: LoginRequest): Promise<AuthResponse> => {
  const response = await api.post<AuthResponse>('/auth/login', data);
  return response.data;
};

export const logoutUser = (): void => {
  localStorage.removeItem('campus_connect_token');
  localStorage.removeItem('campus_connect_user');
};
