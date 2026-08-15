/**
 * Generic API response interface
 */
export interface ApiResponse<T = unknown> {
  status: string;
  data?: T;
  message?: string;
}

/**
 * Backend Health Check response interface
 */
export interface HealthStatus {
  status: string;
  application: string;
}
