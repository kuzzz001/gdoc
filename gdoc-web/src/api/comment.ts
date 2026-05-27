import request from './request'

export interface Comment {
  id: number
  docId: number
  userId: number
  username: string
  nickname: string
  content: string
  rangeStart: number | null
  rangeEnd: number | null
  resolved: number
  replies: CommentReply[]
  createdAt: string
  updatedAt: string
}

export interface CommentReply {
  id: number
  commentId: number
  userId: number
  username: string
  nickname: string
  content: string
  createdAt: string
}

export const commentApi = {
  list: (docId: number) =>
    request.get<any, Comment[]>(`/comments/docs/${docId}`),
  create: (docId: number, data: { content: string; rangeStart?: number; rangeEnd?: number }) =>
    request.post<any, Comment>(`/comments/docs/${docId}`, data),
  resolve: (commentId: number) =>
    request.put<any, void>(`/comments/${commentId}/resolve`),
  reopen: (commentId: number) =>
    request.put<any, void>(`/comments/${commentId}/reopen`),
  delete: (commentId: number) =>
    request.delete<any, void>(`/comments/${commentId}`),
  addReply: (commentId: number, content: string) =>
    request.post<any, CommentReply>(`/comments/${commentId}/replies`, { content }),
}