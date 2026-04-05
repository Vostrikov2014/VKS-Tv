import axios, { type AxiosInstance, type AxiosError } from 'axios';
import type { ApiResponse, AuthResponse, LoginCredentials } from '../types';
import { useAuthStore } from '../store/authStore';

const API_BASE_URL = '/api/v1';

/**
 * Creates and configures the Axios instance with interceptors
 * for authentication and error handling.
 */
function createApiClient(): AxiosInstance {
  const client = axios.create({
    baseURL: API_BASE_URL,
    headers: {
      'Content-Type': 'application/json',
    },
    timeout: 30000, // 30 seconds
  });

  // Request interceptor to add auth token
  client.interceptors.request.use(
    (config) => {
      const state = useAuthStore.getState();
      if (state.accessToken) {
        config.headers.Authorization = `Bearer ${state.accessToken}`;
      }
      return config;
    },
    (error) => Promise.reject(error)
  );

  // Response interceptor for token refresh and error handling
  client.interceptors.response.use(
    (response) => response,
    async (error: AxiosError<ApiResponse<unknown>>) => {
      const originalRequest = error.config;

      // If error is 401 and we haven't retried yet
      if (error.response?.status === 401 && !originalRequest?.headers['X-Retry-Refresh']) {
        const state = useAuthStore.getState();
        
        if (state.refreshToken) {
          try {
            // Mark request as retried
            originalRequest.headers['X-Retry-Refresh'] = 'true';

            // Attempt to refresh token
            const response = await axios.post<AuthResponse>(
              `${API_BASE_URL}/auth/refresh`,
              {},
              {
                headers: {
                  Authorization: `Bearer ${state.refreshToken}`,
                },
              }
            );

            const { access_token, refresh_token } = response.data;
            
            // Update tokens in store
            state.setTokens(access_token, refresh_token);

            // Retry original request with new token
            if (originalRequest.headers) {
              originalRequest.headers.Authorization = `Bearer ${access_token}`;
            }
            return client(originalRequest);
          } catch (refreshError) {
            // Refresh failed, clear auth and redirect to login
            state.clearAuth();
            window.location.href = '/login';
            return Promise.reject(refreshError);
          }
        } else {
          // No refresh token, redirect to login
          state.clearAuth();
          window.location.href = '/login';
        }
      }

      return Promise.reject(error);
    }
  );

  return client;
}

export const apiClient = createApiClient();

/**
 * Authentication API service
 */
export const authApi = {
  login: async (credentials: LoginCredentials) => {
    const response = await apiClient.post<AuthResponse>('/auth/login', credentials);
    return response.data;
  },

  logout: async () => {
    await apiClient.post('/auth/logout');
  },

  refreshToken: async (refreshToken: string) => {
    const response = await axios.post<AuthResponse>(`${API_BASE_URL}/auth/refresh`, null, {
      headers: {
        Authorization: `Bearer ${refreshToken}`,
      },
    });
    return response.data;
  },

  getCurrentUser: async () => {
    const response = await apiClient.get('/auth/me');
    return response.data;
  },
};

/**
 * Conferences API service
 */
export const conferencesApi = {
  getAll: async (params?: { page?: number; size?: number; status?: string }) => {
    const response = await apiClient.get('/conferences', { params });
    return response.data;
  },

  getById: async (id: string) => {
    const response = await apiClient.get(`/conferences/${id}`);
    return response.data;
  },

  create: async (data: Partial<any>) => {
    const response = await apiClient.post('/conferences', data);
    return response.data;
  },

  update: async (id: string, data: Partial<any>) => {
    const response = await apiClient.put(`/conferences/${id}`, data);
    return response.data;
  },

  delete: async (id: string) => {
    await apiClient.delete(`/conferences/${id}`);
  },
};

/**
 * Dashboard API service
 */
export const dashboardApi = {
  getStats: async () => {
    const response = await apiClient.get('/dashboard/stats');
    return response.data;
  },

  getActiveConferences: async () => {
    const response = await apiClient.get('/dashboard/active-conferences');
    return response.data;
  },
};

export default apiClient;
