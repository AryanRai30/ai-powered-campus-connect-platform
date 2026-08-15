export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phone?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  userId: number;
  firstName: string;
  lastName: string;
  email: string;
  roles: string[];
}

export interface AuthenticatedUser {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  roles: string[];
}

export interface AuthState {
  user: AuthenticatedUser | null;
  token: string | null;
  isAuthenticated: boolean;
  loading: boolean;
}
