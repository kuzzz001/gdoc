import request from './request'
import type { ApiResponse } from '@/types'

export interface FolderVO {
  id: number
  name: string
  parentId: number
  children?: FolderVO[]
}

export const folderApi = {
  list: () => request.get<ApiResponse<FolderVO[]>>('/folders'),
  create: (name: string, parentId?: number) => request.post<ApiResponse<FolderVO>>('/folders', { name, parentId }),
  rename: (id: number, name: string) => request.put<ApiResponse<void>>(`/folders/${id}/rename?name=${encodeURIComponent(name)}`),
  delete: (id: number) => request.delete<ApiResponse<void>>(`/folders/${id}`),
}