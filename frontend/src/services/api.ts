import axios from 'axios';
import { HealthStatus } from '../types/api.types';

// Centralized base URL configured via environment variable
const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

/**
 * Global Axios Instance
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
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

/**
 * Response Interceptor for centralized error handling
 */
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Clear token on 401 if unauthenticated
      console.warn('Unauthorized request detected:', error.response.data);
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
