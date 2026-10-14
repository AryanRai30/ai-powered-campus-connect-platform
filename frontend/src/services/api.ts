import axios from 'axios';
import type { HealthStatus } from '../types/api.types';

/**
 * Centralized API base URL resolver and normalizer.
 * Guarantees that:
 * 1. An optional input string or environment variable (VITE_API_URL / VITE_API_BASE_URL) is read.
 * 2. Fallback to 'http://localhost:8080' is applied for local dev.
 * 3. All trailing slashes and any trailing '/api' occurrences are removed.
 * 4. Exactly one '/api' path segment is present at the end of the base URL.
 */
export const normalizeApiBaseUrl = (rawUrl?: string): string => {
  const envUrl = (
    rawUrl ||
    (typeof import.meta !== 'undefined' && import.meta.env
      ? import.meta.env.VITE_API_URL || import.meta.env.VITE_API_BASE_URL
      : '') ||
    'http://localhost:8080'
  ).trim();

  // Strip all trailing slashes
  let cleanUrl = envUrl.replace(/\/+$/, '');

  // Strip any trailing '/api' occurrences to prevent duplicate '/api/api'
  while (cleanUrl.endsWith('/api')) {
    cleanUrl = cleanUrl.slice(0, -4).replace(/\/+$/, '');
  }

  return `${cleanUrl}/api`;
};

export const getApiBaseUrl = (rawUrl?: string): string => normalizeApiBaseUrl(rawUrl);

export const BASE_URL = getApiBaseUrl();

/**
 * Normalizes any request URL path to strip leading duplicate '/api' or 'api/' segments,
 * ensuring the final request path has '/api' exactly once via Axios baseURL.
 */
export const normalizeRequestPath = (url?: string): string => {
  if (!url) return '';
  let normalized = url.trim();

  // Strip all leading duplicate '/api/' or 'api/' segments
  while (
    normalized.startsWith('/api/') ||
    normalized.startsWith('api/') ||
    normalized === '/api' ||
    normalized === 'api'
  ) {
    if (normalized.startsWith('/api/')) {
      normalized = normalized.slice(4);
    } else if (normalized.startsWith('api/')) {
      normalized = normalized.slice(4);
    } else if (normalized === '/api' || normalized === 'api') {
      normalized = '';
      break;
    }
  }

  if (normalized.length > 0 && !normalized.startsWith('/')) {
    normalized = `/${normalized}`;
  }

  return normalized;
};

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
 * and normalize relative request URLs to guarantee no duplicate '/api/api' paths
 */
api.interceptors.request.use(
  (config) => {
    if (!config.headers) {
      config.headers = {} as any;
    }

    // Normalize URL path to prevent duplicate '/api/api' paths under all circumstances
    if (config.url) {
      config.url = normalizeRequestPath(config.url);
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
