import { postRequest } from '@/lib/axios'

export const menuList = () => postRequest('/menu/list') as Promise<MenuResponse[]>

export interface MenuResponse {
  id: string
  name: string
  url: string
  seq: number
  type: string
  childMenu: MenuResponse[]
  pid: string
}
