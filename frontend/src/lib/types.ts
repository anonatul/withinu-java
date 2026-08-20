export interface GeoTokenRequest {
  latitude: number
  longitude: number
}

export interface GeoTokenResponse {
  success: boolean
  token: string
  expiresIn: number
}

export interface Room {
  id: string
  name: string
  slug: string
  description?: string
  messageCount: number
  lastActivity?: string
  active: boolean
}

export interface Message {
  id: string
  roomId: string
  displayName: string
  content?: string
  deleted: boolean
  mine: boolean
  createdAt: string
}

export interface MessageRequest {
  roomId: string
  content: string
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  hasNext: boolean
}

export type ReportReason =
  | 'SPAM'
  | 'HARASSMENT'
  | 'ABUSE'
  | 'ILLEGAL_CONTENT'
  | 'OTHER'

export type ReportStatus = 'PENDING' | 'RESOLVED' | 'DISMISSED'

export interface Report {
  id: string
  messageId: string
  contentPreview?: string
  reason: ReportReason
  status: ReportStatus
  createdAt: string
}

export interface Dashboard {
  totalRooms: number
  activeRooms: number
  totalMessages: number
  activeUsers: number
  pendingReports: number
  deletedMessages: number
  recentReports: Report[]
}

export interface AdminLoginRequest {
  username: string
  password: string
}

export interface AdminLoginResponse {
  success: boolean
  token: string
  expiresIn: number
  username: string
}

export interface ApiError {
  success: false
  errorCode: string
  message: string
  timestamp?: string
  details?: Record<string, string>
}

export const REPORT_REASONS: Record<ReportReason, string> = {
  SPAM: 'Spam',
  HARASSMENT: 'Harassment',
  ABUSE: 'Abuse',
  ILLEGAL_CONTENT: 'Illegal content',
  OTHER: 'Other',
}