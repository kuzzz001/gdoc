export interface User {
  id: number
  username: string
  nickname: string
  email: string
  avatarUrl: string
  accountNo: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  nickname: string
}

export interface AuthResponse {
  token: string
  user: User
}

export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface Document {
  id: number
  title: string
  content: string
  version: number
  ownerName: string
  ownerId: number
  permission: 'owner' | 'editor' | 'viewer'
  createdAt: string
  updatedAt: string
}

export interface ShareLink {
  id: number
  documentId: number
  token: string
  permission: 'view' | 'edit'
  expiresAt: string | null
  createdAt: string
}

export interface Collaborator {
  id: number
  documentId: number
  userId: number
  username: string
  nickname: string
  role: 'editor' | 'viewer'
}

export interface Friend {
  id: number
  userId: number
  username: string
  nickname: string
  avatarUrl: string
  unreadCount: number
}

export interface FriendRequest {
  id: number
  fromUserId: number
  fromUsername: string
  fromNickname: string
  fromAvatar: string
  status: 'pending' | 'accepted' | 'rejected'
  createdAt: string
}

export interface Message {
  id: number
  senderId: number
  receiverId: number
  content: string
  type: 'text' | 'image' | 'file' | 'system'
  status: 'sent' | 'delivered' | 'read'
  createdAt: string
}

export interface CollabInvitation {
  id: number
  inviterId: number
  inviterName: string
  documentId: number
  documentTitle: string
  permission: 'editor' | 'viewer'
  message: string
  status: 'pending' | 'accepted' | 'rejected'
  createdAt: string
}

export interface Snapshot {
  id: number
  documentId: number
  content: string
  version: number
  createdAt: string
}

export interface CursorState {
  userId: number
  username: string
  color: string
  top: number
  left: number
}

export interface Conversation {
  id: number
  userId: number
  username: string
  nickname: string
  avatar: string
  lastMessage: string
  unreadCount: number
  updatedAt: string
}
