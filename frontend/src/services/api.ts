import axios from 'axios';
import { HealthStatus } from '../types/api.types';

// Centralized base URL configured via environment variable
const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

/**
 * Global Axios Instance for API requests
 */
const api = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

/**
 * Request Interceptor to attach JWT Bearer Token automatically
 */
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('campus_connect_token');
    if (token && token.trim() !== '' && token !== 'undefined' && token !== 'null') {
      const authHeader = `Bearer ${token.trim()}`;
      if (config.headers && typeof config.headers.set === 'function') {
        config.headers.set('Authorization', authHeader);
      } else if (config.headers) {
        config.headers['Authorization'] = authHeader;
      }
    }
    return config;
  },
  (error) => Promise.reject(error)
);

/**
 * Response Interceptor for centralized error handling and 401 session invalidation
 */
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const requestUrl = error.config?.url || '';
      // Only clear invalid session and redirect if 401 did not originate from authentication login/register endpoints
      if (!requestUrl.includes('/auth/login') && !requestUrl.includes('/auth/register')) {
        console.warn('Unauthorized 401 response detected on protected endpoint - clearing session and redirecting to login.');
        localStorage.removeItem('campus_connect_token');
        localStorage.removeItem('campus_connect_user');
        if (window.location.pathname !== '/login') {
          window.location.href = '/login';
        }
      }
    }
    return Promise.reject(error);
  }
);

/**
 * Health Check API service to verify backend connection
 */
export const checkBackendHealth = async (): Promise<HealthStatus> => {
  const response = await api.get<HealthStatus>('/health');
  return response.data;
};

export default api;
