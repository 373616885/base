import { postRequest } from '@/lib/axios'

export const login = (param: any) => postRequest('/login', param) as Promise<LoginResponse>

export interface User {
  id: string
  username: string
  name: string
  sex: string
  phone: string
  email: string
  avatar?: any
  isLock?: string
  lastLogin?: string
  registerTime?: string
  updateTime?: string
}

export interface LoginResponse {
  token: string
  refreshToken: string
  user: User
}
