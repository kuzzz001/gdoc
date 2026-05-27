import request from './request'

export interface Template {
  id: number
  name: string
  description: string
  content: string
  category: string
  ownerId: number
  isPublic: number
  createdAt: string
}

export const templateApi = {
  list: () => request.get<Template[]>('/templates'),
  getById: (id: number) => request.get<Template>(`/templates/${id}`),
  create: (data: Partial<Template>) => request.post<Template>('/templates', data),
  delete: (id: number) => request.delete(`/templates/${id}`),
}