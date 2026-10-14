import axios from 'axios';
import { HealthStatus } from '../types/api.types';

// Centralized API base URL resolver supporting VITE_API_URL with localhost fallback
export const getApiBaseUrl = (): string => {
  const envUrl = (
    import.meta.env.VITE_API_URL ||
    import.meta.env.VITE_API_BASE_URL ||
    'http://localhost:8080'
  ).trim();

  // Strip trailing slashes
  const trimmed = envUrl.replace(/\/+$/, '');

  // Ensure /api path prefix is present
  return trimmed.endsWith('/api') ? trimmed : `${trimmed}/api`;
};

export const BASE_URL = getApiBaseUrl();

/**
 * Global Axios Instance for API requests
 */
const api = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 15000,
});

/**
 * Request Interceptor to attach JWT Bearer Token automatically
 * and normalize relative request URLs to avoid duplicate '/api/api' paths
 */
api.interceptors.request.use(
  (config) => {
    if (!config.headers) {
      config.headers = {} as any;
    }

    // Normalize URL path to prevent accidental '/api/api' duplication
    if (config.url && config.url.startsWith('/api/')) {
      config.url = config.url.replace(/^\/api/, '');
    }

    // If request payload is FormData, remove default JSON content-type to allow Axios/browser to set multipart boundary
    if (typeof FormData !== 'undefined' && config.data instanceof FormData) {
      if (typeof config.headers.delete === 'function') {
        config.headers.delete('Content-Type');
      } else {
        delete config.headers['Content-Type'];
      }
    }

    const token = localStorage.getItem('campus_connect_token');
    if (token && token.trim() !== '' && token !== 'undefined' && token !== 'null') {
      const authHeader = `Bearer ${token.trim()}`;
      if (typeof config.headers.set === 'function') {
        config.headers.set('Authorization', authHeader);
      }
      config.headers['Authorization'] = authHeader;
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
