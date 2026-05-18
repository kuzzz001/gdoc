import request from './request'
import type { Document, PageResult, ShareLink, Collaborator } from '@/types'

export const documentApi = {
  list: (params: { page: number; size: number }) =>
    request.get<any, PageResult<Document>>('/docs', { params }),
  create: (title: string) => request.post<any, Document>('/docs', { title }),
  get: (id: number) => request.get<any, Document>(`/docs/${id}`),
  update: (id: number, data: { title?: string; content?: string }) =>
    request.put<any, Document>(`/docs/${id}`, data),
  delete: (id: number) => request.delete<any, void>(`/docs/${id}`),
}

export const shareApi = {
  create: (docId: number, data: { permission: 'view' | 'edit'; expiresAt?: string }) =>
    request.post<any, ShareLink>(`/docs/${docId}/share`, data),
  list: (docId: number) => request.get<any, ShareLink[]>(`/docs/${docId}/shares`),
  revoke: (docId: number, token: string) =>
    request.delete<any, void>(`/docs/${docId}/shares/${token}`),
  getByToken: (token: string) => request.get<any, Document>(`/docs/share/${token}`),
}

export const collaboratorApi = {
  list: (docId: number) => request.get<any, Collaborator[]>(`/docs/${docId}/collaborators`),
  add: (docId: number, data: { userId: number; role: 'editor' | 'viewer' }) =>
    request.post<any, Collaborator>(`/docs/${docId}/collaborators`, data),
  update: (docId: number, userId: number, role: 'editor' | 'viewer') =>
    request.put<any, Collaborator>(`/docs/${docId}/collaborators/${userId}`, { role }),
  remove: (docId: number, userId: number) =>
    request.delete<any, void>(`/docs/${docId}/collaborators/${userId}`),
}
