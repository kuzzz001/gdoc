import request from './request'

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