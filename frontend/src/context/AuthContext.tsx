import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';

import { authService } from '../services/authService';
import { AuthContextType, User } from '../types/auth';

const AuthContext = createContext<AuthContextType | undefined>(undefined);

// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Auto-login: Check for existing token on mount
    const storedToken = authService.getToken();
    const storedUser = authService.getUser();

    if (storedToken && storedUser) {
      setToken(storedToken);
      setUser({
        id: storedUser.userId,
        email: storedUser.email,
        fullName: storedUser.fullName,
        isActive: true,
        emailVerified: false,
        createdAt: '',
        updatedAt: '',
        lastLoginAt: null,
      });
    }

    setIsLoading(false);
  }, []);

  const login = async (email: string, password: string): Promise<void> => {
    const response = await authService.login({ email, password });

    setToken(response.token);
    setUser({
      id: response.userId,
      email: response.email,
      fullName: response.fullName,
      isActive: true,
      emailVerified: false,
      createdAt: '',
      updatedAt: '',
      lastLoginAt: null,
    });
  };

  const register = async (email: string, password: string, fullName?: string): Promise<void> => {
    await authService.register({ email, password, fullName });
    // After registration, automatically log in
    await login(email, password);
  };

  const logout = (): void => {
    authService.logout();
    setToken(null);
    setUser(null);
  };

  const value: AuthContextType = {
    user,
    token,
    isAuthenticated: !!token,
    isLoading,
    login,
    register,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
