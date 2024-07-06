/**
 * Result接口
 * code 200 表示成功
 * code 4xx 表示客户端传递的参数错误
 * code 5xx 表示服务器出现错误
 */
export interface Result<T> {
  code: number
  msg?: string
  data?: T // ? 可选
}

/**
 * 分页接口
 */
export interface PageData<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}
