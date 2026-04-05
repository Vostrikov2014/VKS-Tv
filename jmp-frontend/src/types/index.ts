export interface User {
  id: string;
  email: string;
  firstName?: string;
  lastName?: string;
  role: UserRole;
  tenantId: string;
}

export type UserRole = 'SUPER_ADMIN' | 'TENANT_ADMIN' | 'MODERATOR' | 'USER' | 'AUDITOR';

export interface AuthState {
  user: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
}

export interface LoginCredentials {
  email: string;
  password: string;
  totpCode?: string;
}

export interface AuthResponse {
  access_token: string;
  refresh_token: string;
  token_type: string;
  expires_in: number;
  user_id: string;
  email: string;
  role: string;
  tenant_id: string;
}

export interface Conference {
  id: string;
  name: string;
  roomId: string;
  description?: string;
  status: ConferenceStatus;
  scheduledStartAt?: string;
  scheduledEndAt?: string;
  actualStartAt?: string;
  actualEndAt?: string;
  maxParticipants?: number;
  currentParticipants: number;
  recordingEnabled: boolean;
  chatEnabled: boolean;
  screenSharingEnabled: boolean;
  lobbyEnabled: boolean;
  joinUrl?: string;
  moderatorJoinUrl?: string;
  createdAt: string;
  updatedAt?: string;
}

export type ConferenceStatus = 'SCHEDULED' | 'ACTIVE' | 'COMPLETED' | 'CANCELLED' | 'FAILED';

export interface Tenant {
  id: string;
  name: string;
  slug: string;
  domain?: string;
  status: TenantStatus;
  maxParticipants: number;
  maxDurationMinutes: number;
  maxRecordings: number;
  recordingRetentionDays: number;
  jitsiDomain?: string;
  jibriEnabled: boolean;
  subscriptionTier: string;
  createdAt: string;
}

export type TenantStatus = 'ACTIVE' | 'SUSPENDED' | 'PENDING' | 'DELETED';

export interface Recording {
  id: string;
  conferenceId: string;
  conferenceName: string;
  url: string;
  duration: number;
  size: number;
  status: RecordingStatus;
  createdAt: string;
}

export type RecordingStatus = 'PROCESSING' | 'AVAILABLE' | 'FAILED' | 'DELETED';

export interface ApiResponse<T> {
  data: T;
  meta?: {
    total?: number;
    page?: number;
    size?: number;
    nextCursor?: string;
  };
  error?: ApiError;
}

export interface ApiError {
  code: string;
  message: string;
  details?: Record<string, string[]>;
}

export interface DashboardStats {
  totalConferences: number;
  activeConferences: number;
  totalParticipants: number;
  totalRecordings: number;
  storageUsed: number;
}
