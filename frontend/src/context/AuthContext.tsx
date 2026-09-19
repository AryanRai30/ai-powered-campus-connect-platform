import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { AuthenticatedUser, AuthResponse, LoginRequest, RegisterRequest } from '../types/auth.types';
import { loginUser, registerUser, logoutUser, isTokenExpired } from '../services/authService';

interface AuthContextType {
  user: AuthenticatedUser | null;
  token: string | null;
  isAuthenticated: boolean;
  loading: boolean;
  login: (data: LoginRequest) => Promise<AuthResponse>;
  register: (data: RegisterRequest) => Promise<AuthResponse>;
  logout: () => void;
  hasRole: (role: string) => boolean;
  updateUser: (updatedFields: Partial<AuthenticatedUser>) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<AuthenticatedUser | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    try {
      const storedToken = localStorage.getItem('campus_connect_token');
      const storedUser = localStorage.getItem('campus_connect_user');

      if (
        storedToken &&
        storedUser &&
        !isTokenExpired(storedToken)
      ) {
        setToken(storedToken);
        setUser(JSON.parse(storedUser));
      } else {
        logoutUser();
        setToken(null);
        setUser(null);
      }
    } catch (error) {
      console.error('Failed to restore authentication state:', error);
      logoutUser();
      setToken(null);
      setUser(null);
    } finally {
      setLoading(false);
    }
  }, []);

  const handleAuthSuccess = (response: AuthResponse): AuthResponse => {
    const authUser: AuthenticatedUser = {
      id: response.userId,
      firstName: response.firstName,
      lastName: response.lastName,
      email: response.email,
      roles: response.roles,
    };

    setToken(response.token);
    setUser(authUser);

    localStorage.setItem('campus_connect_token', response.token);
    localStorage.setItem('campus_connect_user', JSON.stringify(authUser));

    return response;
  };

  const login = async (data: LoginRequest): Promise<AuthResponse> => {
    const response = await loginUser(data);
    return handleAuthSuccess(response);
  };

  const register = async (data: RegisterRequest): Promise<AuthResponse> => {
    const response = await registerUser(data);
    return handleAuthSuccess(response);
  };

  const logout = () => {
    logoutUser();
    setToken(null);
    setUser(null);
  };

  const hasRole = (role: string): boolean => {
    if (!user || !user.roles) return false;
    return user.roles.includes(role);
  };

  const updateUser = (updatedFields: Partial<AuthenticatedUser>) => {
    setUser((prevUser) => {
      if (!prevUser) return null;
      const newUser = { ...prevUser, ...updatedFields };
      localStorage.setItem('campus_connect_user', JSON.stringify(newUser));
      return newUser;
    });
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token && !!user,
        loading,
        login,
        register,
        logout,
        hasRole,
        updateUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
