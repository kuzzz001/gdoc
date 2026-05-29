import request from './request'
import type { Friend, FriendRequest, CollabInvitation, Message, User } from '@/types'

export const socialApi = {
  getFriends: () => request.get<any, Friend[]>('/social/friends/list'),
  getPendingRequests: () => request.get<any, FriendRequest[]>('/social/friends/pending'),
  sendFriendRequest: (userId: number) =>
    request.post<any, void>(`/social/friends/add`, { friendId: userId }),
  handleFriendRequest: (friendshipId: number, action: 'accept' | 'reject') =>
    request.post<any, void>(`/social/friends/${action}/${friendshipId}`),
  searchUsers: (keyword: string) =>
    request.get<any, User[]>('/social/friends/search', { params: { keyword } }),
  removeFriend: (friendId: number) =>
    request.delete<any, void>(`/social/friends/${friendId}`),
}

export const messageApi = {
  send: (receiverId: number, content: string) =>
    request.post<any, Message>('/social/messages/send', { receiverId, content }),
  getHistory: (friendId: number) =>
    request.get<any, Message[]>(`/social/messages/history/${friendId}`),
  markRead: (friendId: number) =>
    request.post<any, void>(`/social/messages/read/${friendId}`),
  getUnreadCount: () =>
    request.get<any, number>('/social/messages/unread-total'),
}

export const invitationApi = {
  list: () => request.get<any, CollabInvitation[]>('/social/invitations/received'),
  handle: (invitationId: number, action: 'accept' | 'reject') =>
    request.post<any, void>(`/social/invitations/${action}/${invitationId}`),
  send: (data: { userId: number; docId: number; permission: string; message?: string }) =>
    request.post<any, CollabInvitation>('/social/invitations/send', data),
  cancel: (invitationId: number) =>
    request.post<any, void>(`/social/invitations/cancel/${invitationId}`),
}

export interface Team {
  id: number
  name: string
  description: string
  avatarUrl: string
  ownerId: number
  role?: string
}

export interface TeamMember {
  userId: number
  username: string
  nickname: string
  avatar: string
  role: string
}

export const teamApi = {
  list: () => request.get<Team[]>('/teams'),
  create: (data: { name: string; description?: string }) => request.post<Team>('/teams', data),
  addMember: (teamId: number, userId: number, role?: string) =>
    request.post(`/teams/${teamId}/members`, { userId, role }),
  removeMember: (teamId: number, userId: number) =>
    request.delete(`/teams/${teamId}/members/${userId}`),
  listMembers: (teamId: number) => request.get<TeamMember[]>(`/teams/${teamId}/members`),
  delete: (teamId: number) => request.delete(`/teams/${teamId}`),
}

export interface GroupChat {
  id: number
  name: string
  avatarUrl: string
  ownerId: number
  role?: string
}

export interface GroupMessage {
  id: number
  groupId: number
  senderId: number
  content: string
  msgType: string
  createdAt: string
}

export const groupApi = {
  list: () => request.get<GroupChat[]>('/groups'),
  create: (data: { name: string }) => request.post<GroupChat>('/groups', data),
  addMember: (groupId: number, userId: number) =>
    request.post(`/groups/${groupId}/members`, { userId }),
  removeMember: (groupId: number, userId: number) =>
    request.delete(`/groups/${groupId}/members/${userId}`),
  sendMessage: (groupId: number, content: string) =>
    request.post(`/groups/${groupId}/messages`, { content }),
  getMessages: (groupId: number, limit?: number) =>
    request.get<GroupMessage[]>(`/groups/${groupId}/messages`, { params: { limit } }),
}