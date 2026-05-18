import request from './request'
import type { Friend, FriendRequest, Message, CollabInvitation, User } from '@/types'

export const socialApi = {
  searchUsers: (keyword: string) => request.get<any, User[]>('/social/users/search', { params: { keyword } }),
  sendFriendRequest: (userId: number) => request.post<any, void>('/social/friends/request', { userId }),
  handleFriendRequest: (id: number, action: 'accept' | 'reject') =>
    request.put<any, void>(`/social/friends/request/${id}`, { action }),
  getFriends: () => request.get<any, Friend[]>('/social/friends'),
  deleteFriend: (userId: number) => request.delete<any, void>(`/social/friends/${userId}`),
  getPendingRequests: () => request.get<any, FriendRequest[]>('/social/friends/requests/pending'),
}

export const messageApi = {
  getHistory: (friendId: number) => request.get<any, Message[]>(`/social/message/${friendId}`),
  getUnreadCount: () => request.get<any, number>('/social/message/unread'),
  markRead: (friendId: number) => request.put<any, void>(`/social/message/${friendId}/read`),
}

export const invitationApi = {
  send: (data: { friendId: number; documentId: number; permission: 'editor' | 'viewer'; message?: string }) =>
    request.post<any, CollabInvitation>('/social/invitations', data),
  list: () => request.get<any, CollabInvitation[]>('/social/invitations'),
  handle: (id: number, action: 'accept' | 'reject') =>
    request.put<any, void>(`/social/invitations/${id}`, { action }),
  cancel: (id: number) => request.delete<any, void>(`/social/invitations/${id}`),
}
