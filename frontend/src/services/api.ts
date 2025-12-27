import axios, { AxiosError, AxiosRequestConfig, AxiosResponse } from 'axios';
import { toast } from 'react-hot-toast';

import { API_BASE_URL } from '@/utils/constants';

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('todo_app_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    const correlationId = generateCorrelationId();
    config.headers['X-Correlation-ID'] = correlationId;

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response: AxiosResponse) => {
    return response;
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as AxiosRequestConfig & { _retry?: boolean };

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      localStorage.removeItem('todo_app_token');
      localStorage.removeItem('todo_app_user');

      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }

      toast.error('Your session has expired. Please log in again.');
    }

    if (error.response?.status === 403) {
      toast.error('You do not have permission to perform this action.');
    }

    if (error.response?.status === 404) {
      toast.error('The requested resource was not found.');
    }

    if (error.response?.status === 500) {
      toast.error('An unexpected server error occurred. Please try again later.');
    }

    if (error.code === 'ECONNABORTED') {
      toast.error('Request timeout. Please check your connection and try again.');
    }

    if (!error.response) {
      toast.error('Network error. Please check your internet connection.');
    }

    return Promise.reject(error);
  }
);

function generateCorrelationId(): string {
  return `${Date.now()}-${Math.random().toString(36).substring(2, 15)}`;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}

export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const apiError = error.response?.data as ApiError;
    return apiError?.message || error.message || 'An unexpected error occurred';
  }
  if (error instanceof Error) {
    return error.message;
  }
  return 'An unexpected error occurred';
}

export default api;
