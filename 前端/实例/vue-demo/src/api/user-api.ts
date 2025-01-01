import { postRequest } from '@/lib/axios'
import { type PageData } from '@/types'

export const userQuery = (param: UserQuery) => postRequest('/user/query', param) as Promise<PageData<User>>

export const userLock = (param: any) => postRequest('/user/lock', param) as Promise<void>

export const userInsert = (param: any) => postRequest('/user/insert', param) as Promise<void>

export const userDelete = (param: any) => postRequest('/user/delete', param) as Promise<void>

export const userUpdate = (param: any) => postRequest('/user/update', param) as Promise<void>

export interface UserQuery {
  username?: string
  name?: string
  registerTime?: any
  size: number
  current: number
}

export interface User {
  id?: string
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
  time?: number
}
