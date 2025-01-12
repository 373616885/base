import { getRequest } from '@/lib/axios'

export const getRole = (param: any) => getRequest('/system/role', param)
