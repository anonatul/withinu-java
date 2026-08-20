import axios from 'axios'
import type {
  AdminLoginRequest,
  AdminLoginResponse,
  Dashboard,
  GeoTokenRequest,
  GeoTokenResponse,
  Message,
  MessageRequest,
  PageResponse,
  Report,
  ReportReason,
  ReportStatus,
  Room,
} from './types'

export const TOKEN_KEY = 'withinu.token'
export const ADMIN_TOKEN_KEY = 'withinu.adminToken'

export const api = axios.create({
  baseURL: '/api/v1',
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  const admin = localStorage.getItem(ADMIN_TOKEN_KEY)
  const token = localStorage.getItem(TOKEN_KEY)
  const selected = config.url?.startsWith('/admin') ? admin : token
  if (selected) {
    config.headers.Authorization = `Bearer ${selected}`
  }
  return config
})

function err(error: unknown, message: string): never {
  const payload = axios.isAxiosError(error) ? error.response?.data : undefined
  if (payload && typeof payload === 'object' && 'errorCode' in payload) {
    throw new ApiClientError(
      String(payload.errorCode),
      String(payload.message ?? message),
    )
  }
  throw new ApiClientError('NETWORK_ERROR', message)
}

export class ApiClientError extends Error {
  constructor(
    public errorCode: string,
    message: string,
  ) {
    super(message)
    this.name = 'ApiClientError'
  }
}

export function isApiError(error: unknown, code: string): boolean {
  return error instanceof ApiClientError && error.errorCode === code
}

export const authApi = {
  geoToken: (body: GeoTokenRequest) =>
    api.post<GeoTokenResponse>('/token/geo', body).then((r) => r.data),
}

export const roomsApi = {
  list: () => api.get<Room[]>('/rooms').then((r) => r.data),
  get: (id: string) => api.get<Room>(`/rooms/${id}`).then((r) => r.data),
}

export const messagesApi = {
  list: (roomId: string, page: number, size = 30) =>
    api
      .get<PageResponse<Message>>('/messages', {
        params: { roomId, page, size },
      })
      .then((r) => r.data),
  send: (body: MessageRequest) =>
    api.post<Message>('/messages', body).then((r) => r.data),
  delete: (id: string) =>
    api.delete(`/messages/${id}`).then((r) => r.data),
}

export const reportsApi = {
  create: (messageId: string, reason: ReportReason) =>
    api.post<Report>('/reports', { messageId, reason }).then((r) => r.data),
}

export const adminApi = {
  login: (body: AdminLoginRequest) =>
    api.post<AdminLoginResponse>('/admin/login', body).then((r) => r.data),
  dashboard: () =>
    api.get<Dashboard>('/admin/dashboard').then((r) => r.data),
  rooms: {
    list: () => api.get<Room[]>('/admin/rooms').then((r) => r.data),
    create: (body: { name: string; slug?: string; description?: string; active: boolean }) =>
      api.post<Room>('/admin/rooms', body).then((r) => r.data),
    update: (id: string, body: { name: string; slug?: string; description?: string; active: boolean }) =>
      api.patch<Room>(`/admin/rooms/${id}`, body).then((r) => r.data),
    deactivate: (id: string) =>
      api.delete(`/admin/rooms/${id}`).then((r) => r.data),
  },
  messages: {
    list: (page = 0, size = 30) =>
      api
        .get<PageResponse<Message>>('/admin/messages', { params: { page, size } })
        .then((r) => r.data),
    delete: (id: string) =>
      api.delete(`/admin/messages/${id}`).then((r) => r.data),
  },
  reports: {
    list: (status: ReportStatus, page = 0, size = 30) =>
      api
        .get<PageResponse<Report>>('/admin/reports', {
          params: { status, page, size },
        })
        .then((r) => r.data),
    resolve: (id: string, status: ReportStatus) =>
      api
        .patch<Report>(`/admin/reports/${id}`, null, { params: { status } })
        .then((r) => r.data),
  },
}

export { err }