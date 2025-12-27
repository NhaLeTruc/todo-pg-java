import { RegisterRequest, LoginRequest, LoginResponse, User } from '../types/auth';

import api from './api';

const TOKEN_KEY = 'todo_app_token';
const USER_KEY = 'todo_app_user';

export const authService = {
  async register(data: RegisterRequest): Promise<User> {
    const response = await api.post<User>('/auth/register', data);
    return response.data;
  },

  async login(data: LoginRequest): Promise<LoginResponse> {
    const response = await api.post<LoginResponse>('/auth/login', data);
    const { token, userId, email, fullName } = response.data;

    // Store token and user info in localStorage
    this.setToken(token);
    this.setUser({ userId, email, fullName });

    return response.data;
  },

  async logout(): Promise<void> {
    try {
      await api.post('/auth/logout');
    } finally {
      // Always clear local storage, even if API call fails
      this.clearAuth();
    }
  },

  setToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
  },

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  },

  setUser(user: { userId: number; email: string; fullName: string | null }): void {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  },

  getUser(): { userId: number; email: string; fullName: string | null } | null {
    const userStr = localStorage.getItem(USER_KEY);
    if (!userStr) return null;
    try {
      return JSON.parse(userStr);
    } catch {
      return null;
    }
  },

  clearAuth(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  },

  isAuthenticated(): boolean {
    return !!this.getToken();
  },
};
