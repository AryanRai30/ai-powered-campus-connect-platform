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
 * Response Interceptor for centralized error logging
 */
api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error.response?.data || error.message);
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
