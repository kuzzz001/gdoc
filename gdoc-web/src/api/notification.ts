import request from './request'

export interface Notification {
  id: number
  userId: number
  type: string
  content: string
  relatedId: number | null
  isRead: number
  createdAt: string
}

export const notificationApi = {
  list: () => request.get<any, Notification[]>('/notifications'),
  unreadCount: () => request.get<any, number>('/notifications/unread-count'),
  markRead: (id: number) => request.put<any, void>(`/notifications/${id}/read`),
  markAllRead: () => request.put<any, void>('/notifications/read-all'),
}