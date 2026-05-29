import request from './request'
import type { LoginRequest, RegisterRequest, AuthResponse, User } from '@/types'

export const authApi = {
  login: (data: LoginRequest) => request.post<any, AuthResponse>('/auth/login', data),
  register: (data: RegisterRequest) => request.post<any, AuthResponse>('/auth/register', data),
  logout: () => request.post<any, void>('/auth/logout'),
}

export const userApi = {
  getMe: () => request.get<any, User>('/user/me'),
  updateAvatar: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return request.put<any, string>('/user/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
}
