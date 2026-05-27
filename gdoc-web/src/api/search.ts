import request from './request'
import type { Document, PageResult } from '@/types'

export const searchApi = {
  search: (keyword: string, page = 1, size = 20) =>
    request.get<any, PageResult<Document>>('/docs/search', { params: { keyword, page, size } }),
}

export const recycleBinApi = {
  list: (page = 1, size = 20) =>
    request.get<any, PageResult<Document>>('/recycle-bin', { params: { page, size } }),
  restore: (docId: number) =>
    request.post<any, void>(`/recycle-bin/${docId}/restore`),
  permanentDelete: (docId: number) =>
    request.delete<any, void>(`/recycle-bin/${docId}`),
  emptyBin: () =>
    request.delete<any, void>('/recycle-bin'),
}

export const tagApi = {
  create: (name: string) => request.post<any, any>('/tags', { name }),
  list: () => request.get<any, any[]>('/tags'),
  delete: (tagId: number) => request.delete<any, void>(`/tags/${tagId}`),
  tagDocument: (docId: number, tagIds: number[]) =>
    request.put<any, void>(`/tags/docs/${docId}`, { tagIds }),
  getDocumentTags: (docId: number) =>
    request.get<any, any[]>(`/tags/docs/${docId}`),
}

export const exportApi = {
  exportPdf: (docId: number) => `/docs/${docId}/export/pdf`,
  exportWord: (docId: number) => `/docs/${docId}/export/word`,
  exportMarkdown: (docId: number) => `/docs/${docId}/export/markdown`,
  importMarkdown: (title: string, file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('title', title)
    return request.post<any, Document>('/docs/import/markdown', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
}