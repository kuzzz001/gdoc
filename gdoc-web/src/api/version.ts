import request from './request'

export interface DocumentVersion {
  id: number
  docId: number
  content: string
  versionNumber: number
  versionName: string
  createdBy: number
  createdAt: string
}

export const versionApi = {
  list: (docId: number) => request.get<DocumentVersion[]>(`/documents/${docId}/versions`),
  get: (docId: number, versionNumber: number) =>
    request.get<DocumentVersion>(`/documents/${docId}/versions/${versionNumber}`),
  create: (docId: number, data: { content: string; name?: string }) =>
    request.post<DocumentVersion>(`/documents/${docId}/versions`, data),
  rename: (docId: number, versionId: number, name: string) =>
    request.put(`/documents/${docId}/versions/${versionId}/name`, { name }),
}