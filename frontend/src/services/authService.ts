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

/**
 * Helper utility to safely inspect client-side JWT expiration claim ('exp').
 */
export const isTokenExpired = (token: string): boolean => {
  if (!token || token.trim() === '' || token === 'undefined' || token === 'null') {
    return true;
  }
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return true;
    const payload = JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')));
    if (!payload || !payload.exp) return false;
    const nowInSeconds = Math.floor(Date.now() / 1000);
    return payload.exp <= nowInSeconds;
  } catch (e) {
    return true;
  }
};
